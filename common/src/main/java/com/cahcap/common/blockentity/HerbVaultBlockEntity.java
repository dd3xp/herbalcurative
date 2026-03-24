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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Block entity for Herb Vault multiblock.
 * Enhanced version of HerbCabinetBlockEntity with 8192 capacity and 3x3x3 structure.
 */
public class HerbVaultBlockEntity extends MultiblockPartBlockEntity {

    private static final int MAX_CAPACITY = 8192;

    private final Map<Item, Integer> herbStorage = new HashMap<>();

    private final BlockEntityHelper.DoubleClickTracker doubleClickTracker = new BlockEntityHelper.DoubleClickTracker();

    public HerbVaultBlockEntity(BlockPos pos, BlockState state) {
        super(getBlockEntityType(), pos, state, new int[]{3, 3, 3});
        initializeStorage();
    }

    @SuppressWarnings("unchecked")
    private static BlockEntityType<HerbVaultBlockEntity> getBlockEntityType() {
        return (BlockEntityType<HerbVaultBlockEntity>) ModRegistries.HERB_VAULT_BE.get();
    }

    private void initializeStorage() {
        for (Item herb : HerbRegistry.getAllHerbItems()) {
            herbStorage.put(herb, 0);
        }
    }

    @Override
    public HerbVaultBlockEntity getMaster() {
        return super.getMaster();
    }

    public int getHerbAmount(Item herb) {
        HerbVaultBlockEntity master = getMaster();
        if (master == null) return 0;
        return master.herbStorage.getOrDefault(herb, 0);
    }

    public int getHerbAmount(String herbKey) {
        Item herb = HerbRegistry.getHerbByKey(herbKey);
        return herb != null ? getHerbAmount(herb) : 0;
    }

    public int addHerb(String herbKey, int amount) {
        Item herb = HerbRegistry.getHerbByKey(herbKey);
        return herb != null ? addHerb(herb, amount) : 0;
    }

    public int addHerb(Item herb, int amount) {
        HerbVaultBlockEntity master = getMaster();
        if (master == null) return 0;
        if (!master.herbStorage.containsKey(herb)) return 0;

        int current = master.herbStorage.get(herb);
        int canAdd = Math.min(amount, MAX_CAPACITY - current);

        if (canAdd > 0) {
            master.herbStorage.put(herb, current + canAdd);
            master.setChanged();
            master.syncToClient();
        }
        return canAdd;
    }

    public int removeHerb(String herbKey, int amount) {
        Item herb = HerbRegistry.getHerbByKey(herbKey);
        return herb != null ? removeHerb(herb, amount) : 0;
    }

    public int removeHerb(Item herb, int amount) {
        HerbVaultBlockEntity master = getMaster();
        if (master == null) return 0;
        if (!master.herbStorage.containsKey(herb)) return 0;

        int current = master.herbStorage.get(herb);
        int canRemove = Math.min(amount, current);

        if (canRemove > 0) {
            master.herbStorage.put(herb, current - canRemove);
            master.setChanged();
            master.syncToClient();
        }
        return canRemove;
    }

    public boolean isDoubleClick(UUID playerUUID) {
        return doubleClickTracker.check(level, playerUUID);
    }

