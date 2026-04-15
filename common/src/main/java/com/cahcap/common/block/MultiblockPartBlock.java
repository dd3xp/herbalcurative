package com.cahcap.common.block;

import com.cahcap.common.blockentity.MultiblockPartBlockEntity;
import com.cahcap.common.multiblock.Multiblock;
import com.cahcap.common.util.CustomVoxelShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 * Base class for all multiblock structure blocks.
 * Handles shared block state properties, shape delegation, light/occlusion,
 * onRemove disassembly, and drop/pick logic.
 * <p>
 * Subclasses only need to provide: codec, newBlockEntity, shape lookup,
 * and any unique behavior (interaction, particles, extra properties, ticker).
 */
public abstract class MultiblockPartBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = Multiblock.FACING;
    public static final BooleanProperty FORMED = Multiblock.FORMED;
    public static final BooleanProperty MIRRORED = Multiblock.MIRRORED;

    /**
     * Each subclass defines its own POSITION property with the appropriate range.
     */
    public abstract IntegerProperty getPositionProperty();

    protected MultiblockPartBlock(Properties properties) {
        super(properties);
    }

    /**
     * Subclasses MUST override this to add their own POSITION property.
     * Call super first, then add POSITION.
     */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORMED, MIRRORED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // ==================== Shape ====================

    /**
     * Look up the VoxelShape for a specific position in the formed multiblock.
     * Subclasses implement this with their own shape arrays and indexing.
     */
    protected abstract VoxelShape getMultiblockShape(Direction facing, int[] offset, boolean mirrored);

    /**
     * Get the CustomVoxelShapes instance for this block.
     */
    protected abstract CustomVoxelShapes getCustomVoxelShapes();

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(FORMED)) {
            return Shapes.block();
        }
        return getCustomVoxelShapes().getByIndex(
                state.getValue(FACING),
                state.getValue(getPositionProperty()),
                state.getValue(MIRRORED));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(FORMED)) {
            return Shapes.block();
        }
        return getCustomVoxelShapes().getByIndex(
                state.getValue(FACING),
                state.getValue(getPositionProperty()),
                state.getValue(MIRRORED));
    }

    // ==================== Light / Occlusion ====================

    @Override
    protected boolean isOcclusionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return false;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(FORMED);
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.getValue(FORMED)) {
            return 0;
        }
        return super.getLightBlock(state, level, pos);
    }

    // ==================== Removal / Drops ====================

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof MultiblockPartBlockEntity be) {
                if (!be.isSuppressDrops() && be.isFormed()) {
                    be.disassemble();
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof MultiblockPartBlockEntity be && be.isFormed()) {
            BlockState original = be.getOriginalBlockState();
            if (original != null && !original.isAir()) {
                return new ItemStack(original.getBlock());
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof MultiblockPartBlockEntity be) {
            if (be.isSuppressDrops()) {
                return Collections.emptyList();
            }
            if (be.isFormed()) {
                BlockState original = be.getOriginalBlockState();
                if (original != null && !original.isAir()) {
                    return Collections.singletonList(new ItemStack(original.getBlock()));
                }
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    // ==================== Block Destruction ====================

    /**
     * Handle block destruction logic for formed multiblock.
     * Called by mod loader's block destruction override.
     * Returns true if destruction should proceed, false to cancel.
     * Override in subclasses to prevent breaking specific faces (e.g., front face of herb cabinet).
     */
    public boolean handleBlockDestruction(BlockState state, Level level, BlockPos pos, Player player, FluidState fluid) {
        return true;
    }

    // ==================== Shape Utilities ====================

    /**
     * Rotate a VoxelShape from NORTH orientation to the given direction.
     */
    protected static VoxelShape rotateShape(VoxelShape shape, Direction to) {
        VoxelShape[] accumulatedShape = new VoxelShape[]{Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double x1 = minX * 16, y1 = minY * 16, z1 = minZ * 16;
            double x2 = maxX * 16, y2 = maxY * 16, z2 = maxZ * 16;
            double rotatedX1, rotatedZ1, rotatedX2, rotatedZ2;
            switch (to) {
                case SOUTH -> { rotatedX1 = 16 - x2; rotatedZ1 = 16 - z2; rotatedX2 = 16 - x1; rotatedZ2 = 16 - z1; }
                case WEST  -> { rotatedX1 = z1; rotatedZ1 = 16 - x2; rotatedX2 = z2; rotatedZ2 = 16 - x1; }
                case EAST  -> { rotatedX1 = 16 - z2; rotatedZ1 = x1; rotatedX2 = 16 - z1; rotatedZ2 = x2; }
                default    -> { rotatedX1 = x1; rotatedZ1 = z1; rotatedX2 = x2; rotatedZ2 = z2; }
            }
            accumulatedShape[0] = Shapes.or(accumulatedShape[0], Block.box(
                    Math.min(rotatedX1, rotatedX2), y1, Math.min(rotatedZ1, rotatedZ2),
                    Math.max(rotatedX1, rotatedX2), y2, Math.max(rotatedZ1, rotatedZ2)));
        });
        return accumulatedShape[0];
    }

    /**
     * Mirror a VoxelShape on the X axis (left/right flip).
     */
    protected static VoxelShape mirrorShapeX(VoxelShape shape) {
        VoxelShape[] accumulatedShape = new VoxelShape[]{Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double x1 = minX * 16, y1 = minY * 16, z1 = minZ * 16;
            double x2 = maxX * 16, y2 = maxY * 16, z2 = maxZ * 16;
            accumulatedShape[0] = Shapes.or(accumulatedShape[0], Block.box(16 - x2, y1, z1, 16 - x1, y2, z2));
        });
        return accumulatedShape[0];
    }

    /**
     * Inverse-rotate world offset to model space.
     */
    public static int[] worldToModelOffset(Direction facing, int[] offset) {
        int worldDx = offset[0], dy = offset[1], worldDz = offset[2];
        int modelDx, modelDz;
        switch (facing) {
            case SOUTH -> { modelDx = -worldDx; modelDz = -worldDz; }
            case EAST  -> { modelDx = worldDz; modelDz = -worldDx; }
            case WEST  -> { modelDx = -worldDz; modelDz = worldDx; }
            default    -> { modelDx = worldDx; modelDz = worldDz; }
        }
        return new int[]{modelDx, dy, modelDz};
    }

    /**
     * Precompute rotated shapes for all 4 facings from NORTH-oriented shapes.
     *
     * @param northShapes shapes in NORTH orientation
     * @return [facing_2d][index] shape array
     */
    public static VoxelShape[][] precomputeRotatedShapes(VoxelShape[] northShapes) {
        VoxelShape[][] result = new VoxelShape[4][northShapes.length];
        for (int i = 0; i < northShapes.length; i++) {
            VoxelShape shape = northShapes[i] != null ? northShapes[i] : Shapes.empty();
            result[Direction.NORTH.get2DDataValue()][i] = shape;
            result[Direction.SOUTH.get2DDataValue()][i] = rotateShape(shape, Direction.SOUTH);
            result[Direction.WEST.get2DDataValue()][i] = rotateShape(shape, Direction.WEST);
            result[Direction.EAST.get2DDataValue()][i] = rotateShape(shape, Direction.EAST);
        }
        return result;
    }

    /**
     * Precompute mirrored+rotated shapes for all 4 facings.
     * Mirror swaps the X (left/right) component: for each index, look up the
     * shape at the mirrored index, then flip its geometry on the X axis.
     *
     * @param northShapes  shapes in NORTH orientation
     * @param mirrorIndex  maps an index to its X-mirrored counterpart (e.g., swap dx → -dx)
     * @return [facing_2d][index] mirrored shape array
     */
    public static VoxelShape[][] precomputeMirroredShapes(VoxelShape[] northShapes, IntUnaryOperator mirrorIndex) {
        VoxelShape[][] result = new VoxelShape[4][northShapes.length];
        for (int i = 0; i < northShapes.length; i++) {
            int mirroredIndex = mirrorIndex.applyAsInt(i);
            VoxelShape mirroredNorth = mirrorShapeX(northShapes[mirroredIndex] != null ? northShapes[mirroredIndex] : Shapes.empty());
            result[Direction.NORTH.get2DDataValue()][i] = mirroredNorth;
            result[Direction.SOUTH.get2DDataValue()][i] = rotateShape(mirroredNorth, Direction.SOUTH);
            result[Direction.WEST.get2DDataValue()][i] = rotateShape(mirroredNorth, Direction.WEST);
            result[Direction.EAST.get2DDataValue()][i] = rotateShape(mirroredNorth, Direction.EAST);
        }
        return result;
    }
}
