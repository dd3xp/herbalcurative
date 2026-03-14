package com.cahcap.common.multiblock;

import com.cahcap.common.block.HerbVaultBlock;
import com.cahcap.common.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.ArrayList;
import java.util.List;

/**
 * Herb Vault Multiblock Structure (3x3x3)
 *
 * Layer 1 (bottom, y=-1): 9 lumistone bricks
 * Layer 2 (middle, y=0):
 *   [RedCherryLog][LumistoneBrickWall][RedCherryLog]
 *   [LumistoneBrickWall][MagicAlloyBlock (MASTER)][LumistoneBrickWall]
 *   [RedCherryLog][RedCherryFence (front)][RedCherryLog]
 * Layer 3 (top, y=1):
 *   [LumistoneBrickSlab(bottom)][LumistoneBrickSlab(bottom)][LumistoneBrickSlab(bottom)]
 *   [LumistoneBrickSlab(bottom)][LumistoneBrick][LumistoneBrickSlab(bottom)]
 *   [LumistoneBrickSlab(bottom)][LumistoneBrickSlab(bottom)][LumistoneBrickSlab(bottom)]
 *
 * Trigger: Right-click Red Cherry Fence with Flowweave Ring.
 */
public class MultiblockHerbVault {

    public static final MultiblockHerbVault INSTANCE = new MultiblockHerbVault();

    /**
     * Check if the clicked block can trigger vault formation.
     * Trigger: Red Cherry Fence
     */
    public boolean isBlockTrigger(BlockState state) {
        return state.is(ModRegistries.RED_CHERRY_FENCE.get());
    }

    /**
     * Attempt to create the herb vault multiblock structure.
     */
    public boolean createStructure(Level level, BlockPos clickedPos, Player player) {
        if (level.isClientSide) return false;

        // Find master (magic alloy block adjacent to fence)
        BlockPos masterPos = findMasterFromFence(level, clickedPos);
        if (masterPos == null) return false;

        Direction facing = detectFrontFace(masterPos, clickedPos);
        if (facing == null) return false;

        if (!validateStructure(level, masterPos, facing)) {
            return false;
        }

        // Build transform list: 27 blocks (3x3x3)
        List<Multiblock.BlockTransform> transforms = new ArrayList<>();
        for (int dy = -1; dy <= 1; dy++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    transforms.add(new Multiblock.BlockTransform(
                            new BlockPos(x, dy, z), x == 0 && dy == 0 && z == 0, transforms.size()));
                }
            }
        }

        Multiblock.assemble(level, masterPos, facing, transforms,
                (pos, t) -> ModRegistries.HERB_VAULT.get().defaultBlockState()
                        .setValue(HerbVaultBlock.FACING, facing)
                        .setValue(HerbVaultBlock.FORMED, true)
                        .setValue(HerbVaultBlock.IS_MASTER, t.isMaster()),
                (be, t) -> {
                    if (be instanceof com.cahcap.common.blockentity.HerbVaultBlockEntity vault) {
                        BlockPos pos = t.worldPos(masterPos);
                        vault.facing = facing;
                        vault.formed = true;
                        vault.posInMultiblock = t.posInMultiblock();
                        vault.offset = new int[]{
                                pos.getX() - masterPos.getX(),
                                pos.getY() - masterPos.getY(),
                                pos.getZ() - masterPos.getZ()
                        };
                    }
                });

        level.playSound(null, masterPos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
        return true;
    }

    private BlockPos findMasterFromFence(Level level, BlockPos fencePos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos candidate = fencePos.relative(dir);
            if (level.getBlockState(candidate).is(ModRegistries.MAGIC_ALLOY_BLOCK.get())) {
                return candidate;
            }
        }
        return null;
    }

    private Direction detectFrontFace(BlockPos masterPos, BlockPos fencePos) {
        int dx = fencePos.getX() - masterPos.getX();
        int dz = fencePos.getZ() - masterPos.getZ();
        if (dx == 0 && dz == -1) return Direction.NORTH;
        if (dx == 0 && dz == 1) return Direction.SOUTH;
        if (dx == -1 && dz == 0) return Direction.WEST;
        if (dx == 1 && dz == 0) return Direction.EAST;
        return null;
    }

    private boolean validateStructure(Level level, BlockPos masterPos, Direction facing) {
        // Layer 1 (y=-1): all 9 = lumistone bricks
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos = masterPos.offset(x, -1, z);
                if (!level.getBlockState(pos).is(ModRegistries.LUMISTONE_BRICKS.get())) {
                    return false;
                }
            }
        }

        // Layer 2 (y=0): corners = red cherry log, sides = lumistone brick wall (except front = fence), center = magic alloy
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos = masterPos.offset(x, 0, z);
                if (x == 0 && z == 0) continue;

                boolean isCorner = (x != 0 && z != 0);
                if (isCorner) {
                    if (!level.getBlockState(pos).is(ModRegistries.RED_CHERRY_LOG.get())) {
                        return false;
                    }
                } else {
                    Direction relDir = getRelativeDirection(x, z);
                    if (relDir == facing) {
                        if (!level.getBlockState(pos).is(ModRegistries.RED_CHERRY_FENCE.get())) {
                            return false;
                        }
                    } else {
                        if (!level.getBlockState(pos).is(ModRegistries.LUMISTONE_BRICK_WALL.get())) {
                            return false;
                        }
                    }
                }
            }
        }

        // Layer 3 (y=1): center = lumistone bricks, rest = lumistone brick slab (bottom)
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos = masterPos.offset(x, 1, z);
                if (x == 0 && z == 0) {
                    if (!level.getBlockState(pos).is(ModRegistries.LUMISTONE_BRICKS.get())) {
                        return false;
                    }
                } else {
                    BlockState state = level.getBlockState(pos);
                    if (!state.is(ModRegistries.LUMISTONE_BRICK_SLAB.get()) ||
                            !state.hasProperty(SlabBlock.TYPE) ||
                            state.getValue(SlabBlock.TYPE) != SlabType.BOTTOM) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private Direction getRelativeDirection(int x, int z) {
        if (x == 0 && z == -1) return Direction.NORTH;
        if (x == 0 && z == 1) return Direction.SOUTH;
        if (x == -1 && z == 0) return Direction.WEST;
        if (x == 1 && z == 0) return Direction.EAST;
        return null;
    }
}
