package com.cahcap.herbalcurative.worldgen;

import com.cahcap.herbalcurative.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ForestHeartwoodTreeFeature extends Feature<NoneFeatureConfiguration> {
    
    public ForestHeartwoodTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }
    
    /**
     * Calculate the distance from a position to the nearest log block
     * Uses Manhattan distance for compatibility with Minecraft's leaf decay system
     */
    private int calculateDistanceToLog(BlockPos leafPos, Set<BlockPos> logPositions) {
        int minDistance = 7; // Max distance before decay
        
        for (BlockPos logPos : logPositions) {
            // Manhattan distance (|x1-x2| + |y1-y2| + |z1-z2|)
            int distance = Math.abs(leafPos.getX() - logPos.getX()) +
                          Math.abs(leafPos.getY() - logPos.getY()) +
                          Math.abs(leafPos.getZ() - logPos.getZ());
            minDistance = Math.min(minDistance, distance);
        }
        
        // Distance must be between 1 and 7 (Minecraft's leaf distance range)
        // 0 means the leaf is at the same position as a log, which shouldn't happen
        return Math.max(1, Math.min(minDistance, 7));
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();

        // Tree parameters - moderate size tree with branches
        int trunkHeight = random.nextInt(2) + 6; // 6-7 blocks trunk (moderate height)
        int crownHeight = 5; // 5-layer crown with branches
        int totalHeight = trunkHeight + crownHeight;

        // Check if there's enough space
        if (pos.getY() < 1 || pos.getY() + totalHeight > level.getMaxBuildHeight()) {
            return false;
        }

        // Check ground
        BlockPos groundPos = pos.below();
        BlockState groundState = level.getBlockState(groundPos);
        if (!groundState.is(Blocks.GRASS_BLOCK) && !groundState.is(Blocks.DIRT) && 
            !groundState.is(Blocks.PODZOL) && !groundState.is(Blocks.COARSE_DIRT)) {
            return false;
        }

        // Check distance to other tree trunks (6 block radius minimum spacing for larger trees)
        int spacingRadius = 6;
        for (int x = -spacingRadius; x <= spacingRadius; x++) {
            for (int z = -spacingRadius; z <= spacingRadius; z++) {
                if (x == 0 && z == 0) continue; // Skip center
                
                double horizontalDist = Math.sqrt(x * x + z * z);
                if (horizontalDist > spacingRadius) continue;
                
                // Check ground level and up to 10 blocks above for logs (trunk area)
                for (int yOffset = 0; yOffset <= 10; yOffset++) {
                    BlockPos checkPos = pos.offset(x, yOffset, z);
                    BlockState checkState = level.getBlockState(checkPos);
                    net.minecraft.world.level.block.Block checkBlock = checkState.getBlock();
                    
                    // If we find any log block, this position is too close to another tree
                    if (checkBlock instanceof RotatedPillarBlock &&
                        (checkBlock.toString().contains("log") || checkBlock.toString().contains("stem"))) {
                        return false;
                    }
                }
            }
        }

        // Check space above - up to 9x9 area for larger branches
        for (int y = 0; y <= totalHeight; y++) {
            // Radius based on crown shape with longer branches
            int checkRadius;
            if (y <= trunkHeight) {
                checkRadius = 0; // Trunk
            } else if (y <= trunkHeight + 2) {
                checkRadius = 4; // Lower crown with branches (9x9 to fit longer branches)
            } else {
                checkRadius = 2; // Upper crown (5x5)
            }
            
            for (int x = -checkRadius; x <= checkRadius; x++) {
                for (int z = -checkRadius; z <= checkRadius; z++) {
                    BlockPos checkPos = pos.offset(x, y, z);
                    BlockState state = level.getBlockState(checkPos);
                    net.minecraft.world.level.block.Block block = state.getBlock();
                    
                    // Allow air, leaves, grass, vines
                    if (!state.isAir() && 
                        !state.is(Blocks.SHORT_GRASS) && !state.is(Blocks.TALL_GRASS) &&
                        !state.is(Blocks.FERN) && !state.is(Blocks.LARGE_FERN) &&
                        !state.is(Blocks.VINE) &&
                        !(block instanceof net.minecraft.world.level.block.LeavesBlock)) {
                        return false;
                    }
                }
            }
        }

        // Track all log positions for distance calculation
        Set<BlockPos> logPositions = new HashSet<>();

        // Place trunk
        // Set AXIS to Y for vertical logs (required for RotatedPillarBlock)
        BlockState logState = ModBlocks.FOREST_HEARTWOOD_LOG.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, net.minecraft.core.Direction.Axis.Y);

        // Place trunk logs
        for (int y = 0; y < trunkHeight; y++) {
            BlockPos logPos = pos.above(y);
            setBlock(level, logPos, logState);
            logPositions.add(logPos.immutable());
        }

        // Generate crown with branches - natural tree structure
        List<BlockPos> leafPositions = new ArrayList<>();

        // Crown starts at trunk top
        int crownBase = trunkHeight;
        
        // Create center log column through crown (like a real tree)
        for (int y = 0; y < 4; y++) {
            BlockPos centerLog = pos.above(crownBase + y);
            setBlock(level, centerLog, logState);
            logPositions.add(centerLog.immutable());
        }
        
        // Generate branches - only one pair of mirrored branches
        // Randomly choose which axis to use (X-axis or Z-axis)
        boolean useXAxis = random.nextBoolean();
        
        // Branch height: start at trunk top (lower to make trunk appear taller)
        int branchStartHeight = crownBase;
        
        // All branches on the same tree have the same length: 3 blocks
        int branchLength = 3;
        
        // Generate only one pair of mirrored branches
        if (useXAxis) {
            // East-West branches only
            generateBranch(level, pos, branchStartHeight, 1, 0, branchLength, logPositions, leafPositions);  // East
            generateBranch(level, pos, branchStartHeight, -1, 0, branchLength, logPositions, leafPositions); // West
        } else {
            // North-South branches only
            generateBranch(level, pos, branchStartHeight, 0, -1, branchLength, logPositions, leafPositions); // North
            generateBranch(level, pos, branchStartHeight, 0, 1, branchLength, logPositions, leafPositions);  // South
        }
        
        // Add leaves around center column (creates full crown)
        for (int y = 0; y < 5; y++) {
            int layerY = crownBase + y;
            
            // Radius varies by height - larger in middle, smaller at top/bottom
            int radius;
            if (y == 0) {
                radius = 2; // Bottom layer
            } else if (y <= 2) {
                radius = 3; // Middle layers (widest)
            } else if (y == 3) {
                radius = 2; // Upper layer
            } else {
                radius = 1; // Top layer
            }
            
            // Place leaves in a circle around center
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x == 0 && z == 0) continue; // Skip center log
                    
                    // Use circular shape (distance check)
                    double dist = Math.sqrt(x * x + z * z);
                    if (dist <= radius + 0.5) {
                        BlockPos leafPos = pos.above(layerY).offset(x, 0, z);
                        addLeafPosition(level, leafPos, leafPositions);
                    }
                }
            }
        }
        
        // Top decoration - flat crown on top
        BlockPos topPos = pos.above(crownBase + 4);
        addLeafPosition(level, topPos, leafPositions);         // Center
        addLeafPosition(level, topPos.north(), leafPositions); // North
        addLeafPosition(level, topPos.south(), leafPositions); // South
        addLeafPosition(level, topPos.east(), leafPositions);  // East
        addLeafPosition(level, topPos.west(), leafPositions);  // West
        
        // Now place all leaves with correct distance values
        for (BlockPos leafPos : leafPositions) {
            int distance = calculateDistanceToLog(leafPos, logPositions);
            BlockState leafState = ModBlocks.FOREST_HEARTWOOD_LEAVES.get().defaultBlockState()
                    .setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, false)
                    .setValue(net.minecraft.world.level.block.LeavesBlock.DISTANCE, distance);
            setBlock(level, leafPos, leafState);
        }

        // Generate berry bushes (50% chance, 1-3 bushes)
        if (random.nextFloat() < 0.5f && !leafPositions.isEmpty()) {
            int berryCount = random.nextInt(3) + 1; // 1-3 bushes
            berryCount = Math.min(berryCount, leafPositions.size());
            
            List<BlockPos> shuffled = new ArrayList<>(leafPositions);
            java.util.Collections.shuffle(shuffled);
            
            int placed = 0;
            for (BlockPos leafPos : shuffled) {
                if (placed >= berryCount) break;
                
                BlockPos berryPos = leafPos.below();
                BlockState berryPosState = level.getBlockState(berryPos);
                
                // Check if position is valid for berry bush:
                // - Must be air or replaceable (grass, etc.)
                // - Must have Forest Heartwood Leaves above (canSurvive check)
                if ((berryPosState.isAir() || berryPosState.canBeReplaced()) &&
                    level.getBlockState(berryPos.above()).is(ModBlocks.FOREST_HEARTWOOD_LEAVES.get())) {
                    // Place mature berry bush (age = 2)
                    BlockState matureBerryState = ModBlocks.FOREST_BERRY_BUSH.get().defaultBlockState()
                        .setValue(com.cahcap.herbalcurative.block.ForestBerryBushBlock.AGE, Integer.valueOf(2));
                    level.setBlock(berryPos, matureBerryState, 2);
                    placed++;
                }
            }
        }

        return true;
    }

    /**
     * Generate a single branch in the specified direction
     */
    private void generateBranch(WorldGenLevel level, BlockPos treeBase, int height, int dx, int dz, 
                                int branchLength, Set<BlockPos> logPositions, List<BlockPos> leafPositions) {
        BlockState logState = ModBlocks.FOREST_HEARTWOOD_LOG.get().defaultBlockState();
        
        for (int i = 1; i <= branchLength; i++) {
            BlockPos branchPos = treeBase.above(height).offset(dx * i, 0, dz * i);
            
            // Place horizontal log
            BlockState branchLog;
            if (dx != 0) {
                branchLog = logState.setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
            } else {
                branchLog = logState.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
            }
            
            setBlock(level, branchPos, branchLog);
            logPositions.add(branchPos.immutable());
            
            // Add rounded leaves around branch tip (last 2 blocks for smoother look)
            if (i >= branchLength - 1) {
                // Create a layered leaf cluster at branch end
                for (int ox = -2; ox <= 2; ox++) {
                    for (int oz = -2; oz <= 2; oz++) {
                        for (int oy = -1; oy <= 1; oy++) {
                            if (ox == 0 && oz == 0 && oy == 0) continue; // Skip branch itself
                            
                            // Layer the leaves - top layer is smaller (more layered look)
                            double dist = Math.sqrt(ox * ox + oz * oz);
                            
                            // Bottom layer (oy = -1): full 5x5 circle
                            // Middle layer (oy = 0): medium circle
                            // Top layer (oy = 1): small circle for layered effect
                            double threshold;
                            if (oy == -1) {
                                threshold = 2.2; // Full circle
                            } else if (oy == 0) {
                                threshold = 2.0; // Medium
                            } else {
                                threshold = 1.5; // Small top layer for depth
                            }
                            
                            if (dist <= threshold) {
                                BlockPos leafPos = branchPos.offset(ox, oy, oz);
                                addLeafPosition(level, leafPos, leafPositions);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Add a leaf position to the list if the space is available
     */
    private void addLeafPosition(WorldGenLevel level, BlockPos pos, List<BlockPos> list) {
        if (level.isEmptyBlock(pos) || level.getBlockState(pos).getBlock().toString().contains("leaves")) {
                list.add(pos.immutable());
        }
    }
}
