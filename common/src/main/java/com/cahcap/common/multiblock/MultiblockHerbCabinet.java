package com.cahcap.common.multiblock;

import com.cahcap.common.block.HerbCabinetBlock;
import com.cahcap.common.blockentity.HerbCabinetBlockEntity;
import com.cahcap.common.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Herb Cabinet Multiblock Structure (3x2)
 * Layout (wall-mounted view):
 * [Log][Log][Log]
 * [Log][Log][Log]  <- Trigger block is center-bottom
 */
public class MultiblockHerbCabinet {
    
    public static final MultiblockHerbCabinet INSTANCE = new MultiblockHerbCabinet();
    
    public boolean isBlockTrigger(BlockState state) {
        return state.is(ModRegistries.RED_CHERRY_LOG.get());
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
                
                if (!state.is(ModRegistries.RED_CHERRY_LOG.get())) {
                    return false;
                }
            }
        }
        
        // Structure is valid - transform it
        BlockPos masterPos = clickedPos;

        // Build transform list: 3x2, master at (0,0) center-bottom = (1,0) in grid
        List<Multiblock.BlockTransform> transforms = new ArrayList<>();
        for (int h = 0; h < 2; h++) {
            for (int w = 0; w < 3; w++) {
                int offsetRight = w - 1;
                int offsetUp = h;
                int offsetX = right.getStepX() * offsetRight + facing.getStepX() * 0;
                int offsetZ = right.getStepZ() * offsetRight + facing.getStepZ() * 0;
                transforms.add(new Multiblock.BlockTransform(
                        new BlockPos(offsetX, offsetUp, offsetZ),
                        h == 0 && w == 1,
                        h * 3 + w));
            }
        }

        Multiblock.assemble(level, masterPos, facing, transforms,
                (pos, t) -> ModRegistries.HERB_CABINET.get().defaultBlockState()
                        .setValue(HerbCabinetBlock.FACING, facing)
                        .setValue(HerbCabinetBlock.FORMED, true)
                        .setValue(HerbCabinetBlock.IS_MASTER, t.isMaster()),
                (be, t) -> {
                    if (be instanceof HerbCabinetBlockEntity cabinet) {
                        BlockPos pos = t.worldPos(masterPos);
                        int offsetX = pos.getX() - masterPos.getX();
                        int offsetY = pos.getY() - masterPos.getY();
                        int offsetZ = pos.getZ() - masterPos.getZ();
                        cabinet.facing = facing;
                        cabinet.formed = true;
                        cabinet.posInMultiblock = t.posInMultiblock();
                        cabinet.offset = new int[]{offsetX, offsetY, offsetZ};
                        cabinet.renderAABB = null;
                    }
                });

        level.playSound(null, masterPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        
        return true;
    }
}

