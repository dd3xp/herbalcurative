package com.cahcap.herbalcurative.common.block;

import com.cahcap.herbalcurative.common.blockentity.CauldronBlockEntity;
import com.cahcap.herbalcurative.common.registry.ModRegistries;
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
    
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final BooleanProperty IS_MASTER = BooleanProperty.create("is_master");
    
    // Collision/selection shapes for original block types
    private static final VoxelShape FULL_BLOCK = Shapes.block();           // Full block (Lumistone Bricks)
    private static final VoxelShape TOP_SLAB = Block.box(0, 8, 0, 16, 16, 16);  // Top slab (Lumistone Brick Slab)
    private static final VoxelShape EMPTY = Shapes.empty();                 // Air (Layer 2 center)
    
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
            // Unformed: return full block shape (lumistone)
            return Shapes.block();
        }
        
        // Formed: each block has its own selection shape based on original block type
        if (level.getBlockEntity(pos) instanceof CauldronBlockEntity be) {
            BlockPos masterPos = be.getMasterPos();
            if (masterPos != null) {
                return getOriginalCollisionShape(pos, masterPos);
            }
        }
        
        return Shapes.block();
    }
    
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(FORMED)) {
            // Unformed: return full block shape
            return Shapes.block();
        }
        
        // Use original block's collision shape based on position in multiblock
        if (level.getBlockEntity(pos) instanceof CauldronBlockEntity be) {
            BlockPos masterPos = be.getMasterPos();
            if (masterPos != null) {
                return getOriginalCollisionShape(pos, masterPos);
            }
        }
        
        return Shapes.block();
    }
    
    /**
     * Get the collision shape for a position based on original block type.
     * Layer 1 (y=0): corners = full block, edges+center = top slab
     * Layer 2 (y=1): outer 8 = full block, center = empty (air)
     */
    private VoxelShape getOriginalCollisionShape(BlockPos targetPos, BlockPos masterPos) {
        int dy = targetPos.getY() - masterPos.getY();
        int dx = targetPos.getX() - masterPos.getX();
        int dz = targetPos.getZ() - masterPos.getZ();
        
        if (dy == 0) {
            // Layer 1 (master layer)
            // Corners: full block (Lumistone Bricks)
            if ((dx == -1 || dx == 1) && (dz == -1 || dz == 1)) {
                return FULL_BLOCK;
            }
            // Edge middles + center: top slab (Lumistone Brick Slab)
            return TOP_SLAB;
        } else if (dy == 1) {
            // Layer 2
            if (dx == 0 && dz == 0) {
                // Center: empty (was air)
                return EMPTY;
            } else {
                // Outer 8: full block (Lumistone Bricks)
                return FULL_BLOCK;
            }
        }
        
        return FULL_BLOCK;
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
            
            // Handle water bucket - add water
            if (stack.is(Items.WATER_BUCKET)) {
                if (master.addWater()) {
                    if (!player.isCreative()) {
                        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                    }
                    level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return ItemInteractionResult.SUCCESS;
                }
            }
            
            // Handle Flowweave Ring - start/finish brewing or other interactions
            if (stack.is(ModRegistries.FLOWWEAVE_RING.get())) {
                master.onFlowweaveRingUse(player);
                return ItemInteractionResult.SUCCESS;
            }
            
            // Handle material/herb input
            if (!stack.isEmpty()) {
                // Phase 3: Try cauldron crafting first
                if (master.getPhase() == CauldronBlockEntity.PHASE_COMPLETE) {
                    if (master.addCraftingItem(stack)) {
                        if (!player.isCreative()) {
                            stack.shrink(1);
                        }
                        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
                        return ItemInteractionResult.SUCCESS;
                    }
                }
                
                // Phase 1 or 2: Add material/herb
                if (master.addItem(stack, player)) {
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
                    return ItemInteractionResult.SUCCESS;
                }
            }
            
            // Shift + empty hand - try to extract
            if (stack.isEmpty() && player.isShiftKeyDown()) {
                // Phase 3: Try to extract crafting output first
                if (master.getPhase() == CauldronBlockEntity.PHASE_COMPLETE) {
                    ItemStack output = master.extractCraftingOutput();
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
        // Return the original block for creative pick block
        if (level.getBlockEntity(pos) instanceof CauldronBlockEntity cauldron) {
            BlockPos masterPos = cauldron.getMasterPos();
            if (masterPos != null) {
                return getOriginalItemForPosition(pos, masterPos);
            }
        }
        return new ItemStack(ModRegistries.LUMISTONE_BRICKS.get());
    }
    
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof CauldronBlockEntity cauldron) {
            if (cauldron.suppressDrops) {
                return Collections.emptyList();
            }
            // Drop the original block based on position
            BlockPos masterPos = cauldron.getMasterPos();
            BlockPos thisPos = params.getOptionalParameter(LootContextParams.ORIGIN) != null ?
                    BlockPos.containing(params.getOptionalParameter(LootContextParams.ORIGIN)) :
                    cauldron.getBlockPos();
            if (masterPos != null) {
                return Collections.singletonList(getOriginalItemForPosition(thisPos, masterPos));
            }
        }
        // Default: drop Lumistone Bricks
        return Collections.singletonList(new ItemStack(ModRegistries.LUMISTONE_BRICKS.get()));
    }
    
    /**
     * Get the original item that should drop for a position in the multiblock.
     */
    private ItemStack getOriginalItemForPosition(BlockPos targetPos, BlockPos masterPos) {
        int dy = targetPos.getY() - masterPos.getY();
        int dx = targetPos.getX() - masterPos.getX();
        int dz = targetPos.getZ() - masterPos.getZ();
        
        if (dy == 0) {
            // Layer 1 (master layer)
            // Corners: Lumistone Bricks
            if ((dx == -1 || dx == 1) && (dz == -1 || dz == 1)) {
                return new ItemStack(ModRegistries.LUMISTONE_BRICKS.get());
            }
            // Edge middles + center: Lumistone Brick Slab
            return new ItemStack(ModRegistries.LUMISTONE_BRICK_SLAB.get());
        } else if (dy == 1) {
            // Layer 2
            if (dx == 0 && dz == 0) {
                // Center: nothing (was air)
                return ItemStack.EMPTY;
            } else {
                // Outer 8: Lumistone Bricks
                return new ItemStack(ModRegistries.LUMISTONE_BRICKS.get());
            }
        }
        
        return new ItemStack(ModRegistries.LUMISTONE_BRICKS.get());
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
        
        int phase = be.getPhase();
        
        // Phase 2: Brewing - add bubbles and steam
        if (phase == CauldronBlockEntity.PHASE_BREWING && be.hasHeatSource()) {
            // Bubbles rising from the liquid
            for (int i = 0; i < 3; i++) {
                if (random.nextInt(3) == 0) {
                    double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
                    double y = pos.getY() + 0.85;
                    double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
                    level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0, 0.05, 0);
                }
            }
            
            // Steam/smoke rising
            if (random.nextInt(5) == 0) {
                double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
                double y = pos.getY() + 1.0;
                double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 0, 0.05, 0);
            }
            
            // Enchanting glint particles (magical brewing)
            if (random.nextInt(8) == 0) {
                double x = pos.getX() + random.nextDouble();
                double y = pos.getY() + 0.5 + random.nextDouble() * 0.5;
                double z = pos.getZ() + random.nextDouble();
                level.addParticle(ParticleTypes.ENCHANT, x, y, z, 
                        (random.nextDouble() - 0.5) * 0.5, 
                        random.nextDouble() * 0.2, 
                        (random.nextDouble() - 0.5) * 0.5);
            }
        }
        
        // Phase 3: Complete - gentle glow effect
        if (phase == CauldronBlockEntity.PHASE_COMPLETE) {
            if (random.nextInt(15) == 0) {
                double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
                double y = pos.getY() + 0.9;
                double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
                level.addParticle(ParticleTypes.ENCHANTED_HIT, x, y, z, 0, 0.02, 0);
            }
        }
    }
}
