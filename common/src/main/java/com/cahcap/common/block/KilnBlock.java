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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Kiln Block - A 3x3x3 multiblock structure for smelting.
 */
public class KilnBlock extends MultiblockPartBlock {

    public static final MapCodec<KilnBlock> CODEC = simpleCodec(KilnBlock::new);

    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    // Per-block collision/selection shapes from Blockbench model (voxel.py --per-block)
    // NORTH-facing shapes (model default orientation), indexed by (dy+1)*9 + (dx+1)*3 + (dz+1)
    private static final VoxelShape[] NORTH_SHAPES = new VoxelShape[27];
    private static final VoxelShape[][] SHAPES_BY_FACING;
    private static final VoxelShape[][] SHAPES_BY_FACING_MIRRORED;

    static {
        // dy=-1
        NORTH_SHAPES[idx(-1,-1,-1)] = Shapes.or(Block.box(4, 0, 4, 15, 16, 16), Block.box(15, 13, 0, 16, 16, 16), Block.box(15, 0, 0, 16, 13, 16));
        NORTH_SHAPES[idx(-1,-1, 0)] = Shapes.or(Block.box(0, 11, 0, 16, 16, 16), Block.box(0, 0, 0, 16, 5, 16), Block.box(0, 5, 11, 16, 11, 16), Block.box(0, 5, 0, 16, 11, 5));
        NORTH_SHAPES[idx(-1,-1, 1)] = Block.box(4, 0, 0, 16, 16, 12);
        NORTH_SHAPES[idx( 0,-1,-1)] = Shapes.or(Block.box(2, 0, 4, 14, 8, 16), Block.box(3, 4, 0, 13, 8, 4), Block.box(5, 15, 0, 11, 16, 12), Block.box(11, 13, 0, 16, 16, 16), Block.box(0, 13, 0, 5, 16, 16), Block.box(13, 0, 0, 16, 13, 16), Block.box(0, 0, 0, 3, 13, 16));
        NORTH_SHAPES[idx( 0,-1, 0)] = Block.box(0, 0, 0, 16, 4, 16);
        NORTH_SHAPES[idx( 0,-1, 1)] = Shapes.or(Block.box(0, 11, 0, 16, 16, 16), Block.box(0, 5, 0, 5, 11, 16), Block.box(11, 5, 0, 16, 11, 16), Block.box(0, 0, 0, 16, 5, 16));
        NORTH_SHAPES[idx( 1,-1,-1)] = Shapes.or(Block.box(1, 0, 4, 12, 16, 16), Block.box(0, 13, 0, 1, 16, 16), Block.box(0, 0, 0, 1, 13, 16));
        NORTH_SHAPES[idx( 1,-1, 0)] = Shapes.or(Block.box(0, 11, 0, 16, 16, 16), Block.box(0, 0, 0, 16, 5, 16), Block.box(0, 5, 11, 16, 11, 16), Block.box(0, 5, 0, 16, 11, 5));
        NORTH_SHAPES[idx( 1,-1, 1)] = Block.box(0, 0, 0, 12, 16, 12);
        // dy=0
        NORTH_SHAPES[idx(-1, 0,-1)] = Shapes.or(Block.box(4, 0, 4, 15, 16, 16), Block.box(15, 3, 4, 16, 16, 12), Block.box(15, 1, 4, 16, 3, 12), Block.box(15, 1, 12, 16, 16, 16), Block.box(15, 0, 0, 16, 1, 16));
        NORTH_SHAPES[idx(-1, 0, 0)] = Shapes.or(Block.box(4, 0, 0, 12, 16, 16), Block.box(12, 0, 0, 16, 16, 16));
        NORTH_SHAPES[idx(-1, 0, 1)] = Block.box(4, 0, 0, 16, 16, 12);
        NORTH_SHAPES[idx( 0, 0,-1)] = Shapes.or(Block.box(0, 3, 4, 16, 16, 12), Block.box(14, 1, 4, 16, 3, 12), Block.box(0, 1, 4, 2, 3, 12), Block.box(2, 1, 0, 14, 3, 12), Block.box(5, 0, 0, 11, 1, 12), Block.box(0, 1, 12, 16, 16, 16), Block.box(11, 0, 0, 16, 1, 16), Block.box(0, 0, 0, 5, 1, 16));
        NORTH_SHAPES[idx( 0, 0, 0)] = Block.box(0, 0, 0, 16, 4, 16);
        NORTH_SHAPES[idx( 0, 0, 1)] = Shapes.or(Block.box(0, 0, 4, 16, 16, 12), Block.box(0, 0, 0, 16, 16, 4));
        NORTH_SHAPES[idx( 1, 0,-1)] = Shapes.or(Block.box(1, 0, 4, 12, 16, 16), Block.box(0, 3, 4, 1, 16, 12), Block.box(0, 1, 4, 1, 3, 12), Block.box(0, 1, 12, 1, 16, 16), Block.box(0, 0, 0, 1, 1, 16));
        NORTH_SHAPES[idx( 1, 0, 0)] = Shapes.or(Block.box(4, 0, 0, 12, 16, 16), Block.box(0, 0, 0, 4, 16, 16));
        NORTH_SHAPES[idx( 1, 0, 1)] = Block.box(0, 0, 0, 12, 16, 12);
        // dy=1
        NORTH_SHAPES[idx(-1, 1,-1)] = Block.box(8, 0, 4, 16, 6, 16);
        NORTH_SHAPES[idx(-1, 1, 0)] = Block.box(8, 0, 0, 16, 6, 16);
        NORTH_SHAPES[idx(-1, 1, 1)] = Block.box(8, 0, 0, 16, 6, 12);
        NORTH_SHAPES[idx( 0, 1,-1)] = Block.box(0, 0, 4, 16, 6, 16);
        NORTH_SHAPES[idx( 0, 1, 0)] = Block.box(0, 0, 0, 16, 6, 16);
        NORTH_SHAPES[idx( 0, 1, 1)] = Block.box(0, 0, 0, 16, 6, 12);
        NORTH_SHAPES[idx( 1, 1,-1)] = Block.box(0, 0, 4, 8, 6, 16);
        NORTH_SHAPES[idx( 1, 1, 0)] = Block.box(0, 0, 0, 8, 6, 16);
        NORTH_SHAPES[idx( 1, 1, 1)] = Block.box(0, 0, 0, 8, 6, 12);

        SHAPES_BY_FACING = precomputeRotatedShapes(NORTH_SHAPES);
        SHAPES_BY_FACING_MIRRORED = precomputeMirroredShapes(NORTH_SHAPES, i -> {
            int dy = (i / 9) - 1, dx = ((i % 9) / 3) - 1, dz = (i % 3) - 1;
            return idx(-dx, dy, dz);
        });
    }

