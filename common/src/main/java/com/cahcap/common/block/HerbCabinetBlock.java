package com.cahcap.common.block;

import com.cahcap.common.blockentity.HerbCabinetBlockEntity;
import com.cahcap.common.multiblock.Multiblock;
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
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.material.PushReaction;
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

public class HerbCabinetBlock extends BaseEntityBlock {
    
    // Per-block collision/selection shapes from Blockbench model (voxel.py --per-block)
    // Herb Cabinet is 3x2x1 (WxHxD), facing NORTH by default
    // posInMultiblock layout: [3][4][5] top, [0][1][2] bottom, [1] is master
    
    // NORTH-facing shapes (model default orientation)
    private static final VoxelShape SHAPE_N_0 = Shapes.or(Block.box(2, 2, 1, 4, 16, 2), Block.box(4, 2, 1, 16, 4, 2), Block.box(4, 15, 1, 16, 16, 2), Block.box(2, 2, 2, 16, 16, 3), Block.box(1, 2, 2, 2, 16, 14), Block.box(0, 2, 0, 2, 16, 2), Block.box(0, 2, 14, 2, 16, 16), Block.box(2, 2, 14, 16, 16, 15), Block.box(0, 0, 0, 16, 2, 16));
    private static final VoxelShape SHAPE_N_1 = Shapes.or(Block.box(0, 2, 1, 16, 4, 2), Block.box(0, 15, 1, 16, 16, 2), Block.box(14, 4, 1, 16, 16, 2), Block.box(0, 4, 1, 2, 16, 2), Block.box(0, 2, 2, 16, 16, 3), Block.box(0, 2, 14, 16, 16, 15), Block.box(0, 0, 0, 16, 2, 16));
    private static final VoxelShape SHAPE_N_2 = Shapes.or(Block.box(12, 2, 1, 14, 16, 2), Block.box(0, 2, 1, 12, 4, 2), Block.box(0, 15, 1, 12, 16, 2), Block.box(0, 2, 2, 14, 16, 3), Block.box(14, 2, 0, 16, 16, 2), Block.box(14, 2, 2, 15, 16, 14), Block.box(14, 2, 14, 16, 16, 16), Block.box(0, 2, 14, 14, 16, 15), Block.box(0, 0, 0, 16, 2, 16));
    private static final VoxelShape SHAPE_N_3 = Shapes.or(Block.box(2, 0, 1, 4, 14, 2), Block.box(4, 12, 1, 16, 14, 2), Block.box(4, 0, 1, 16, 1, 2), Block.box(2, 0, 2, 16, 14, 3), Block.box(1, 0, 2, 2, 14, 14), Block.box(0, 0, 0, 2, 14, 2), Block.box(0, 0, 14, 2, 14, 16), Block.box(2, 0, 14, 16, 14, 15), Block.box(0, 14, 0, 16, 16, 16));
    private static final VoxelShape SHAPE_N_4 = Shapes.or(Block.box(0, 12, 1, 16, 14, 2), Block.box(0, 0, 1, 16, 1, 2), Block.box(14, 0, 1, 16, 12, 2), Block.box(0, 0, 1, 2, 12, 2), Block.box(0, 0, 2, 16, 14, 3), Block.box(0, 0, 14, 16, 14, 15), Block.box(0, 14, 0, 16, 16, 16));
    private static final VoxelShape SHAPE_N_5 = Shapes.or(Block.box(12, 0, 1, 14, 14, 2), Block.box(0, 12, 1, 12, 14, 2), Block.box(0, 0, 1, 12, 1, 2), Block.box(0, 0, 2, 14, 14, 3), Block.box(14, 0, 0, 16, 14, 2), Block.box(14, 0, 2, 15, 14, 14), Block.box(14, 0, 14, 16, 14, 16), Block.box(0, 0, 14, 14, 14, 15), Block.box(0, 14, 0, 16, 16, 16));
    
    // Precomputed rotated shapes for all 4 directions
    private static final VoxelShape[][] SHAPES_BY_FACING = new VoxelShape[4][6];
    
