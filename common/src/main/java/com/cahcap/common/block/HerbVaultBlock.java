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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class HerbVaultBlock extends BaseEntityBlock {

    public static final MapCodec<HerbVaultBlock> CODEC = simpleCodec(HerbVaultBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final BooleanProperty IS_MASTER = BooleanProperty.create("is_master");

    // Per-block collision shapes (27 blocks, NORTH orientation)
    private static final VoxelShape[] NORTH_SHAPES = new VoxelShape[27];
    private static final VoxelShape[][] SHAPES_BY_FACING = new VoxelShape[4][27];

    static {
        // dy=-1
        NORTH_SHAPES[idx(-1,-1,-1)] = Shapes.or(Block.box(0, 0, 0, 5, 8, 5), Block.box(1, 8, 1, 4, 16, 4), Block.box(2, 0, 4, 4, 16, 16), Block.box(4, 4, 3, 16, 16, 4), Block.box(4, 0, 5, 16, 2, 16), Block.box(5, 0, 1, 16, 4, 5), Block.box(4, 4, 2, 6, 16, 3), Block.box(4, 4, 2, 16, 6, 3));
        NORTH_SHAPES[idx(-1,-1, 0)] = Shapes.or(Block.box(2, 0, 0, 4, 16, 16), Block.box(4, 0, 0, 16, 2, 16));
        NORTH_SHAPES[idx(-1,-1, 1)] = Shapes.or(Block.box(0, 0, 11, 5, 8, 16), Block.box(1, 8, 12, 4, 16, 15), Block.box(2, 0, 0, 4, 16, 12), Block.box(4, 0, 12, 16, 16, 14), Block.box(4, 0, 0, 16, 2, 12));
        NORTH_SHAPES[idx( 0,-1,-1)] = Shapes.or(Block.box(0, 4, 3, 16, 16, 4), Block.box(0, 0, 5, 16, 2, 16), Block.box(0, 0, 1, 16, 4, 5), Block.box(0, 4, 2, 3, 16, 3), Block.box(13, 4, 2, 16, 16, 3), Block.box(0, 4, 2, 16, 6, 3));
        NORTH_SHAPES[idx( 0,-1, 0)] = Block.box(0, 0, 0, 16, 2, 16);
        NORTH_SHAPES[idx( 0,-1, 1)] = Shapes.or(Block.box(0, 0, 12, 16, 16, 14), Block.box(0, 0, 0, 16, 2, 12));
        NORTH_SHAPES[idx( 1,-1,-1)] = Shapes.or(Block.box(11, 0, 0, 16, 8, 5), Block.box(12, 8, 1, 15, 16, 4), Block.box(12, 0, 4, 14, 16, 16), Block.box(0, 4, 3, 12, 16, 4), Block.box(0, 0, 5, 12, 2, 16), Block.box(0, 0, 1, 11, 4, 5), Block.box(10, 4, 2, 12, 16, 3), Block.box(0, 4, 2, 10, 6, 3));
        NORTH_SHAPES[idx( 1,-1, 0)] = Shapes.or(Block.box(12, 0, 0, 14, 16, 16), Block.box(0, 0, 0, 12, 2, 16));
        NORTH_SHAPES[idx( 1,-1, 1)] = Shapes.or(Block.box(11, 0, 11, 16, 8, 16), Block.box(12, 8, 12, 15, 16, 15), Block.box(12, 0, 0, 14, 16, 12), Block.box(0, 0, 12, 12, 16, 14), Block.box(0, 0, 0, 12, 2, 12));
        // dy=0
        NORTH_SHAPES[idx(-1, 0,-1)] = Shapes.or(Block.box(1, 0, 1, 4, 14, 4), Block.box(0, 14, 0, 16, 16, 16), Block.box(2, 0, 4, 4, 14, 16), Block.box(4, 0, 3, 16, 14, 4), Block.box(4, 0, 2, 6, 12, 3), Block.box(4, 12, 2, 16, 14, 3), Block.box(4, 0, 2, 16, 2, 3));
        NORTH_SHAPES[idx(-1, 0, 0)] = Shapes.or(Block.box(0, 14, 0, 16, 16, 16), Block.box(2, 0, 0, 4, 14, 16));
        NORTH_SHAPES[idx(-1, 0, 1)] = Shapes.or(Block.box(1, 0, 12, 4, 14, 15), Block.box(0, 14, 0, 16, 16, 16), Block.box(2, 0, 0, 4, 14, 12), Block.box(4, 0, 12, 16, 14, 14));
        NORTH_SHAPES[idx( 0, 0,-1)] = Shapes.or(Block.box(0, 14, 0, 16, 16, 16), Block.box(0, 0, 3, 16, 14, 4), Block.box(0, 0, 2, 3, 12, 3), Block.box(13, 0, 2, 16, 12, 3), Block.box(0, 12, 2, 16, 14, 3), Block.box(0, 0, 2, 16, 2, 3));
        NORTH_SHAPES[idx( 0, 0, 0)] = Block.box(0, 14, 0, 16, 16, 16);
        NORTH_SHAPES[idx( 0, 0, 1)] = Shapes.or(Block.box(0, 14, 0, 16, 16, 16), Block.box(0, 0, 12, 16, 14, 14));
        NORTH_SHAPES[idx( 1, 0,-1)] = Shapes.or(Block.box(12, 0, 1, 15, 14, 4), Block.box(0, 14, 0, 16, 16, 16), Block.box(12, 0, 4, 14, 14, 16), Block.box(0, 0, 3, 12, 14, 4), Block.box(10, 0, 2, 12, 12, 3), Block.box(0, 12, 2, 12, 14, 3), Block.box(0, 0, 2, 10, 2, 3));
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

        // Precompute rotated shapes
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

    private static VoxelShape rotateShape(VoxelShape shape, Direction to) {
        VoxelShape[] buffer = new VoxelShape[]{Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double x1 = minX * 16, y1 = minY * 16, z1 = minZ * 16;
            double x2 = maxX * 16, y2 = maxY * 16, z2 = maxZ * 16;
            double nx1, nz1, nx2, nz2;
            switch (to) {
                case SOUTH -> { nx1 = 16 - x2; nz1 = 16 - z2; nx2 = 16 - x1; nz2 = 16 - z1; }
                case WEST -> { nx1 = z1; nz1 = 16 - x2; nx2 = z2; nz2 = 16 - x1; }
                case EAST -> { nx1 = 16 - z2; nz1 = x1; nx2 = 16 - z1; nz2 = x2; }
                default -> { nx1 = x1; nz1 = z1; nx2 = x2; nz2 = z2; }
            }
            buffer[0] = Shapes.or(buffer[0], Block.box(
                    Math.min(nx1, nx2), y1, Math.min(nz1, nz2),
                    Math.max(nx1, nx2), y2, Math.max(nz1, nz2)));
        });
        return buffer[0];
    }

    public HerbVaultBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FORMED, false)
                .setValue(IS_MASTER, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORMED, IS_MASTER);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HerbVaultBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(FORMED)) return Shapes.block();
        if (level.getBlockEntity(pos) instanceof HerbVaultBlockEntity be) {
            BlockPos masterPos = be.getMasterPos();
            if (masterPos != null) return getShapeForPosition(pos, masterPos, be.facing);
        }
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(FORMED)) return Shapes.block();
        if (level.getBlockEntity(pos) instanceof HerbVaultBlockEntity be) {
            BlockPos masterPos = be.getMasterPos();
            if (masterPos != null) return getShapeForPosition(pos, masterPos, be.facing);
        }
        return Shapes.empty();
    }

    private VoxelShape getShapeForPosition(BlockPos targetPos, BlockPos masterPos, Direction facing) {
        int worldDx = targetPos.getX() - masterPos.getX();
        int dy = targetPos.getY() - masterPos.getY();
        int worldDz = targetPos.getZ() - masterPos.getZ();

        // Inverse-rotate world (dx,dz) back to NORTH-model space
        int modelDx, modelDz;
        switch (facing) {
            case SOUTH -> { modelDx = -worldDx; modelDz = -worldDz; }
            case EAST  -> { modelDx = worldDz; modelDz = -worldDx; }
            case WEST  -> { modelDx = -worldDz; modelDz = worldDx; }
            default    -> { modelDx = worldDx; modelDz = worldDz; }
        }

        if (modelDx < -1 || modelDx > 1 || dy < -1 || dy > 1 || modelDz < -1 || modelDz > 1) return Shapes.block();
        int index = idx(modelDx, dy, modelDz);
        return SHAPES_BY_FACING[facing.get2DDataValue()][index];
    }


    @Override
    protected boolean isOcclusionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) { return false; }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) { return false; }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(FORMED);
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(FORMED) ? 0 : super.getLightBlock(state, level, pos);
    }

    // ==================== Interaction (same as HerbCabinetBlock) ====================

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hand == InteractionHand.OFF_HAND) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (hitResult.getDirection() != state.getValue(FACING)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
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

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        handleHerbExtraction(level, pos, player, state);
    }

    private void handleHerbExtraction(Level level, BlockPos pos, Player player, BlockState state) {
        if (level.isClientSide) return;
        if (!(level.getBlockEntity(pos) instanceof HerbVaultBlockEntity be) || !be.formed) return;

        HitResult hitResult = player.pick(player.blockInteractionRange(), 0.0F, false);
        if (!(hitResult instanceof BlockHitResult blockHit) || blockHit.getDirection() != state.getValue(FACING)) return;

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

    public boolean handleBlockDestruction(BlockState state, Level level, BlockPos pos, Player player, FluidState fluid) {
        if (level.getBlockEntity(pos) instanceof HerbVaultBlockEntity be && be.formed) {
            HitResult hitResult = player.pick(player.blockInteractionRange(), 0.0F, false);
            if (hitResult instanceof BlockHitResult blockHit && blockHit.getDirection() == state.getValue(FACING)) {
                if (player.isCreative()) handleHerbExtraction(level, pos, player, state);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof HerbVaultBlockEntity be) {
                if (!be.suppressDrops && be.formed) be.disassemble();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof HerbVaultBlockEntity vault) {
            BlockPos masterPos = vault.getMasterPos();
            if (masterPos != null) {
                return vault.getOriginalItemForPosition(pos, masterPos);
            }
        }
        return new ItemStack(ModRegistries.LUMISTONE_BRICKS.get());
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof HerbVaultBlockEntity vault && vault.suppressDrops) return Collections.emptyList();
        if (be instanceof HerbVaultBlockEntity vault && vault.formed) {
            BlockPos masterPos = vault.getMasterPos();
            if (masterPos != null) {
                return Collections.singletonList(vault.getOriginalItemForPosition(vault.getBlockPos(), masterPos));
            }
        }
        return Collections.singletonList(new ItemStack(ModRegistries.LUMISTONE_BRICKS.get()));
    }
}