    private static int idx(int dx, int dy, int dz) {
        return (dy + 1) * 9 + (dx + 1) * 3 + (dz + 1);
    }

    public KilnBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new KilnBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getMultiblockShape(Direction facing, int[] offset, boolean mirrored) {
        int[] model = worldToModelOffset(facing, offset);
        int modelDx = model[0], dy = model[1], modelDz = model[2];

        if (modelDx < -1 || modelDx > 1 || dy < -1 || dy > 1 || modelDz < -1 || modelDz > 1) {
            return Shapes.block();
        }

        int index = idx(modelDx, dy, modelDz);
        VoxelShape[][] table = mirrored ? SHAPES_BY_FACING_MIRRORED : SHAPES_BY_FACING;
        return table[facing.get2DDataValue()][index];
    }

    @Override
    protected ItemStack getDefaultDropItem() {
        return new ItemStack(net.minecraft.world.level.block.Blocks.STONE_BRICKS);
    }

    @SuppressWarnings("unchecked")
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
        if (stack.is(ModRegistries.FLOWWEAVE_RING.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(FORMED)) return;
        if (!(level.getBlockEntity(pos) instanceof KilnBlockEntity be)) return;
        if (!be.isMaster()) return;
        if (!be.isSmelting()) return;

        for (int i = 0; i < 3; i++) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
            double y = pos.getY() + 1.5 + random.nextDouble() * 0.3;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z,
                    (random.nextDouble() - 0.5) * 0.01, 0.07, (random.nextDouble() - 0.5) * 0.01);
        }

        if (random.nextInt(3) == 0) {
            Direction facing = state.getValue(FACING);
            double x = pos.getX() + 0.5 + facing.getStepX() * 0.6;
            double y = pos.getY() - 0.3;
            double z = pos.getZ() + 0.5 + facing.getStepZ() * 0.6;
            level.addParticle(ParticleTypes.SMALL_FLAME, x, y, z, 0, 0.01, 0);
        }

        if (random.nextInt(4) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }
    }
}
