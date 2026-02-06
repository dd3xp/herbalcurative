package com.cahcap.herbalcurative.common.blockentity;

import com.cahcap.herbalcurative.common.item.FlowweaveRingItem;
import com.cahcap.herbalcurative.common.recipe.CauldronRecipe;
import com.cahcap.herbalcurative.common.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Block Entity for the Cauldron multiblock structure.
 * 
 * Phases:
 * - Phase 0: Empty/No water
 * - Phase 1: Water added, can add materials
 * - Phase 2: Brewing started, can add herbs, cannot remove materials
 * - Phase 3: Brewing complete, can extract potion with pot
 */
public class CauldronBlockEntity extends MultiblockPartBlockEntity {
    
    // Brewing phases
    public static final int PHASE_EMPTY = 0;
    public static final int PHASE_WATER = 1;
    public static final int PHASE_BREWING = 2;
    public static final int PHASE_COMPLETE = 3;
    
    // Cached render bounding box for multiblock rendering
    public net.minecraft.world.phys.AABB renderAABB = null;
    
    // Storage limits
    public static final int MAX_MATERIAL_TYPES = 9;
    public static final int MAX_MATERIAL_COUNT = 64;
    public static final int MAX_HERB_DURATION_MINUTES = 10;
    public static final int BASE_DURATION_MINUTES = 2;
    
    // Current phase
    private int phase = PHASE_EMPTY;
    
    // Has water
    private boolean hasWater = false;
    
    // Materials storage (Phase 1 - can be added/removed)
    private final List<ItemStack> materials = new ArrayList<>();
    
    // Herbs storage (Phase 2 - can only be added)
    private final Map<Item, Integer> herbs = new HashMap<>();
    
    // Potion properties (calculated when brewing completes)
    private int potionColor = 0x3F76E4; // Default water color
    private int potionDuration = BASE_DURATION_MINUTES; // In minutes
    private int potionLevel = 1;
    private String potionType = ""; // Determined by materials
    
    // Brewing progress
    private int brewingTicks = 0;
    private static final int BREWING_TIME = 200; // 10 seconds
    
    // Heat source detection
    private boolean hasHeatSource = false;
    
    // Suppress drops during disassembly
    public boolean suppressDrops = false;
    
    // Cauldron crafting (Phase 3)
    private ItemStack craftingInput = ItemStack.EMPTY;
    private ItemStack craftingOutput = ItemStack.EMPTY;
    private int craftingProgress = 0;
    private int craftingTime = 0;
    private boolean isCrafting = false;
    
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
    
    // ==================== Phase Management ====================
    
    public int getPhase() {
        CauldronBlockEntity master = getMaster();
        return master != null ? master.phase : PHASE_EMPTY;
    }
    
    public boolean hasWater() {
        CauldronBlockEntity master = getMaster();
        return master != null && master.hasWater;
    }
    
    /**
     * Add water to the cauldron (Phase 0 -> Phase 1)
     */
    public boolean addWater() {
        CauldronBlockEntity master = getMaster();
        if (master == null) return false;
        
        if (master.phase == PHASE_EMPTY && !master.hasWater) {
            master.hasWater = true;
            master.phase = PHASE_WATER;
            master.setChanged();
            master.syncToClient();
            return true;
        }
        return false;
    }
    
    // ==================== Material Management ====================
    
    /**
     * Add an item (material or herb) to the cauldron
     */
    public boolean addItem(ItemStack stack, Player player) {
        CauldronBlockEntity master = getMaster();
        if (master == null) return false;
        
        // Phase 1: Add materials
        if (master.phase == PHASE_WATER) {
            return master.addMaterial(stack, player);
        }
        
        // Phase 2: Add herbs
        if (master.phase == PHASE_BREWING) {
            return master.addHerb(stack, player);
        }
        
        return false;
    }
    
    private boolean addMaterial(ItemStack stack, Player player) {
        if (materials.size() >= MAX_MATERIAL_TYPES) {
            return false;
        }
        
        // Check if this material type already exists
        for (ItemStack existing : materials) {
            if (ItemStack.isSameItemSameComponents(existing, stack)) {
                int canAdd = Math.min(stack.getCount(), MAX_MATERIAL_COUNT - existing.getCount());
                if (canAdd > 0) {
                    existing.grow(canAdd);
                    stack.shrink(canAdd);
                    setChanged();
                    syncToClient();
                    return true;
                }
                return false;
            }
        }
        
        // Add new material type
        ItemStack toAdd = stack.copy();
        toAdd.setCount(Math.min(stack.getCount(), MAX_MATERIAL_COUNT));
        materials.add(toAdd);
        stack.shrink(toAdd.getCount());
        setChanged();
        syncToClient();
        return true;
    }
    
