package com.cahcap.common.worldgen;

import com.cahcap.common.registry.ModRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * World generator for herb flower patches (all herbs: Overworld, Nether, End)
 * Generates small patches (2-3 flowers)
 */
public class HerbFlowerPatchFeature extends Feature<NoneFeatureConfiguration> {
    
    private final Block flowerBlock;
    private final boolean requiresWater;
    private final Block[] validGroundBlocks;
    
    public HerbFlowerPatchFeature(Codec<NoneFeatureConfiguration> codec, Block flowerBlock, boolean requiresWater, Block... validGroundBlocks) {
        super(codec);
        this.flowerBlock = flowerBlock;
        this.requiresWater = requiresWater;
        this.validGroundBlocks = validGroundBlocks.length > 0 ? validGroundBlocks : new Block[]{
            Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.PODZOL, Blocks.FARMLAND
        };
    }
    
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();
        
        int flowerCount = random.nextInt(2) + 2;
        int placedCount = 0;
        
        for (int i = 0; i < 10 && placedCount < flowerCount; i++) {
            int offsetX = random.nextInt(3) - 1;
            int offsetZ = random.nextInt(3) - 1;
            BlockPos flowerPos = pos.offset(offsetX, 0, offsetZ);
            
            int yPos = findGroundLevel(level, flowerPos.getX(), flowerPos.getZ());
            if (yPos > 0) {
                BlockPos finalPos = new BlockPos(flowerPos.getX(), yPos, flowerPos.getZ());
                if (canPlaceFlower(level, finalPos)) {
                    level.setBlock(finalPos, flowerBlock.defaultBlockState(), 2);
                    placedCount++;
                }
            }
        }
        
        return placedCount > 0;
    }
    
    private int findGroundLevel(WorldGenLevel level, int x, int z) {
        int maxY = level.getMaxBuildHeight() - 10;
        int minY = level.getMinBuildHeight();
        
        boolean isNether = isNetherBlock(validGroundBlocks[0]);
        if (isNether) {
            maxY = 128;
            minY = 32;
        }
        
        for (int y = maxY; y >= minY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();
            
            for (Block validBlock : validGroundBlocks) {
                if (block == validBlock) {
                BlockPos posAbove = pos.above();
                BlockState stateAbove = level.getBlockState(posAbove);
                if (stateAbove.isAir() || stateAbove.getBlock() instanceof LeavesBlock) {
                    return y + 1;
                    }
                }
            }
        }
        return -1;
    }
    
    private boolean isNetherBlock(Block block) {
        return block == Blocks.WARPED_NYLIUM || block == Blocks.CRIMSON_NYLIUM || 
               block == Blocks.SOUL_SAND || block == Blocks.NETHERRACK || block == Blocks.BASALT;
    }
    
    private boolean canPlaceFlower(WorldGenLevel level, BlockPos pos) {
        BlockPos groundPos = pos.below();
        BlockState groundState = level.getBlockState(groundPos);
        Block groundBlock = groundState.getBlock();
        
        boolean validGround = false;
        for (Block validBlock : validGroundBlocks) {
            if (groundBlock == validBlock) {
                validGround = true;
                break;
            }
        }
        if (!validGround) {
            return false;
        }
        
        BlockState posState = level.getBlockState(pos);
        if (!posState.isAir() && !(posState.getBlock() instanceof LeavesBlock)) {
            return false;
        }
        
        if (requiresWater) {
            boolean hasWater = false;
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (x == 0 && z == 0) continue;
                    BlockPos checkPos = pos.offset(x, 0, z);
                    if (level.getBlockState(checkPos).is(Blocks.WATER)) {
                        hasWater = true;
                        break;
                    }
                }
                if (hasWater) break;
            }
            if (!hasWater) {
                return false;
            }
        }
        
        return true;
    }
    
    // ==================== Factory methods for each flower type ====================
    
    public static HerbFlowerPatchFeature verdscaleFern(Codec<NoneFeatureConfiguration> codec) {
        return new HerbFlowerPatchFeature(codec, ModRegistries.VERDSCALE_FERN.get(), false,
                Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.PODZOL);
    }
    
    public static HerbFlowerPatchFeature zephyrLily(Codec<NoneFeatureConfiguration> codec) {
        return new HerbFlowerPatchFeature(codec, ModRegistries.ZEPHYR_LILY.get(), false,
                Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.PODZOL);
    }
    
    public static HerbFlowerPatchFeature dewpetal(Codec<NoneFeatureConfiguration> codec) {
        return new HerbFlowerPatchFeature(codec, ModRegistries.DEWPETAL.get(), false,
                Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.PODZOL);
    }
    
    public static HerbFlowerPatchFeature crystbud(Codec<NoneFeatureConfiguration> codec) {
        return new HerbFlowerPatchFeature(codec, ModRegistries.CRYSTBUD.get(), false,
                Blocks.BASALT, Blocks.NETHERRACK);
    }
    
    public static HerbFlowerPatchFeature pyrisage(Codec<NoneFeatureConfiguration> codec) {
        return new HerbFlowerPatchFeature(codec, ModRegistries.PYRISAGE.get(), false,
                Blocks.WARPED_NYLIUM, Blocks.CRIMSON_NYLIUM, Blocks.SOUL_SAND);
    }
    
    public static HerbFlowerPatchFeature rosynia(Codec<NoneFeatureConfiguration> codec) {
        return new HerbFlowerPatchFeature(codec, ModRegistries.ROSYNIA.get(), false,
                Blocks.END_STONE);
    }
}

