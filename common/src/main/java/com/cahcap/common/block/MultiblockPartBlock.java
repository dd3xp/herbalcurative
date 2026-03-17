package com.cahcap.common.block;

import com.cahcap.common.blockentity.MultiblockPartBlockEntity;
import com.cahcap.common.multiblock.Multiblock;
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
    public static final BooleanProperty IS_MASTER = Multiblock.IS_MASTER;
    public static final BooleanProperty MIRRORED = Multiblock.MIRRORED;

    protected MultiblockPartBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FORMED, false)
                .setValue(IS_MASTER, false)
                .setValue(MIRRORED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORMED, IS_MASTER, MIRRORED);
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

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(FORMED)) {
            return Shapes.block();
        }
        if (level.getBlockEntity(pos) instanceof MultiblockPartBlockEntity be && be.formed) {
            return getMultiblockShape(be.facing, be.offset, be.mirrored);
        }
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(FORMED)) {
            return Shapes.block();
        }
        if (level.getBlockEntity(pos) instanceof MultiblockPartBlockEntity be && be.formed) {
            return getMultiblockShape(be.facing, be.offset, be.mirrored);
        }
        return Shapes.empty();
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
                if (!be.suppressDrops && be.formed) {
                    be.disassemble();
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof MultiblockPartBlockEntity be && be.formed) {
            BlockState original = be.getOriginalBlockState();
            if (original != null && !original.isAir()) {
                return new ItemStack(original.getBlock());
            }
        }
        return getDefaultDropItem();
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof MultiblockPartBlockEntity be) {
            if (be.suppressDrops) {
                return Collections.emptyList();
            }
            if (be.formed) {
                BlockState original = be.getOriginalBlockState();
                if (original != null && !original.isAir()) {
                    return Collections.singletonList(new ItemStack(original.getBlock()));
                }
            }
        }
        return Collections.singletonList(getDefaultDropItem());
    }

    /**
     * Fallback item when the block is not formed or has no stored original state.
     * Override in subclasses to return the appropriate construction material.
     */
    protected abstract ItemStack getDefaultDropItem();

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
        VoxelShape[] buffer = new VoxelShape[]{Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double x1 = minX * 16, y1 = minY * 16, z1 = minZ * 16;
            double x2 = maxX * 16, y2 = maxY * 16, z2 = maxZ * 16;
            double nx1, nz1, nx2, nz2;
            switch (to) {
                case SOUTH -> { nx1 = 16 - x2; nz1 = 16 - z2; nx2 = 16 - x1; nz2 = 16 - z1; }
                case WEST  -> { nx1 = z1; nz1 = 16 - x2; nx2 = z2; nz2 = 16 - x1; }
                case EAST  -> { nx1 = 16 - z2; nz1 = x1; nx2 = 16 - z1; nz2 = x2; }
                default    -> { nx1 = x1; nz1 = z1; nx2 = x2; nz2 = z2; }
            }
            buffer[0] = Shapes.or(buffer[0], Block.box(
                    Math.min(nx1, nx2), y1, Math.min(nz1, nz2),
                    Math.max(nx1, nx2), y2, Math.max(nz1, nz2)));
        });
        return buffer[0];
    }

    /**
     * Mirror a VoxelShape on the X axis (left/right flip).
     */
    protected static VoxelShape mirrorShapeX(VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double x1 = minX * 16, y1 = minY * 16, z1 = minZ * 16;
            double x2 = maxX * 16, y2 = maxY * 16, z2 = maxZ * 16;
            buffer[0] = Shapes.or(buffer[0], Block.box(16 - x2, y1, z1, 16 - x1, y2, z2));
        });
        return buffer[0];
    }

    /**
     * Inverse-rotate world offset to model space.
     */
    protected static int[] worldToModelOffset(Direction facing, int[] offset) {
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
    protected static VoxelShape[][] precomputeRotatedShapes(VoxelShape[] northShapes) {
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
    protected static VoxelShape[][] precomputeMirroredShapes(VoxelShape[] northShapes, IntUnaryOperator mirrorIndex) {
        VoxelShape[][] result = new VoxelShape[4][northShapes.length];
        for (int i = 0; i < northShapes.length; i++) {
            int mi = mirrorIndex.applyAsInt(i);
            VoxelShape mirroredNorth = mirrorShapeX(northShapes[mi] != null ? northShapes[mi] : Shapes.empty());
            result[Direction.NORTH.get2DDataValue()][i] = mirroredNorth;
            result[Direction.SOUTH.get2DDataValue()][i] = rotateShape(mirroredNorth, Direction.SOUTH);
            result[Direction.WEST.get2DDataValue()][i] = rotateShape(mirroredNorth, Direction.WEST);
            result[Direction.EAST.get2DDataValue()][i] = rotateShape(mirroredNorth, Direction.EAST);
        }
        return result;
    }
}
