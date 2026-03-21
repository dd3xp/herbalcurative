package com.cahcap.common.block;

import com.cahcap.common.blockentity.ObeliskBlockEntity;
import com.cahcap.common.recipe.ObeliskOfferingRecipe;
import com.cahcap.common.registry.ModRegistries;
import com.cahcap.common.util.MultiblockShapes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Obelisk Block - A 3x3x3 multiblock structure for offering food to summon mobs.
 */
public class ObeliskBlock extends MultiblockPartBlock {

    public static final MapCodec<ObeliskBlock> CODEC = simpleCodec(ObeliskBlock::new);

    private static final MultiblockShapes SHAPES = MultiblockShapes.load("/assets/herbalcurative/voxelshapes/obelisk.json");
    public static final IntegerProperty POSITION = IntegerProperty.create("position", 0, 26); // 3x3x3 = 27 positions

    public ObeliskBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(POSITION, 0));
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
        builder.add(POSITION);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ObeliskBlockEntity(pos, state);
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
            return createTickerHelper(type, (BlockEntityType<ObeliskBlockEntity>) ModRegistries.OBELISK_BE.get(),
                    ObeliskBlockEntity::serverTick);
        }
        return null;
    }

    /**
     * Check if the clicked block is the offering pedestal position.
     */
    private boolean isOfferingPedestal(BlockPos pos, ObeliskBlockEntity master) {
        BlockPos pedestalPos = master.getOfferingTablePos();
        return pedestalPos != null && pedestalPos.equals(pos);
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

        if (stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!(level.getBlockEntity(pos) instanceof ObeliskBlockEntity be)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        ObeliskBlockEntity master = be.getMaster();
        if (master == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Only the pedestal block accepts offering interaction
        if (!isOfferingPedestal(pos, master)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        // If already offering, do nothing
        if (master.isOffering()) {
            return ItemInteractionResult.CONSUME;
        }

        // Try to place offering
        if (!stack.isEmpty()) {
            ObeliskOfferingRecipe recipe = master.findRecipe(stack);
            if (recipe != null) {
                boolean isCreative = player.getAbilities().instabuild;
                master.startOffering(stack, recipe, isCreative);
                level.playSound(null, master.getBlockPos(), SoundEvents.ENCHANTMENT_TABLE_USE,
                        SoundSource.BLOCKS, 1.0f, 1.0f);
                return ItemInteractionResult.SUCCESS;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (!state.getValue(FORMED) || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (!(level.getBlockEntity(pos) instanceof ObeliskBlockEntity be)) {
            return InteractionResult.PASS;
        }

        ObeliskBlockEntity master = be.getMaster();
        if (master == null || !master.isOffering()) {
            return InteractionResult.PASS;
        }

        // Only the pedestal block accepts cancel interaction
        if (!isOfferingPedestal(pos, master)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack returned = master.cancelOffering();
        if (!returned.isEmpty()) {
            if (!player.getInventory().add(returned)) {
                player.drop(returned, false);
            }
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.2f);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
