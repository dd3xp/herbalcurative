package com.cahcap.common.multiblock;

import com.cahcap.common.block.KilnBlock;
import com.cahcap.common.blockentity.KilnBlockEntity;
import com.cahcap.common.registry.ModRegistries;
import com.cahcap.HerbalCurativeCommon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.ArrayList;
import java.util.List;

/**
 * Kiln Multiblock Structure (3x3x3)
 *
 * Layout (ground placement, front = south by default):
 *
 * Layer 1 (bottom, y=-1 from master):
 *   [StoneBricks][StoneBricks][StoneBricks]
 *   [StoneBricks][PyrisagePlantable][StoneBricks]
 *   [StoneBricks][StoneBricks][StoneBricks]
 *
 * Layer 2 (middle, y=0, master layer):
 *   [StoneBricks][StoneBricks][StoneBricks]
 *   [StoneBricks][Pyrisage (MASTER)][StoneBricks]
 *   [StoneBricks][StoneBrickSlab(top, front)][StoneBricks]
 *
 * Layer 3 (top, y=1):
 *   [StoneBrickSlab(bottom)][StoneBricks][StoneBrickSlab(bottom)]
 *   [StoneBrickSlab(bottom)][StoneBricks][StoneBrickSlab(bottom)]
 *   [StoneBrickSlab(bottom)][StoneBricks][StoneBrickSlab(bottom)]
 *
 * Trigger: Right-click Pyrisage with Flowweave Ring to assemble.
 * Front face: The side with the stone brick slab on layer 2.
 */