    private boolean addHerb(ItemStack stack, Player player) {
        Item item = stack.getItem();
        
        // Check if it's a valid herb
        if (!isHerb(item)) {
            return false;
        }
        
        int current = herbs.getOrDefault(item, 0);
        int canAdd = Math.min(stack.getCount(), 64 - current);
        
        if (canAdd > 0) {
            herbs.put(item, current + canAdd);
            stack.shrink(canAdd);
            setChanged();
            syncToClient();
            return true;
        }
        return false;
    }
    
    /**
     * Extract a material from the cauldron (Phase 1 only)
     */
    public ItemStack extractItem(Player player) {
        CauldronBlockEntity master = getMaster();
        if (master == null || master.phase != PHASE_WATER) {
            return ItemStack.EMPTY;
        }
        
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
    
    // ==================== Flowweave Ring Interaction ====================
    
    /**
     * Handle Flowweave Ring right-click
     */
    public void onFlowweaveRingUse(Player player) {
        CauldronBlockEntity master = getMaster();
        if (master == null) return;
        
        // Phase 1 -> Phase 2: Start brewing
        if (master.phase == PHASE_WATER && !master.materials.isEmpty()) {
            if (master.checkHeatSource()) {
                master.phase = PHASE_BREWING;
                master.brewingTicks = 0;
                master.calculatePotionFromMaterials();
                master.setChanged();
                master.syncToClient();
            }
            return;
        }
        
        // Phase 2 -> Phase 3: Complete brewing
        if (master.phase == PHASE_BREWING && master.brewingTicks >= BREWING_TIME) {
            master.phase = PHASE_COMPLETE;
            master.calculateFinalPotion();
            master.setChanged();
            master.syncToClient();
        }
    }
    
    /**
     * Calculate initial potion properties from materials
     */
    private void calculatePotionFromMaterials() {
        // TODO: Implement recipe matching for materials
        // For now, just use a default potion
        potionType = "healing";
        potionColor = 0xF82423; // Red for healing
    }
    
    /**
     * Calculate final potion with herb bonuses
     */
    private void calculateFinalPotion() {
        // Store herbs for Flowweave Ring binding before clearing
        lastBrewedHerbs.clear();
        lastBrewedHerbs.putAll(herbs);
        
        // Calculate duration from overworld herbs
        int extraMinutes = 0;
        for (Map.Entry<Item, Integer> entry : herbs.entrySet()) {
            if (isOverworldHerb(entry.getKey())) {
                extraMinutes += entry.getValue();
            }
        }
        potionDuration = Math.min(BASE_DURATION_MINUTES + extraMinutes, MAX_HERB_DURATION_MINUTES);
        
        // Calculate level from nether/end herbs (every 8 herbs = +1 level)
        int netherEndCount = 0;
        for (Map.Entry<Item, Integer> entry : herbs.entrySet()) {
            if (isNetherOrEndHerb(entry.getKey())) {
                netherEndCount += entry.getValue();
            }
        }
        potionLevel = 1 + (netherEndCount / 8);
        
        // Clear materials and herbs after brewing
        materials.clear();
        herbs.clear();
    }
    
    // ==================== Heat Source Detection ====================
    
    /**
     * Check if there's at least one heat source below the cauldron
     */
    public boolean checkHeatSource() {
        if (level == null || !formed) return false;
        
        CauldronBlockEntity master = getMaster();
        if (master == null) return false;
        
        BlockPos masterPos = master.getBlockPos();
        
        // Check the 3x3 area below the cauldron (y-2 from master which is at y+1)
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos checkPos = masterPos.offset(x, -2, z);
                if (isHeatSource(level.getBlockState(checkPos))) {
                    master.hasHeatSource = true;
                    return true;
                }
            }
        }
        
        master.hasHeatSource = false;
        return false;
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
        
        // Phase 2: Brewing progress
        if (be.phase == PHASE_BREWING && be.hasHeatSource) {
            be.brewingTicks++;
            if (be.brewingTicks % 20 == 0) {
                be.setChanged();
                be.syncToClient();
            }
        }
        
