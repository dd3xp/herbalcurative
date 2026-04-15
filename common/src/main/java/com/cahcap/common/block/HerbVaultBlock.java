package com.cahcap.common.block;

import com.cahcap.common.util.GridHitHelper;
import com.cahcap.common.util.HerbRegistry;
import com.cahcap.common.blockentity.HerbVaultBlockEntity;
import com.cahcap.common.util.HerbRegistry;
import com.cahcap.common.registry.ModRegistries;
import com.cahcap.common.util.HerbRegistry;
import com.cahcap.common.util.CustomVoxelShapes;
import com.cahcap.common.util.HerbRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class HerbVaultBlock extends MultiblockPartBlock {

    public static final MapCodec<HerbVaultBlock> CODEC = simpleCodec(HerbVaultBlock::new);

    private static final CustomVoxelShapes SHAPES = CustomVoxelShapes.loadFromModel("/assets/herbalcurative/models/block/herb_vault.json");
    public static final IntegerProperty POSITION = IntegerProperty.create("position", 0, SHAPES.totalPositions() - 1);

    public HerbVaultBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(POSITION, 0));
    }

    @Override
    public IntegerProperty getPositionProperty() { return POSITION; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POSITION);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HerbVaultBlockEntity(pos, state);
    }

    @Override
    protected CustomVoxelShapes getCustomVoxelShapes() { return SHAPES; }

    @Override
    protected VoxelShape getMultiblockShape(Direction facing, int[] offset, boolean mirrored) {
        return SHAPES.get(facing, offset, mirrored);
    }

    /** Returns true if the block at pos is on the front row (forwardOffset==1, dy==0). */
    public static boolean isFrontRow(HerbVaultBlockEntity be) {
        Direction facing = be.getFacing();
        int[] off = be.getOffset();
        int fwd = facing.getStepX() * off[0] + facing.getStepZ() * off[2];
        return fwd == 1 && off[1] <= 0;
    }

    // ==================== Grid Cell Targeting ====================

    /**
     * Grid cell bounds per slot in local block coordinates (0-16), NORTH facing.
     * [slot][0=minX, 1=maxX, 2=minY, 3=maxY]
     */
    private static final double[][] GRID_CELLS = {
            {6, 16, 2, 12},   // slot 0: top-left (dx=-1, dy=0)
            {3, 13, 2, 12},   // slot 1: top-center (dx=0, dy=0)
            {0, 10, 2, 12},   // slot 2: top-right (dx=1, dy=0)
            {6, 16, 6, 16},   // slot 3: bottom-left (dx=-1, dy=-1)
            {3, 13, 6, 16},   // slot 4: bottom-center (dx=0, dy=-1)
            {0, 10, 6, 16},   // slot 5: bottom-right (dx=1, dy=-1)
    };

    /**
     * Check if a hit location on the front face is within the grid cell for the given herb slot.
     */
    public static boolean isHitInGridCell(BlockHitResult hitResult, BlockPos pos, Direction facing, int herbIndex) {
        return GridHitHelper.isHitInGridCell(hitResult, pos, facing, herbIndex, GRID_CELLS);
    }

    // ==================== Interaction ====================

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hand == InteractionHand.OFF_HAND) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (hitResult.getDirection() != state.getValue(FACING)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level.getBlockEntity(pos) instanceof HerbVaultBlockEntity be && !isFrontRow(be))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (stack.is(ModRegistries.HERB_BOX.get())) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;

        if (level.getBlockEntity(pos) instanceof HerbVaultBlockEntity be && be.isFormed()) {
            int herbIndex = be.getHerbIndexForBlock();
            if (!isHitInGridCell(hitResult, pos, state.getValue(FACING), herbIndex)) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            boolean isDouble = be.isDoubleClick(player.getUUID());
            int totalAdded = 0;

            if (isDouble && (stack.isEmpty() || !HerbRegistry.isHerb(stack.getItem()))) {
                totalAdded = HerbRegistry.transferAllHerbsFromInventory(player, be::addHerb);
            } else if (!stack.isEmpty() && HerbRegistry.isHerb(stack.getItem())) {
                totalAdded = be.addHerb(stack.getItem(), stack.getCount());
                stack.shrink(totalAdded);
            }

            if (totalAdded > 0) {
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F,
                        1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F);
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    /**
     * Extract herbs on left-click via Block.attack().
     * Called once when the player starts breaking (like Storage Drawers).
     */
    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return;
        if (!(level.getBlockEntity(pos) instanceof HerbVaultBlockEntity be) || !be.isFormed()) return;

        HitResult hitResult = player.pick(player.blockInteractionRange(), 0.0F, false);
        if (!(hitResult instanceof BlockHitResult blockHit) || blockHit.getDirection() != state.getValue(FACING)) return;
        if (!isFrontRow(be)) return;

        int herbIndex = be.getHerbIndexForBlock();
        if (!isHitInGridCell(blockHit, pos, state.getValue(FACING), herbIndex)) return;
        if (herbIndex < 0 || herbIndex >= 6) return;

        Item herb = HerbRegistry.getAllHerbItems()[herbIndex];
        int amount = player.isShiftKeyDown() ? 64 : 1;
        int removed = be.removeHerb(herb, amount);

        if (removed > 0) {
            ItemStack extracted = new ItemStack(herb, removed);
            if (!player.getInventory().add(extracted)) player.drop(extracted, false);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                    ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }
    }

    /**
     * Check if the left-click targets the front face of a formed vault.
     * Used by the creative-mode event handler.
     */
    public boolean isFrontFaceClick(BlockState state, Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof HerbVaultBlockEntity be) || !be.isFormed()) return false;
        if (!isFrontRow(be)) return false;

        HitResult hitResult = player.pick(player.blockInteractionRange(), 0.0F, false);
        return hitResult instanceof BlockHitResult blockHit && blockHit.getDirection() == state.getValue(FACING);
    }
}
