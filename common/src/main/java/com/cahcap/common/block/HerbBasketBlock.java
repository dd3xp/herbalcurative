package com.cahcap.common.block;

import com.cahcap.common.blockentity.HerbBasketBlockEntity;
import com.cahcap.common.blockentity.HerbCabinetBlockEntity;
import com.cahcap.common.registry.ModRegistries;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Herb Basket - A container for storing a single type of herb.
 * Can store up to 256 of one herb type.
 */
public class HerbBasketBlock extends BaseEntityBlock {
    
    public static final MapCodec<HerbBasketBlock> CODEC = simpleCodec(HerbBasketBlock::new);
    
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ON_WALL = BooleanProperty.create("on_wall");
    // 0 = empty, 1-6 = herb types (scaleplate, dewpetal_shard, golden_lilybell, cryst_spine, burnt_node, heart_of_stardream)
    public static final IntegerProperty HERB_TYPE = IntegerProperty.create("herb_type", 0, 6);
    
    // VoxelShape for wall placement (6 pixels thick)
    protected static final VoxelShape WALL_SHAPE_NORTH = Block.box(0, 0, 10, 16, 16, 16);
    protected static final VoxelShape WALL_SHAPE_SOUTH = Block.box(0, 0, 0, 16, 16, 6);
    protected static final VoxelShape WALL_SHAPE_WEST = Block.box(10, 0, 0, 16, 16, 16);
    protected static final VoxelShape WALL_SHAPE_EAST = Block.box(0, 0, 0, 6, 16, 16);
    
    // VoxelShape for floor placement (6 pixels tall, full width)
    protected static final VoxelShape FLOOR_SHAPE = Block.box(0, 0, 0, 16, 6, 16);
    