        // Phase 3: Cauldron crafting progress
        if (be.phase == PHASE_COMPLETE && be.isCrafting && be.hasHeatSource) {
            be.craftingProgress++;
            if (be.craftingProgress >= be.craftingTime) {
                // Crafting complete
                be.isCrafting = false;
                be.craftingInput = ItemStack.EMPTY;
                be.setChanged();
                be.syncToClient();
            } else if (be.craftingProgress % 20 == 0) {
                be.setChanged();
                be.syncToClient();
            }
        }
    }
    
    // ==================== Getters ====================
    
    public int getPotionColor() {
        CauldronBlockEntity master = getMaster();
        return master != null ? master.potionColor : 0x3F76E4;
    }
    
    public int getPotionDuration() {
        CauldronBlockEntity master = getMaster();
        return master != null ? master.potionDuration : BASE_DURATION_MINUTES;
    }
    
    public int getPotionLevel() {
        CauldronBlockEntity master = getMaster();
        return master != null ? master.potionLevel : 1;
    }
    
    public String getPotionType() {
        CauldronBlockEntity master = getMaster();
        return master != null ? master.potionType : "";
    }
    
    public int getBrewingProgress() {
        CauldronBlockEntity master = getMaster();
        if (master == null) return 0;
        return (master.brewingTicks * 100) / BREWING_TIME;
    }
    
    public boolean hasHeatSource() {
        CauldronBlockEntity master = getMaster();
        return master != null && master.hasHeatSource;
    }
    
    /**
     * Reset the cauldron to empty state after potion is collected
     */
    public void resetCauldron() {
        CauldronBlockEntity master = getMaster();
        if (master == null) return;
        
        master.phase = PHASE_EMPTY;
        master.hasWater = false;
        master.materials.clear();
        master.herbs.clear();
        master.brewingTicks = 0;
        master.potionType = "";
        master.potionColor = 0x3F76E4;
        master.potionDuration = BASE_DURATION_MINUTES;
        master.potionLevel = 1;
        master.craftingInput = ItemStack.EMPTY;
        master.craftingOutput = ItemStack.EMPTY;
        master.craftingProgress = 0;
        master.craftingTime = 0;
        master.isCrafting = false;
        master.setChanged();
        master.syncToClient();
    }
    
    // ==================== Cauldron Crafting (Phase 3) ====================
    
    /**
     * Try to add an item for cauldron crafting (Phase 3 only)
     */
    public boolean addCraftingItem(ItemStack stack) {
        CauldronBlockEntity master = getMaster();
        if (master == null || level == null) return false;
        
        // Can only craft in phase 3 (complete)
        if (master.phase != PHASE_COMPLETE) return false;
        
        // Can't add if already crafting
        if (master.isCrafting) return false;
        
        // Can't add if there's already an output
        if (!master.craftingOutput.isEmpty()) return false;
        
        // Special case: Flowweave Ring binding
        if (stack.is(ModRegistries.FLOWWEAVE_RING.get())) {
            return tryBindFlowweaveRing(stack);
        }
        
        // Try to find a matching recipe
        CauldronRecipe recipe = findCraftingRecipe(stack);
        if (recipe == null) return false;
        
        // Check if potion matches recipe requirements
        if (!recipe.matchesPotion(master.potionType, master.potionDuration, master.potionLevel)) {
            return false;
        }
        
        // Start crafting
        master.craftingInput = stack.copy();
        master.craftingInput.setCount(1);
        master.craftingOutput = recipe.getOutput();
        master.craftingTime = recipe.getProcessingTime();
        master.craftingProgress = 0;
        master.isCrafting = true;
        master.setChanged();
        master.syncToClient();
        
        return true;
    }
    
    /**
     * Try to bind a Flowweave Ring to the current potion
     */
    private boolean tryBindFlowweaveRing(ItemStack ringStack) {
        CauldronBlockEntity master = getMaster();
        if (master == null) return false;
        
        // Already bound - can't rebind
        if (FlowweaveRingItem.hasBoundPotion(ringStack)) {
            return false;
        }
        
        // Requires at least 8 minutes duration
        if (master.potionDuration < FlowweaveRingItem.MIN_BIND_DURATION) {
            return false;
        }
        
        // Create the bound ring
        ItemStack boundRing = ringStack.copy();
        boundRing.setCount(1);
        
        // Get the herbs that were used in brewing (from lastBrewedHerbs)
        Map<Item, Integer> herbCost = new HashMap<>(master.lastBrewedHerbs);
        
        // Bind the potion to the ring
        FlowweaveRingItem.bindPotion(boundRing, 
                master.potionType, 
                master.potionColor, 
                master.potionDuration, 
                master.potionLevel, 
                herbCost);
        
        // Set up as instant crafting (no processing time)
        master.craftingInput = ringStack.copy();
        master.craftingInput.setCount(1);
        master.craftingOutput = boundRing;
        master.craftingTime = 100; // 5 seconds
        master.craftingProgress = 0;
        master.isCrafting = true;
        master.setChanged();
        master.syncToClient();
        
        return true;
    }
    
    /**
     * Find a recipe that matches the given input item
     */
    private CauldronRecipe findCraftingRecipe(ItemStack input) {
        if (level == null) return null;
        
        var recipes = level.getRecipeManager().getAllRecipesFor(ModRegistries.CAULDRON_RECIPE_TYPE.get());
        for (var recipeHolder : recipes) {
            CauldronRecipe recipe = recipeHolder.value();
            if (recipe.getInput().test(input)) {
                return recipe;
            }
        }
        return null;
    }
    
    /**
     * Extract crafting output (Shift + right-click)
     */
    public ItemStack extractCraftingOutput() {
        CauldronBlockEntity master = getMaster();
        if (master == null) return ItemStack.EMPTY;
        
        // Can only extract when crafting is complete
        if (master.isCrafting || master.craftingOutput.isEmpty()) {
            return ItemStack.EMPTY;
        }
        
        ItemStack output = master.craftingOutput.copy();
        master.craftingOutput = ItemStack.EMPTY;
        master.craftingInput = ItemStack.EMPTY;
        
        // Reset cauldron after extracting crafted item
        master.resetCauldron();
        
        return output;
    }
    
    /**
     * Get crafting progress percentage (0-100)
     */
    public int getCraftingProgress() {
        CauldronBlockEntity master = getMaster();
        if (master == null || master.craftingTime <= 0) return 0;
        return (master.craftingProgress * 100) / master.craftingTime;
    }
    
    public boolean isCrafting() {
        CauldronBlockEntity master = getMaster();
        return master != null && master.isCrafting;
    }
    
    public ItemStack getCraftingInput() {
        CauldronBlockEntity master = getMaster();
        return master != null ? master.craftingInput.copy() : ItemStack.EMPTY;
    }
    
    public ItemStack getCraftingOutput() {
        CauldronBlockEntity master = getMaster();
        return master != null ? master.craftingOutput.copy() : ItemStack.EMPTY;
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
        
        // Drop all stored materials
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
            tag.putInt("Phase", phase);
            tag.putBoolean("HasWater", hasWater);
            tag.putBoolean("HasHeatSource", hasHeatSource);
            tag.putInt("BrewingTicks", brewingTicks);
            tag.putInt("PotionColor", potionColor);
            tag.putInt("PotionDuration", potionDuration);
            tag.putInt("PotionLevel", potionLevel);
            tag.putString("PotionType", potionType);
            
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
            
            // Save crafting state
            tag.putBoolean("IsCrafting", isCrafting);
            tag.putInt("CraftingProgress", craftingProgress);
            tag.putInt("CraftingTime", craftingTime);
            if (!craftingInput.isEmpty()) {
                tag.put("CraftingInput", craftingInput.save(registries));
            }
            if (!craftingOutput.isEmpty()) {
                tag.put("CraftingOutput", craftingOutput.save(registries));
            }
        }
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
        if (isMaster()) {
            phase = tag.getInt("Phase");
            hasWater = tag.getBoolean("HasWater");
            hasHeatSource = tag.getBoolean("HasHeatSource");
            brewingTicks = tag.getInt("BrewingTicks");
            potionColor = tag.getInt("PotionColor");
            potionDuration = tag.getInt("PotionDuration");
            potionLevel = tag.getInt("PotionLevel");
            potionType = tag.getString("PotionType");
            
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
            
            // Load crafting state
            isCrafting = tag.getBoolean("IsCrafting");
            craftingProgress = tag.getInt("CraftingProgress");
            craftingTime = tag.getInt("CraftingTime");
            if (tag.contains("CraftingInput")) {
                craftingInput = ItemStack.parse(registries, tag.getCompound("CraftingInput")).orElse(ItemStack.EMPTY);
            }
            if (tag.contains("CraftingOutput")) {
                craftingOutput = ItemStack.parse(registries, tag.getCompound("CraftingOutput")).orElse(ItemStack.EMPTY);
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
}
