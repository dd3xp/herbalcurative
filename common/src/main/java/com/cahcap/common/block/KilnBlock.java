package com.cahcap.common.block;

import com.cahcap.common.blockentity.KilnBlockEntity;
import com.cahcap.common.registry.ModRegistries;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Kiln Block - A 3x3x3 multiblock structure for smelting.
 *
 * Structure (ground placement, looking from above):
 *
 * Layer 1 (y=-1, bottom):
 *   [StoneBricks][StoneBricks][StoneBricks]
 *   [StoneBricks][PyrisagePlantable][StoneBricks]
 *   [StoneBricks][StoneBricks][StoneBricks]
 *
 * Layer 2 (y=0, middle - master layer):
 *   [StoneBricks][StoneBricks][StoneBricks]
 *   [StoneBricks][Pyrisage (MASTER)][StoneBricks]
 *   [StoneBricks][StoneBrickSlab(top, front)][StoneBricks]
 *
 * Layer 3 (y=1, top):
 *   [StoneBrickSlab(bottom)][StoneBricks][StoneBrickSlab(bottom)]
 *   [StoneBrickSlab(bottom)][StoneBricks][StoneBrickSlab(bottom)]
 *   [StoneBrickSlab(bottom)][StoneBricks][StoneBrickSlab(bottom)]
 *
 * The master block is at the center of Layer 2 (Pyrisage position).
 */
public class KilnBlock extends BaseEntityBlock {

    public static final MapCodec<KilnBlock> CODEC = simpleCodec(KilnBlock::new);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final BooleanProperty IS_MASTER = BooleanProperty.create("is_master");
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    // Per-block collision/selection shapes from Blockbench model (voxel.py --per-block)
    // NORTH-facing shapes (model default orientation), indexed by (dy+1)*9 + (dx+1)*3 + (dz+1)
    private static final VoxelShape[] NORTH_SHAPES = new VoxelShape[27];

    // Precomputed rotated shapes for all 4 directions [facing][posIndex]
    private static final VoxelShape[][] SHAPES_BY_FACING = new VoxelShape[4][27];

