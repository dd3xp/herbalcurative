package com.cahcap.herbalcurative.common.blockentity;

import com.cahcap.herbalcurative.common.item.FlowweaveRingItem;
import com.cahcap.herbalcurative.common.recipe.CauldronBrewingRecipe;
import com.cahcap.herbalcurative.common.recipe.CauldronInfusingRecipe;
import com.cahcap.herbalcurative.common.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Block Entity for the Cauldron multiblock structure.
 * 
 * States:
 * - Empty: No fluid
 * - Fluid: Contains water/lava/etc, can add materials for brewing or items for infusing
 * - Brewing: Actively brewing (adding herbs), cannot infuse
 * - Potion: Contains potion, can infuse items
 * 
 * Infusing: Automatic process when item is added to fluid/potion
 * Brewing: Manual process triggered by Flowweave Ring
 */
public class CauldronBlockEntity extends MultiblockPartBlockEntity {
    
    // Fluid capacity (in millibuckets, 1000 = 1 bucket)
    public static final int MAX_FLUID_AMOUNT = 1000;
    
    // Cached render bounding box for multiblock rendering
    public net.minecraft.world.phys.AABB renderAABB = null;
    
    // Storage limits
    public static final int MAX_MATERIAL_TYPES = 10;  // 10 stacks of materials
    public static final int MAX_MATERIAL_COUNT = 64;
    public static final int MAX_HERB_DURATION_SECONDS = 480;  // 8 minutes
    public static final int BASE_DURATION_SECONDS = 120;     // 2 minutes
    
    // Fluid content
    private CauldronFluid fluid = CauldronFluid.empty();
    
    // Brewing state
    private boolean isBrewing = false;
    private boolean isStartingBrew = false;  // Waiting to enter brewing phase
    private boolean isCompletingBrew = false; // Waiting to complete brewing
    private int startingBrewTicks = 0;
    private int completingBrewTicks = 0;
    
    // Minimum wait times (1 tick placeholder - adjust later)
    public static final int MIN_STARTING_BREW_TICKS = 1;  // Time to start brewing
    public static final int MIN_COMPLETING_BREW_TICKS = 1; // Time to complete brewing
    
    // Materials storage (can be added/removed when not brewing)
    private final List<ItemStack> materials = new ArrayList<>();
    
    // Herbs storage (can only be added during brewing)
    private final Map<Item, Integer> herbs = new HashMap<>();
    
    // Heat source detection
    private boolean hasHeatSource = false;
    
    // Suppress drops during disassembly
    public boolean suppressDrops = false;
    
    // Infusing state (automatic crafting when item is in fluid)
    private ItemStack infusingInput = ItemStack.EMPTY;
    private ItemStack infusingOutput = ItemStack.EMPTY;
    private int infusingProgress = 0;
    private int infusingTime = 0;
    private boolean isInfusing = false;
    
    // Store the herbs used in last brewing (for Flowweave Ring binding)
    private final Map<Item, Integer> lastBrewedHerbs = new HashMap<>();
    
    public CauldronBlockEntity(BlockPos pos, BlockState state) {
        super(getBlockEntityType(), pos, state, new int[]{3, 2, 3});
    }
    
    @SuppressWarnings("unchecked")
    private static BlockEntityType<CauldronBlockEntity> getBlockEntityType() {
        return (BlockEntityType<CauldronBlockEntity>) ModRegistries.CAULDRON_BE.get();
    }
    
    @Override
    public CauldronBlockEntity getMaster() {
        return super.getMaster();
    }
    
    // ==================== Fluid Management ====================
    
    /**
     * Get the fluid in the cauldron
     */
    public CauldronFluid getFluid() {
        CauldronBlockEntity master = getMaster();
        return master != null ? master.fluid : CauldronFluid.empty();
    }
    
    public boolean hasFluid() {
        CauldronBlockEntity master = getMaster();
        return master != null && !master.fluid.isEmpty();
    }
    
    public boolean hasWater() {
        CauldronBlockEntity master = getMaster();
        return master != null && master.fluid.isWater();
    }
    
    public boolean isBrewing() {
        CauldronBlockEntity master = getMaster();
        return master != null && master.isBrewing;
    }
    
