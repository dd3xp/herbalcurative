package com.cahcap.common.multiblock;

import com.cahcap.common.block.CauldronBlock;
import com.cahcap.common.blockentity.CauldronBlockEntity;
import com.cahcap.common.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.ArrayList;
import java.util.List;

/**
 * Cauldron Multiblock Structure (3x3x2)
 * 
 * Layout (ground placement, looking from above):
 * 
 * Layer 1 (bottom, y=0) - Master layer:
 *   [LumiBricks][LumiBrickSlab(top)][LumiBricks]
 *   [LumiBrickSlab(top)][LumiBrickSlab(top) MASTER][LumiBrickSlab(top)]
 *   [LumiBricks][LumiBrickSlab(top)][LumiBricks]
 * 
 * Layer 2 (top, y=1):
 *   [LumiStoneBricks][LumiStoneBricks][LumiStoneBricks]
 *   [LumiStoneBricks][     Empty     ][LumiStoneBricks]
 *   [LumiStoneBricks][LumiStoneBricks][LumiStoneBricks]
 * 
 * The master block is at the center of Layer 1 (top slab position).
 * Trigger: Right-click with Flowweave Ring on any edge Lumistone Brick on Layer 2.
 */
public class MultiblockCauldron {
    
    public static final MultiblockCauldron INSTANCE = new MultiblockCauldron();
    
    /**
     * Check if the clicked block can trigger cauldron formation.
     * Accepts any Lumistone Bricks on the edges of Layer 2.
     */
    public boolean isBlockTrigger(BlockState state) {
        // Accept Lumistone Bricks as trigger (edge blocks of Layer 2)
        return state.is(ModRegistries.LUMISTONE_BRICKS.get());
    }
    
    /**
     * Attempt to create the cauldron multiblock structure.
     * Can be triggered by clicking any edge Lumistone Brick on Layer 2.
     * 
     * @param level The world
     * @param clickedPos Position of the clicked block (any edge brick on layer 2)
     * @param player The player who triggered the formation
     * @return true if structure was successfully formed
     */
    public boolean createStructure(Level level, BlockPos clickedPos, Player player) {
        if (level.isClientSide) {
            return false;
        }
        
        // Try to find the master position (center) from the clicked edge brick
        BlockPos masterPos = findMasterPosition(level, clickedPos);
        if (masterPos == null) {
            return false;
        }
        
        // Validate structure
        if (!validateStructure(level, masterPos)) {
            return false;
        }
        
        // Get facing direction from player
        Direction facing = player.getDirection().getOpposite();

        // Build transform list: Layer 1 (9) + Layer 2 (9)
        List<Multiblock.BlockTransform> transforms = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                transforms.add(new Multiblock.BlockTransform(
                        new BlockPos(x, 0, z), x == 0 && z == 0, transforms.size()));
            }
        }
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                transforms.add(new Multiblock.BlockTransform(
                        new BlockPos(x, 1, z), false, transforms.size()));
            }
        }

        Multiblock.assemble(level, masterPos, facing, transforms,
                (pos, t) -> ModRegistries.CAULDRON.get().defaultBlockState()
                        .setValue(CauldronBlock.FACING, facing)
                        .setValue(CauldronBlock.FORMED, true)
                        .setValue(CauldronBlock.IS_MASTER, t.isMaster()),
                (be, t) -> {
                    if (be instanceof CauldronBlockEntity cauldron) {
                        BlockPos pos = t.worldPos(masterPos);
                        int offsetX = pos.getX() - masterPos.getX();
                        int offsetY = pos.getY() - masterPos.getY();
                        int offsetZ = pos.getZ() - masterPos.getZ();
                        cauldron.facing = facing;
                        cauldron.formed = true;
                        cauldron.posInMultiblock = t.posInMultiblock();
                        cauldron.offset = new int[]{offsetX, offsetY, offsetZ};
                        cauldron.renderAABB = null;
                    }
                });

        level.playSound(null, masterPos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);

        return true;
    }
    
    /**
     * Find the master position (center of Layer 1) from a clicked edge brick on Layer 2.
     * The master is the top slab at the center of Layer 1 (one block below Layer 2 center).
     */
    private BlockPos findMasterPosition(Level level, BlockPos clickedPos) {
        // The clicked block could be at any of the 8 edge positions on Layer 2
        // Check each possible center position
        int[][] offsets = {
            {-1, -1}, {0, -1}, {1, -1},
            {-1, 0},          {1, 0},
            {-1, 1}, {0, 1}, {1, 1}
        };
        
        for (int[] offset : offsets) {
            // Get potential Layer 2 center (should be air)
            BlockPos layer2Center = clickedPos.offset(-offset[0], 0, -offset[1]);
            if (!level.getBlockState(layer2Center).isAir()) {
                continue;
            }
            
            // Master is one block below (Layer 1 center)
            BlockPos masterPos = layer2Center.below();
            BlockState masterState = level.getBlockState(masterPos);
            
            // Check if this is a top slab
            if (masterState.is(ModRegistries.LUMISTONE_BRICK_SLAB.get()) &&
                masterState.hasProperty(SlabBlock.TYPE) &&
                masterState.getValue(SlabBlock.TYPE) == SlabType.TOP) {
                return masterPos;
            }
        }
        
        return null;
    }
    
    /**
     * Validate that all required blocks are in place.
     * 
     * Layer 1 (master layer, y=0): corners = Lumistone Bricks, edge middles + center = Lumistone Slab (top)
     * Layer 2 (y=1): edges = Lumistone Bricks, center = air
     */
    private boolean validateStructure(Level level, BlockPos masterPos) {
        // Check Layer 1 (y=0): 4 corners should be Lumistone Bricks
        BlockPos[] cornerPositions = {
            masterPos.offset(-1, 0, -1),
            masterPos.offset(1, 0, -1),
            masterPos.offset(-1, 0, 1),
            masterPos.offset(1, 0, 1)
        };
        
        for (BlockPos pos : cornerPositions) {
            if (!level.getBlockState(pos).is(ModRegistries.LUMISTONE_BRICKS.get())) {
                return false;
            }
        }
        
        // Check Layer 1: edge middles (4 blocks, not center) should be Lumistone Brick Slab (top half)
        // Center is already validated as master
        BlockPos[] slabPositions = {
            masterPos.offset(0, 0, -1),  // north edge middle
            masterPos.offset(-1, 0, 0),  // west edge middle
            masterPos.offset(1, 0, 0),   // east edge middle
            masterPos.offset(0, 0, 1)    // south edge middle
        };
        
        for (BlockPos pos : slabPositions) {
            BlockState state = level.getBlockState(pos);
            if (!state.is(ModRegistries.LUMISTONE_BRICK_SLAB.get())) {
                return false;
            }
            // Must be top slab
            if (!state.hasProperty(SlabBlock.TYPE) || 
                state.getValue(SlabBlock.TYPE) != SlabType.TOP) {
                return false;
            }
        }
        
        // Check Layer 2 (y+1): 8 outer blocks should be Lumistone Bricks
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue; // Skip center (should be air)
                
                BlockPos pos = masterPos.offset(x, 1, z);
                if (!level.getBlockState(pos).is(ModRegistries.LUMISTONE_BRICKS.get())) {
                    return false;
                }
            }
        }
        
        // Check Layer 2 center: should be air
        if (!level.getBlockState(masterPos.above()).isAir()) {
            return false;
        }
        
        return true;
    }
}
