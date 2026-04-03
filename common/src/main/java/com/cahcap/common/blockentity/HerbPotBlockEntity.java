package com.cahcap.common.blockentity;

import com.cahcap.common.recipe.HerbPotGrowingRecipe;
import com.cahcap.common.util.BlockEntityHelper;
import com.cahcap.common.util.HerbRegistry;
import com.cahcap.common.registry.ModRegistries;
import com.cahcap.common.util.HerbRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Block entity for Herb Pot.
 * 
 * 8 abstract slots:
 * - 1 soil slot (holds 1 item)
 * - 1 seedling slot (holds 1 item, requires soil)
 * - 6 herb slots (keyed by herb type, holds up to 64 each)
 * 
 * Growth logic:
 * - When seedling is present and herbs match a recipe, start growing
 * - Consume herbs when growth starts
 * - Drop output when growth completes
 * - Seedling is NOT consumed
 */
public class HerbPotBlockEntity extends BlockEntity {

    /**
     * States of the herb pot growth process.
     * IDLE: Not growing
     * GROWING: Actively growing (herbs consumed, waiting for completion)
     * COMPLETE: Growth finished (output pending)
     */
    public enum GrowthState {
        IDLE, GROWING, COMPLETE
    }

    public static final int MAX_HERB_PER_TYPE = 64;
    public static final int MAX_HERB_TYPES = 6;

    private ItemStack soilSlot = ItemStack.EMPTY;
    private ItemStack seedlingSlot = ItemStack.EMPTY;
    private final Map<Item, Integer> herbSlots = new LinkedHashMap<>();

    private Set<Item> cachedAcceptedHerbs = null;

    private GrowthState growthState = GrowthState.IDLE;
    private int growthTicks = 0;
    private int totalGrowthTicks = 0;
    @Nullable
    private ItemStack pendingOutput = null;
    @Nullable
    private ResourceLocation currentRecipeId = null;
    
    public HerbPotBlockEntity(BlockPos pos, BlockState state) {
        super(getBlockEntityType(), pos, state);
    }
    
    @SuppressWarnings("unchecked")
    private static BlockEntityType<HerbPotBlockEntity> getBlockEntityType() {
        return (BlockEntityType<HerbPotBlockEntity>) ModRegistries.HERB_POT_BE.get();
    }
    
    public ItemStack getSoil() {
        return soilSlot;
    }
    
    public ItemStack getSeedling() {
        return seedlingSlot;
    }
    
    public Map<Item, Integer> getHerbs() {
        return Collections.unmodifiableMap(herbSlots);
    }
    
    public boolean isGrowing() {
        return growthState == GrowthState.GROWING;
    }

    @Nullable
    public ItemStack getPendingOutput() {
        return pendingOutput;
    }
    
    /**
     * Get recipe output preview for tooltip display when not growing.
     */
    // Cached recipe preview (invalidated when seedling changes)
    private ItemStack cachedPreviewSeedling = ItemStack.EMPTY;
    private ItemStack cachedPreviewResult = ItemStack.EMPTY;

    public ItemStack getRecipePreview() {
        if (pendingOutput != null && !pendingOutput.isEmpty()) return pendingOutput.copy();
        if (level == null || seedlingSlot.isEmpty()) return ItemStack.EMPTY;
        if (!ItemStack.isSameItemSameComponents(seedlingSlot, cachedPreviewSeedling)) {
            cachedPreviewSeedling = seedlingSlot.copy();
            cachedPreviewResult = ItemStack.EMPTY;
            for (RecipeHolder<HerbPotGrowingRecipe> holder : level.getRecipeManager()
                    .getAllRecipesFor(ModRegistries.HERB_POT_GROWING_RECIPE_TYPE.get())) {
                if (holder.value().getSeedling().test(seedlingSlot)) {
                    cachedPreviewResult = holder.value().getResultItem(level.registryAccess()).copy();
                    break;
                }
            }
        }
        return cachedPreviewResult;
    }

    public float getGrowthProgress() {
        if (growthState != GrowthState.GROWING || totalGrowthTicks <= 0) {
            return 0f;
        }
        return (float) growthTicks / totalGrowthTicks;
    }
    