public class MultiblockKiln {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiblockKiln.class);
    public static final MultiblockKiln INSTANCE = new MultiblockKiln();

    /**
     * Check if the clicked block can trigger kiln formation.
     * Trigger: stone brick slab (top half) — the front opening of the kiln on Layer 2.
     */
    public boolean isBlockTrigger(BlockState state) {
        return state.is(Blocks.STONE_BRICK_SLAB) &&
                state.hasProperty(SlabBlock.TYPE) &&
                state.getValue(SlabBlock.TYPE) == SlabType.TOP;
    }

    /**
     * Attempt to create the kiln multiblock structure.
     * The clicked block is the stone brick slab (top half) on the front of Layer 2.
     * We search adjacent blocks for the Pyrisage to locate the master position.
     *
     * @param level      The world
     * @param clickedPos Position of the clicked stone brick slab
     * @param player     The player who triggered the formation
     * @return true if structure was successfully formed
     */
    public boolean createStructure(Level level, BlockPos clickedPos, Player player) {
        if (level.isClientSide) {
            return false;
        }

        LOGGER.info("[Kiln] createStructure called at {}", clickedPos);

        // Find master position: Pyrisage should be adjacent to the clicked slab on the same Y level
        BlockPos masterPos = findMasterFromSlab(level, clickedPos);
        if (masterPos == null) {
            LOGGER.info("[Kiln] Failed: no Pyrisage found adjacent to clicked slab at {}", clickedPos);
            return false;
        }

        // Detect front face direction
        Direction facing = detectFrontFace(level, masterPos);
        if (facing == null) {
            LOGGER.info("[Kiln] Failed: no front face detected (no stone brick top slab adjacent to pyrisage)");
            return false;
        }

        LOGGER.info("[Kiln] Detected front face: {}", facing);

        // Validate structure
        if (!validateStructure(level, masterPos, facing)) {
            return false;
        }

        // Build transform list: Layer 1 (9) + Layer 2 (9) + Layer 3 (9) = 27
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
                (pos, t) -> ModRegistries.KILN.get().defaultBlockState()
                        .setValue(KilnBlock.FACING, facing)
                        .setValue(KilnBlock.FORMED, true)
                        .setValue(KilnBlock.IS_MASTER, t.isMaster()),
                (be, t) -> {
                    if (be instanceof KilnBlockEntity kiln) {
                        BlockPos pos = t.worldPos(masterPos);
                        int offsetX = pos.getX() - masterPos.getX();
                        int offsetY = pos.getY() - masterPos.getY();
                        int offsetZ = pos.getZ() - masterPos.getZ();
                        kiln.facing = facing;
                        kiln.formed = true;
                        kiln.posInMultiblock = t.posInMultiblock();
                        kiln.offset = new int[]{offsetX, offsetY, offsetZ};
                    }
                });

        level.playSound(null, masterPos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);

        return true;
    }

    /**
     * Find the master position (Pyrisage) from the clicked slab position.
     * The slab is adjacent to the Pyrisage on the same Y level.
     */
    private BlockPos findMasterFromSlab(Level level, BlockPos slabPos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos candidate = slabPos.relative(dir);
            if (level.getBlockState(candidate).is(ModRegistries.PYRISAGE.get())) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Detect the front face direction by finding the stone brick slab (top) on layer 2.
     * The front is the direction from master towards the slab.
     */
    private Direction detectFrontFace(Level level, BlockPos masterPos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos slabPos = masterPos.relative(dir);
            BlockState state = level.getBlockState(slabPos);
            if (state.is(Blocks.STONE_BRICK_SLAB) &&
                    state.hasProperty(SlabBlock.TYPE) &&
                    state.getValue(SlabBlock.TYPE) == SlabType.TOP) {
                return dir;
            }
        }
        return null;
    }

    /**
     * Validate that all required blocks are in place.
     */
    private boolean validateStructure(Level level, BlockPos masterPos, Direction facing) {
        // === Layer 1 (y=-1): 8 stone bricks + 1 pyrisage-plantable center ===
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos = masterPos.offset(x, -1, z);
                if (x == 0 && z == 0) {
                    BlockState state = level.getBlockState(pos);
                    if (!isPyrisagePlantable(state)) {
                        LOGGER.info("[Kiln] Layer 1 center ({}) is not pyrisage-plantable: {}", pos, state);
                        return false;
                    }
                } else {
                    if (!level.getBlockState(pos).is(Blocks.STONE_BRICKS)) {
                        LOGGER.info("[Kiln] Layer 1 ({},{}) at {} expected stone_bricks, got: {}", x, z, pos, level.getBlockState(pos));
                        return false;
                    }
                }
            }
        }

        // === Layer 2 (y=0): 7 stone bricks + 1 pyrisage(master) + 1 stone brick slab(top, front) ===
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos = masterPos.offset(x, 0, z);
                if (x == 0 && z == 0) {
                    continue;
                }
                Direction relDir = getRelativeDirection(x, z);
                if (relDir == facing) {
                    BlockState state = level.getBlockState(pos);
                    if (!state.is(Blocks.STONE_BRICK_SLAB) ||
                            !state.hasProperty(SlabBlock.TYPE) ||
                            state.getValue(SlabBlock.TYPE) != SlabType.TOP) {
                        LOGGER.info("[Kiln] Layer 2 front ({},{}) at {} expected stone_brick_slab(top), got: {}", x, z, pos, state);
                        return false;
                    }
                } else {
                    if (!level.getBlockState(pos).is(Blocks.STONE_BRICKS)) {
                        LOGGER.info("[Kiln] Layer 2 ({},{}) at {} expected stone_bricks, got: {}", x, z, pos, level.getBlockState(pos));
                        return false;
                    }
                }
            }
        }

        // === Layer 3 (y=1): center column (along facing axis) = stone bricks, sides = stone brick slab (bottom) ===
        // Design (viewed from front):
        // [slab][bricks][slab]
        // [slab][bricks][slab]
        // [slab][bricks][slab]
        // The "center column" runs along the facing direction (front-to-back).
        // For north/south facing: center column is x=0, sides are x!=0
        // For east/west facing: center column is z=0, sides are z!=0
        boolean facingAlongZ = (facing == Direction.NORTH || facing == Direction.SOUTH);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos = masterPos.offset(x, 1, z);
                boolean isCenterColumn = facingAlongZ ? (x == 0) : (z == 0);
                if (isCenterColumn) {
                    // Center column: stone bricks
                    if (!level.getBlockState(pos).is(Blocks.STONE_BRICKS)) {
                        LOGGER.info("[Kiln] Layer 3 ({},{}) at {} expected stone_bricks, got: {}", x, z, pos, level.getBlockState(pos));
                        return false;
                    }
                } else {
                    // Side columns: stone brick slab (bottom half)
                    BlockState state = level.getBlockState(pos);
                    if (!state.is(Blocks.STONE_BRICK_SLAB) ||
                            !state.hasProperty(SlabBlock.TYPE) ||
                            state.getValue(SlabBlock.TYPE) != SlabType.BOTTOM) {
                        LOGGER.info("[Kiln] Layer 3 ({},{}) at {} expected stone_brick_slab(bottom), got: {}", x, z, pos, state);
                        return false;
                    }
                }
            }
        }

        LOGGER.info("[Kiln] Structure validated successfully!");
        return true;
    }

    /**
     * Check if a block can have Pyrisage planted on it.
     * Same condition as PyrisageFlowerBlock.mayPlaceOn.
     */
    private boolean isPyrisagePlantable(BlockState state) {
        return state.is(Blocks.WARPED_NYLIUM) || state.is(Blocks.CRIMSON_NYLIUM) ||
                state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL);
    }

    /**
     * Get the direction from center (0,0) to the given offset, or null if not adjacent.
     */
    private Direction getRelativeDirection(int x, int z) {
        if (x == 0 && z == -1) return Direction.NORTH;
        if (x == 0 && z == 1) return Direction.SOUTH;
        if (x == -1 && z == 0) return Direction.WEST;
        if (x == 1 && z == 0) return Direction.EAST;
        return null; // diagonal
    }
}
