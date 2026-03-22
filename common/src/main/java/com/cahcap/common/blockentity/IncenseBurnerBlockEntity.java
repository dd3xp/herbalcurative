package com.cahcap.common.blockentity;

import com.cahcap.common.block.IncenseBurnerBlock;
import com.cahcap.common.item.IncensePowderItem;
import com.cahcap.common.recipe.IncenseBurningRecipe;
import com.cahcap.common.registry.ModRegistries;
import com.cahcap.common.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Block entity for Incense Burner.
 * 
 * 7 abstract slots:
 * - 1 powder slot (holds 1 incense powder item)
 * - 6 herb slots (keyed by herb type, holds up to 64 each)
 * 
 * Burning logic:
 * - When powder is present, herbs match a recipe, and heat source below
 * - Consume herbs when burning starts
 * - Spawn mob when burning completes (8 seconds)
 * - Powder is NOT consumed
 */
public class IncenseBurnerBlockEntity extends BlockEntity {
    
    public static final int MAX_HERB_PER_TYPE = 64;
    public static final int MAX_HERB_TYPES = 6;
    public static final int DEFAULT_BURN_TIME = 160; // 8 seconds
    
    private ItemStack powderSlot = ItemStack.EMPTY;
    private final Map<Item, Integer> herbSlots = new LinkedHashMap<>();

    private Set<Item> cachedAcceptedHerbs = null;
    
    private boolean isBurning = false;
    private int burnTicks = 0;
    private int totalBurnTicks = 0;
    @Nullable
    private ResourceLocation pendingEntityType = null;
    @Nullable
    private ResourceLocation currentRecipeId = null;
    
    public IncenseBurnerBlockEntity(BlockPos pos, BlockState state) {
        super(getBlockEntityType(), pos, state);
    }
    
    @SuppressWarnings("unchecked")
    private static BlockEntityType<IncenseBurnerBlockEntity> getBlockEntityType() {
        return (BlockEntityType<IncenseBurnerBlockEntity>) ModRegistries.INCENSE_BURNER_BE.get();
    }
    
    public ItemStack getPowder() {
        return powderSlot;
    }
    
    public Map<Item, Integer> getHerbs() {
        return Collections.unmodifiableMap(herbSlots);
    }
    
    public boolean isBurning() {
        return isBurning;
    }
    
    public float getBurnProgress() {
        if (!isBurning || totalBurnTicks <= 0) {
            return 0f;
        }
        return (float) burnTicks / totalBurnTicks;
    }
    