    /**
     * Returns the set of herb items accepted by recipes matching the current seedling/soil.
     * Empty set if no seedling is present.
     */
    public Set<Item> getAcceptedHerbs() {
        if (cachedAcceptedHerbs != null) return cachedAcceptedHerbs;
        if (level == null || seedlingSlot.isEmpty()) {
            cachedAcceptedHerbs = Collections.emptySet();
            return cachedAcceptedHerbs;
        }
        Set<Item> accepted = new HashSet<>();
        for (RecipeHolder<HerbPotGrowingRecipe> holder : level.getRecipeManager()
                .getAllRecipesFor(ModRegistries.HERB_POT_GROWING_RECIPE_TYPE.get())) {
            HerbPotGrowingRecipe recipe = holder.value();
            if (!recipe.getSeedling().test(seedlingSlot)) continue;
            if (!recipe.getSoil().isEmpty() && !soilSlot.isEmpty() && !recipe.getSoil().test(soilSlot)) continue;
            for (var req : recipe.getHerbRequirements()) {
                accepted.add(req.getKey());
            }
        }
        cachedAcceptedHerbs = accepted;
        return accepted;
    }

    private void invalidateHerbCache() {
        cachedAcceptedHerbs = null;
    }

    private void ejectInvalidHerbs() {
        if (level == null || level.isClientSide) return;
        Set<Item> accepted = getAcceptedHerbs();
        Iterator<Map.Entry<Item, Integer>> it = herbSlots.entrySet().iterator();
        boolean changed = false;
        while (it.hasNext()) {
            Map.Entry<Item, Integer> entry = it.next();
            if (!accepted.contains(entry.getKey())) {
                ItemStack ejected = new ItemStack(entry.getKey(), entry.getValue());
                ItemEntity itemEntity = new ItemEntity(level,
                        worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5,
                        ejected);
                level.addFreshEntity(itemEntity);
                it.remove();
                changed = true;
            }
        }
        if (changed) {
            setChanged();
            syncToClient();
        }
    }

    public boolean hasSoil() {
        return !soilSlot.isEmpty();
    }
    
    public boolean hasSeedling() {
        return !seedlingSlot.isEmpty();
    }
    
    public boolean canAddSoil(ItemStack stack) {
        return soilSlot.isEmpty() && isValidSoil(stack);
    }
    
    public boolean canAddSeedling(ItemStack stack) {
        return hasSoil() && seedlingSlot.isEmpty() && isValidSeedling(stack);
    }
    
    public static boolean isValidSoil(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        String path = id.getPath();
        return path.contains("dirt") || path.contains("soil") || path.contains("grass_block") ||
               path.contains("podzol") || path.contains("mycelium") || path.contains("farmland") ||
               path.contains("mud") || path.contains("rooted_dirt") ||
               id.toString().contains("herbalcurative:herb_planting_soil");
    }
    
    public static boolean isValidSeedling(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        String path = id.getPath();
        String namespace = id.getNamespace();
        
        if (path.contains("seed") || path.contains("sapling") || path.contains("seedling")) {
            return true;
        }
        
        if ("herbalcurative".equals(namespace)) {
            return path.equals("scleris") || path.equals("dorella") ||
                   path.equals("sephrel") || path.equals("crysel") ||
                   path.equals("pyraze") || path.equals("stellia") ||
                   path.contains("crystal_plant") || path.contains("cryst_plant") || 
                   path.contains("cryst_spine");
        }
        
        return false;
    }
    
    public boolean addSoil(ItemStack stack, boolean isCreative) {
        if (!canAddSoil(stack)) return false;
        
        soilSlot = stack.copyWithCount(1);
        if (!isCreative) {
            stack.shrink(1);
        }
        setChanged();
        syncToClient();
        return true;
    }
    
    public boolean addSeedling(ItemStack stack, boolean isCreative) {
        if (!canAddSeedling(stack)) return false;

        seedlingSlot = stack.copyWithCount(1);
        if (!isCreative) {
            stack.shrink(1);
        }
        invalidateHerbCache();
        setChanged();
        syncToClient();
        ejectInvalidHerbs();
        checkAndStartGrowth();
        return true;
    }
    
