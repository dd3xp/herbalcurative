package com.cahcap.common.block;

import com.cahcap.common.blockentity.CauldronBlockEntity;
import com.cahcap.common.item.PotItem;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Cauldron Block - A 3x3x2 multiblock structure for brewing potions.
 */
public class CauldronBlock extends MultiblockPartBlock {

    public static final MapCodec<CauldronBlock> CODEC = simpleCodec(CauldronBlock::new);

    // Per-block collision/selection shapes (NORTH orientation)
    // Indexed by dy * 9 + (dx+1) * 3 + (dz+1), where dy=0..1, dx/dz=-1..1
    private static final VoxelShape[] NORTH_SHAPES = new VoxelShape[18];
    private static final VoxelShape[][] SHAPES_BY_FACING;
    private static final VoxelShape[][] SHAPES_BY_FACING_MIRRORED;

    static {
        // dy=0 (bottom layer - master layer)
        NORTH_SHAPES[idx(-1, 0,-1)] = Shapes.or(Block.box(0, 8, 0, 16, 12, 16), Block.box(0, 12, 0, 16, 16, 6), Block.box(0, 12, 6, 6, 16, 16), Block.box(4, 0, 0, 8, 8, 4), Block.box(0, 0, 4, 4, 8, 8), Block.box(0, 0, 0, 4, 8, 4));
        NORTH_SHAPES[idx(-1, 0, 0)] = Shapes.or(Block.box(0, 8, 0, 16, 12, 16), Block.box(0, 12, 0, 6, 16, 16));
        NORTH_SHAPES[idx(-1, 0, 1)] = Shapes.or(Block.box(0, 8, 0, 16, 12, 16), Block.box(0, 12, 10, 16, 16, 16), Block.box(0, 12, 0, 6, 16, 10), Block.box(0, 0, 8, 4, 8, 12), Block.box(4, 0, 12, 8, 8, 16), Block.box(0, 0, 12, 4, 8, 16));
        NORTH_SHAPES[idx( 0, 0,-1)] = Shapes.or(Block.box(0, 8, 0, 16, 12, 16), Block.box(0, 12, 0, 16, 16, 6));
        NORTH_SHAPES[idx( 0, 0, 0)] = Block.box(0, 8, 0, 16, 12, 16);
        NORTH_SHAPES[idx( 0, 0, 1)] = Shapes.or(Block.box(0, 8, 0, 16, 12, 16), Block.box(0, 12, 10, 16, 16, 16));
        NORTH_SHAPES[idx( 1, 0,-1)] = Shapes.or(Block.box(0, 8, 0, 16, 12, 16), Block.box(0, 12, 0, 16, 16, 6), Block.box(10, 12, 6, 16, 16, 16), Block.box(12, 0, 4, 16, 8, 8), Block.box(12, 0, 0, 16, 8, 4), Block.box(8, 0, 0, 12, 8, 4));
        NORTH_SHAPES[idx( 1, 0, 0)] = Shapes.or(Block.box(0, 8, 0, 16, 12, 16), Block.box(10, 12, 0, 16, 16, 16));
        NORTH_SHAPES[idx( 1, 0, 1)] = Shapes.or(Block.box(0, 8, 0, 16, 12, 16), Block.box(0, 12, 10, 16, 16, 16), Block.box(10, 12, 0, 16, 16, 10), Block.box(12, 0, 8, 16, 8, 12), Block.box(12, 0, 12, 16, 8, 16), Block.box(8, 0, 12, 12, 8, 16));
        // dy=1 (top layer)
        NORTH_SHAPES[idx(-1, 1,-1)] = Shapes.or(Block.box(0, 0, 0, 16, 16, 6), Block.box(0, 0, 6, 6, 16, 16));
        NORTH_SHAPES[idx(-1, 1, 0)] = Block.box(0, 0, 0, 6, 16, 16);
        NORTH_SHAPES[idx(-1, 1, 1)] = Shapes.or(Block.box(0, 0, 10, 16, 16, 16), Block.box(0, 0, 0, 6, 16, 10));
        NORTH_SHAPES[idx( 0, 1,-1)] = Block.box(0, 0, 0, 16, 16, 6);
        NORTH_SHAPES[idx( 0, 1, 0)] = Shapes.empty();
        NORTH_SHAPES[idx( 0, 1, 1)] = Block.box(0, 0, 10, 16, 16, 16);
        NORTH_SHAPES[idx( 1, 1,-1)] = Shapes.or(Block.box(0, 0, 0, 16, 16, 6), Block.box(10, 0, 6, 16, 16, 16));
        NORTH_SHAPES[idx( 1, 1, 0)] = Block.box(10, 0, 0, 16, 16, 16);
        NORTH_SHAPES[idx( 1, 1, 1)] = Shapes.or(Block.box(0, 0, 10, 16, 16, 16), Block.box(10, 0, 0, 16, 16, 10));

        SHAPES_BY_FACING = precomputeRotatedShapes(NORTH_SHAPES);
        SHAPES_BY_FACING_MIRRORED = precomputeMirroredShapes(NORTH_SHAPES, i -> {
            int dy = i / 9, dx = ((i % 9) / 3) - 1, dz = (i % 3) - 1;
            return idx(-dx, dy, dz);
        });
    }