    /**
     * Get the herb index (0-5) for a block position in the multiblock.
     * Front face displays a 3x2 grid of herbs.
     * The grid is determined by the block's position relative to the master,
     * projected onto the front face.
     *
     * Layout on front face (looking at it):
     * [0][1][2]  top row (dy=0, master layer)
     * [3][4][5]  bottom row (dy=-1)
     *
     * Columns map to the horizontal axis perpendicular to facing.
     */
    public int getHerbIndexForBlock(BlockPos targetPos) {
        if (!isFormed()) return -1;

        int[] off = getOffset();
        BlockPos masterPos = getBlockPos().offset(-off[0], -off[1], -off[2]);
        int dy = targetPos.getY() - masterPos.getY();

        // Only Layer 1 (dy=-1) and Layer 2 (dy=0) show herbs on front
        if (dy < -1 || dy > 0) return -1;

        int row = (dy == 0) ? 0 : 1;

        // Determine column based on horizontal position along the front face
        Direction right = getFacing().getClockWise();
        int dx = targetPos.getX() - masterPos.getX();
        int dz = targetPos.getZ() - masterPos.getZ();

        // Project onto right axis to get column
        int col;
        if (right == Direction.EAST) col = dx + 1;
        else if (right == Direction.WEST) col = -(dx - 1);
        else if (right == Direction.SOUTH) col = dz + 1;
        else col = -(dz - 1); // NORTH

        // Reverse column order (right-to-left = 0,1,2)
        col = 2 - col;

        if (col < 0 || col > 2) return -1;

        return row * 3 + col;
    }

    public int getHerbIndexForBlock() {
        return getHerbIndexForBlock(getBlockPos());
    }

    @Override
    protected Block getMultiblockBlock() {
        return ModRegistries.HERB_VAULT.get();
    }

    @Override
    protected void dropStoredItems(BlockPos masterPos) {
        HerbVaultBlockEntity master = getMaster();
        if (master == null) return;
        for (Item herb : HerbRegistry.getAllHerbItems()) {
            int amount = master.getHerbAmount(herb);
            while (amount > 0) {
                int stackSize = Math.min(amount, 64);
                ItemStack stack = new ItemStack(herb, stackSize);
                ItemEntity entity = new ItemEntity(level,
                        masterPos.getX() + 0.5, masterPos.getY() + 0.5, masterPos.getZ() + 0.5, stack);
                level.addFreshEntity(entity);
                amount -= stackSize;
            }
        }
    }

    @Override
    protected BlockState postProcessRestoredBlock(BlockState state, BlockPos pos) {
        return Block.updateFromNeighbourShapes(state, level, pos);
    }

