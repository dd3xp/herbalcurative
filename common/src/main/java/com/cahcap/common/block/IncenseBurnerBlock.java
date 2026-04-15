package com.cahcap.common.block;

import com.cahcap.common.util.HerbRegistry;
import com.cahcap.common.blockentity.IncenseBurnerBlockEntity;
import com.cahcap.common.util.HerbRegistry;
import com.cahcap.common.registry.ModRegistries;
import com.cahcap.common.util.HerbRegistry;
import com.cahcap.common.util.CustomVoxelShapes;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Incense Burner block for summoning mobs.
 * 
 * No GUI, 7 abstract slots:
 * - 1 powder slot (holds 1 incense powder item)
 * - 6 herb slots (holds up to 64 of each herb type)
 * 
 * Interactions:
 * - Right-click with powder: place in powder slot
 * - Right-click with herb: place in herb slot
 * - Empty hand + Shift + Right-click: take out herbs
 * - Flowweave Ring + Shift + Right-click: take out powder
 */
public class IncenseBurnerBlock extends BaseEntityBlock {
    
    public static final MapCodec<IncenseBurnerBlock> CODEC = simpleCodec(IncenseBurnerBlock::new);
    
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    
    private static final CustomVoxelShapes SHAPES = CustomVoxelShapes.loadFromModel(
            "/assets/herbalcurative/models/block/incense_burner.json", java.util.Set.of("Incense"));
    
    public IncenseBurnerBlock(Properties properties) {
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
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
    
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.getByIndex(state.getValue(FACING), 0, false);
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IncenseBurnerBlockEntity(pos, state);
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, (BlockEntityType<IncenseBurnerBlockEntity>) ModRegistries.INCENSE_BURNER_BE.get(),
                IncenseBurnerBlockEntity::serverTick);
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
        
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof IncenseBurnerBlockEntity burner)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        
        ItemStack heldItem = player.getItemInHand(hand);
        
        if (heldItem.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        
        // Flowweave Ring interactions are handled by FlowweaveRingItem.useOn
        if (heldItem.is(ModRegistries.FLOWWEAVE_RING.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        
        boolean isCreative = player.getAbilities().instabuild;
        
        // Try to add powder
        if (burner.canAddPowder(heldItem)) {
            if (burner.addPowder(heldItem, isCreative)) {
                level.playSound(null, pos, SoundEvents.SAND_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
                return ItemInteractionResult.SUCCESS;
            }
        }
        
        // Try to add herb
        if (HerbRegistry.isHerb(heldItem.getItem())) {
            int added = burner.addHerb(heldItem, isCreative);
            if (added > 0) {
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0f, 1.0f);
                return ItemInteractionResult.SUCCESS;
            }
        }
        
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof IncenseBurnerBlockEntity burner)) {
            return InteractionResult.PASS;
        }
        
        // Empty hand + Shift: take out herbs
        ItemStack removed = burner.removeHerb();
        if (!removed.isEmpty()) {
            if (!player.getInventory().add(removed)) {
                ItemEntity itemEntity = new ItemEntity(level,
                        pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, removed);
                level.addFreshEntity(itemEntity);
            }
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        
        return InteractionResult.PASS;
    }
    
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(this));

        BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof IncenseBurnerBlockEntity burner) {
            drops.addAll(burner.getAllItems());
        }

        return drops;
    }
    
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (movedByPiston) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof IncenseBurnerBlockEntity burner) {
                    for (ItemStack item : burner.getAllItems()) {
                        ItemEntity itemEntity = new ItemEntity(level,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, item);
                        level.addFreshEntity(itemEntity);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
