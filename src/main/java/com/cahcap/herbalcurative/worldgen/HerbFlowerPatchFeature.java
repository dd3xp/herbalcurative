package com.cahcap.herbalcurative.worldgen;

import com.cahcap.herbalcurative.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
    private final boolean requiresWater; // For Dewpetal
    private final Block[] validGroundBlocks; // Valid blocks this flower can grow on
    
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
        
        // Generate 2-3 flowers in a small patch
        int flowerCount = random.nextInt(2) + 2; // 2-3 flowers
        int placedCount = 0;
        
        // Try up to 10 times to place the target number of flowers
        for (int i = 0; i < 10 && placedCount < flowerCount; i++) {
            // Random offset within 2 blocks radius
            int offsetX = random.nextInt(3) - 1; // -1 to 1
            int offsetZ = random.nextInt(3) - 1; // -1 to 1
            BlockPos flowerPos = pos.offset(offsetX, 0, offsetZ);
            
            // Find ground level at this position
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
        
        // For Nether, search in middle layers (32-128)
        boolean isNether = isNetherBlock(validGroundBlocks[0]);
        if (isNether) {
            maxY = 128;
            minY = 32;
        }
        
        for (int y = maxY; y >= minY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();
            
            // Check for suitable ground blocks
            for (Block validBlock : validGroundBlocks) {
                if (block == validBlock) {
                BlockPos posAbove = pos.above();
                BlockState stateAbove = level.getBlockState(posAbove);
                if (stateAbove.isAir() || stateAbove.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock) {
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
        // Check ground block
        BlockPos groundPos = pos.below();
        BlockState groundState = level.getBlockState(groundPos);
        Block groundBlock = groundState.getBlock();
        
        // Check if ground is valid for this flower
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
        
        // Check if position is air
        BlockState posState = level.getBlockState(pos);
        if (!posState.isAir() && !(posState.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock)) {
            return false;
        }
        
        // For Dewpetal, check if there's water nearby (within 2 blocks)
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
    
    // Overworld herbs
    public static HerbFlowerPatchFeature verdscaleFern(Codec<NoneFeatureConfiguration> codec) {
        return new HerbFlowerPatchFeature(codec, ModBlocks.VERDSCALE_FERN.get(), false,
                Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.PODZOL);
    }
    
    public static HerbFlowerPatchFeature zephyrLily(Codec<NoneFeatureConfiguration> codec) {
        return new HerbFlowerPatchFeature(codec, ModBlocks.ZEPHYR_LILY.get(), false,
                Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.PODZOL);
    }
    
    public static HerbFlowerPatchFeature dewpetal(Codec<NoneFeatureConfiguration> codec) {
        // Changed: No longer requires water, grows in cold biomes like other overworld herbs
        return new HerbFlowerPatchFeature(codec, ModBlocks.DEWPETAL.get(), false,
                Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.PODZOL, Blocks.SNOW_BLOCK, Blocks.SNOW);
    }
    
    // Nether herbs
    public static HerbFlowerPatchFeature crystbud(Codec<NoneFeatureConfiguration> codec) {
        // Grows in Basalt Deltas (Basalt) and Nether Wastes (Netherrack) - stone-like blocks
        return new HerbFlowerPatchFeature(codec, ModBlocks.CRYSTBUD.get(), false,
                Blocks.BASALT, Blocks.NETHERRACK);
    }
    
    public static HerbFlowerPatchFeature pyrisage(Codec<NoneFeatureConfiguration> codec) {
        // Grows in Warped Forest, Crimson Forest, Soul Sand Valley - dirt-like blocks
        return new HerbFlowerPatchFeature(codec, ModBlocks.PYRISAGE.get(), false,
                Blocks.WARPED_NYLIUM, Blocks.CRIMSON_NYLIUM, Blocks.SOUL_SAND);
    }
    
    // End herb
    public static HerbFlowerPatchFeature rosynia(Codec<NoneFeatureConfiguration> codec) {
        // Grows throughout The End on End Stone
        return new HerbFlowerPatchFeature(codec, ModBlocks.ROSYNIA.get(), false,
                Blocks.END_STONE);
    }
}