    static {
        // Define NORTH shapes: dx=-1..1, dy=-1..1, dz=-1..1
        // dy=-1
        NORTH_SHAPES[idx(-1,-1,-1)] = Shapes.or(Block.box(4, 0, 4, 12, 16, 16), Block.box(12, 0, 0, 16, 16, 16));
        NORTH_SHAPES[idx(-1,-1, 0)] = Shapes.or(Block.box(0, 12, 0, 16, 16, 16), Block.box(0, 0, 0, 16, 4, 16), Block.box(0, 4, 12, 16, 12, 16), Block.box(0, 4, 0, 16, 12, 4));
        NORTH_SHAPES[idx(-1,-1, 1)] = Block.box(4, 0, 0, 16, 16, 12);
        NORTH_SHAPES[idx( 0,-1,-1)] = Shapes.or(Block.box(0, 0, 4, 16, 8, 16), Block.box(0, 4, 0, 16, 8, 4));
        NORTH_SHAPES[idx( 0,-1, 0)] = Block.box(0, 0, 0, 16, 4, 16);
        NORTH_SHAPES[idx( 0,-1, 1)] = Shapes.or(Block.box(0, 12, 0, 16, 16, 16), Block.box(0, 4, 0, 4, 12, 16), Block.box(12, 4, 0, 16, 12, 16), Block.box(0, 0, 0, 16, 4, 16));
        NORTH_SHAPES[idx( 1,-1,-1)] = Shapes.or(Block.box(4, 0, 4, 12, 16, 16), Block.box(0, 0, 0, 4, 16, 16));
        NORTH_SHAPES[idx( 1,-1, 0)] = Shapes.or(Block.box(0, 12, 0, 16, 16, 16), Block.box(0, 0, 0, 16, 4, 16), Block.box(0, 4, 12, 16, 12, 16), Block.box(0, 4, 0, 16, 12, 4));
        NORTH_SHAPES[idx( 1,-1, 1)] = Block.box(0, 0, 0, 12, 16, 12);
        // dy=0
        NORTH_SHAPES[idx(-1, 0,-1)] = Shapes.or(Block.box(4, 0, 4, 12, 16, 16), Block.box(12, 8, 4, 16, 16, 12), Block.box(12, 4, 4, 16, 8, 16), Block.box(12, 0, 0, 16, 4, 16));
        NORTH_SHAPES[idx(-1, 0, 0)] = Shapes.or(Block.box(4, 0, 0, 12, 16, 16), Block.box(12, 0, 0, 16, 16, 16));
        NORTH_SHAPES[idx(-1, 0, 1)] = Block.box(4, 0, 0, 16, 16, 12);
        NORTH_SHAPES[idx( 0, 0,-1)] = Shapes.or(Block.box(0, 8, 4, 16, 16, 12), Block.box(4, 4, 0, 12, 8, 16), Block.box(0, 8, 12, 16, 16, 16), Block.box(12, 0, 0, 16, 4, 16), Block.box(0, 0, 0, 4, 4, 16), Block.box(12, 4, 0, 16, 8, 16), Block.box(0, 4, 0, 4, 8, 16));
        NORTH_SHAPES[idx( 0, 0, 0)] = Shapes.empty();
        NORTH_SHAPES[idx( 0, 0, 1)] = Shapes.or(Block.box(0, 0, 4, 16, 16, 12), Block.box(0, 0, 0, 16, 16, 4));
        NORTH_SHAPES[idx( 1, 0,-1)] = Shapes.or(Block.box(4, 0, 4, 12, 16, 16), Block.box(0, 8, 4, 4, 16, 12), Block.box(0, 4, 4, 4, 8, 16), Block.box(0, 0, 0, 4, 4, 16));
        NORTH_SHAPES[idx( 1, 0, 0)] = Shapes.or(Block.box(4, 0, 0, 12, 16, 16), Block.box(0, 0, 0, 4, 16, 16));
        NORTH_SHAPES[idx( 1, 0, 1)] = Block.box(0, 0, 0, 12, 16, 12);
        // dy=1
        NORTH_SHAPES[idx(-1, 1,-1)] = Shapes.or(Block.box(4, 0, 4, 12, 4, 16), Block.box(12, 0, 4, 16, 4, 12), Block.box(8, 4, 4, 16, 10, 16));
        NORTH_SHAPES[idx(-1, 1, 0)] = Shapes.or(Block.box(4, 0, 0, 12, 4, 16), Block.box(8, 4, 0, 16, 10, 16), Block.box(12, 0, 0, 16, 4, 16));
        NORTH_SHAPES[idx(-1, 1, 1)] = Shapes.or(Block.box(4, 0, 0, 16, 4, 12), Block.box(8, 4, 0, 16, 10, 12));
        NORTH_SHAPES[idx( 0, 1,-1)] = Shapes.or(Block.box(0, 0, 4, 16, 4, 12), Block.box(0, 4, 4, 16, 10, 16), Block.box(0, 0, 12, 16, 4, 16));
        NORTH_SHAPES[idx( 0, 1, 0)] = Block.box(0, 4, 0, 16, 10, 16);
        NORTH_SHAPES[idx( 0, 1, 1)] = Shapes.or(Block.box(0, 0, 4, 16, 4, 12), Block.box(0, 4, 0, 16, 10, 12), Block.box(0, 0, 0, 16, 4, 4));
        NORTH_SHAPES[idx( 1, 1,-1)] = Shapes.or(Block.box(4, 0, 4, 12, 4, 16), Block.box(0, 0, 4, 4, 4, 12), Block.box(0, 4, 4, 8, 10, 16));
        NORTH_SHAPES[idx( 1, 1, 0)] = Shapes.or(Block.box(4, 0, 0, 12, 4, 16), Block.box(0, 4, 0, 8, 10, 16), Block.box(0, 0, 0, 4, 4, 16));
        NORTH_SHAPES[idx( 1, 1, 1)] = Shapes.or(Block.box(0, 0, 0, 12, 4, 12), Block.box(0, 4, 0, 8, 10, 12));

        // Precompute rotated shapes for all facings
        for (int i = 0; i < 27; i++) {
            VoxelShape shape = NORTH_SHAPES[i];
            if (shape == null) shape = Shapes.empty();
            SHAPES_BY_FACING[Direction.NORTH.get2DDataValue()][i] = shape;
            SHAPES_BY_FACING[Direction.SOUTH.get2DDataValue()][i] = rotateShape(shape, Direction.SOUTH);
            SHAPES_BY_FACING[Direction.WEST.get2DDataValue()][i] = rotateShape(shape, Direction.WEST);
            SHAPES_BY_FACING[Direction.EAST.get2DDataValue()][i] = rotateShape(shape, Direction.EAST);
        }
    }

    private static int idx(int dx, int dy, int dz) {
        return (dy + 1) * 9 + (dx + 1) * 3 + (dz + 1);
    }

