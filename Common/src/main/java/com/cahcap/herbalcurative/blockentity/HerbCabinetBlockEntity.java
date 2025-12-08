package com.cahcap.herbalcurative.blockentity;

import com.cahcap.herbalcurative.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HerbCabinetBlockEntity extends MultiblockPartBlockEntity {
    
    private static final int MAX_CAPACITY = 4096;
    
    private final Map<Item, Integer> herbStorage = new HashMap<>();
    
    private long lastClickTime;
    private UUID lastClickUUID;
    
    public boolean suppressDrops = false;
    
    public HerbCabinetBlockEntity(BlockPos pos, BlockState state) {
        super(getBlockEntityType(), pos, state, new int[]{2, 1, 3});
        initializeStorage();
    }
    
    @SuppressWarnings("unchecked")
    private static BlockEntityType<HerbCabinetBlockEntity> getBlockEntityType() {
        return (BlockEntityType<HerbCabinetBlockEntity>) ModRegistries.HERB_CABINET_BE.get();
    }
    
    private void initializeStorage() {
        herbStorage.put(ModRegistries.SCALEPLATE.get(), 0);
        herbStorage.put(ModRegistries.DEWPETAL_SHARD.get(), 0);
        herbStorage.put(ModRegistries.GOLDEN_LILYBELL.get(), 0);
        herbStorage.put(ModRegistries.CRYST_SPINE.get(), 0);
        herbStorage.put(ModRegistries.BURNT_NODE.get(), 0);
        herbStorage.put(ModRegistries.HEART_OF_STARDREAM.get(), 0);
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
        if (level == null) {
            return false;
        }
        
        long currentTime = level.getGameTime();
        boolean isDouble = (currentTime - lastClickTime < 10 && playerUUID.equals(lastClickUUID));
        
        lastClickTime = currentTime;
        lastClickUUID = playerUUID;
        
        return isDouble;
    }
    
    @Override
    public void disassemble() {
        if (level == null || level.isClientSide || !formed) {
            return;
        }
        
        HerbCabinetBlockEntity master = getMaster();
        if (master == null) {
            return;
        }
        
        BlockPos masterPos = master.getBlockPos();
        Direction masterFacing = master.facing;
        Direction right = masterFacing.getClockWise();
        BlockPos bottomLeft = masterPos.relative(right.getOpposite());
        
        BlockPos breakPos = getBlockPos();
        
        // Drop all stored herbs
        for (Item herb : getAllHerbItems()) {
            int amount = master.getHerbAmount(herb);
            
            while (amount > 0) {
                int stackSize = Math.min(amount, 64);
                ItemStack stack = new ItemStack(herb, stackSize);
                ItemEntity entityItem = new ItemEntity(
                        level, 
                        masterPos.getX() + 0.5, 
                        masterPos.getY() + 0.5, 
                        masterPos.getZ() + 0.5, 
                        stack
                );
                level.addFreshEntity(entityItem);
                amount -= stackSize;
            }
        }
        
        // First pass: Mark all blocks as not formed
        for (int h = 0; h < 2; h++) {
            for (int w = 0; w < 3; w++) {
                BlockPos targetPos = bottomLeft.relative(Direction.UP, h).relative(right, w);
                
                if (level.getBlockState(targetPos).is(ModRegistries.HERB_CABINET.get())) {
                    if (level.getBlockEntity(targetPos) instanceof HerbCabinetBlockEntity cabinet) {
                        cabinet.formed = false;
                        
                        if (!targetPos.equals(breakPos)) {
                            cabinet.suppressDrops = true;
                        }
                        
                        cabinet.setChanged();
                        BlockState state = level.getBlockState(targetPos);
                        level.sendBlockUpdated(targetPos, state, state, 3);
                    }
                }
            }
        }
        
        // Second pass: Replace non-broken blocks with original block
        for (int h = 0; h < 2; h++) {
            for (int w = 0; w < 3; w++) {
                BlockPos targetPos = bottomLeft.relative(Direction.UP, h).relative(right, w);
                
                if (level.getBlockState(targetPos).is(ModRegistries.HERB_CABINET.get())) {
                    if (!targetPos.equals(breakPos)) {
                        level.setBlock(targetPos, ModRegistries.RED_CHERRY_LOG.get().defaultBlockState(), 2);
                    }
                }
            }
        }
        
        setChanged();
    }
    
    @Override
    public ItemStack getOriginalBlock() {
        return new ItemStack(ModRegistries.RED_CHERRY_LOG.get());
    }
    
    public static Item[] getAllHerbItems() {
        return new Item[] {
                ModRegistries.SCALEPLATE.get(),
                ModRegistries.DEWPETAL_SHARD.get(),
                ModRegistries.GOLDEN_LILYBELL.get(),
                ModRegistries.CRYST_SPINE.get(),
                ModRegistries.BURNT_NODE.get(),
                ModRegistries.HEART_OF_STARDREAM.get()
        };
    }
    
    public static boolean isHerb(Item item) {
        for (Item herb : getAllHerbItems()) {
            if (herb == item) {
                return true;
            }
        }
        return false;
    }
    
    public static int getHerbIndex(Item herb) {
        Item[] herbs = getAllHerbItems();
        for (int i = 0; i < herbs.length; i++) {
            if (herbs[i] == herb) {
                return i;
            }
        }
        return -1;
    }
    
    public static Item getHerbFromKey(String herbKey) {
        return switch (herbKey) {
            case "scaleplate" -> ModRegistries.SCALEPLATE.get();
            case "dewpetal_shard" -> ModRegistries.DEWPETAL_SHARD.get();
            case "golden_lilybell" -> ModRegistries.GOLDEN_LILYBELL.get();
            case "cryst_spine" -> ModRegistries.CRYST_SPINE.get();
            case "burnt_node" -> ModRegistries.BURNT_NODE.get();
            case "heart_of_stardream" -> ModRegistries.HEART_OF_STARDREAM.get();
            default -> null;
        };
    }
    
    public int getHerbIndexForBlock() {
        return getHerbIndexForBlock(getBlockPos());
    }
    
    public int getHerbIndexForBlock(BlockPos targetPos) {
        if (!formed) {
            return -1;
        }
        
        BlockPos masterPos = getBlockPos().offset(-offset[0], -offset[1], -offset[2]);
        
        int dy = targetPos.getY() - masterPos.getY();
        if (dy != 0 && dy != 1) {
            return -1;
        }
        
        int row = (dy == 0) ? 1 : 0;
        
        Direction right = facing.getClockWise();
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
     * Used by NeoForge capabilities system
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

