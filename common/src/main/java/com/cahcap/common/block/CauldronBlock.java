package com.cahcap.common.block;

import com.cahcap.common.blockentity.CauldronBlockEntity;
import com.cahcap.common.item.PotItem;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Cauldron Block - A 3x3x2 multiblock structure for brewing potions.
 * 
 * Structure (ground placement):
 * Layer 1 (bottom, y=0):
 *   [Lumistone][ Empty ][Lumistone]
 *   [ Empty   ][ Empty ][ Empty   ]
 *   [Lumistone][ Empty ][Lumistone]
 * 
 * Layer 2 (top, y=1):
 *   [LumiStoneBricks][LumiStoneBricks][LumiStoneBricks]
 *   [LumiStoneBricks][RuneStoneSlab  ][LumiStoneBricks]
 *   [LumiStoneBricks][LumiStoneBricks][LumiStoneBricks]
 * 
 * The master block is at the center of Layer 2 (Rune Stone Slab position).
 */
public class CauldronBlock extends BaseEntityBlock {
    
    public static final MapCodec<CauldronBlock> CODEC = simpleCodec(CauldronBlock::new);
    
    public static final DirectionProperty FACING = Multiblock.FACING;
    public static final BooleanProperty FORMED = Multiblock.FORMED;
    public static final BooleanProperty IS_MASTER = Multiblock.IS_MASTER;
    
    // Per-block collision/selection shapes (NORTH orientation)
    // Indexed by dy * 9 + (dx+1) * 3 + (dz+1), where dy=0..1, dx/dz=-1..1
    private static final VoxelShape[] NORTH_SHAPES = new VoxelShape[18];
    private static final VoxelShape[][] SHAPES_BY_FACING = new VoxelShape[4][18];

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

