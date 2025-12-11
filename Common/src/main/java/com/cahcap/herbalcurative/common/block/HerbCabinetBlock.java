package com.cahcap.herbalcurative.common.block;

import com.cahcap.herbalcurative.common.blockentity.HerbCabinetBlockEntity;
import com.cahcap.herbalcurative.common.registry.ModRegistries;
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
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class HerbCabinetBlock extends BaseEntityBlock {
    
    public static final MapCodec<HerbCabinetBlock> CODEC = simpleCodec(HerbCabinetBlock::new);
    
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    
    public HerbCabinetBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FORMED, false));
    }
    
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORMED);
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
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
    
    @Override
    protected boolean isOcclusionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
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

