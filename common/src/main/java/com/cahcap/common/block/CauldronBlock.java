package com.cahcap.common.block;

import com.cahcap.common.blockentity.cauldron.CauldronBlockEntity;
import com.cahcap.common.item.PotItem;
import com.cahcap.common.registry.ModRegistries;
import com.cahcap.common.util.CustomVoxelShapes;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Cauldron Block - A 3x3x2 multiblock structure for brewing potions.
 */
public class CauldronBlock extends MultiblockPartBlock {

    public static final MapCodec<CauldronBlock> CODEC = simpleCodec(CauldronBlock::new);

    private static final CustomVoxelShapes SHAPES = CustomVoxelShapes.loadFromModel("/assets/herbalcurative/models/block/cauldron.json");
    public static final IntegerProperty POSITION = IntegerProperty.create("position", 0, SHAPES.totalPositions() - 1);

    public CauldronBlock(Properties properties) {
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
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CauldronBlockEntity(pos, state);
    }

    @Override
    protected CustomVoxelShapes getCustomVoxelShapes() { return SHAPES; }

    @Override
    protected VoxelShape getMultiblockShape(Direction facing, int[] offset, boolean mirrored) {
        return SHAPES.get(facing, offset, mirrored);
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

            // Empty pot on potion cauldron: fill pot
            if (stack.getItem() instanceof PotItem && !PotItem.isFilled(stack) && master.getFluid().isPotion()) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            // Filled pot on empty cauldron: pour back
            if (stack.getItem() instanceof PotItem && PotItem.isFilled(stack) && master.getFluid().isEmpty()) {
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

            ItemInteractionResult bucketResult = handleBucketInteraction(stack, master, level, pos, player, hand);
            if (bucketResult != null) {
                return bucketResult;
            }

            ItemInteractionResult emptyHandResult = handleEmptyHand(stack, master, level, pos, player);
            if (emptyHandResult != null) {
                return emptyHandResult;
            }
        }

        return ItemInteractionResult.SUCCESS;
    }

    /**
     * Handle water bucket filling and empty bucket extraction.
     * Returns non-null if the interaction was handled.
     */
    private ItemInteractionResult handleBucketInteraction(ItemStack stack, CauldronBlockEntity master,
                                                          Level level, BlockPos pos, Player player, InteractionHand hand) {
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

        return null;
    }

    /**
     * Handle shift-click with empty hand to retrieve output items.
     * Returns non-null if the interaction was handled.
     */
    private ItemInteractionResult handleEmptyHand(ItemStack stack, CauldronBlockEntity master,
                                                   Level level, BlockPos pos, Player player) {
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

        return null;
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