    /**
     * Add fluid to the cauldron using a bucket
     */
    public boolean addFluid(Fluid fluidToAdd, int amount) {
        CauldronBlockEntity master = getMaster();
        if (master == null) return false;
        
        // Can only add fluid if empty or same fluid type
        if (master.fluid.isEmpty()) {
            master.fluid = CauldronFluid.ofFluid(fluidToAdd, amount);
            master.setChanged();
            master.syncToClient();
            return true;
        } else if (master.fluid.isFluid() && master.fluid.getFluid() == fluidToAdd) {
            int newAmount = Math.min(master.fluid.getAmount() + amount, MAX_FLUID_AMOUNT);
            if (newAmount > master.fluid.getAmount()) {
                master.fluid.setAmount(newAmount);
                master.setChanged();
                master.syncToClient();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Extract fluid from the cauldron (only works for non-potion fluids)
     * @return The fluid that was extracted, or null if cannot extract
     */
    public Fluid extractFluid(int amount) {
        CauldronBlockEntity master = getMaster();
        if (master == null) return null;
        
        // Cannot extract potion with bucket
        if (!master.fluid.isFluid()) return null;
        
        // Cannot extract while brewing
        if (master.isBrewing) return null;
        
        Fluid extracted = master.fluid.getFluid();
        int newAmount = master.fluid.getAmount() - amount;
        
        if (newAmount <= 0) {
            master.fluid.clear();
        } else {
            master.fluid.setAmount(newAmount);
        }
        
        master.setChanged();
        master.syncToClient();
        return extracted;
    }
    
    /**
     * Extract fluid and clear all materials (used when scooping with bucket)
     * @return The fluid that was extracted, or null if cannot extract
     */
    public Fluid extractFluidWithClear(int amount) {
        CauldronBlockEntity master = isMaster() ? this : getMaster();
        if (master == null) return null;
        
        // Cannot extract potion with bucket
        if (!master.fluid.isFluid()) return null;
        
        // Cannot extract while brewing
        if (master.isBrewing) return null;
        
        Fluid extracted = master.fluid.getFluid();
        int newAmount = master.fluid.getAmount() - amount;
        
        if (newAmount <= 0) {
            master.fluid.clear();
            // Also clear materials since fluid is gone
            master.materials.clear();
            master.herbs.clear();
            master.lastBrewedHerbs.clear();
        } else {
            master.fluid.setAmount(newAmount);
        }
        
        master.setChanged();
        master.syncToClient();
        return extracted;
    }
    
    /**
     * Force clear the fluid (without returning materials)
     */
    public void clearFluid() {
        clearFluidAndReturnMaterials(null);
    }
    
    /**
     * Force clear the fluid and return materials to player.
     * Note: During brewing phase (isBrewing=true), materials are NOT returned
     * because they are already being used in the brewing process.
     */
    public void clearFluidAndReturnMaterials(Player player) {
        // If this is master, operate directly; otherwise get master
        CauldronBlockEntity master = isMaster() ? this : getMaster();
        if (master == null) return;
        
        // Return materials to player ONLY if not brewing
        // During brewing, materials have already been "used" in the process
        if (player != null && !master.isBrewing) {
            for (ItemStack material : master.materials) {
                if (!material.isEmpty()) {
                    if (!player.getInventory().add(material.copy())) {
                        // Drop if can't add to inventory
                        player.drop(material.copy(), false);
                    }
                }
            }
            // Return infusing input if any
            if (!master.infusingInput.isEmpty()) {
                if (!player.getInventory().add(master.infusingInput.copy())) {
                    player.drop(master.infusingInput.copy(), false);
                }
            }
            // Return infusing output if any
            if (!master.infusingOutput.isEmpty()) {
                if (!player.getInventory().add(master.infusingOutput.copy())) {
                    player.drop(master.infusingOutput.copy(), false);
                }
            }
        }
        
        master.fluid.clear();
        master.isBrewing = false;
        master.isStartingBrew = false;
        master.isCompletingBrew = false;
        master.startingBrewTicks = 0;
        master.completingBrewTicks = 0;
        master.materials.clear();
        master.herbs.clear();
        master.isInfusing = false;
        master.infusingInput = ItemStack.EMPTY;
        master.infusingOutput = ItemStack.EMPTY;
        master.infusingProgress = 0;
        master.lastBrewedHerbs.clear();
        master.setChanged();
        master.syncToClient();
    }
    
    // ==================== Material Management ====================
    
    /**
     * Add an item to the cauldron.
     * - If brewing: only herbs can be added
     * - If has fluid but not brewing/infusing: add as material, then check for infusing
     */
    public boolean addItem(ItemStack stack, Player player) {
        CauldronBlockEntity master = getMaster();
        if (master == null || master.fluid.isEmpty()) return false;
        
        // If brewing, only accept herbs
        if (master.isBrewing) {
            return master.addHerb(stack, player);
        }
        
        // If already infusing, don't accept more items
        if (master.isInfusing) {
            return false;
        }
        
        // Add as material first (works for any fluid: water or potion)
        boolean added = master.addMaterial(stack, player);
        
        if (added) {
            // After adding material, check if materials now match an infusing recipe
            master.checkAndStartInfusing(player);
        }
        
        return added;
    }
    
    private boolean addMaterial(ItemStack stack, Player player) {
        // Like a chest: max 10 slots, each slot max 64 items (stack limit)
        // First try to stack with existing same items
        for (ItemStack existing : materials) {
            if (ItemStack.isSameItemSameComponents(existing, stack)) {
                int maxStack = existing.getMaxStackSize();
                int canAdd = Math.min(stack.getCount(), maxStack - existing.getCount());
                if (canAdd > 0) {
                    existing.grow(canAdd);
                    stack.shrink(canAdd);
                    setChanged();
                    syncToClient();
                    playItemSplashSound();
                    return true;
                }
            }
        }
        
        // If can't stack and we have room for a new slot, add it
        if (materials.size() < MAX_MATERIAL_TYPES) {
            ItemStack toAdd = stack.copy();
            int maxStack = toAdd.getMaxStackSize();
            toAdd.setCount(Math.min(stack.getCount(), maxStack));
            materials.add(toAdd);
            stack.shrink(toAdd.getCount());
            setChanged();
            syncToClient();
            playItemSplashSound();
            return true;
        }
        
        return false;
    }
    
    private boolean addHerb(ItemStack stack, Player player) {
        Item item = stack.getItem();
        
        // Check if it's a valid herb
        if (!isHerb(item)) {
            return false;
        }
        
        // No limit on herbs - absorb all
        int current = herbs.getOrDefault(item, 0);
        int toAdd = stack.getCount();
        
        herbs.put(item, current + toAdd);
        stack.shrink(toAdd);
        setChanged();
        syncToClient();
        playItemSplashSound();
        return true;
    }
    
    /**
     * Extract a material from the cauldron (only when not brewing)
     */
    public ItemStack extractItem(Player player) {
        CauldronBlockEntity master = getMaster();
        if (master == null) return ItemStack.EMPTY;
        
        // Cannot extract while brewing
        if (master.isBrewing) return ItemStack.EMPTY;
        
        // If infusing is complete, extract the output
        if (!master.isInfusing && !master.infusingOutput.isEmpty()) {
            ItemStack output = master.infusingOutput.copy();
            master.infusingOutput = ItemStack.EMPTY;
            master.infusingInput = ItemStack.EMPTY;
            master.setChanged();
            master.syncToClient();
            return output;
        }
        
        // Extract from materials
        if (master.materials.isEmpty()) {
            return ItemStack.EMPTY;
        }
        
        // Extract from the last added material
        ItemStack last = master.materials.get(master.materials.size() - 1);
        int extractAmount = player.isShiftKeyDown() ? Math.min(last.getCount(), 64) : 1;
        
        ItemStack extracted = last.copy();
        extracted.setCount(extractAmount);
        last.shrink(extractAmount);
        
        if (last.isEmpty()) {
            master.materials.remove(master.materials.size() - 1);
        }
        
        // If extracting material during infusing, cancel the infusing process
        if (master.isInfusing) {
            master.isInfusing = false;
            master.infusingProgress = 0;
            master.infusingTime = 0;
            master.infusingOutput = ItemStack.EMPTY;
            master.infusingInput = ItemStack.EMPTY;
        }
        
        master.setChanged();
        master.syncToClient();
        return extracted;
    }
    
    /**
     * Get all materials in the cauldron
     */
    public List<ItemStack> getMaterials() {
        CauldronBlockEntity master = getMaster();
        return master != null ? new ArrayList<>(master.materials) : new ArrayList<>();
    }
    
    /**
     * Get all herbs in the cauldron
     */
    public Map<Item, Integer> getHerbs() {
        CauldronBlockEntity master = getMaster();
        return master != null ? new HashMap<>(master.herbs) : new HashMap<>();
    }
    
    // ==================== Herb Validation ====================
    
    public static boolean isHerb(Item item) {
        // Check all herb products
        return item == ModRegistries.SCALEPLATE.get() ||
               item == ModRegistries.DEWPETAL_SHARD.get() ||
               item == ModRegistries.GOLDEN_LILYBELL.get() ||
               item == ModRegistries.CRYST_SPINE.get() ||
               item == ModRegistries.BURNT_NODE.get() ||
               item == ModRegistries.HEART_OF_STARDREAM.get();
    }
    
    public static boolean isOverworldHerb(Item item) {
        return item == ModRegistries.SCALEPLATE.get() ||
               item == ModRegistries.DEWPETAL_SHARD.get() ||
               item == ModRegistries.GOLDEN_LILYBELL.get();
    }
    
    public static boolean isNetherOrEndHerb(Item item) {
        return item == ModRegistries.CRYST_SPINE.get() ||
               item == ModRegistries.BURNT_NODE.get() ||
               item == ModRegistries.HEART_OF_STARDREAM.get();
    }
    
    // ==================== Infusing ====================
    
    /**
     * Check if current materials exactly match an infusing recipe and start infusing if so.
     * Called after adding a material to the cauldron.
     * 
     * Handles both normal infusing recipes AND Flowweave Ring binding recipes
     * (indicated by isFlowweaveRingBinding flag).
     */
    private void checkAndStartInfusing(Player player) {
        if (level == null || materials.isEmpty() || fluid.isEmpty()) return;
        
        // Already infusing or brewing
        if (isInfusing || isBrewing) return;
        
        // Find a recipe that exactly matches current materials
        CauldronInfusingRecipe recipe = findExactInfusingRecipe();
        
        if (recipe != null) {
            // Determine output based on recipe type
            ItemStack output;
            if (recipe.isFlowweaveRingBinding()) {
                // Special case: Flowweave Ring binding - create dynamic output
                output = createBoundFlowweaveRing();
            } else if (recipe.isFlowweaveRingUnbinding()) {
                // Special case: Flowweave Ring unbinding - create unbound ring
                output = createUnboundFlowweaveRing();
            } else {
                // Normal infusing - use recipe output
                output = recipe.getOutput();
            }
            
            // Start infusing
            infusingOutput = output;
            infusingTime = CauldronInfusingRecipe.INFUSING_TIME_TICKS;  // 5 seconds
            infusingProgress = 0;
            isInfusing = true;
            
            setChanged();
            syncToClient();
        }
    }
    
    /**
     * Create a bound Flowweave Ring based on current potion and herb costs.
     */
    private ItemStack createBoundFlowweaveRing() {
        if (materials.isEmpty()) return ItemStack.EMPTY;
        
        ItemStack ringStack = materials.get(0);
        ItemStack boundRing = ringStack.copy();
        
        MobEffect effect = fluid.getEffect();
        String effectId = effect != null ? 
                BuiltInRegistries.MOB_EFFECT.getKey(effect).toString() : "";
        
        Map<Item, Integer> herbCost = new HashMap<>(lastBrewedHerbs);
        
        FlowweaveRingItem.bindPotion(boundRing,
                effectId,
                fluid.getColor(),
                fluid.getDuration(),
                fluid.getAmplifier() + 1,
                herbCost);
        
        return boundRing;
    }
    
    /**
     * Create an unbound Flowweave Ring (clear any existing binding).
     */
    private ItemStack createUnboundFlowweaveRing() {
        if (materials.isEmpty()) return ItemStack.EMPTY;
        
        ItemStack ringStack = materials.get(0);
        ItemStack unboundRing = new ItemStack(ModRegistries.FLOWWEAVE_RING.get(), 1);
        // New ring has no custom data, so it's unbound
        
        return unboundRing;
    }
    
    /**
     * Find an infusing recipe that EXACTLY matches current materials and fluid.
     * This includes both normal infusing recipes and Flowweave Ring binding recipes.
     */
    private CauldronInfusingRecipe findExactInfusingRecipe() {
        if (level == null) return null;
        
        var recipes = level.getRecipeManager().getAllRecipesFor(ModRegistries.CAULDRON_INFUSING_RECIPE_TYPE.get());
        for (var recipeHolder : recipes) {
            CauldronInfusingRecipe recipe = recipeHolder.value();
            // Check fluid first (cheaper check)
            if (!recipe.matchesFluid(fluid)) continue;
            
            // For Flowweave Ring binding, also check that the ring is unbound
            if (recipe.isFlowweaveRingBinding()) {
                if (!matchesFlowweaveRingBindingConditions()) continue;
            }
            
            // Check materials exact match
            if (recipe.matchesMaterialsExactly(materials)) {
                return recipe;
            }
        }
        return null;
    }
    
    /**
     * Check additional conditions for Flowweave Ring binding.
     * Returns true if the ring in materials is a Flowweave Ring (bound or unbound).
     */
    private boolean matchesFlowweaveRingBindingConditions() {
        if (materials.size() != 1) return false;
        
        ItemStack ringStack = materials.get(0);
        if (!ringStack.is(ModRegistries.FLOWWEAVE_RING.get())) return false;
        if (ringStack.getCount() != 1) return false;
        
        // Ring can be bound or unbound - will be rebound to new potion
        return true;
    }
    
    // ==================== Flowweave Ring Interaction ====================
    
    /**
     * Handle Flowweave Ring right-click (not shift)
     * Used to start/complete brewing
     */
    public void onFlowweaveRingUse(Player player) {
        CauldronBlockEntity master = getMaster();
        if (master == null) return;
        
        // Already in a waiting state - don't trigger again
        if (master.isStartingBrew || master.isCompletingBrew) {
            return;
        }
        
        // Must have water and materials to start brewing
        if (!master.isBrewing && master.fluid.isWater() && !master.materials.isEmpty()) {
            boolean hasHeat = master.checkHeatSource();
            
            if (hasHeat) {
                // Find matching brewing recipe
                CauldronBrewingRecipe recipe = master.findBrewingRecipe();
                
                if (recipe != null) {
                    // Start the "starting brew" phase
                    master.isStartingBrew = true;
                    master.startingBrewTicks = 0;
                    // Convert water to boiling potion - effect type is now determined
                    // Keep water color during boiling (color changes on completion)
                    MobEffect effect = recipe.getEffect();
                    int boilingColor = 0x3F76E4; // Water color during boiling
                    master.fluid.convertToBoilingPotion(effect, boilingColor);
                    // Consume materials immediately - they are now part of the brewing process
                    master.materials.clear();
                    master.setChanged();
                    master.syncToClient();
                }
            }
            return;
        }
        
        // Complete brewing on second click (requires heat source)
        if (master.isBrewing) {
            boolean hasHeat = master.checkHeatSource();
            if (hasHeat) {
                // Start the "completing brew" phase
                master.isCompletingBrew = true;
                master.completingBrewTicks = 0;
                master.setChanged();
                master.syncToClient();
            }
        }
    }
    
    /**
     * Handle Flowweave Ring shift+right-click (force clear)
     */
    public void onFlowweaveRingShiftUse(Player player) {
        clearFluidAndReturnMaterials(player);
    }
    
    /**
     * Find a brewing recipe that matches current materials
     */
    private CauldronBrewingRecipe findBrewingRecipe() {
        if (level == null) return null;
        
        var recipes = level.getRecipeManager().getAllRecipesFor(ModRegistries.CAULDRON_BREWING_RECIPE_TYPE.get());
        for (var recipeHolder : recipes) {
            CauldronBrewingRecipe recipe = recipeHolder.value();
            if (recipe.matchesMaterials(materials)) {
                return recipe;
            }
        }
        return null;
    }
    
    /**
     * Complete the brewing process - convert boiling potion to finished potion
     */
    private void completeBrewing() {
        // Must be in boiling state
        if (!fluid.isBoilingPotion()) {
            isBrewing = false;
            return;
        }
        
        // Store herbs for Flowweave Ring binding
        lastBrewedHerbs.clear();
        lastBrewedHerbs.putAll(herbs);
        
        // Calculate duration from overworld herbs (30 seconds per herb)
        int extraSeconds = 0;
        for (Map.Entry<Item, Integer> entry : herbs.entrySet()) {
            if (isOverworldHerb(entry.getKey())) {
                extraSeconds += entry.getValue() * 30;  // 30 seconds per overworld herb
            }
        }
        int duration = Math.min(BASE_DURATION_SECONDS + extraSeconds, MAX_HERB_DURATION_SECONDS);
        
        // Calculate level from nether/end herbs (every 12 herbs = +1 level)
        int netherEndCount = 0;
        for (Map.Entry<Item, Integer> entry : herbs.entrySet()) {
            if (isNetherOrEndHerb(entry.getKey())) {
                netherEndCount += entry.getValue();
            }
        }
        int amplifier = netherEndCount / 12;  // Every 12 nether/end herbs = +1 level
        
        // Get the effect from boiling potion
        MobEffect effect = fluid.getEffect();
        
        // Limit amplifier based on effect type
        // Healing, harming, and regeneration max at level 2 (amplifier 1)
        // Most other effects max at level 4 (amplifier 3)
        int maxAmplifier = 3;  // Default max: level 4
        if (effect != null) {
            net.minecraft.resources.ResourceLocation effectId = BuiltInRegistries.MOB_EFFECT.getKey(effect);
            if (effectId != null) {
                String path = effectId.getPath();
                // Instant effects and regeneration have lower max level
                if (path.equals("instant_health") || path.equals("instant_damage") || path.equals("regeneration")) {
                    maxAmplifier = 1;  // Max level 2
                }
            }
        }
        amplifier = Math.min(amplifier, maxAmplifier);
        
        // Convert boiling potion to finished potion (effect and color already set)
        fluid.convertToPotion(duration, amplifier);
        
        // Clear brewing state
        isBrewing = false;
        isStartingBrew = false;
        isCompletingBrew = false;
        startingBrewTicks = 0;
        completingBrewTicks = 0;
        materials.clear();
        herbs.clear();
        
        setChanged();
        syncToClient();
    }
    
    /**
     * Complete the infusing process:
     * - Clear the consumed materials
     * - Add output to materials (floats on water surface)
     * - Convert any fluid to water
     */
    private void completeInfusing() {
        if (!isInfusing || infusingOutput.isEmpty()) {
            isInfusing = false;
            return;
        }
        
        // Clear the materials that were consumed
        materials.clear();
        
        // Add the output to materials (so it floats on the water surface)
        materials.add(infusingOutput.copy());
        
        // Convert any fluid (water, potion, lava, etc.) to water
        fluid.convertToWater();
        
        // Reset infusing state
        isInfusing = false;
        infusingProgress = 0;
        infusingTime = 0;
        infusingOutput = ItemStack.EMPTY;
        
        setChanged();
        syncToClient();
    }
    
    // ==================== Heat Source Detection ====================
    
    /**
     * Check if there's at least one heat source below the cauldron
     */
    public boolean checkHeatSource() {
        if (level == null || !formed) return false;
        
        CauldronBlockEntity master = getMaster();
        if (master == null) return false;
        
        boolean previousState = master.hasHeatSource;
        boolean foundHeat = false;
        
        BlockPos masterPos = master.getBlockPos();
        
        // Check the 3x3 area below the cauldron (y-1 from master)
        outer:
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos checkPos = masterPos.offset(x, -1, z);
                BlockState state = level.getBlockState(checkPos);
                
                if (isHeatSource(state)) {
                    foundHeat = true;
                    break outer;
                }
            }
        }
        
        master.hasHeatSource = foundHeat;
        
        // Sync to client if state changed
        if (previousState != foundHeat) {
            master.setChanged();
            master.syncToClient();
        }
        
        return foundHeat;
    }
    
    private boolean isHeatSource(BlockState state) {
        // Fire
        if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) {
            return true;
        }
        
        // Lava
        if (state.is(Blocks.LAVA)) {
            return true;
        }
        
        // Campfire (lit)
        if (state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)) {
            return true;
        }
        