    /**
     * Returns the set of herb items accepted by recipes matching the current powder.
     * Empty set if no powder is present.
     */
    public Set<Item> getAcceptedHerbs() {
        if (cachedAcceptedHerbs != null) return cachedAcceptedHerbs;
        if (level == null || powderSlot.isEmpty()) {
            cachedAcceptedHerbs = Collections.emptySet();
            return cachedAcceptedHerbs;
        }
        if (!(powderSlot.getItem() instanceof IncensePowderItem powder)) {
            cachedAcceptedHerbs = Collections.emptySet();
            return cachedAcceptedHerbs;
        }
        ResourceLocation entityTypeId = powder.getEntityTypeId();
        Set<Item> accepted = new HashSet<>();
        for (RecipeHolder<IncenseBurningRecipe> holder : level.getRecipeManager()
                .getAllRecipesFor(ModRegistries.INCENSE_BURNING_RECIPE_TYPE.get())) {
            IncenseBurningRecipe recipe = holder.value();
            if (!recipe.getEntityType().equals(entityTypeId)) continue;
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

    public boolean hasPowder() {
        return !powderSlot.isEmpty();
    }
    
    public boolean canAddPowder(ItemStack stack) {
        return powderSlot.isEmpty() && isValidPowder(stack);
    }
    
    public static boolean isValidPowder(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof IncensePowderItem;
    }
    
    public boolean addPowder(ItemStack stack, boolean isCreative) {
        if (!canAddPowder(stack)) return false;

        powderSlot = stack.copyWithCount(1);
        if (!isCreative) {
            stack.shrink(1);
        }
        invalidateHerbCache();
        setChanged();
        syncToClient();
        ejectInvalidHerbs();
        checkAndStartBurning();
        return true;
    }
    
    public int addHerb(ItemStack stack, boolean isCreative) {
        if (!HerbCabinetBlockEntity.isHerb(stack.getItem())) return 0;
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
        checkAndStartBurning();
        return canAdd;
    }
    
    public ItemStack removePowder() {
        if (!powderSlot.isEmpty()) {
            ItemStack removed = powderSlot.copy();
            powderSlot = ItemStack.EMPTY;

            if (isBurning) {
                stopBurning();
            }

            invalidateHerbCache();
            ejectInvalidHerbs();
            setChanged();
            syncToClient();
            return removed;
        }

        return ItemStack.EMPTY;
    }
    
    private void stopBurning() {
        isBurning = false;
        burnTicks = 0;
        totalBurnTicks = 0;
        pendingEntityType = null;
        currentRecipeId = null;
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
        if (!powderSlot.isEmpty()) {
            items.add(powderSlot.copy());
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
    
    private boolean checkHeatSource() {
        if (level == null) return false;
        
        BlockPos belowPos = worldPosition.below();
        BlockState state = level.getBlockState(belowPos);
        
        // Check heat sources tag (can be extended via datapacks)
        if (state.is(ModTags.Blocks.HEAT_SOURCES)) {
            return true;
        }
        
        // Campfire (lit) - special case due to block state requirement
        if (state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)) {
            return true;
        }
        
        return false;
    }
    
    private void checkAndStartBurning() {
        if (level == null || level.isClientSide || isBurning) return;
        if (!hasPowder()) return;
        if (!checkHeatSource()) return;
        
        RecipeManager recipeManager = level.getRecipeManager();
        IncenseBurningRecipe.BurnerInput input = new IncenseBurningRecipe.BurnerInput(powderSlot, herbSlots);
        
        Optional<RecipeHolder<IncenseBurningRecipe>> recipeOpt = recipeManager.getRecipeFor(
                ModRegistries.INCENSE_BURNING_RECIPE_TYPE.get(), input, level);
        
        if (recipeOpt.isPresent()) {
            IncenseBurningRecipe recipe = recipeOpt.get().value();
            
            // Check if we have enough herbs
            for (var req : recipe.getHerbRequirements()) {
                Item herb = req.getKey();
                int required = req.getValue();
                int have = herbSlots.getOrDefault(herb, 0);
                if (have < required) {
                    return;
                }
            }
            
            // Consume herbs
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
            
            isBurning = true;
            burnTicks = 0;
            totalBurnTicks = recipe.getBurnTime();
            pendingEntityType = recipe.getEntityType();
            currentRecipeId = recipeOpt.get().id();
            
            setChanged();
            syncToClient();
        }
    }
    
    public static void serverTick(Level level, BlockPos pos, BlockState state, IncenseBurnerBlockEntity blockEntity) {
        if (!blockEntity.isBurning) {
            blockEntity.checkAndStartBurning();
            return;
        }
        
        // Check if heat source is still present
        if (!blockEntity.checkHeatSource()) {
            // Heat source removed, stop burning but don't refund herbs
            blockEntity.isBurning = false;
            blockEntity.burnTicks = 0;
            blockEntity.totalBurnTicks = 0;
            blockEntity.pendingEntityType = null;
            blockEntity.currentRecipeId = null;
            blockEntity.setChanged();
            blockEntity.syncToClient();
            return;
        }
        
        blockEntity.burnTicks++;
        
        // Spawn small flame particles (similar to Pyrisage)
        if (level instanceof ServerLevel serverLevel && blockEntity.burnTicks % 10 == 0) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.25;
            double z = pos.getZ() + 0.5;
            // Small flame with minimal spread and no velocity
            serverLevel.sendParticles(ParticleTypes.SMALL_FLAME, x, y, z, 1, 0.05, 0.02, 0.05, 0);
            // Small smoke rising slowly
            serverLevel.sendParticles(ParticleTypes.SMOKE, x, y + 0.1, z, 1, 0.03, 0.05, 0.03, 0.002);
        }
        
        if (blockEntity.burnTicks >= blockEntity.totalBurnTicks) {
            // Spawn the mob
            if (blockEntity.pendingEntityType != null && level instanceof ServerLevel serverLevel) {
                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(blockEntity.pendingEntityType);
                if (entityType != null) {
                    // Get spawn position: in front of the burner
                    Direction facing = state.getValue(IncenseBurnerBlock.FACING);
                    BlockPos spawnPos = pos.relative(facing);
                    
                    Entity entity = entityType.spawn(
                            serverLevel,
                            spawnPos,
                            MobSpawnType.MOB_SUMMONED
                    );
                }
            }
            
            // Reset burning state
            blockEntity.isBurning = false;
            blockEntity.burnTicks = 0;
            blockEntity.totalBurnTicks = 0;
            blockEntity.pendingEntityType = null;
            blockEntity.currentRecipeId = null;
            
            blockEntity.setChanged();
            blockEntity.syncToClient();
            
            // Check if can start another burn cycle
            blockEntity.checkAndStartBurning();
        } else if (blockEntity.burnTicks % 20 == 0) {
            blockEntity.syncToClient();
        }
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        
        if (!powderSlot.isEmpty()) {
            tag.put("Powder", powderSlot.save(registries));
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
        
        tag.putBoolean("IsBurning", isBurning);
        tag.putInt("BurnTicks", burnTicks);
        tag.putInt("TotalBurnTicks", totalBurnTicks);
        
        if (pendingEntityType != null) {
            tag.putString("PendingEntity", pendingEntityType.toString());
        }
        if (currentRecipeId != null) {
            tag.putString("RecipeId", currentRecipeId.toString());
        }
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
        powderSlot = tag.contains("Powder") ? ItemStack.parse(registries, tag.getCompound("Powder")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
        
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
        
        isBurning = tag.getBoolean("IsBurning");
        burnTicks = tag.getInt("BurnTicks");
        totalBurnTicks = tag.getInt("TotalBurnTicks");
        
        pendingEntityType = tag.contains("PendingEntity") ? 
                ResourceLocation.parse(tag.getString("PendingEntity")) : null;
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
        if (level != null && !level.isClientSide) {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, 3);
            setChanged();
        }
    }
    
    /**
     * Called when player shift+right-clicks with Flowweave Ring.
     * Removes powder.
     * @return the removed item, or EMPTY if nothing to remove
     */
    public ItemStack onFlowweaveRingShiftUse(Player player) {
        return removePowder();
    }
    
    /**
     * Get the color for the powder layer rendering.
     * Returns the color from the IncensePowderItem, or white if not applicable.
     */
    public int getPowderColor() {
        if (powderSlot.isEmpty()) {
            return 0xFFFFFF;
        }
        if (powderSlot.getItem() instanceof IncensePowderItem powder) {
            return powder.getColor(powderSlot);
        }
        return 0xFFFFFF;
    }
}