    @Override
    protected List<BlockPos> getStructurePositions(BlockPos masterPos) {
        List<BlockPos> positions = new ArrayList<>();
        for (int dy = -1; dy <= 1; dy++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    positions.add(masterPos.offset(x, dy, z));
                }
            }
        }
        return positions;
    }

    /**
     * Get the original block state for a position when disassembling.
     */
    public BlockState getOriginalBlockForPosition(BlockPos targetPos, BlockPos masterPos) {
        int dx = targetPos.getX() - masterPos.getX();
        int dy = targetPos.getY() - masterPos.getY();
        int dz = targetPos.getZ() - masterPos.getZ();

        if (dy == -1) {
            // Layer 1: all lumistone bricks
            return ModRegistries.LUMISTONE_BRICKS.get().defaultBlockState();
        } else if (dy == 0) {
            if (dx == 0 && dz == 0) {
                // Center: magic alloy block
                return ModRegistries.MAGIC_ALLOY_BLOCK.get().defaultBlockState();
            }
            boolean isCorner = (dx != 0 && dz != 0);
            if (isCorner) {
                return ModRegistries.RED_CHERRY_LOG.get().defaultBlockState();
            }
            // Cardinal side
            Direction relDir = getRelativeDirection(dx, dz);
            if (relDir == getFacing()) {
                // Front: red cherry fence
                return ModRegistries.RED_CHERRY_FENCE.get().defaultBlockState();
            }
            return ModRegistries.LUMISTONE_BRICK_WALL.get().defaultBlockState();
        } else if (dy == 1) {
            if (dx == 0 && dz == 0) {
                return ModRegistries.LUMISTONE_BRICKS.get().defaultBlockState();
            }
            return ModRegistries.LUMISTONE_BRICK_SLAB.get().defaultBlockState()
                    .setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        }

        return Blocks.AIR.defaultBlockState();
    }

    public ItemStack getOriginalItemForPosition(BlockPos targetPos, BlockPos masterPos) {
        BlockState state = getOriginalBlockForPosition(targetPos, masterPos);
        if (state.isAir()) return ItemStack.EMPTY;
        return new ItemStack(state.getBlock());
    }

    private Direction getRelativeDirection(int x, int z) {
        if (x == 0 && z == -1) return Direction.NORTH;
        if (x == 0 && z == 1) return Direction.SOUTH;
        if (x == -1 && z == 0) return Direction.WEST;
        if (x == 1 && z == 0) return Direction.EAST;
        return null;
    }

    @Override
    public ItemStack getOriginalBlock() {
        return new ItemStack(ModRegistries.LUMISTONE_BRICKS.get());
    }

    @Override
    public BlockState getOriginalBlockState() {
        BlockState stored = getRawOriginalBlockState();
        if (stored != null) {
            return stored;
        }
        BlockPos masterPos = getMasterPos();
        if (masterPos == null) {
            return ModRegistries.LUMISTONE_BRICKS.get().defaultBlockState();
        }
        return getOriginalBlockForPosition(getBlockPos(), masterPos);
    }

    public AABB computeRenderAABB() {
        if (renderAABB == null && isFormed()) {
            BlockPos masterPos = getMasterPos();
            if (masterPos != null) {
                renderAABB = new AABB(
                        masterPos.getX() - 1, masterPos.getY() - 1, masterPos.getZ() - 1,
                        masterPos.getX() + 2, masterPos.getY() + 2, masterPos.getZ() + 2
                );
            }
        }
        return renderAABB;
    }

    /**
     * Item handler callback for automation (hoppers, pipes, drawers).
     */
    public HerbCabinetBlockEntity.ItemHandlerCallback getItemHandlerCallback() {
        return new HerbCabinetBlockEntity.ItemHandlerCallback() {
            @Override
            public ItemStack getStackInSlot(int slot) {
                if (slot < 0 || slot >= 6) return ItemStack.EMPTY;
                HerbVaultBlockEntity master = getMaster();
                if (master == null) return ItemStack.EMPTY;
                Item herb = HerbRegistry.getAllHerbItems()[slot];
                int amount = master.getHerbAmount(herb);
                return amount <= 0 ? ItemStack.EMPTY : new ItemStack(herb, Math.min(amount, 64));
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (stack.isEmpty() || !HerbRegistry.isHerb(stack.getItem())) return stack;
                HerbVaultBlockEntity master = getMaster();
                if (master == null) return stack;
                int toInsert = stack.getCount();
                int inserted = simulate ?
                        Math.min(toInsert, MAX_CAPACITY - master.getHerbAmount(stack.getItem())) :
                        master.addHerb(stack.getItem(), toInsert);
                if (inserted >= toInsert) return ItemStack.EMPTY;
                ItemStack remainder = stack.copy();
                remainder.setCount(toInsert - inserted);
                return remainder;
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot < 0 || slot >= 6 || amount <= 0) return ItemStack.EMPTY;
                HerbVaultBlockEntity master = getMaster();
                if (master == null) return ItemStack.EMPTY;
                Item herb = HerbRegistry.getAllHerbItems()[slot];
                int stored = master.getHerbAmount(herb);
                if (stored <= 0) return ItemStack.EMPTY;
                int toExtract = Math.min(amount, stored);
                if (!simulate) master.removeHerb(herb, toExtract);
                return new ItemStack(herb, toExtract);
            }

            @Override
            public int getSlotLimit(int slot) { return 64; }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return HerbRegistry.isHerb(stack.getItem());
            }
        };
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (isMaster()) {
            for (Item herb : HerbRegistry.getAllHerbItems()) {
                String key = "Herb_" + herb.builtInRegistryHolder().key().location().toString();
                tag.putInt(key, herbStorage.getOrDefault(herb, 0));
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (isMaster()) {
            for (Item herb : HerbRegistry.getAllHerbItems()) {
                String key = "Herb_" + herb.builtInRegistryHolder().key().location().toString();
                if (tag.contains(key)) {
                    herbStorage.put(herb, tag.getInt(key));
                }
            }
        }
    }
}