    public HerbBasketBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ON_WALL, false)
                .setValue(HERB_TYPE, 0));
    }
    
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ON_WALL, HERB_TYPE);
    }
    
    /**
     * Get the herb type index for a given herb item.
     * @return 0 if not a valid herb, 1-6 for valid herbs
     */
    public static int getHerbTypeIndex(Item herb) {
        Item[] herbs = HerbCabinetBlockEntity.getAllHerbItems();
        for (int i = 0; i < herbs.length; i++) {
            if (herbs[i] == herb) {
                return i + 1; // 1-based index
            }
        }
        return 0;
    }
    
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(ON_WALL)) {
            // Wall placement
            return switch (state.getValue(FACING)) {
                case NORTH -> WALL_SHAPE_NORTH;
                case SOUTH -> WALL_SHAPE_SOUTH;
                case WEST -> WALL_SHAPE_WEST;
                case EAST -> WALL_SHAPE_EAST;
                default -> WALL_SHAPE_NORTH;
            };
        } else {
            // Floor placement
            return FLOOR_SHAPE;
        }
    }
    
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        Direction horizontalFacing = context.getHorizontalDirection().getOpposite();
        
        // If clicked on a horizontal face (side of a block), place on wall
        if (clickedFace.getAxis().isHorizontal()) {
            return defaultBlockState()
                    .setValue(FACING, clickedFace)
                    .setValue(ON_WALL, true);
        }
        
        // Otherwise, place on floor
        return defaultBlockState()
                .setValue(FACING, horizontalFacing)
                .setValue(ON_WALL, false);
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HerbBasketBlockEntity(pos, state);
    }
    
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, 
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hand == InteractionHand.OFF_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        
        if (!(level.getBlockEntity(pos) instanceof HerbBasketBlockEntity basket)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        
        // Shift + right-click with item in hand: pass to default (extract only works with empty hand)
        if (player.isShiftKeyDown()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        
        // Flowweave Ring: clear binding and eject all herbs
        if (!stack.isEmpty() && stack.getItem() == ModRegistries.FLOWWEAVE_RING.get()) {
            Item boundHerbToClear = basket.getBoundHerb();
            if (boundHerbToClear != null) {
                int count = basket.getHerbCount();
                
                // Eject all herbs
                if (count > 0) {
                    while (count > 0) {
                        int stackSize = Math.min(count, 64);
                        ItemStack herbStack = new ItemStack(boundHerbToClear, stackSize);
                        ItemEntity entityItem = new ItemEntity(
                                level,
                                pos.getX() + 0.5,
                                pos.getY() + 0.5,
                                pos.getZ() + 0.5,
                                herbStack
                        );
                        entityItem.setDeltaMovement(
                                (level.random.nextDouble() - 0.5) * 0.2,
                                level.random.nextDouble() * 0.2 + 0.1,
                                (level.random.nextDouble() - 0.5) * 0.2
                        );
                        level.addFreshEntity(entityItem);
                        count -= stackSize;
                    }
                }
                
                // Clear binding
                basket.unbindHerb();
                
                // Play leaf break particles
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.COMPOSTER,
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            15,  // particle count
                            0.3, 0.3, 0.3,  // spread
                            0.05  // speed
                    );
                }
                
                // Play sound
                level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
                
                return ItemInteractionResult.SUCCESS;
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        
        Item boundHerb = basket.getBoundHerb();
        boolean isDouble = basket.isDoubleClick(player.getUUID());
        int totalAdded = 0;
        
        // Check if holding the bound herb (single click or double-click with bound herb adds held stack only)
        boolean holdingBoundHerb = boundHerb != null && !stack.isEmpty() && stack.getItem() == boundHerb;
        
        if (isDouble && boundHerb != null && !holdingBoundHerb) {
            // Double-click with empty hand or non-bound-herb: add ALL matching herbs from inventory
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack invStack = player.getInventory().getItem(i);
                if (!invStack.isEmpty() && invStack.getItem() == boundHerb) {
                    int added = basket.addHerb(invStack.getCount());
                    invStack.shrink(added);
                    totalAdded += added;
                    if (invStack.isEmpty()) {
                        player.getInventory().setItem(i, ItemStack.EMPTY);
                    }
                }
            }
        } else if (!stack.isEmpty() && HerbCabinetBlockEntity.isHerb(stack.getItem())) {
            // Single click (or double-click) with herb: add held stack only
            Item herb = stack.getItem();
            
            // If basket is not bound, bind to this herb
            if (boundHerb == null) {
                basket.bindHerb(herb);
            }
            
            // If basket is bound to this herb (or just got bound), add
            if (basket.getBoundHerb() == herb) {
                totalAdded = basket.addHerb(stack.getCount());
                stack.shrink(totalAdded);
            }
            // If bound to different herb, totalAdded stays 0
        }
        
        if (totalAdded > 0) {
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F,
                    1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F);
        }
        
        return ItemInteractionResult.SUCCESS;
    }
    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, 
                                                Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        
        if (!(level.getBlockEntity(pos) instanceof HerbBasketBlockEntity basket)) {
            return InteractionResult.PASS;
        }
        
        // Shift + right-click with empty hand: extract herbs
        if (player.isShiftKeyDown()) {
            Item boundHerb = basket.getBoundHerb();
            if (boundHerb != null && basket.getHerbCount() > 0) {
                int toRemove = Math.min(64, basket.getHerbCount());
                int removed = basket.removeHerb(toRemove);
                
                if (removed > 0) {
                    ItemStack extractedStack = new ItemStack(boundHerb, removed);
                    
                    if (!player.getInventory().add(extractedStack)) {
                        player.drop(extractedStack, false);
                    }
                    
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                            ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                }
            }
            return InteractionResult.SUCCESS;
        }
        
        // Double-click with empty hand: add ALL matching herbs from inventory (if bound)
        Item boundHerb = basket.getBoundHerb();
        if (boundHerb != null) {
            boolean isDouble = basket.isDoubleClick(player.getUUID());
            if (isDouble) {
                int totalAdded = 0;
                
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack invStack = player.getInventory().getItem(i);
                    if (!invStack.isEmpty() && invStack.getItem() == boundHerb) {
                        int added = basket.addHerb(invStack.getCount());
                        invStack.shrink(added);
                        totalAdded += added;
                        if (invStack.isEmpty()) {
                            player.getInventory().setItem(i, ItemStack.EMPTY);
                        }
                    }
                }
                
                if (totalAdded > 0) {
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F,
                            1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F);
                }
            }
        }
        
        return InteractionResult.SUCCESS;
    }
    
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof HerbBasketBlockEntity basket) {
                // Drop all stored herbs
                Item boundHerb = basket.getBoundHerb();
                int count = basket.getHerbCount();
                
                if (boundHerb != null && count > 0) {
                    while (count > 0) {
                        int stackSize = Math.min(count, 64);
                        ItemStack stack = new ItemStack(boundHerb, stackSize);
                        ItemEntity entityItem = new ItemEntity(
                                level,
                                pos.getX() + 0.5,
                                pos.getY() + 0.5,
                                pos.getZ() + 0.5,
                                stack
                        );
                        level.addFreshEntity(entityItem);
                        count -= stackSize;
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
    
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        // Drop the basket item itself
        return Collections.singletonList(new ItemStack(ModRegistries.HERB_BASKET_ITEM.get()));
    }
}
