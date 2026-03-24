package com.cahcap.common.blockentity;

import com.cahcap.common.registry.ModRegistries;
import com.cahcap.common.util.BlockEntityHelper;
import com.cahcap.common.util.HerbRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HerbCabinetBlockEntity extends MultiblockPartBlockEntity {
    
    private static final int MAX_CAPACITY = 2048;
    
    private final Map<Item, Integer> herbStorage = new HashMap<>();
    
    private final BlockEntityHelper.DoubleClickTracker doubleClickTracker = new BlockEntityHelper.DoubleClickTracker();
    
    public HerbCabinetBlockEntity(BlockPos pos, BlockState state) {
        super(getBlockEntityType(), pos, state, new int[]{2, 1, 3});
        initializeStorage();
    }
    
    @SuppressWarnings("unchecked")
    private static BlockEntityType<HerbCabinetBlockEntity> getBlockEntityType() {
        return (BlockEntityType<HerbCabinetBlockEntity>) ModRegistries.HERB_CABINET_BE.get();
    }
    
    private void initializeStorage() {
        for (Item herb : HerbRegistry.getAllHerbItems()) {
            herbStorage.put(herb, 0);
        }
    }
    
    @Override
    public HerbCabinetBlockEntity getMaster() {
        return super.getMaster();
    }
    
    public int getHerbAmount(Item herb) {
        HerbCabinetBlockEntity master = getMaster();
        if (master == null) {
            return 0;
        }
        return master.herbStorage.getOrDefault(herb, 0);
    }
    
    public int getHerbAmount(String herbKey) {
        Item herb = getHerbFromKey(herbKey);
        return herb != null ? getHerbAmount(herb) : 0;
    }
    
    public int addHerb(Item herb, int amount) {
        HerbCabinetBlockEntity master = getMaster();
        if (master == null) {
            return 0;
        }
        
        if (!master.herbStorage.containsKey(herb)) {
            return 0;
        }
        
        int current = master.herbStorage.get(herb);
        int canAdd = Math.min(amount, MAX_CAPACITY - current);
        
        if (canAdd > 0) {
            master.herbStorage.put(herb, current + canAdd);
            master.setChanged();
            master.syncToClient();
        }
        
        return canAdd;
    }
    
    public int addHerb(String herbKey, int amount) {
        Item herb = getHerbFromKey(herbKey);
        return herb != null ? addHerb(herb, amount) : 0;
    }
    
    public int removeHerb(Item herb, int amount) {
        HerbCabinetBlockEntity master = getMaster();
        if (master == null) {
            return 0;
        }
        
        if (!master.herbStorage.containsKey(herb)) {
            return 0;
        }
        
        int current = master.herbStorage.get(herb);
        int canRemove = Math.min(amount, current);
        
        if (canRemove > 0) {
            master.herbStorage.put(herb, current - canRemove);
            master.setChanged();
            master.syncToClient();
        }
        
        return canRemove;
    }
    
    public int removeHerb(String herbKey, int amount) {
        Item herb = getHerbFromKey(herbKey);
        return herb != null ? removeHerb(herb, amount) : 0;
    }
    
    public boolean isDoubleClick(UUID playerUUID) {
        return doubleClickTracker.check(level, playerUUID);
    }
    
    @Override
    protected Block getMultiblockBlock() {
        return ModRegistries.HERB_CABINET.get();
    }

    @Override
    protected List<BlockPos> getStructurePositions(BlockPos masterPos) {
        HerbCabinetBlockEntity master = getMaster();
        Direction masterFacing = master != null ? master.getFacing() : Direction.NORTH;
        Direction right = masterFacing.getClockWise();
        BlockPos bottomLeft = masterPos.relative(right.getOpposite());

        List<BlockPos> positions = new java.util.ArrayList<>();
        for (int h = 0; h < 2; h++) {
            for (int w = 0; w < 3; w++) {
                positions.add(bottomLeft.relative(Direction.UP, h).relative(right, w));
            }
        }
        return positions;
    }

    @Override
    protected void dropStoredItems(BlockPos masterPos) {
        HerbCabinetBlockEntity master = getMaster();
        if (master == null) return;
        for (Item herb : getAllHerbItems()) {
            int amount = master.getHerbAmount(herb);
            while (amount > 0) {
                int stackSize = Math.min(amount, 64);
                ItemStack stack = new ItemStack(herb, stackSize);
                ItemEntity entityItem = new ItemEntity(level,
                    masterPos.getX() + 0.5, masterPos.getY() + 0.5, masterPos.getZ() + 0.5, stack);
                level.addFreshEntity(entityItem);
                amount -= stackSize;
            }
        }
    }

    @Override
    public ItemStack getOriginalBlock() {
        return new ItemStack(ModRegistries.RED_CHERRY_LOG.get());
    }

    public AABB computeRenderAABB() {
        if (renderAABB == null && isFormed()) {
            BlockPos masterPos = getMasterPos();
            if (masterPos != null && getFacing() != null) {
                Direction right = getFacing().getClockWise();
                BlockPos bottomLeft = masterPos.relative(right.getOpposite());
                BlockPos topRight = bottomLeft.relative(Direction.UP, 1).relative(right, 2);
                renderAABB = new AABB(
                        Math.min(bottomLeft.getX(), topRight.getX()),
                        Math.min(bottomLeft.getY(), topRight.getY()),
                        Math.min(bottomLeft.getZ(), topRight.getZ()),
                        Math.max(bottomLeft.getX(), topRight.getX()) + 1,
                        Math.max(bottomLeft.getY(), topRight.getY()) + 1,
                        Math.max(bottomLeft.getZ(), topRight.getZ()) + 1
                );
            }
        }
        return renderAABB;
    }

    public static Item[] getAllHerbItems() {
        return HerbRegistry.getAllHerbItems();
    }

    public static boolean isHerb(Item item) {
        return HerbRegistry.isHerb(item);
    }

    public static int getHerbIndex(Item herb) {
        return HerbRegistry.getHerbIndex(herb);
    }

    public static Item getHerbFromKey(String herbKey) {
        return HerbRegistry.getHerbByKey(herbKey);
    }
    
    public int getHerbIndexForBlock() {
        return getHerbIndexForBlock(getBlockPos());
    }
    
    public int getHerbIndexForBlock(BlockPos targetPos) {
        if (!isFormed()) {
            return -1;
        }

        int[] off = getOffset();
        BlockPos masterPos = getBlockPos().offset(-off[0], -off[1], -off[2]);

        int dy = targetPos.getY() - masterPos.getY();
        if (dy != 0 && dy != 1) {
            return -1;
        }

        int row = (dy == 0) ? 1 : 0;

        Direction right = getFacing().getClockWise();
        BlockPos bottomLeft = masterPos.relative(right.getOpposite());
        
        for (int w = 0; w < 3; w++) {
            BlockPos testPos = bottomLeft.relative(Direction.UP, dy).relative(right, w);
            if (testPos.equals(targetPos)) {
                int col = 2 - w;
                return row * 3 + col;
            }
        }
        
        return -1;
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        
        if (isMaster()) {
            for (Item herb : getAllHerbItems()) {
                String key = "Herb_" + herb.builtInRegistryHolder().key().location().toString();
                tag.putInt(key, herbStorage.getOrDefault(herb, 0));
            }
        }
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
        if (isMaster()) {
            for (Item herb : getAllHerbItems()) {
                String key = "Herb_" + herb.builtInRegistryHolder().key().location().toString();
                if (tag.contains(key)) {
                    herbStorage.put(herb, tag.getInt(key));
                }
            }
        }
    }
    
    /**
     * Interface for platform-specific item handler implementations
     * Platform modules should provide the actual IItemHandler wrapper
     */
    public interface ItemHandlerCallback {
        ItemStack getStackInSlot(int slot);
        ItemStack insertItem(int slot, ItemStack stack, boolean simulate);
        ItemStack extractItem(int slot, int amount, boolean simulate);
        int getSlotLimit(int slot);
        boolean isItemValid(int slot, ItemStack stack);
    }
    
    /**
     * Get item handler callback for this block entity
     * Used by mod loader capabilities system
     */
    public ItemHandlerCallback getItemHandlerCallback() {
        return new ItemHandlerCallback() {
            @Override
            public ItemStack getStackInSlot(int slot) {
                if (slot < 0 || slot >= 6) {
                    return ItemStack.EMPTY;
                }
                
                HerbCabinetBlockEntity master = getMaster();
                if (master == null) {
                    return ItemStack.EMPTY;
                }
                
                Item herb = getAllHerbItems()[slot];
                int amount = master.getHerbAmount(herb);
                
                if (amount <= 0) {
                    return ItemStack.EMPTY;
                }
                
                return new ItemStack(herb, amount);
            }
            
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                
                Item item = stack.getItem();
                if (!isHerb(item)) {
                    return stack;
                }
                
                HerbCabinetBlockEntity master = getMaster();
                if (master == null) {
                    return stack;
                }
                
                int toInsert = stack.getCount();
                int inserted;
                
                if (simulate) {
                    int current = master.getHerbAmount(item);
                    int space = MAX_CAPACITY - current;
                    inserted = Math.min(toInsert, space);
                } else {
                    inserted = master.addHerb(item, toInsert);
                }
                
                if (inserted >= toInsert) {
                    return ItemStack.EMPTY;
                }
                
                ItemStack remainder = stack.copy();
                remainder.setCount(toInsert - inserted);
                return remainder;
            }
            
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot < 0 || slot >= 6 || amount <= 0) {
                    return ItemStack.EMPTY;
                }
                
                HerbCabinetBlockEntity master = getMaster();
                if (master == null) {
                    return ItemStack.EMPTY;
                }
                
                Item herb = getAllHerbItems()[slot];
                int stored = master.getHerbAmount(herb);
                
                if (stored <= 0) {
                    return ItemStack.EMPTY;
                }
                
                int toExtract = Math.min(amount, stored);
                
                if (!simulate) {
                    master.removeHerb(herb, toExtract);
                }
                
                return new ItemStack(herb, toExtract);
            }
            
            @Override
            public int getSlotLimit(int slot) {
                return 64;
            }
            
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return isHerb(stack.getItem());
            }
        };
    }
}