    public int addHerb(ItemStack stack, boolean isCreative) {
        if (!HerbRegistry.isHerb(stack.getItem())) return 0;
        if (!getAcceptedHerbs().contains(stack.getItem())) return 0;

        Item herbType = stack.getItem();
        int current = herbSlots.getOrDefault(herbType, 0);
        
        if (current >= MAX_HERB_PER_TYPE) return 0;
        if (herbSlots.size() >= MAX_HERB_TYPES && !herbSlots.containsKey(herbType)) return 0;
        
        int canAdd = Math.min(stack.getCount(), MAX_HERB_PER_TYPE - current);
        herbSlots.put(herbType, current + canAdd);
        
        if (!isCreative) {
            stack.shrink(canAdd);
        }
        
        setChanged();
        syncToClient();
        checkAndStartGrowth();
        return canAdd;
    }
    
    public ItemStack removeSeedlingOrSoil() {
        if (!seedlingSlot.isEmpty()) {
            ItemStack removed = seedlingSlot.copy();
            seedlingSlot = ItemStack.EMPTY;
            stopGrowth();
            invalidateHerbCache();
            ejectInvalidHerbs();
            setChanged();
            syncToClient();
            return removed;
        }

        if (!soilSlot.isEmpty()) {
            ItemStack removed = soilSlot.copy();
            soilSlot = ItemStack.EMPTY;
            stopGrowth();
            invalidateHerbCache();
            ejectInvalidHerbs();
            setChanged();
            syncToClient();
            return removed;
        }

        return ItemStack.EMPTY;
    }
    
    private void stopGrowth() {
        if (growthState != GrowthState.IDLE) {
            growthState = GrowthState.IDLE;
            growthTicks = 0;
            totalGrowthTicks = 0;
            pendingOutput = null;
            currentRecipeId = null;
        }
    }
    
    public ItemStack removeHerb() {
        if (herbSlots.isEmpty()) return ItemStack.EMPTY;
        
        Iterator<Map.Entry<Item, Integer>> it = herbSlots.entrySet().iterator();
        if (it.hasNext()) {
            Map.Entry<Item, Integer> entry = it.next();
            Item herbType = entry.getKey();
            int count = entry.getValue();
            int toRemove = Math.min(count, 64);
            
            if (count <= toRemove) {
                it.remove();
            } else {
                herbSlots.put(herbType, count - toRemove);
            }
            
            setChanged();
            syncToClient();
            return new ItemStack(herbType, toRemove);
        }
        
        return ItemStack.EMPTY;
    }
    
    public List<ItemStack> getAllItems() {
        List<ItemStack> items = new ArrayList<>();
        if (!soilSlot.isEmpty()) {
            items.add(soilSlot.copy());
        }
        if (!seedlingSlot.isEmpty()) {
            items.add(seedlingSlot.copy());
        }
        for (Map.Entry<Item, Integer> entry : herbSlots.entrySet()) {
            int count = entry.getValue();
            while (count > 0) {
                int stackSize = Math.min(count, 64);
                items.add(new ItemStack(entry.getKey(), stackSize));
                count -= stackSize;
            }
        }
        return items;
    }
    
    private void checkAndStartGrowth() {
        if (level == null || level.isClientSide || growthState != GrowthState.IDLE) return;
        if (!hasSoil() || !hasSeedling()) return;
        
        RecipeManager recipeManager = level.getRecipeManager();
        HerbPotGrowingRecipe.PotInput input = new HerbPotGrowingRecipe.PotInput(seedlingSlot, soilSlot, herbSlots);
        
        Optional<RecipeHolder<HerbPotGrowingRecipe>> recipeOpt = recipeManager.getRecipeFor(
                ModRegistries.HERB_POT_GROWING_RECIPE_TYPE.get(), input, level);
        
        if (recipeOpt.isPresent()) {
            HerbPotGrowingRecipe recipe = recipeOpt.get().value();
            
            for (var req : recipe.getHerbRequirements()) {
                Item herb = req.getKey();
                int required = req.getValue();
                int have = herbSlots.getOrDefault(herb, 0);
                if (have < required) {
                    return;
                }
            }
            
            for (var req : recipe.getHerbRequirements()) {
                Item herb = req.getKey();
                int required = req.getValue();
                int remaining = herbSlots.get(herb) - required;
                if (remaining <= 0) {
                    herbSlots.remove(herb);
                } else {
                    herbSlots.put(herb, remaining);
                }
            }
            
            growthState = GrowthState.GROWING;
            growthTicks = 0;
            totalGrowthTicks = recipe.getGrowthTime();
            pendingOutput = recipe.getOutput().copy();
            currentRecipeId = recipeOpt.get().id();
            
            setChanged();
            syncToClient();
        }
    }
    
