package com.cahcap.herbalcurative.multiblock;

import com.cahcap.herbalcurative.block.HerbCabinetBlock;
import com.cahcap.herbalcurative.blockentity.HerbCabinetBlockEntity;
import com.cahcap.herbalcurative.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Herb Cabinet Multiblock Structure (3x2)
 * Layout (wall-mounted view):
 * [Log][Log][Log]
 * [Log][Log][Log]  <- Trigger block is center-bottom
 */
public class MultiblockHerbCabinet {
    
    public static final MultiblockHerbCabinet INSTANCE = new MultiblockHerbCabinet();
    
    public boolean isBlockTrigger(BlockState state) {
        return state.is(ModBlocks.FOREST_HEARTWOOD_LOG.get());
    }
    
    public boolean createStructure(Level level, BlockPos clickedPos, Direction side, Player player) {
        if (level.isClientSide) {
            return false;
        }
        
        // Get facing direction from the clicked face
        Direction facing;
        if (side.getAxis() == Direction.Axis.Y) {
            facing = player.getDirection().getOpposite();
        } else {
            facing = side;
        }
        
        Direction right = facing.getClockWise();
        
        // Clicked position should be center-bottom
        BlockPos bottomLeft = clickedPos.relative(right.getOpposite());
        
        // Validate structure
        for (int h = 0; h < 2; h++) {
            for (int w = 0; w < 3; w++) {
                BlockPos checkPos = bottomLeft.relative(Direction.UP, h).relative(right, w);
                BlockState state = level.getBlockState(checkPos);
                
                if (!state.is(ModBlocks.FOREST_HEARTWOOD_LOG.get())) {
                    return false;
                }
            }
        }
        
        // Structure is valid - transform it
        BlockPos masterPos = clickedPos;
        
        for (int h = 0; h < 2; h++) {
            for (int w = 0; w < 3; w++) {
                BlockPos targetPos = bottomLeft.relative(Direction.UP, h).relative(right, w);
                
                // Calculate offset relative to the facing direction
                // This ensures consistent behavior regardless of cardinal direction
                int offsetRight = w - 1;  // -1 (left), 0 (center), 1 (right)
                int offsetUp = h;          // 0 (bottom), 1 (top)
                int offsetForward = 0;     // Always 0 for 2D multiblock
                
                // Convert direction-relative offset to world coordinates
                int offsetX = right.getStepX() * offsetRight + Direction.UP.getStepX() * offsetUp + facing.getStepX() * offsetForward;
                int offsetY = right.getStepY() * offsetRight + Direction.UP.getStepY() * offsetUp + facing.getStepY() * offsetForward;
                int offsetZ = right.getStepZ() * offsetRight + Direction.UP.getStepZ() * offsetUp + facing.getStepZ() * offsetForward;
                
                BlockState newState = ModBlocks.HERB_CABINET.get().defaultBlockState()
                        .setValue(HerbCabinetBlock.FACING, facing)
                        .setValue(HerbCabinetBlock.FORMED, true);
                
                level.setBlock(targetPos, newState, 3);
                
                if (level.getBlockEntity(targetPos) instanceof HerbCabinetBlockEntity cabinet) {
                    cabinet.facing = facing;
                    cabinet.formed = true;
                    cabinet.posInMultiblock = h * 3 + w;
                    cabinet.offset = new int[]{offsetX, offsetY, offsetZ};
                    cabinet.setChanged();
                    level.sendBlockUpdated(targetPos, newState, newState, 3);
                }
            }
        }
        
        level.playSound(null, masterPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        
        return true;
    }
}
