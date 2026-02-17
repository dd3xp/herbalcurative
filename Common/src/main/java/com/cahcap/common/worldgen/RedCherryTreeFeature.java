package com.cahcap.common.worldgen;

import com.cahcap.common.block.RedCherryBushBlock;
import com.cahcap.common.registry.ModRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RedCherryTreeFeature extends Feature<NoneFeatureConfiguration> {
    
    public RedCherryTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }
    
    private int calculateDistanceToLog(BlockPos leafPos, Set<BlockPos> logPositions) {
        int minDistance = 7;
        
        for (BlockPos logPos : logPositions) {
            int distance = Math.abs(leafPos.getX() - logPos.getX()) +
                          Math.abs(leafPos.getY() - logPos.getY()) +
                          Math.abs(leafPos.getZ() - logPos.getZ());
            minDistance = Math.min(minDistance, distance);
        }
        
        return Math.max(1, Math.min(minDistance, 7));
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();

        int trunkHeight = random.nextInt(2) + 6;
        int crownHeight = 5;
        int totalHeight = trunkHeight + crownHeight;

        if (pos.getY() < 1 || pos.getY() + totalHeight > level.getMaxBuildHeight()) {
            return false;
        }

        BlockPos groundPos = pos.below();
        BlockState groundState = level.getBlockState(groundPos);
        if (!groundState.is(Blocks.GRASS_BLOCK) && !groundState.is(Blocks.DIRT) && 
            !groundState.is(Blocks.PODZOL) && !groundState.is(Blocks.COARSE_DIRT)) {
            return false;
        }

        int spacingRadius = 6;
        for (int x = -spacingRadius; x <= spacingRadius; x++) {
            for (int z = -spacingRadius; z <= spacingRadius; z++) {
                if (x == 0 && z == 0) continue;
                
                double horizontalDist = Math.sqrt(x * x + z * z);
                if (horizontalDist > spacingRadius) continue;
                
                for (int yOffset = 0; yOffset <= 10; yOffset++) {
                    BlockPos checkPos = pos.offset(x, yOffset, z);
                    BlockState checkState = level.getBlockState(checkPos);
                    net.minecraft.world.level.block.Block checkBlock = checkState.getBlock();
                    
                    if (checkBlock instanceof RotatedPillarBlock &&
                        (checkBlock.toString().contains("log") || checkBlock.toString().contains("stem"))) {
                        return false;
                    }
                }
            }
        }

        for (int y = 0; y <= totalHeight; y++) {
            int checkRadius;
            if (y <= trunkHeight) {
                checkRadius = 0;
            } else if (y <= trunkHeight + 2) {
                checkRadius = 4;
            } else {
                checkRadius = 2;
            }
            
            for (int x = -checkRadius; x <= checkRadius; x++) {
                for (int z = -checkRadius; z <= checkRadius; z++) {
                    BlockPos checkPos = pos.offset(x, y, z);
                    BlockState state = level.getBlockState(checkPos);
                    net.minecraft.world.level.block.Block block = state.getBlock();
                    
                    if (!state.isAir() && 
                        !state.is(Blocks.SHORT_GRASS) && !state.is(Blocks.TALL_GRASS) &&
                        !state.is(Blocks.FERN) && !state.is(Blocks.LARGE_FERN) &&
                        !state.is(Blocks.VINE) &&
                        !(block instanceof LeavesBlock)) {
                        return false;
                    }
                }
            }
        }

        Set<BlockPos> logPositions = new HashSet<>();

        BlockState logState = ModRegistries.RED_CHERRY_LOG.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);

        for (int y = 0; y < trunkHeight; y++) {
            BlockPos logPos = pos.above(y);
            setBlock(level, logPos, logState);
            logPositions.add(logPos.immutable());
        }

        List<BlockPos> leafPositions = new ArrayList<>();

        int crownBase = trunkHeight;
        
        for (int y = 0; y < 4; y++) {
            BlockPos centerLog = pos.above(crownBase + y);
            setBlock(level, centerLog, logState);
            logPositions.add(centerLog.immutable());
        }
        
        boolean useXAxis = random.nextBoolean();
        int branchStartHeight = crownBase;
        int branchLength = 3;
        
        if (useXAxis) {
            generateBranch(level, pos, branchStartHeight, 1, 0, branchLength, logPositions, leafPositions);
            generateBranch(level, pos, branchStartHeight, -1, 0, branchLength, logPositions, leafPositions);
        } else {
            generateBranch(level, pos, branchStartHeight, 0, -1, branchLength, logPositions, leafPositions);
            generateBranch(level, pos, branchStartHeight, 0, 1, branchLength, logPositions, leafPositions);
        }
        
        for (int y = 0; y < 5; y++) {
            int layerY = crownBase + y;
            
            int radius;
            if (y == 0) {
                radius = 2;
            } else if (y <= 2) {
                radius = 3;
            } else if (y == 3) {
                radius = 2;
            } else {
                radius = 1;
            }
            
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x == 0 && z == 0) continue;
                    
                    double dist = Math.sqrt(x * x + z * z);
                    if (dist <= radius + 0.5) {
                        BlockPos leafPos = pos.above(layerY).offset(x, 0, z);
                        addLeafPosition(level, leafPos, leafPositions);
                    }
                }
            }
        }
        
        BlockPos topPos = pos.above(crownBase + 4);
        addLeafPosition(level, topPos, leafPositions);
        addLeafPosition(level, topPos.north(), leafPositions);
        addLeafPosition(level, topPos.south(), leafPositions);
        addLeafPosition(level, topPos.east(), leafPositions);
        addLeafPosition(level, topPos.west(), leafPositions);
        
        for (BlockPos leafPos : leafPositions) {
            int distance = calculateDistanceToLog(leafPos, logPositions);
            BlockState leafState = ModRegistries.RED_CHERRY_LEAVES.get().defaultBlockState()
                    .setValue(LeavesBlock.PERSISTENT, false)
                    .setValue(LeavesBlock.DISTANCE, distance);
            setBlock(level, leafPos, leafState);
        }

        if (random.nextFloat() < 0.5f && !leafPositions.isEmpty()) {
            int berryCount = random.nextInt(3) + 1;
            berryCount = Math.min(berryCount, leafPositions.size());
            
            List<BlockPos> shuffled = new ArrayList<>(leafPositions);
            java.util.Collections.shuffle(shuffled);
            
            int placed = 0;
            for (BlockPos leafPos : shuffled) {
                if (placed >= berryCount) break;
                
                BlockPos berryPos = leafPos.below();
                BlockState berryPosState = level.getBlockState(berryPos);
                
                if ((berryPosState.isAir() || berryPosState.canBeReplaced()) &&
                    level.getBlockState(berryPos.above()).is(ModRegistries.RED_CHERRY_LEAVES.get())) {
                    BlockState matureBerryState = ModRegistries.RED_CHERRY_BUSH.get().defaultBlockState()
                        .setValue(RedCherryBushBlock.AGE, Integer.valueOf(2));
                    level.setBlock(berryPos, matureBerryState, 2);
                    placed++;
                }
            }
        }

        return true;
    }

    private void generateBranch(WorldGenLevel level, BlockPos treeBase, int height, int dx, int dz, 
                                int branchLength, Set<BlockPos> logPositions, List<BlockPos> leafPositions) {
        BlockState logState = ModRegistries.RED_CHERRY_LOG.get().defaultBlockState();
        
        for (int i = 1; i <= branchLength; i++) {
            BlockPos branchPos = treeBase.above(height).offset(dx * i, 0, dz * i);
            
            BlockState branchLog;
            if (dx != 0) {
                branchLog = logState.setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
            } else {
                branchLog = logState.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
            }
            
            setBlock(level, branchPos, branchLog);
            logPositions.add(branchPos.immutable());
            
            if (i >= branchLength - 1) {
                for (int ox = -2; ox <= 2; ox++) {
                    for (int oz = -2; oz <= 2; oz++) {
                        for (int oy = -1; oy <= 1; oy++) {
                            if (ox == 0 && oz == 0 && oy == 0) continue;
                            
                            double dist = Math.sqrt(ox * ox + oz * oz);
                            
                            double threshold;
                            if (oy == -1) {
                                threshold = 2.2;
                            } else if (oy == 0) {
                                threshold = 2.0;
                            } else {
                                threshold = 1.5;
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
    
    private void addLeafPosition(WorldGenLevel level, BlockPos pos, List<BlockPos> list) {
        if (level.isEmptyBlock(pos) || level.getBlockState(pos).getBlock().toString().contains("leaves")) {
                list.add(pos.immutable());
        }
    }
}