    public static void serverTick(Level level, BlockPos pos, BlockState state, HerbPotBlockEntity blockEntity) {
        if (blockEntity.growthState != GrowthState.GROWING) {
            blockEntity.checkAndStartGrowth();
            return;
        }

        blockEntity.growthTicks++;

        if (blockEntity.growthTicks >= blockEntity.totalGrowthTicks) {
            if (blockEntity.pendingOutput != null && !blockEntity.pendingOutput.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(level,
                        pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                        blockEntity.pendingOutput.copy());
                itemEntity.setDeltaMovement(0, 0.1, 0);
                level.addFreshEntity(itemEntity);
            }

            blockEntity.growthState = GrowthState.IDLE;
            blockEntity.growthTicks = 0;
            blockEntity.totalGrowthTicks = 0;
            blockEntity.pendingOutput = null;
            blockEntity.currentRecipeId = null;
            
            blockEntity.setChanged();
            blockEntity.syncToClient();
            
            blockEntity.checkAndStartGrowth();
        } else if (blockEntity.growthTicks % 20 == 0) {
            blockEntity.syncToClient();
        }
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        
        if (!soilSlot.isEmpty()) {
            tag.put("Soil", soilSlot.save(registries));
        }
        if (!seedlingSlot.isEmpty()) {
            tag.put("Seedling", seedlingSlot.save(registries));
        }
        
        if (!herbSlots.isEmpty()) {
            ListTag herbList = new ListTag();
            for (Map.Entry<Item, Integer> entry : herbSlots.entrySet()) {
                CompoundTag herbTag = new CompoundTag();
                herbTag.putString("Item", BuiltInRegistries.ITEM.getKey(entry.getKey()).toString());
                herbTag.putInt("Count", entry.getValue());
                herbList.add(herbTag);
            }
            tag.put("Herbs", herbList);
        }
        
        tag.putInt("GrowthState", growthState.ordinal());
        tag.putInt("GrowthTicks", growthTicks);
        tag.putInt("TotalGrowthTicks", totalGrowthTicks);
        
        if (pendingOutput != null && !pendingOutput.isEmpty()) {
            tag.put("PendingOutput", pendingOutput.save(registries));
        }
        if (currentRecipeId != null) {
            tag.putString("RecipeId", currentRecipeId.toString());
        }
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
        soilSlot = tag.contains("Soil") ? ItemStack.parse(registries, tag.getCompound("Soil")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
        seedlingSlot = tag.contains("Seedling") ? ItemStack.parse(registries, tag.getCompound("Seedling")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
        
        herbSlots.clear();
        if (tag.contains("Herbs")) {
            ListTag herbList = tag.getList("Herbs", Tag.TAG_COMPOUND);
            for (int i = 0; i < herbList.size(); i++) {
                CompoundTag herbTag = herbList.getCompound(i);
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(herbTag.getString("Item")));
                if (item != Items.AIR) {
                    herbSlots.put(item, herbTag.getInt("Count"));
                }
            }
        }
        
        if (tag.contains("GrowthState")) {
            int ordinal = tag.getInt("GrowthState");
            GrowthState[] states = GrowthState.values();
            growthState = (ordinal >= 0 && ordinal < states.length) ? states[ordinal] : GrowthState.IDLE;
        } else {
            // Backward compatibility: convert old boolean key to enum
            growthState = tag.getBoolean("IsGrowing") ? GrowthState.GROWING : GrowthState.IDLE;
        }
        growthTicks = tag.getInt("GrowthTicks");
        totalGrowthTicks = tag.getInt("TotalGrowthTicks");
        
        pendingOutput = tag.contains("PendingOutput") ? 
                ItemStack.parse(registries, tag.getCompound("PendingOutput")).orElse(null) : null;
        currentRecipeId = tag.contains("RecipeId") ? 
                ResourceLocation.parse(tag.getString("RecipeId")) : null;
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }
    
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    public void syncToClient() {
        BlockEntityHelper.syncToClient(this);
    }
    
    /**
     * Called when player shift+right-clicks with Flowweave Ring.
     * Removes seedling first, then soil.
     * @return the removed item, or EMPTY if nothing to remove
     */
    public ItemStack onFlowweaveRingShiftUse(Player player) {
        return removeSeedlingOrSoil();
    }
}