    static {
        VoxelShape[] northShapes = {SHAPE_N_0, SHAPE_N_1, SHAPE_N_2, SHAPE_N_3, SHAPE_N_4, SHAPE_N_5};
        for (int i = 0; i < 6; i++) {
            SHAPES_BY_FACING[Direction.NORTH.get2DDataValue()][i] = northShapes[i];
            SHAPES_BY_FACING[Direction.SOUTH.get2DDataValue()][i] = rotateShape(northShapes[i], Direction.SOUTH);
            SHAPES_BY_FACING[Direction.WEST.get2DDataValue()][i] = rotateShape(northShapes[i], Direction.WEST);
            SHAPES_BY_FACING[Direction.EAST.get2DDataValue()][i] = rotateShape(northShapes[i], Direction.EAST);
        }
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
    
    public static final MapCodec<HerbCabinetBlock> CODEC = simpleCodec(HerbCabinetBlock::new);
    
    public static final DirectionProperty FACING = Multiblock.FACING;
    public static final BooleanProperty FORMED = Multiblock.FORMED;
    public static final BooleanProperty IS_MASTER = Multiblock.IS_MASTER;
    
    public HerbCabinetBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FORMED, false)
                .setValue(IS_MASTER, false));
    }
    
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORMED, IS_MASTER);
    }
    
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HerbCabinetBlockEntity(pos, state);
    }
    
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        // Use MODEL to render JSON model, BlockEntityRenderer will add herb icons on top
        return RenderShape.MODEL;
    }
    
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(FORMED)) {
            return Shapes.block();
        }
        if (level.getBlockEntity(pos) instanceof HerbCabinetBlockEntity be && be.formed) {
            return getShapeForPosition(state.getValue(FACING), be.posInMultiblock);
        }
        // BE not ready (chunk loading race): return empty to avoid suffocation/collision issues
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(FORMED)) {
            return Shapes.block();
        }
        if (level.getBlockEntity(pos) instanceof HerbCabinetBlockEntity be && be.formed) {
            return getShapeForPosition(state.getValue(FACING), be.posInMultiblock);
        }
        // BE not ready (chunk loading race): return empty to avoid suffocation/collision issues
        return Shapes.empty();
    }
    
    /**
     * Get the collision/selection shape for a position based on facing and posInMultiblock.
     * Layout (3x2, posInMultiblock = h * 3 + w):
     * [3][4][5]  <- Top row (h=1)
     * [0][1][2]  <- Bottom row (h=0), [1] is master
     */
    private VoxelShape getShapeForPosition(Direction facing, int posInMultiblock) {
        if (posInMultiblock < 0 || posInMultiblock >= 6) {
            return Shapes.block();
        }
        return SHAPES_BY_FACING[facing.get2DDataValue()][posInMultiblock];
    }


    @Override
    protected boolean isOcclusionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }
    
    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        // Don't use block shape for light occlusion since model extends beyond block bounds
        return false;
    }
    
    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        // Formed multiblock should let light through (model renders separately)
        if (state.getValue(FORMED)) {
            return true;
        }
        return false;
    }
    
    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        // Formed multiblock should not block light
        if (state.getValue(FORMED)) {
            return 0;
        }
        return super.getLightBlock(state, level, pos);
    }
    
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, 
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hand == InteractionHand.OFF_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        
        // Check if clicking on front face
        if (hitResult.getDirection() != state.getValue(FACING)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        
        // Check if player is holding herb box
        if (stack.is(ModRegistries.HERB_BOX.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        
        // Server-side logic
        if (level.getBlockEntity(pos) instanceof HerbCabinetBlockEntity be) {
            if (!be.formed) {
                return ItemInteractionResult.SUCCESS;
            }
            
            boolean isDouble = be.isDoubleClick(player.getUUID());
            int totalAdded = 0;
            
            if (isDouble && (stack.isEmpty() || !HerbCabinetBlockEntity.isHerb(stack.getItem()))) {
                // Double-click with empty hand or non-herb item: add ALL herbs from inventory
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack invStack = player.getInventory().getItem(i);
                    if (!invStack.isEmpty() && HerbCabinetBlockEntity.isHerb(invStack.getItem())) {
                        int added = be.addHerb(invStack.getItem(), invStack.getCount());
                        invStack.shrink(added);
                        totalAdded += added;
                        if (invStack.isEmpty()) {
                            player.getInventory().setItem(i, ItemStack.EMPTY);
                        }
                    }
                }
            } else if (!stack.isEmpty() && HerbCabinetBlockEntity.isHerb(stack.getItem())) {
                // Single click (or double-click) with herb: add held stack only
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
        if (level.isClientSide) {
            return;
        }
        
        if (!(level.getBlockEntity(pos) instanceof HerbCabinetBlockEntity be) || !be.formed) {
            return;
        }
        
        // Only handle clicks on front face
        HitResult hitResult = player.pick(player.blockInteractionRange(), 0.0F, false);
        if (!(hitResult instanceof BlockHitResult blockHit) || blockHit.getDirection() != state.getValue(FACING)) {
            return;
        }
        
        int herbIndex = be.getHerbIndexForBlock();
        if (herbIndex < 0 || herbIndex >= 6) {
            return;
        }
        
        Item herb = HerbCabinetBlockEntity.getAllHerbItems()[herbIndex];
        
        int amount = player.isShiftKeyDown() ? 64 : 1;
        int removed = be.removeHerb(herb, amount);
        
        if (removed > 0) {
            ItemStack extractedStack = new ItemStack(herb, removed);
            
            if (!player.getInventory().add(extractedStack)) {
                player.drop(extractedStack, false);
            }
            
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                    ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }
    }
    
    /**
     * Handle block destruction logic for formed multiblock
     * Called by NeoForge's onDestroyedByPlayer override
     * Returns true if destruction should proceed, false to cancel
     */
    public boolean handleBlockDestruction(BlockState state, Level level, BlockPos pos, Player player, FluidState fluid) {
        // Check if this is a formed multiblock
        if (level.getBlockEntity(pos) instanceof HerbCabinetBlockEntity be && be.formed) {
            HitResult hitResult = player.pick(player.blockInteractionRange(), 0.0F, false);
            if (hitResult instanceof BlockHitResult blockHit && blockHit.getDirection() == state.getValue(FACING)) {
                // This is the front face - prevent breaking
                // In creative mode, extract herbs instead of breaking
                if (player.isCreative()) {
                    handleHerbExtraction(level, pos, player, state);
                }
                return false; // Return false to prevent block removal
            }
        }
        return true; // Allow destruction
    }
    
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof HerbCabinetBlockEntity be) {
                if (!be.suppressDrops && be.formed) {
                    be.disassemble();
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
    
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        // Return the forest heartwood log (construction material) for creative pick block
        // Note: Jade icon is overridden by HerbCabinetIconProvider to show herb cabinet item
        return new ItemStack(ModRegistries.RED_CHERRY_LOG.get());
    }
    
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        // Check if this block should drop (suppressDrops flag)
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof HerbCabinetBlockEntity cabinet) {
            if (cabinet.suppressDrops) {
                return Collections.emptyList();
            }
        }
        // Drop forest heartwood log instead of herb cabinet
        return Collections.singletonList(new ItemStack(ModRegistries.RED_CHERRY_LOG.get()));
    }
    
}

