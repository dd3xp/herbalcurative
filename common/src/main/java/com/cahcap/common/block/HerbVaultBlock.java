package com.cahcap.common.block;

import com.cahcap.common.blockentity.HerbCabinetBlockEntity;
import com.cahcap.common.blockentity.HerbVaultBlockEntity;
import com.cahcap.common.registry.ModRegistries;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class HerbVaultBlock extends MultiblockPartBlock {

    public static final MapCodec<HerbVaultBlock> CODEC = simpleCodec(HerbVaultBlock::new);

    private static final VoxelShape[] NORTH_SHAPES = new VoxelShape[27];
    private static final VoxelShape[][] SHAPES_BY_FACING;
    private static final VoxelShape[][] SHAPES_BY_FACING_MIRRORED;

    static {
        // dy=-1
        NORTH_SHAPES[idx(-1,-1,-1)] = Shapes.or(Block.box(0, 0, 0, 5, 8, 5), Block.box(1, 8, 1, 4, 16, 4), Block.box(2, 4, 4, 4, 16, 16), Block.box(4, 4, 3, 16, 16, 4), Block.box(5, 0, 5, 16, 4, 16), Block.box(5, 0, 1, 16, 4, 5), Block.box(1, 0, 5, 5, 4, 16), Block.box(4, 4, 2, 6, 16, 3), Block.box(4, 4, 2, 16, 6, 3));
        NORTH_SHAPES[idx(-1,-1, 0)] = Shapes.or(Block.box(2, 4, 0, 4, 16, 16), Block.box(5, 0, 0, 16, 4, 16), Block.box(1, 0, 0, 5, 4, 16));
        NORTH_SHAPES[idx(-1,-1, 1)] = Shapes.or(Block.box(0, 0, 11, 5, 8, 16), Block.box(1, 8, 12, 4, 16, 15), Block.box(2, 4, 0, 4, 16, 12), Block.box(4, 4, 12, 16, 16, 14), Block.box(5, 0, 0, 16, 4, 11), Block.box(5, 0, 11, 16, 4, 15), Block.box(1, 0, 0, 5, 4, 11));
        NORTH_SHAPES[idx( 0,-1,-1)] = Shapes.or(Block.box(0, 4, 3, 16, 16, 4), Block.box(0, 0, 5, 16, 4, 16), Block.box(0, 0, 1, 16, 4, 5), Block.box(0, 4, 2, 3, 16, 3), Block.box(13, 4, 2, 16, 16, 3), Block.box(0, 4, 2, 16, 6, 3));
        NORTH_SHAPES[idx( 0,-1, 0)] = Block.box(0, 0, 0, 16, 4, 16);
        NORTH_SHAPES[idx( 0,-1, 1)] = Shapes.or(Block.box(0, 4, 12, 16, 16, 14), Block.box(0, 0, 0, 16, 4, 11), Block.box(0, 0, 11, 16, 4, 15));
        NORTH_SHAPES[idx( 1,-1,-1)] = Shapes.or(Block.box(11, 0, 0, 16, 8, 5), Block.box(12, 8, 1, 15, 16, 4), Block.box(12, 4, 4, 14, 16, 16), Block.box(0, 4, 3, 12, 16, 4), Block.box(0, 0, 5, 11, 4, 16), Block.box(0, 0, 1, 11, 4, 5), Block.box(11, 0, 5, 15, 4, 16), Block.box(10, 4, 2, 12, 16, 3), Block.box(0, 4, 2, 12, 6, 3));
        NORTH_SHAPES[idx( 1,-1, 0)] = Shapes.or(Block.box(12, 4, 0, 14, 16, 16), Block.box(0, 0, 0, 11, 4, 16), Block.box(11, 0, 0, 15, 4, 16));
        NORTH_SHAPES[idx( 1,-1, 1)] = Shapes.or(Block.box(11, 0, 11, 16, 8, 16), Block.box(12, 8, 12, 15, 16, 15), Block.box(12, 4, 0, 14, 16, 12), Block.box(0, 4, 12, 12, 16, 14), Block.box(0, 0, 0, 11, 4, 11), Block.box(0, 0, 11, 11, 4, 15), Block.box(11, 0, 0, 15, 4, 11));
        // dy=0
        NORTH_SHAPES[idx(-1, 0,-1)] = Shapes.or(Block.box(1, 0, 1, 4, 14, 4), Block.box(0, 14, 0, 16, 16, 16), Block.box(2, 0, 4, 4, 14, 16), Block.box(4, 0, 3, 16, 14, 4), Block.box(4, 0, 2, 6, 12, 3), Block.box(4, 12, 2, 16, 14, 3), Block.box(4, 0, 2, 16, 2, 3));
        NORTH_SHAPES[idx(-1, 0, 0)] = Shapes.or(Block.box(0, 14, 0, 16, 16, 16), Block.box(2, 0, 0, 4, 14, 16));
        NORTH_SHAPES[idx(-1, 0, 1)] = Shapes.or(Block.box(1, 0, 12, 4, 14, 15), Block.box(0, 14, 0, 16, 16, 16), Block.box(2, 0, 0, 4, 14, 12), Block.box(4, 0, 12, 16, 14, 14));
        NORTH_SHAPES[idx( 0, 0,-1)] = Shapes.or(Block.box(0, 14, 0, 16, 16, 16), Block.box(0, 0, 3, 16, 14, 4), Block.box(0, 0, 2, 3, 14, 3), Block.box(13, 0, 2, 16, 14, 3), Block.box(0, 12, 2, 16, 14, 3), Block.box(0, 0, 2, 16, 2, 3));
        NORTH_SHAPES[idx( 0, 0, 0)] = Block.box(0, 14, 0, 16, 16, 16);
        NORTH_SHAPES[idx( 0, 0, 1)] = Shapes.or(Block.box(0, 14, 0, 16, 16, 16), Block.box(0, 0, 12, 16, 14, 14));
        NORTH_SHAPES[idx( 1, 0,-1)] = Shapes.or(Block.box(12, 0, 1, 15, 14, 4), Block.box(0, 14, 0, 16, 16, 16), Block.box(12, 0, 4, 14, 14, 16), Block.box(0, 0, 3, 12, 14, 4), Block.box(10, 0, 2, 12, 14, 3), Block.box(0, 12, 2, 12, 14, 3), Block.box(0, 0, 2, 12, 2, 3));
        NORTH_SHAPES[idx( 1, 0, 0)] = Shapes.or(Block.box(0, 14, 0, 16, 16, 16), Block.box(12, 0, 0, 14, 14, 16));
        NORTH_SHAPES[idx( 1, 0, 1)] = Shapes.or(Block.box(12, 0, 12, 15, 14, 15), Block.box(0, 14, 0, 16, 16, 16), Block.box(12, 0, 0, 14, 14, 12), Block.box(0, 0, 12, 12, 14, 14));
        // dy=1
        NORTH_SHAPES[idx(-1, 1,-1)] = Shapes.or(Block.box(0, 0, 0, 16, 3, 16), Block.box(4, 3, 4, 16, 8, 16));
        NORTH_SHAPES[idx(-1, 1, 0)] = Shapes.or(Block.box(0, 0, 0, 16, 3, 16), Block.box(4, 3, 0, 16, 8, 16));
        NORTH_SHAPES[idx(-1, 1, 1)] = Shapes.or(Block.box(0, 0, 0, 16, 3, 16), Block.box(4, 3, 0, 16, 8, 12));
        NORTH_SHAPES[idx( 0, 1,-1)] = Shapes.or(Block.box(0, 0, 0, 16, 3, 16), Block.box(0, 3, 4, 16, 8, 16));
        NORTH_SHAPES[idx( 0, 1, 0)] = Shapes.or(Block.box(0, 0, 0, 16, 3, 16), Block.box(0, 3, 0, 16, 8, 16));
        NORTH_SHAPES[idx( 0, 1, 1)] = Shapes.or(Block.box(0, 0, 0, 16, 3, 16), Block.box(0, 3, 0, 16, 8, 12));
        NORTH_SHAPES[idx( 1, 1,-1)] = Shapes.or(Block.box(0, 0, 0, 16, 3, 16), Block.box(0, 3, 4, 12, 8, 16));
        NORTH_SHAPES[idx( 1, 1, 0)] = Shapes.or(Block.box(0, 0, 0, 16, 3, 16), Block.box(0, 3, 0, 12, 8, 16));
        NORTH_SHAPES[idx( 1, 1, 1)] = Shapes.or(Block.box(0, 0, 0, 16, 3, 16), Block.box(0, 3, 0, 12, 8, 12));

        SHAPES_BY_FACING = precomputeRotatedShapes(NORTH_SHAPES);
        SHAPES_BY_FACING_MIRRORED = precomputeMirroredShapes(NORTH_SHAPES, i -> {
            int dy = (i / 9) - 1, dx = ((i % 9) / 3) - 1, dz = (i % 3) - 1;
            return idx(-dx, dy, dz);
        });
    }

    private static int idx(int dx, int dy, int dz) {
        return (dy + 1) * 9 + (dx + 1) * 3 + (dz + 1);
    }

    public HerbVaultBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HerbVaultBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getMultiblockShape(Direction facing, int[] offset, boolean mirrored) {
        int[] model = worldToModelOffset(facing, offset);
        int modelDx = model[0], dy = model[1], modelDz = model[2];

        if (modelDx < -1 || modelDx > 1 || dy < -1 || dy > 1 || modelDz < -1 || modelDz > 1) return Shapes.block();
        int index = idx(modelDx, dy, modelDz);
        VoxelShape[][] table = mirrored ? SHAPES_BY_FACING_MIRRORED : SHAPES_BY_FACING;
        return table[facing.get2DDataValue()][index];
    }

    @Override
    protected ItemStack getDefaultDropItem() {
        return new ItemStack(ModRegistries.LUMISTONE_BRICKS.get());
    }

    /** Returns true if the block at pos is on the front row (forwardOffset==1, dy==0). */
    public static boolean isFrontRow(HerbVaultBlockEntity be) {
        Direction facing = be.facing;
        int[] off = be.offset;
        int fwd = facing.getStepX() * off[0] + facing.getStepZ() * off[2];
        return fwd == 1 && off[1] <= 0;
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

        if (level.getBlockEntity(pos) instanceof HerbVaultBlockEntity be && be.formed) {
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
     */
    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return;
        if (!(level.getBlockEntity(pos) instanceof HerbVaultBlockEntity be) || !be.formed) return;

        HitResult hitResult = player.pick(player.blockInteractionRange(), 0.0F, false);
        if (!(hitResult instanceof BlockHitResult blockHit) || blockHit.getDirection() != state.getValue(FACING)) return;
        if (!isFrontRow(be)) return;

        int herbIndex = be.getHerbIndexForBlock();
        if (herbIndex < 0 || herbIndex >= 6) return;

        Item herb = HerbCabinetBlockEntity.getAllHerbItems()[herbIndex];
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
        if (!(level.getBlockEntity(pos) instanceof HerbVaultBlockEntity be) || !be.formed) return false;
        if (!isFrontRow(be)) return false;

        HitResult hitResult = player.pick(player.blockInteractionRange(), 0.0F, false);
        return hitResult instanceof BlockHitResult blockHit && blockHit.getDirection() == state.getValue(FACING);
    }
}
