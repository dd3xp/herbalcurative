package com.cahcap.herbalcurative.common.block;

import com.cahcap.herbalcurative.common.blockentity.WorkbenchBlockEntity;
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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Workbench - A 3-block wide crafting table for the mod.
 * 
 * Layout (when facing north, looking at the front):
 * [LEFT (Tools)] [CENTER (Input)] [RIGHT (Materials)]
 * 
 * Left block: 4 tool slots (top-left, top-right, bottom-left, bottom-right on top face)
 * Center block: 1 input slot, also the main block that stores all data
 * Right block: 9 material slots (3x3 grid, stack structure - LIFO)
 * 
 * Interaction:
 * - Right-click with item: place item in appropriate slot
 * - Empty hand + Shift + Right-click: take item out
 * - Flowweave Ring + Right-click center: trigger crafting
 */
public class WorkbenchBlock extends BaseEntityBlock {
    
    public static final MapCodec<WorkbenchBlock> CODEC = simpleCodec(WorkbenchBlock::new);
    
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<WorkbenchPart> PART = EnumProperty.create("part", WorkbenchPart.class);
    
    // VoxelShape for the workbench - full blocks for simplicity
    protected static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);
    
    public WorkbenchBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, WorkbenchPart.CENTER));
    }
    
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }
    
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos pos = context.getClickedPos();
        
        // Calculate left and right positions based on facing
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();
        
        BlockPos leftPos = pos.relative(left);
        BlockPos rightPos = pos.relative(right);
        
        // Check if left and right positions are available
        Level level = context.getLevel();
        if (!level.getBlockState(leftPos).canBeReplaced(context) ||
            !level.getBlockState(rightPos).canBeReplaced(context)) {
            return null; // Cannot place - not enough space
        }
        
        // Check world bounds
        if (!level.getWorldBorder().isWithinBounds(leftPos) ||
            !level.getWorldBorder().isWithinBounds(rightPos)) {
            return null;
        }
        
        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(PART, WorkbenchPart.CENTER);
    }
    
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide && state.getValue(PART) == WorkbenchPart.CENTER) {
            Direction facing = state.getValue(FACING);
            Direction left = facing.getCounterClockWise();
            Direction right = facing.getClockWise();
            
            // Place left and right parts
            BlockPos leftPos = pos.relative(left);
            BlockPos rightPos = pos.relative(right);
            
            level.setBlock(leftPos, state.setValue(PART, WorkbenchPart.LEFT), 3);
            level.setBlock(rightPos, state.setValue(PART, WorkbenchPart.RIGHT), 3);
        }
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }
    
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                      LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        WorkbenchPart part = state.getValue(PART);
        Direction facing = state.getValue(FACING);
        
        // Check if the connected parts are still valid
        if (part == WorkbenchPart.CENTER) {
            Direction left = facing.getCounterClockWise();
            Direction right = facing.getClockWise();
            
            if (direction == left || direction == right) {
                if (!neighborState.is(this) || neighborState.getValue(PART) == WorkbenchPart.CENTER) {
                    return Blocks.AIR.defaultBlockState();
                }
            }
        } else {
            // LEFT or RIGHT part - check if center is still there
            Direction toCenter = part == WorkbenchPart.LEFT ? facing.getClockWise() : facing.getCounterClockWise();
            
            if (direction == toCenter) {
                if (!neighborState.is(this) || neighborState.getValue(PART) != WorkbenchPart.CENTER) {
                    return Blocks.AIR.defaultBlockState();
                }
            }
        }
        
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Only the center part has a block entity
        if (state.getValue(PART) == WorkbenchPart.CENTER) {
            return new WorkbenchBlockEntity(pos, state);
        }
        return null;
    }
    
    /**
     * Get the center block entity for any part of the workbench.
     */
    @Nullable
    private WorkbenchBlockEntity getCenterBlockEntity(Level level, BlockPos pos, BlockState state) {
        WorkbenchPart part = state.getValue(PART);
        Direction facing = state.getValue(FACING);
        
        BlockPos centerPos;
        if (part == WorkbenchPart.CENTER) {
            centerPos = pos;
        } else if (part == WorkbenchPart.LEFT) {
            centerPos = pos.relative(facing.getClockWise());
        } else { // RIGHT
            centerPos = pos.relative(facing.getCounterClockWise());
        }
        
        BlockEntity be = level.getBlockEntity(centerPos);
        if (be instanceof WorkbenchBlockEntity workbench) {
            return workbench;
        }
        return null;
    }
    
    /**
     * Determine which tool slot (0-3) is being clicked based on hit location on top face.
     * Returns -1 if not clicking on top face or not on left part.
     * 
     * Layout (looking down at top face from player's perspective):
     * [0: top-left] [1: top-right]   (far from player)
     * [2: bot-left] [3: bot-right]   (near player)
     */
    private int getToolSlotFromHit(BlockHitResult hitResult, BlockState state) {
        if (hitResult.getDirection() != Direction.UP) {
            return -1;
        }
        
        Vec3 hitLoc = hitResult.getLocation();
        double localX = hitLoc.x - Math.floor(hitLoc.x);
        double localZ = hitLoc.z - Math.floor(hitLoc.z);
        
        Direction facing = state.getValue(FACING);
        
        // Determine "left" and "top" based on facing
        // "top" = far from the front of workbench (away from player when facing the workbench)
        // "left" = left side when looking at the workbench
        boolean isLeft, isTop;
        
        switch (facing) {
            case NORTH:
                // Player looks from south, top is at lower Z
                isLeft = localX < 0.5;
                isTop = localZ < 0.5;
                break;
            case SOUTH:
                // Player looks from north, top is at higher Z
                isLeft = localX > 0.5;
                isTop = localZ > 0.5;
                break;
            case EAST:
                // Player looks from west, top is at lower X
                isLeft = localZ < 0.5;
                isTop = localX > 0.5;
                break;
            case WEST:
                // Player looks from east, top is at higher X
                isLeft = localZ > 0.5;
                isTop = localX < 0.5;
                break;
            default:
                return -1;
        }
        
        // Map to slot index:
        // [0: top-left] [1: top-right]
        // [2: bot-left] [3: bot-right]
        if (isTop && isLeft) return 0;
        if (isTop && !isLeft) return 1;
        if (!isTop && isLeft) return 2;
        return 3; // !isTop && !isLeft
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
        
        WorkbenchBlockEntity workbench = getCenterBlockEntity(level, pos, state);
        if (workbench == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        
        ItemStack heldItem = player.getItemInHand(hand);
        WorkbenchPart part = state.getValue(PART);
        
        // Check if using Flowweave Ring on center block
        if (heldItem.is(ModRegistries.FLOWWEAVE_RING.get()) && part == WorkbenchPart.CENTER) {
            // Trigger crafting logic (handled by FlowweaveRingItem)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        
        // Place item
        if (!heldItem.isEmpty()) {
            boolean placed = false;
            boolean isCreative = player.getAbilities().instabuild;
            
            switch (part) {
                case LEFT:
                    // Auto-place tool in first available slot
                    placed = workbench.addTool(heldItem, isCreative);
                    break;
                case CENTER:
                    // Place input item
                    placed = workbench.setInputItem(heldItem, isCreative);
                    break;
                case RIGHT:
                    // Push to material stack
                    placed = workbench.pushMaterial(heldItem, isCreative);
                    break;
            }
            
            if (placed) {
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
        
        // Only take out items when shift+right-click with empty hand
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        
        WorkbenchBlockEntity workbench = getCenterBlockEntity(level, pos, state);
        if (workbench == null) {
            return InteractionResult.PASS;
        }
        
        WorkbenchPart part = state.getValue(PART);
        ItemStack removed = ItemStack.EMPTY;
        
        switch (part) {
            case LEFT:
                // Take out tool from specific slot based on hit location
                int toolSlot = getToolSlotFromHit(hitResult, state);
                if (toolSlot >= 0) {
                    removed = workbench.removeTool(toolSlot);
                }
                break;
            case CENTER:
                // Take out input item
                removed = workbench.removeInputItem();
                break;
            case RIGHT:
                // Pop from material stack
                removed = workbench.popMaterial();
                break;
        }
        
        if (!removed.isEmpty()) {
            // Give to player or drop
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
        
        // Only drop items from center block to avoid duplicates
        if (state.getValue(PART) == WorkbenchPart.CENTER) {
            drops.addAll(super.getDrops(state, builder));
            
            // Add stored items to drops
            BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
            if (be instanceof WorkbenchBlockEntity workbench) {
                drops.addAll(workbench.getAllItems());
            }
        }
        
        return drops;
    }
    
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            // Drop items if moved by piston
            if (movedByPiston && state.getValue(PART) == WorkbenchPart.CENTER) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof WorkbenchBlockEntity workbench) {
                    for (ItemStack item : workbench.getAllItems()) {
                        ItemEntity itemEntity = new ItemEntity(level,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, item);
                        level.addFreshEntity(itemEntity);
                    }
                }
            }
            
            // Break connected parts
            if (state.getValue(PART) == WorkbenchPart.CENTER) {
                Direction facing = state.getValue(FACING);
                Direction left = facing.getCounterClockWise();
                Direction right = facing.getClockWise();
                
                BlockPos leftPos = pos.relative(left);
                BlockPos rightPos = pos.relative(right);
                
                if (level.getBlockState(leftPos).is(this)) {
                    level.removeBlock(leftPos, false);
                }
                if (level.getBlockState(rightPos).is(this)) {
                    level.removeBlock(rightPos, false);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
    
    /**
     * Enum representing the three parts of the workbench.
     */
    public enum WorkbenchPart implements net.minecraft.util.StringRepresentable {
        LEFT("left"),
        CENTER("center"),
        RIGHT("right");
        
        private final String name;
        
        WorkbenchPart(String name) {
            this.name = name;
        }
        
        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
