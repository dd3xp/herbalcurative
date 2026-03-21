package com.cahcap.common.block;

import com.cahcap.common.blockentity.KilnBlockEntity;
import com.cahcap.common.registry.ModRegistries;
import com.cahcap.common.util.MultiblockShapes;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Kiln Block - A 3x3x3 multiblock structure for smelting.
 */
public class KilnBlock extends MultiblockPartBlock {

    public static final MapCodec<KilnBlock> CODEC = simpleCodec(KilnBlock::new);

    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    private static final MultiblockShapes SHAPES = MultiblockShapes.load("/assets/herbalcurative/voxelshapes/kiln.json");
    public static final IntegerProperty POSITION = IntegerProperty.create("position", 0, SHAPES.totalPositions() - 1);

    public KilnBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LIT, false).setValue(POSITION, 0));
    }

    @Override
    public IntegerProperty getPositionProperty() { return POSITION; }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT, POSITION);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new KilnBlockEntity(pos, state);
    }

    @Override
    protected MultiblockShapes getMultiblockShapes() { return SHAPES; }

    @Override
    protected VoxelShape getMultiblockShape(Direction facing, int[] offset, boolean mirrored) {
        return SHAPES.get(facing, offset, mirrored);
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