        // Precompute rotated shapes for all facings
        for (int i = 0; i < 18; i++) {
            VoxelShape shape = NORTH_SHAPES[i];
            if (shape == null) shape = Shapes.empty();
            SHAPES_BY_FACING[Direction.NORTH.get2DDataValue()][i] = shape;
            SHAPES_BY_FACING[Direction.SOUTH.get2DDataValue()][i] = rotateShape(shape, Direction.SOUTH);
            SHAPES_BY_FACING[Direction.WEST.get2DDataValue()][i] = rotateShape(shape, Direction.WEST);
            SHAPES_BY_FACING[Direction.EAST.get2DDataValue()][i] = rotateShape(shape, Direction.EAST);
        }
    }

    private static int idx(int dx, int dy, int dz) {
        return dy * 9 + (dx + 1) * 3 + (dz + 1);
    }

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
    
    public CauldronBlock(Properties properties) {
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
        return new CauldronBlockEntity(pos, state);
    }
    
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        // Use MODEL for JSON model, BlockEntityRenderer will add liquid on top
        return RenderShape.MODEL;
    }
    
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(FORMED)) {
            return Shapes.block();
        }
        if (level.getBlockEntity(pos) instanceof CauldronBlockEntity be && be.formed) {
            return getShapeForPosition(be.facing, be.offset);
        }
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(FORMED)) {
            return Shapes.block();
        }
        if (level.getBlockEntity(pos) instanceof CauldronBlockEntity be && be.formed) {
            return getShapeForPosition(be.facing, be.offset);
        }
        return Shapes.empty();
    }
    
    private VoxelShape getShapeForPosition(Direction facing, int[] offset) {
        if (offset == null) return Shapes.block();
        int worldDx = offset[0], dy = offset[1], worldDz = offset[2];

        int modelDx, modelDz;
        switch (facing) {
            case SOUTH -> { modelDx = -worldDx; modelDz = -worldDz; }
            case EAST  -> { modelDx = worldDz; modelDz = -worldDx; }
            case WEST  -> { modelDx = -worldDz; modelDz = worldDx; }
            default    -> { modelDx = worldDx; modelDz = worldDz; }
        }

        if (modelDx < -1 || modelDx > 1 || dy < 0 || dy > 1 || modelDz < -1 || modelDz > 1) {
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
        if (state.getValue(FORMED)) {
            return true;
        }
        return false;
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
        
        // Get master block entity
        if (level.getBlockEntity(pos) instanceof CauldronBlockEntity be) {
            CauldronBlockEntity master = be.getMaster();
            if (master == null) {
                return ItemInteractionResult.SUCCESS;
            }
            
            // Empty pot on potion cauldron: pass to item so PotItem.useOn fills pot (right-click only; shift+right-click = extract materials)
            if (stack.getItem() instanceof PotItem && !PotItem.isFilled(stack) && master.getFluid().isPotion() && !player.isShiftKeyDown()) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            
            // Flowweave Ring on formed cauldron: pass to item so FlowweaveRingItem.useOn handles brewing/clearing
            if (stack.is(ModRegistries.FLOWWEAVE_RING.get())) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            
            // Extract from output slot: only when NOT shift clicking (shift + empty hand = extract materials)
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
            
            // Handle water bucket - add water
            if (stack.is(Items.WATER_BUCKET)) {
                if (master.addFluid(net.minecraft.world.level.material.Fluids.WATER, 1000)) {
                    if (!player.isCreative()) {
                        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                    }
                    level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return ItemInteractionResult.SUCCESS;
                }
            }
            
            // Handle empty bucket - extract fluid and return materials + output slot
            if (stack.is(Items.BUCKET) && master.hasFluid() && !master.isBrewing() && !master.isInfusing()) {
                // Return output slot to player first
                if (master.hasOutputSlotItems()) {
                    ItemStack output = master.extractFromOutputSlot();
                    if (!output.isEmpty()) {
                        if (!player.getInventory().add(output)) {
                            player.drop(output, false);
                        }
                    }
                }
                
                // Return materials to player
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
            
            // Shift + empty hand - try to extract
            if (stack.isEmpty() && player.isShiftKeyDown()) {
                // Try to extract infusing output first
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
                
                // Phase 1: Extract material
                ItemStack extracted = master.extractItem(player);
                if (!extracted.isEmpty()) {
                    if (!player.getInventory().add(extracted)) {
                        player.drop(extracted, false);
                    }
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.2F);
                    return ItemInteractionResult.SUCCESS;
                }
            }
        }
        
        return ItemInteractionResult.SUCCESS;
    }
    
    /**
     * Handle block destruction logic for formed multiblock.
     * Returns true if destruction should proceed, false to cancel.
     */
    public boolean handleBlockDestruction(BlockState state, Level level, BlockPos pos, Player player, FluidState fluid) {
        if (level.getBlockEntity(pos) instanceof CauldronBlockEntity be && be.formed) {
            // Allow destruction from any direction for cauldron
            return true;
        }
        return true;
    }
    
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof CauldronBlockEntity be) {
                if (!be.suppressDrops && be.formed) {
                    be.disassemble();
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
    
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof CauldronBlockEntity be && be.formed) {
            BlockPos masterPos = be.getMasterPos();
            if (masterPos != null) {
                return be.getOriginalItemForPosition(pos, masterPos);
            }
        }
        return new ItemStack(ModRegistries.LUMISTONE_BRICKS.get());
    }
    
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof CauldronBlockEntity be) {
            if (be.suppressDrops) {
                return Collections.emptyList();
            }
            if (be.formed) {
                BlockPos masterPos = be.getMasterPos();
                if (masterPos != null) {
                    return Collections.singletonList(be.getOriginalItemForPosition(be.getBlockPos(), masterPos));
                }
            }
        }
        return Collections.singletonList(new ItemStack(ModRegistries.LUMISTONE_BRICKS.get()));
    }
    
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(FORMED)) {
            return;
        }
        
        if (!(level.getBlockEntity(pos) instanceof CauldronBlockEntity be)) {
            return;
        }
        
        // Only the master block spawns particles
        if (!be.isMaster()) {
            return;
        }
        
        // Heat source with fluid (not brewing) - smoke effect to indicate heat
        if (be.hasHeatSource() && be.hasFluid() && !be.isBrewing() && !be.isInfusing()) {
            // Multiple smoke particles rising from the liquid surface
            for (int i = 0; i < 3; i++) {
                if (random.nextInt(2) == 0) {
                    double x = pos.getX() - 0.625 + random.nextDouble() * 2.25;
                    double y = pos.getY() + 0.9;
                    double z = pos.getZ() - 0.625 + random.nextDouble() * 2.25;
                    level.addParticle(ParticleTypes.SMOKE, x, y, z, 
                            (random.nextDouble() - 0.5) * 0.01, 0.03, (random.nextDouble() - 0.5) * 0.01);
                }
            }
            // Furnace fire crackle sound
            if (random.nextInt(2) == 0) {
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
        }
        
        // Brewing - campfire smoke + boiling bubbles
        if (be.isBrewing() && be.hasHeatSource()) {
            // Campfire smoke rising - boiling effect
            for (int i = 0; i < 2; i++) {
                if (random.nextInt(2) == 0) {
                    double x = pos.getX() - 0.4 + random.nextDouble() * 1.8;
                    double y = pos.getY() + 1.0;
                    double z = pos.getZ() - 0.4 + random.nextDouble() * 1.8;
                    level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z,
                            (random.nextDouble() - 0.5) * 0.02, 0.05, (random.nextDouble() - 0.5) * 0.02);
                }
            }
            // Bubble pop particles on the liquid surface (boiling effect)
            double liquidY = pos.getY() + (27.0 / 16.0);
            for (int i = 0; i < 8; i++) {
                double bx = pos.getX() - 0.625 + random.nextDouble() * 2.25;
                double bz = pos.getZ() - 0.625 + random.nextDouble() * 2.25;
                level.addParticle(ParticleTypes.BUBBLE_POP, bx, liquidY, bz,
                        (random.nextDouble() - 0.5) * 0.03, 0.03 + random.nextDouble() * 0.02, (random.nextDouble() - 0.5) * 0.03);
            }
            // Bubble whirlpool sound (intense boiling)
            if (random.nextInt(2) == 0) {
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
        }
        
        // Infusing - gentle glow effect (does NOT require heat source)
        if (be.isInfusing()) {
            if (random.nextInt(10) == 0) {
                double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
                double y = pos.getY() + 0.9;
                double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
                level.addParticle(ParticleTypes.ENCHANTED_HIT, x, y, z, 0, 0.02, 0);
            }
        }
        
        // Potion ready - gentle glow effect
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