    private static int idx(int dx, int dy, int dz) {
        return dy * 9 + (dx + 1) * 3 + (dz + 1);
    }

    public CauldronBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CauldronBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getMultiblockShape(Direction facing, int[] offset, boolean mirrored) {
        int[] model = worldToModelOffset(facing, offset);
        int modelDx = model[0], dy = model[1], modelDz = model[2];

        if (modelDx < -1 || modelDx > 1 || dy < 0 || dy > 1 || modelDz < -1 || modelDz > 1) {
            return Shapes.block();
        }

        int index = idx(modelDx, dy, modelDz);
        VoxelShape[][] table = mirrored ? SHAPES_BY_FACING_MIRRORED : SHAPES_BY_FACING;
        return table[facing.get2DDataValue()][index];
    }

    @Override
    protected ItemStack getDefaultDropItem() {
        return new ItemStack(ModRegistries.LUMISTONE_BRICKS.get());
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide && state.getValue(FORMED)) {
            return createTickerHelper(type, (BlockEntityType<CauldronBlockEntity>) ModRegistries.CAULDRON_BE.get(),
                    CauldronBlockEntity::serverTick);
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
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        if (level.getBlockEntity(pos) instanceof CauldronBlockEntity be) {
            CauldronBlockEntity master = be.getMaster();
            if (master == null) {
                return ItemInteractionResult.SUCCESS;
            }

            if (stack.getItem() instanceof PotItem && !PotItem.isFilled(stack) && master.getFluid().isPotion() && !player.isShiftKeyDown()) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (stack.is(ModRegistries.FLOWWEAVE_RING.get())) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            if (master.hasOutputSlotItems() && !player.isShiftKeyDown()) {
                ItemStack output = master.extractFromOutputSlot();
                if (!output.isEmpty()) {
                    if (!player.getInventory().add(output)) {
                        player.drop(output, false);
                    }
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.2F);
                    return ItemInteractionResult.SUCCESS;
                }
            }

            if (stack.is(Items.WATER_BUCKET)) {
                if (master.addFluid(net.minecraft.world.level.material.Fluids.WATER, 1000)) {
                    if (!player.isCreative()) {
                        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                    }
                    level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return ItemInteractionResult.SUCCESS;
                }
            }

            if (stack.is(Items.BUCKET) && master.hasFluid() && !master.isBrewing() && !master.isInfusing()) {
                if (master.hasOutputSlotItems()) {
                    ItemStack output = master.extractFromOutputSlot();
                    if (!output.isEmpty()) {
                        if (!player.getInventory().add(output)) {
                            player.drop(output, false);
                        }
                    }
                }
                for (ItemStack material : master.getMaterials()) {
                    if (!material.isEmpty()) {
                        if (!player.getInventory().add(material.copy())) {
                            player.drop(material.copy(), false);
                        }
                    }
                }
                net.minecraft.world.level.material.Fluid extracted = master.extractFluidWithClear(1000);
                if (extracted != null && extracted == net.minecraft.world.level.material.Fluids.WATER) {
                    if (!player.isCreative()) {
                        stack.shrink(1);
                        ItemStack filledBucket = new ItemStack(Items.WATER_BUCKET);
                        if (!player.getInventory().add(filledBucket)) {
                            player.drop(filledBucket, false);
                        }
                    }
                    level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return ItemInteractionResult.SUCCESS;
                }
            }

            if (stack.isEmpty() && player.isShiftKeyDown()) {
                ItemStack output = master.getInfusingOutput();
                if (!output.isEmpty() && !master.isInfusing()) {
                    output = master.extractItem(player);
                    if (!output.isEmpty()) {
                        if (!player.getInventory().add(output)) {
                            player.drop(output, false);
                        }
                        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.2F);
                        return ItemInteractionResult.SUCCESS;
                    }
                }
                ItemStack extractedItem = master.extractItem(player);
                if (!extractedItem.isEmpty()) {
                    if (!player.getInventory().add(extractedItem)) {
                        player.drop(extractedItem, false);
                    }
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.2F);
                    return ItemInteractionResult.SUCCESS;
                }
            }
        }

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(FORMED)) return;
        if (!(level.getBlockEntity(pos) instanceof CauldronBlockEntity be)) return;
        if (!be.isMaster()) return;

        if (be.hasHeatSource() && be.hasFluid() && !be.isBrewing() && !be.isInfusing()) {
            for (int i = 0; i < 3; i++) {
                if (random.nextInt(2) == 0) {
                    double x = pos.getX() - 0.625 + random.nextDouble() * 2.25;
                    double y = pos.getY() + 0.9;
                    double z = pos.getZ() - 0.625 + random.nextDouble() * 2.25;
                    level.addParticle(ParticleTypes.SMOKE, x, y, z,
                            (random.nextDouble() - 0.5) * 0.01, 0.03, (random.nextDouble() - 0.5) * 0.01);
                }
            }
            if (random.nextInt(2) == 0) {
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
        }

        if (be.isBrewing() && be.hasHeatSource()) {
            for (int i = 0; i < 2; i++) {
                if (random.nextInt(2) == 0) {
                    double x = pos.getX() - 0.4 + random.nextDouble() * 1.8;
                    double y = pos.getY() + 1.0;
                    double z = pos.getZ() - 0.4 + random.nextDouble() * 1.8;
                    level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z,
                            (random.nextDouble() - 0.5) * 0.02, 0.05, (random.nextDouble() - 0.5) * 0.02);
                }
            }
            double liquidY = pos.getY() + (27.0 / 16.0);
            for (int i = 0; i < 8; i++) {
                double bx = pos.getX() - 0.625 + random.nextDouble() * 2.25;
                double bz = pos.getZ() - 0.625 + random.nextDouble() * 2.25;
                level.addParticle(ParticleTypes.BUBBLE_POP, bx, liquidY, bz,
                        (random.nextDouble() - 0.5) * 0.03, 0.03 + random.nextDouble() * 0.02, (random.nextDouble() - 0.5) * 0.03);
            }
            if (random.nextInt(2) == 0) {
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
        }

        if (be.isInfusing()) {
            if (random.nextInt(10) == 0) {
                double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
                double y = pos.getY() + 0.9;
                double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
                level.addParticle(ParticleTypes.ENCHANTED_HIT, x, y, z, 0, 0.02, 0);
            }
        }

        if (be.getFluid().isPotion() && !be.isInfusing()) {
            if (random.nextInt(15) == 0) {
                double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
                double y = pos.getY() + 0.9;
                double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
                level.addParticle(ParticleTypes.ENCHANTED_HIT, x, y, z, 0, 0.02, 0);
            }
        }
    }
}
