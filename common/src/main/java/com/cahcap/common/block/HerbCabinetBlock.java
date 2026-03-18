package com.cahcap.common.block;

import com.cahcap.common.blockentity.HerbCabinetBlockEntity;
import com.cahcap.common.registry.ModRegistries;
import com.cahcap.common.util.MultiblockShapes;
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

public class HerbCabinetBlock extends MultiblockPartBlock {

    public static final MapCodec<HerbCabinetBlock> CODEC = simpleCodec(HerbCabinetBlock::new);

    private static final MultiblockShapes SHAPES = MultiblockShapes.load("/assets/herbalcurative/voxelshapes/herb_cabinet.json");
    public static final IntegerProperty POSITION = IntegerProperty.create("position", 0, SHAPES.totalPositions() - 1);

    public HerbCabinetBlock(Properties properties) {
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
        return new HerbCabinetBlockEntity(pos, state);
    }

    @Override
    protected MultiblockShapes getMultiblockShapes() { return SHAPES; }

    @Override
    protected VoxelShape getMultiblockShape(Direction facing, int[] offset, boolean mirrored) {
        if (offset == null) return Shapes.block();
        return SHAPES.get(facing, offset, mirrored);
    }

    @Override
    protected ItemStack getDefaultDropItem() {
        return new ItemStack(ModRegistries.RED_CHERRY_LOG.get());
    }

    // ==================== Interaction ====================

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hand == InteractionHand.OFF_HAND) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (hitResult.getDirection() != state.getValue(FACING)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (stack.is(ModRegistries.HERB_BOX.get())) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;

        if (level.getBlockEntity(pos) instanceof HerbCabinetBlockEntity be) {
            if (!be.formed) return ItemInteractionResult.SUCCESS;

            boolean isDouble = be.isDoubleClick(player.getUUID());
            int totalAdded = 0;

            if (isDouble && (stack.isEmpty() || !HerbCabinetBlockEntity.isHerb(stack.getItem()))) {
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack invStack = player.getInventory().getItem(i);
                    if (!invStack.isEmpty() && HerbCabinetBlockEntity.isHerb(invStack.getItem())) {
                        int added = be.addHerb(invStack.getItem(), invStack.getCount());
                        invStack.shrink(added);
                        totalAdded += added;
                        if (invStack.isEmpty()) player.getInventory().setItem(i, ItemStack.EMPTY);
                    }
                }
            } else if (!stack.isEmpty() && HerbCabinetBlockEntity.isHerb(stack.getItem())) {
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
     * In survival mode the block enters "breaking" state normally; subsequent
     * ticks go through continueDestroyBlock which does NOT call attack() again,
     * so extraction only happens once per click.
     */
    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return;
        if (!(level.getBlockEntity(pos) instanceof HerbCabinetBlockEntity be) || !be.formed) return;

        HitResult hitResult = player.pick(player.blockInteractionRange(), 0.0F, false);
        if (!(hitResult instanceof BlockHitResult blockHit) || blockHit.getDirection() != state.getValue(FACING)) return;

        int herbIndex = be.getHerbIndexForBlock();
        if (herbIndex < 0 || herbIndex >= 6) return;

        Item herb = HerbCabinetBlockEntity.getAllHerbItems()[herbIndex];
        int amount = player.isShiftKeyDown() ? 64 : 1;
        int removed = be.removeHerb(herb, amount);

        if (removed > 0) {
            ItemStack extractedStack = new ItemStack(herb, removed);
            if (!player.getInventory().add(extractedStack)) player.drop(extractedStack, false);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                    ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }
    }

    /**
     * Check if the left-click targets the front face of a formed cabinet.
     * Used by the creative-mode event handler.
     */
    public boolean isFrontFaceClick(BlockState state, Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof HerbCabinetBlockEntity be) || !be.formed) return false;

        HitResult hitResult = player.pick(player.blockInteractionRange(), 0.0F, false);
        return hitResult instanceof BlockHitResult blockHit && blockHit.getDirection() == state.getValue(FACING);
    }
}