    /**
     * Rotate a VoxelShape from NORTH orientation to the given direction.
     * Same approach as HerbCabinetBlock.
     */
    private static VoxelShape rotateShape(VoxelShape shape, Direction to) {
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

    public KilnBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FORMED, false)
                .setValue(IS_MASTER, false)
                .setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORMED, IS_MASTER, LIT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new KilnBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(FORMED)) {
            return Shapes.block();
        }
        if (level.getBlockEntity(pos) instanceof KilnBlockEntity be) {
            BlockPos masterPos = be.getMasterPos();
            if (masterPos != null) return getShapeForPosition(pos, masterPos, be.getFacing());
        }
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(FORMED)) {
            return Shapes.block();
        }
        if (level.getBlockEntity(pos) instanceof KilnBlockEntity be) {
            BlockPos masterPos = be.getMasterPos();
            if (masterPos != null) return getShapeForPosition(pos, masterPos, be.getFacing());
        }
        return Shapes.empty();
    }

    private VoxelShape getShapeForPosition(BlockPos targetPos, BlockPos masterPos, Direction facing) {
        int worldDx = targetPos.getX() - masterPos.getX();
        int dy = targetPos.getY() - masterPos.getY();
        int worldDz = targetPos.getZ() - masterPos.getZ();

        // Inverse-rotate world (dx,dz) back to NORTH-model space to get the shape index,
        // then use the pre-rotated shape for the actual facing.
        // The blockstate rotation for facing X uses rotateShape(northShape, X),
        // which rotates the geometry. So we need to look up using NORTH-space coords.
        //
        // Blockstate visual rotation: NORTH→identity, SOUTH→180, EAST→90CW, WEST→270CW
        // Inverse (world→model): SOUTH: (-dx,-dz), EAST: (dz,-dx), WEST: (-dz,dx)
        int modelDx, modelDz;
        switch (facing) {
            case SOUTH -> { modelDx = -worldDx; modelDz = -worldDz; }
            case EAST  -> { modelDx = worldDz; modelDz = -worldDx; }
            case WEST  -> { modelDx = -worldDz; modelDz = worldDx; }
            default    -> { modelDx = worldDx; modelDz = worldDz; }  // NORTH
        }

        if (modelDx < -1 || modelDx > 1 || dy < -1 || dy > 1 || modelDz < -1 || modelDz > 1) {
            return Shapes.block();
        }

        int index = idx(modelDx, dy, modelDz);
        return SHAPES_BY_FACING[facing.get2DDataValue()][index];
    }

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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide && state.getValue(FORMED)) {
            return createTickerHelper(type, (BlockEntityType<KilnBlockEntity>) ModRegistries.KILN_BE.get(),
                    KilnBlockEntity::serverTick);
        }
        return null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hand == InteractionHand.OFF_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!state.getValue(FORMED)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Flowweave Ring: pass to item handler (for potential future interactions)
        if (stack.is(ModRegistries.FLOWWEAVE_RING.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Kiln has no manual item insertion/extraction - all I/O is automatic via adjacent chests
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /**
     * Handle block destruction logic for formed multiblock.
     */
    public boolean handleBlockDestruction(BlockState state, Level level, BlockPos pos, Player player, FluidState fluid) {
        return true;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof KilnBlockEntity be) {
                if (!be.suppressDrops && be.formed) {
                    be.disassemble();
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof KilnBlockEntity kiln) {
            BlockPos masterPos = kiln.getMasterPos();
            if (masterPos != null) {
                return kiln.getOriginalItemForPosition(pos, masterPos);
            }
        }
        return new ItemStack(net.minecraft.world.level.block.Blocks.STONE_BRICKS);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof KilnBlockEntity kiln) {
            if (kiln.suppressDrops) {
                return Collections.emptyList();
            }
            BlockPos masterPos = kiln.getMasterPos();
            BlockPos thisPos = params.getOptionalParameter(LootContextParams.ORIGIN) != null ?
                    BlockPos.containing(params.getOptionalParameter(LootContextParams.ORIGIN)) :
                    kiln.getBlockPos();
            if (masterPos != null) {
                return Collections.singletonList(kiln.getOriginalItemForPosition(thisPos, masterPos));
            }
        }
        return Collections.singletonList(new ItemStack(net.minecraft.world.level.block.Blocks.STONE_BRICKS));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(FORMED)) {
            return;
        }

        if (!(level.getBlockEntity(pos) instanceof KilnBlockEntity be)) {
            return;
        }

        if (!be.isMaster()) {
            return;
        }

        // Only show effects when smelting
        if (!be.isSmelting()) {
            return;
        }

        // Campfire smoke rising from chimney (top center)
        if (random.nextInt(2) == 0) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 1.5;
            double z = pos.getZ() + 0.5;
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z,
                    (random.nextDouble() - 0.5) * 0.01, 0.05, (random.nextDouble() - 0.5) * 0.01);
        }

        // Fire particles from the front opening
        if (random.nextInt(3) == 0) {
            Direction facing = state.getValue(FACING);
            double x = pos.getX() + 0.5 + facing.getStepX() * 0.6;
            double y = pos.getY() - 0.3;
            double z = pos.getZ() + 0.5 + facing.getStepZ() * 0.6;
            level.addParticle(ParticleTypes.SMALL_FLAME, x, y, z, 0, 0.01, 0);
        }

        // Fire crackle sound
        if (random.nextInt(4) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }
    }
}