        // Magma block
        if (state.is(Blocks.MAGMA_BLOCK)) {
            return true;
        }
        
        return false;
    }
    
    // ==================== Ticking ====================
    
    public static void serverTick(Level level, BlockPos pos, BlockState state, CauldronBlockEntity be) {
        if (!be.isMaster()) return;
        
        // Update heat source status
        be.checkHeatSource();
        
        // Collect items thrown into the cauldron
        be.collectItemsFromAbove(level);
        
        // Starting brew countdown (requires heat source)
        if (be.isStartingBrew && be.hasHeatSource) {
            be.startingBrewTicks++;
            if (be.startingBrewTicks >= MIN_STARTING_BREW_TICKS) {
                // Enter brewing phase - can now add herbs
                be.isStartingBrew = false;
                be.isBrewing = true;
                be.startingBrewTicks = 0;
                be.setChanged();
                be.syncToClient();
            }
        }
        
        // Completing brew countdown (requires heat source)
        if (be.isCompletingBrew && be.hasHeatSource) {
            be.completingBrewTicks++;
            if (be.completingBrewTicks >= MIN_COMPLETING_BREW_TICKS) {
                // Complete brewing - get product
                be.isCompletingBrew = false;
                be.completingBrewTicks = 0;
                be.completeBrewing();
            }
        }
        
        // Infusing progress (does NOT require heat source)
        if (be.isInfusing) {
            be.infusingProgress++;
            if (be.infusingProgress >= be.infusingTime) {
                // Infusing complete
                be.completeInfusing();
            } else if (be.infusingProgress % 20 == 0) {
                be.setChanged();
                be.syncToClient();
            }
        }
    }
    
    /**
     * Collect item entities that fall into the cauldron
     */
    private void collectItemsFromAbove(Level level) {
        // Only collect if we have fluid
        if (fluid.isEmpty()) return;
        
        // Define the collection area (3x3 above the cauldron, slightly above liquid surface)
        BlockPos masterPos = getBlockPos();
        double minX = masterPos.getX() - 1;
        double maxX = masterPos.getX() + 2;
        double minY = masterPos.getY() + 1.5; // Liquid surface level
        double maxY = masterPos.getY() + 3;   // Up to 1.5 blocks above
        double minZ = masterPos.getZ() - 1;
        double maxZ = masterPos.getZ() + 2;
        
        AABB collectArea = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        
        // Find all item entities in the area
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, collectArea);
        
        for (ItemEntity itemEntity : items) {
            ItemStack stack = itemEntity.getItem();
            if (stack.isEmpty()) continue;
            
            // Try to add the item
            int originalCount = stack.getCount();
            boolean added = false;
            
            // If brewing, only accept herbs
            if (isBrewing) {
                if (isHerb(stack.getItem())) {
                    added = addHerbFromEntity(stack);
                }
            } else if (!isInfusing) {
                // Add as material (works for any fluid: water or potion)
                added = addMaterialFromEntity(stack);
                
                if (added) {
                    // After adding material, check if materials now match an infusing recipe
                    checkAndStartInfusing(null);
                }
            }
            
            if (added) {
                if (stack.isEmpty()) {
                    itemEntity.discard();
                } else if (stack.getCount() < originalCount) {
                    itemEntity.setItem(stack);
                }
            }
        }
    }
    
    private boolean addMaterialFromEntity(ItemStack stack) {
        // Like a chest: max 10 slots, each slot max 64 items (stack limit)
        // First try to stack with existing same items
        for (ItemStack existing : materials) {
            if (ItemStack.isSameItemSameComponents(existing, stack)) {
                int maxStack = existing.getMaxStackSize();
                int canAdd = Math.min(stack.getCount(), maxStack - existing.getCount());
                if (canAdd > 0) {
                    existing.grow(canAdd);
                    stack.shrink(canAdd);
                    setChanged();
                    syncToClient();
                    playItemSplashSound();
                    return true;
                }
            }
        }
        
        // If can't stack and we have room for a new slot, add it
        if (materials.size() < MAX_MATERIAL_TYPES) {
            ItemStack toAdd = stack.copy();
            int maxStack = toAdd.getMaxStackSize();
            toAdd.setCount(Math.min(stack.getCount(), maxStack));
            materials.add(toAdd);
            stack.shrink(toAdd.getCount());
            setChanged();
            syncToClient();
            playItemSplashSound();
            return true;
        }
        
        return false;
    }
    
    private boolean addHerbFromEntity(ItemStack stack) {
        Item item = stack.getItem();
        
        if (!isHerb(item)) {
            return false;
        }
        
        // No limit on herbs - absorb all
        int current = herbs.getOrDefault(item, 0);
        int toAdd = stack.getCount();
        
        herbs.put(item, current + toAdd);
        stack.shrink(toAdd);
        setChanged();
        syncToClient();
        playItemSplashSound();
        return true;
    }
    
    /**
     * Play splash sound when item enters the cauldron
     */
    private void playItemSplashSound() {
        if (level != null && !level.isClientSide) {
            level.playSound(null, worldPosition, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.3F, 1.2F);
        }
    }
    
    // ==================== Getters ====================
    
    /**
     * Get the color to render the fluid
     */
    public int getFluidColor() {
        CauldronBlockEntity master = getMaster();
        return master != null ? master.fluid.getColor() : 0x3F76E4;
    }
    
    public int getPotionDuration() {
        CauldronBlockEntity master = getMaster();
        if (master == null || !master.fluid.isPotion()) return BASE_DURATION_SECONDS;
        return master.fluid.getDuration();
    }
    
    public int getPotionLevel() {
        CauldronBlockEntity master = getMaster();
        if (master == null || !master.fluid.isPotion()) return 1;
        return master.fluid.getAmplifier() + 1;
    }
    
    public MobEffect getPotionEffect() {
        CauldronBlockEntity master = getMaster();
        if (master == null || !master.fluid.isPotion()) return null;
        return master.fluid.getEffect();
    }
    
    public int getInfusingProgress() {
        CauldronBlockEntity master = getMaster();
        if (master == null || master.infusingTime <= 0) return 0;
        return (master.infusingProgress * 100) / master.infusingTime;
    }
    
    public boolean isInfusing() {
        CauldronBlockEntity master = getMaster();
        return master != null && master.isInfusing;
    }
    
    public boolean hasHeatSource() {
        CauldronBlockEntity master = getMaster();
        return master != null && master.hasHeatSource;
    }
    
    public ItemStack getInfusingInput() {
        CauldronBlockEntity master = getMaster();
        return master != null ? master.infusingInput.copy() : ItemStack.EMPTY;
    }
    
    public ItemStack getInfusingOutput() {
        CauldronBlockEntity master = getMaster();
        return master != null ? master.infusingOutput.copy() : ItemStack.EMPTY;
    }
    
    /**
     * Get the herbs used in last brewing (for Flowweave Ring binding)
     */
    public Map<Item, Integer> getLastBrewedHerbs() {
        CauldronBlockEntity master = getMaster();
        return master != null ? new HashMap<>(master.lastBrewedHerbs) : new HashMap<>();
    }
    
    // ==================== Multiblock Management ====================
    
    @Override
    public void disassemble() {
        if (level == null || level.isClientSide || !formed) {
            return;
        }
        
        CauldronBlockEntity master = getMaster();
        if (master == null) {
            return;
        }
        
        BlockPos masterPos = master.getBlockPos();
        BlockPos breakPos = getBlockPos();
        
        // Drop all stored materials ONLY if not brewing
        // During brewing, materials have already been consumed
        if (!master.isBrewing) {
            for (ItemStack stack : master.materials) {
                if (!stack.isEmpty()) {
                    ItemEntity entityItem = new ItemEntity(
                            level,
                            masterPos.getX() + 0.5,
                            masterPos.getY() + 0.5,
                            masterPos.getZ() + 0.5,
                            stack.copy()
                    );
                    level.addFreshEntity(entityItem);
                }
            }
        }
        // Note: herbs are also consumed during brewing, so no need to drop them
        
        // Get all block positions in the structure
        List<BlockPos> structurePositions = getStructurePositions(masterPos);
        
        // First pass: Mark all blocks as not formed
        for (BlockPos targetPos : structurePositions) {
            if (level.getBlockState(targetPos).is(ModRegistries.CAULDRON.get())) {
                if (level.getBlockEntity(targetPos) instanceof CauldronBlockEntity cauldron) {
                    cauldron.formed = false;
                    cauldron.renderAABB = null;
                    
                    if (!targetPos.equals(breakPos)) {
                        cauldron.suppressDrops = true;
                    }
                    
                    cauldron.setChanged();
                    BlockState state = level.getBlockState(targetPos);
                    level.sendBlockUpdated(targetPos, state, state, 3);
                }
            }
        }
        
        // Second pass: Replace non-broken blocks with original blocks
        for (BlockPos targetPos : structurePositions) {
            if (level.getBlockState(targetPos).is(ModRegistries.CAULDRON.get())) {
                if (!targetPos.equals(breakPos)) {
                    // Determine what original block to place
                    BlockState originalBlock = getOriginalBlockForPosition(targetPos, masterPos);
                    level.setBlock(targetPos, originalBlock, 2);
                }
            }
        }
        
        setChanged();
    }
    
    private List<BlockPos> getStructurePositions(BlockPos masterPos) {
        List<BlockPos> positions = new ArrayList<>();
        
        // Layer 1 (y=0, master layer): All 9 positions
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                positions.add(masterPos.offset(x, 0, z));
            }
        }
        
        // Layer 2 (y+1): All 9 positions
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                positions.add(masterPos.offset(x, 1, z));
            }
        }
        
        return positions;
    }
    
    private BlockState getOriginalBlockForPosition(BlockPos targetPos, BlockPos masterPos) {
        int dy = targetPos.getY() - masterPos.getY();
        int dx = targetPos.getX() - masterPos.getX();
        int dz = targetPos.getZ() - masterPos.getZ();
        
        if (dy == 0) {
            // Layer 1 (master layer)
            // Corners: Lumistone Bricks
            if ((dx == -1 || dx == 1) && (dz == -1 || dz == 1)) {
                return ModRegistries.LUMISTONE_BRICKS.get().defaultBlockState();
            }
            // Edge middles + center (5 positions): Lumistone Brick Slab (top half)
            return ModRegistries.LUMISTONE_BRICK_SLAB.get().defaultBlockState()
                    .setValue(SlabBlock.TYPE, SlabType.TOP);
        } else if (dy == 1) {
            // Layer 2
            if (dx == 0 && dz == 0) {
                // Center: Air
                return Blocks.AIR.defaultBlockState();
            } else {
                // Outer 8: Lumistone Bricks
                return ModRegistries.LUMISTONE_BRICKS.get().defaultBlockState();
            }
        }
        
        return Blocks.AIR.defaultBlockState();
    }
    
    @Override
    public ItemStack getOriginalBlock() {
        return new ItemStack(ModRegistries.LUMISTONE_BRICKS.get());
    }
    
    // ==================== NBT Serialization ====================
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        
        if (isMaster()) {
            // Save fluid
            tag.put("Fluid", fluid.save(registries));
            
            // Save brewing state
            tag.putBoolean("IsBrewing", isBrewing);
            tag.putBoolean("IsStartingBrew", isStartingBrew);
            tag.putBoolean("IsCompletingBrew", isCompletingBrew);
            tag.putInt("StartingBrewTicks", startingBrewTicks);
            tag.putInt("CompletingBrewTicks", completingBrewTicks);
            tag.putBoolean("HasHeatSource", hasHeatSource);
            
            // Save materials
            ListTag materialsTag = new ListTag();
            for (ItemStack stack : materials) {
                if (!stack.isEmpty()) {
                    materialsTag.add(stack.save(registries));
                }
            }
            tag.put("Materials", materialsTag);
            
            // Save herbs
            CompoundTag herbsTag = new CompoundTag();
            for (Map.Entry<Item, Integer> entry : herbs.entrySet()) {
                String key = entry.getKey().builtInRegistryHolder().key().location().toString();
                herbsTag.putInt(key, entry.getValue());
            }
            tag.put("Herbs", herbsTag);
            
            // Save last brewed herbs (for Flowweave Ring binding)
            CompoundTag lastHerbsTag = new CompoundTag();
            for (Map.Entry<Item, Integer> entry : lastBrewedHerbs.entrySet()) {
                String key = entry.getKey().builtInRegistryHolder().key().location().toString();
                lastHerbsTag.putInt(key, entry.getValue());
            }
            tag.put("LastBrewedHerbs", lastHerbsTag);
            
            // Save infusing state
            tag.putBoolean("IsInfusing", isInfusing);
            tag.putInt("InfusingProgress", infusingProgress);
            tag.putInt("InfusingTime", infusingTime);
            if (!infusingInput.isEmpty()) {
                tag.put("InfusingInput", infusingInput.save(registries));
            }
            if (!infusingOutput.isEmpty()) {
                tag.put("InfusingOutput", infusingOutput.save(registries));
            }
        }
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
        if (isMaster()) {
            // Load fluid
            if (tag.contains("Fluid")) {
                fluid = CauldronFluid.load(tag.getCompound("Fluid"), registries);
            } else {
                fluid = CauldronFluid.empty();
            }
            
            // Load brewing state
            isBrewing = tag.getBoolean("IsBrewing");
            isStartingBrew = tag.getBoolean("IsStartingBrew");
            isCompletingBrew = tag.getBoolean("IsCompletingBrew");
            startingBrewTicks = tag.getInt("StartingBrewTicks");
            completingBrewTicks = tag.getInt("CompletingBrewTicks");
            hasHeatSource = tag.getBoolean("HasHeatSource");
            
            // Load materials
            materials.clear();
            ListTag materialsTag = tag.getList("Materials", Tag.TAG_COMPOUND);
            for (int i = 0; i < materialsTag.size(); i++) {
                ItemStack.parse(registries, materialsTag.getCompound(i)).ifPresent(materials::add);
            }
            
            // Load herbs
            herbs.clear();
            CompoundTag herbsTag = tag.getCompound("Herbs");
            for (String key : herbsTag.getAllKeys()) {
                Item herb = getHerbFromKey(key);
                if (herb != null) {
                    herbs.put(herb, herbsTag.getInt(key));
                }
            }
            
            // Load last brewed herbs
            lastBrewedHerbs.clear();
            if (tag.contains("LastBrewedHerbs")) {
                CompoundTag lastHerbsTag = tag.getCompound("LastBrewedHerbs");
                for (String key : lastHerbsTag.getAllKeys()) {
                    Item herb = getHerbFromKey(key);
                    if (herb != null) {
                        lastBrewedHerbs.put(herb, lastHerbsTag.getInt(key));
                    }
                }
            }
            
            // Load infusing state
            isInfusing = tag.getBoolean("IsInfusing");
            infusingProgress = tag.getInt("InfusingProgress");
            infusingTime = tag.getInt("InfusingTime");
            if (tag.contains("InfusingInput")) {
                infusingInput = ItemStack.parse(registries, tag.getCompound("InfusingInput")).orElse(ItemStack.EMPTY);
            }
            if (tag.contains("InfusingOutput")) {
                infusingOutput = ItemStack.parse(registries, tag.getCompound("InfusingOutput")).orElse(ItemStack.EMPTY);
            }
        }
    }
    
    private Item getHerbFromKey(String key) {
        if (key.contains("scaleplate")) return ModRegistries.SCALEPLATE.get();
        if (key.contains("dewpetal_shard")) return ModRegistries.DEWPETAL_SHARD.get();
        if (key.contains("golden_lilybell")) return ModRegistries.GOLDEN_LILYBELL.get();
        if (key.contains("cryst_spine")) return ModRegistries.CRYST_SPINE.get();
        if (key.contains("burnt_node")) return ModRegistries.BURNT_NODE.get();
        if (key.contains("heart_of_stardream")) return ModRegistries.HEART_OF_STARDREAM.get();
        return null;
    }
    
    // ==================== CauldronFluid Inner Class ====================
    
    /**
     * Represents the fluid content of a Cauldron.
     * Can be empty, a vanilla/modded fluid, or a potion with custom properties.
     */
    public static class CauldronFluid {
        
        public enum FluidType {
            EMPTY,          // No fluid
            FLUID,          // Vanilla/modded fluid (water, lava, etc.)
            BOILING_POTION, // Brewing in progress - effect determined, duration/level pending
            POTION          // Finished potion with effect properties
        }
        
        private FluidType type = FluidType.EMPTY;
        private int amount = 0;  // In millibuckets (1000 = 1 bucket)
        
        // For FLUID type
        private Fluid fluid = null;
        
        // For POTION type
        private MobEffect effect = null;
        private int duration = 0;      // In seconds
        private int amplifier = 0;     // Potion level (0 = level 1)
        private int color = 0x3F76E4;  // Default water color
        
        // Private constructor - use factory methods
        private CauldronFluid() {}
        
        // ==================== Factory Methods ====================
        
        public static CauldronFluid empty() {
            return new CauldronFluid();
        }
        
        public static CauldronFluid ofFluid(Fluid fluid, int amount) {
            CauldronFluid cf = new CauldronFluid();
            cf.type = FluidType.FLUID;
            cf.fluid = fluid;
            cf.amount = amount;
            return cf;
        }
        
        public static CauldronFluid ofWater(int amount) {
            return ofFluid(Fluids.WATER, amount);
        }
        
        public static CauldronFluid ofPotion(MobEffect effect, int duration, int amplifier, int color, int amount) {
            CauldronFluid cf = new CauldronFluid();
            cf.type = FluidType.POTION;
            cf.effect = effect;
            cf.duration = duration;
            cf.amplifier = amplifier;
            cf.color = color;
            cf.amount = amount;
            return cf;
        }
        
        public static CauldronFluid ofBoilingPotion(MobEffect effect, int color, int amount) {
            CauldronFluid cf = new CauldronFluid();
            cf.type = FluidType.BOILING_POTION;
            cf.effect = effect;
            cf.duration = 0;   // To be determined by herbs
            cf.amplifier = 0;  // To be determined by herbs
            cf.color = color;
            cf.amount = amount;
            return cf;
        }
        
        // ==================== Getters ====================
        
        public FluidType getType() {
            return type;
        }
        
        public boolean isEmpty() {
            return type == FluidType.EMPTY || amount <= 0;
        }
        
        public boolean isFluid() {
            return type == FluidType.FLUID;
        }
        
        public boolean isPotion() {
            return type == FluidType.POTION;
        }
        
        public boolean isBoilingPotion() {
            return type == FluidType.BOILING_POTION;
        }
        
        public boolean isWater() {
            return type == FluidType.FLUID && fluid == Fluids.WATER;
        }
        
        public int getAmount() {
            return amount;
        }
        
        public Fluid getFluid() {
            return fluid;
        }
        
        public MobEffect getEffect() {
            return effect;
        }
        
        public int getDuration() {
            return duration;
        }
        
        public int getAmplifier() {
            return amplifier;
        }
        
        public int getColor() {
            if (type == FluidType.EMPTY) {
                return 0;
            } else if (type == FluidType.FLUID) {
                return getFluidColor(fluid);
            } else {
                // POTION or BOILING_POTION
                return color;
            }
        }
        
        private static int getFluidColor(Fluid fluid) {
            if (fluid == null || fluid == Fluids.EMPTY) return 0;
            if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) return 0x3F76E4;
            if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) return 0xFF5500;
            return 0xFFFFFF;
        }
        
        // ==================== Setters ====================
        
        public void setAmount(int amount) {
            this.amount = amount;
            if (amount <= 0) {
                clear();
            }
        }
        
        public void clear() {
            this.type = FluidType.EMPTY;
            this.amount = 0;
            this.fluid = null;
            this.effect = null;
            this.duration = 0;
            this.amplifier = 0;
            this.color = 0x3F76E4;
        }
        
        /**
         * Convert this fluid to water (used after infusing completes)
         */
        public void convertToWater() {
            this.type = FluidType.FLUID;
            this.fluid = Fluids.WATER;
            this.effect = null;
            this.duration = 0;
            this.amplifier = 0;
            this.color = 0x3F76E4;
            // Keep the same amount
        }
        
        /**
         * Convert water to boiling potion (used when brewing starts)
         * Effect type is determined, but duration/amplifier are pending (based on herbs added later)
         */
        public void convertToBoilingPotion(MobEffect effect, int color) {
            this.type = FluidType.BOILING_POTION;
            this.fluid = null;
            this.effect = effect;
            this.duration = 0;   // To be determined
            this.amplifier = 0;  // To be determined
            this.color = color;
            // Keep the same amount
        }
        
        /**
         * Convert boiling potion to finished potion (used when brewing completes)
         */
        public void convertToPotion(int duration, int amplifier) {
            if (this.type != FluidType.BOILING_POTION) return;
            this.type = FluidType.POTION;
            this.duration = duration;
            this.amplifier = amplifier;
            // Set color from effect (boiling used water color)
            if (this.effect != null) {
                this.color = this.effect.getColor();
            }
        }
        
        /**
         * Convert water to potion directly (used for legacy compatibility)
         */
        public void convertToPotion(MobEffect effect, int duration, int amplifier, int color) {
            this.type = FluidType.POTION;
            this.fluid = null;
            this.effect = effect;
            this.duration = duration;
            this.amplifier = amplifier;
            this.color = color;
            // Keep the same amount
        }
        
        // ==================== NBT Serialization ====================
        
        public CompoundTag save(HolderLookup.Provider registries) {
            CompoundTag tag = new CompoundTag();
            tag.putString("Type", type.name());
            tag.putInt("Amount", amount);
            
            if (type == FluidType.FLUID && fluid != null) {
                tag.putString("Fluid", net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(fluid).toString());
            } else if ((type == FluidType.POTION || type == FluidType.BOILING_POTION) && effect != null) {
                tag.putString("Effect", net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect).toString());
                tag.putInt("Duration", duration);
                tag.putInt("Amplifier", amplifier);
                tag.putInt("Color", color);
            }
            
            return tag;
        }
        
        public static CauldronFluid load(CompoundTag tag, HolderLookup.Provider registries) {
            CauldronFluid cf = new CauldronFluid();
            
            String typeName = tag.getString("Type");
            cf.type = FluidType.valueOf(typeName.isEmpty() ? "EMPTY" : typeName);
            cf.amount = tag.getInt("Amount");
            
            if (cf.type == FluidType.FLUID && tag.contains("Fluid")) {
                net.minecraft.resources.ResourceLocation fluidId = net.minecraft.resources.ResourceLocation.tryParse(tag.getString("Fluid"));
                if (fluidId != null) {
                    cf.fluid = net.minecraft.core.registries.BuiltInRegistries.FLUID.get(fluidId);
                }
            } else if ((cf.type == FluidType.POTION || cf.type == FluidType.BOILING_POTION) && tag.contains("Effect")) {
                net.minecraft.resources.ResourceLocation effectId = net.minecraft.resources.ResourceLocation.tryParse(tag.getString("Effect"));
                if (effectId != null) {
                    cf.effect = net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.get(effectId);
                }
                cf.duration = tag.getInt("Duration");
                cf.amplifier = tag.getInt("Amplifier");
                cf.color = tag.getInt("Color");
            }
            
            return cf;
        }
        
        // ==================== Matching ====================
        
        /**
         * Check if this fluid matches a specific vanilla/modded fluid
         */
        public boolean matchesFluid(Fluid fluid) {
            return type == FluidType.FLUID && this.fluid == fluid;
        }
        
        /**
         * Check if this fluid matches potion requirements
         */
        public boolean matchesPotion(MobEffect requiredEffect, int minDuration, int minAmplifier) {
            if (type != FluidType.POTION) return false;
            if (requiredEffect != null && this.effect != requiredEffect) return false;
            return this.duration >= minDuration && this.amplifier >= minAmplifier;
        }
        
        /**
         * Check if this matches any fluid (water or potion)
         */
        public boolean matchesAny() {
            return !isEmpty();
        }
        
        // ==================== Copy ====================
        
        public CauldronFluid copy() {
            CauldronFluid cf = new CauldronFluid();
            cf.type = this.type;
            cf.amount = this.amount;
            cf.fluid = this.fluid;
            cf.effect = this.effect;
            cf.duration = this.duration;
            cf.amplifier = this.amplifier;
            cf.color = this.color;
            return cf;
        }
    }
}
