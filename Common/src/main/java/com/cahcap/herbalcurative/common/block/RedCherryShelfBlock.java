package com.cahcap.herbalcurative.common.block;

import com.cahcap.herbalcurative.common.blockentity.RedCherryShelfBlockEntity;
import com.cahcap.herbalcurative.common.registry.ModRegistries;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Red Cherry Shelf - A wall-mounted shelf that can hold a single item.
 * Similar to an item frame but as a block.
 * Right-click to place/remove item.
 */
public class RedCherryShelfBlock extends BaseEntityBlock {
    
    public static final MapCodec<RedCherryShelfBlock> CODEC = simpleCodec(RedCherryShelfBlock::new);
    
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    
    // VoxelShape for wall placement (6 pixels thick, 12x12 face)
    protected static final VoxelShape SHAPE_NORTH = Block.box(2, 2, 10, 14, 14, 16);
    protected static final VoxelShape SHAPE_SOUTH = Block.box(2, 2, 0, 14, 14, 6);
    protected static final VoxelShape SHAPE_WEST = Block.box(10, 2, 2, 16, 14, 14);
    protected static final VoxelShape SHAPE_EAST = Block.box(0, 2, 2, 6, 14, 14);
    
    public RedCherryShelfBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }
    
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_NORTH;
        };
    }
    
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        
        // Only allow placement on horizontal faces (walls)
        if (clickedFace.getAxis().isHorizontal()) {
            return defaultBlockState().setValue(FACING, clickedFace);
        }
        
        // If placed on floor/ceiling, use player's facing direction
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RedCherryShelfBlockEntity(pos, state);
    }
    
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, 
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RedCherryShelfBlockEntity shelf)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        
        ItemStack heldItem = player.getItemInHand(hand);
        
        if (shelf.hasItem()) {
            // Shelf has item - take it out
            ItemStack storedItem = shelf.removeItem();
            
            // Give to player or drop
            if (!player.getInventory().add(storedItem)) {
                ItemEntity itemEntity = new ItemEntity(level, 
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, storedItem);
                level.addFreshEntity(itemEntity);
            }
            
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0f, 1.0f);
            return ItemInteractionResult.SUCCESS;
        } else if (!heldItem.isEmpty()) {
            // Shelf is empty and player has item - place it
            ItemStack toPlace = heldItem.copyWithCount(1);
            shelf.setItem(toPlace);
            
            if (!player.getAbilities().instabuild) {
                heldItem.shrink(1);
            }
            
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0f, 1.0f);
            return ItemInteractionResult.SUCCESS;
        }
        
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, 
                                                Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RedCherryShelfBlockEntity shelf)) {
            return InteractionResult.PASS;
        }
        
        if (shelf.hasItem()) {
            // Take item out
            ItemStack storedItem = shelf.removeItem();
            
            // Give to player or drop
            if (!player.getInventory().add(storedItem)) {
                ItemEntity itemEntity = new ItemEntity(level, 
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, storedItem);
                level.addFreshEntity(itemEntity);
            }
            
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        
        return InteractionResult.PASS;
    }
    
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, builder));
        
        // Add stored item to drops
        BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof RedCherryShelfBlockEntity shelf && shelf.hasItem()) {
            drops.add(shelf.getItem().copy());
        }
        
        return drops;
    }
    
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RedCherryShelfBlockEntity shelf && shelf.hasItem()) {
                // Item will be dropped by loot table, but we need to handle piston movement
                if (movedByPiston) {
                    ItemStack storedItem = shelf.getItem();
                    ItemEntity itemEntity = new ItemEntity(level,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, storedItem);
                    level.addFreshEntity(itemEntity);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
