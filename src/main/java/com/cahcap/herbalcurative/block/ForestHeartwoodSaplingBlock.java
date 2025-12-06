package com.cahcap.herbalcurative.block;

import com.cahcap.herbalcurative.registry.ModBlocks;
import com.cahcap.herbalcurative.worldgen.ForestHeartwoodTreeFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.serialization.MapCodec;

public class ForestHeartwoodSaplingBlock extends BushBlock implements BonemealableBlock {
    
    public static final MapCodec<ForestHeartwoodSaplingBlock> CODEC = simpleCodec(ForestHeartwoodSaplingBlock::new);
    
    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }
    
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 1);
    protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);
    
    public ForestHeartwoodSaplingBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, 0));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(7) == 0) {
            this.advanceTree(level, pos, state, random);
        }
    }
    
    public void advanceTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (state.getValue(STAGE) == 0) {
            level.setBlock(pos, state.cycle(STAGE), 4);
        } else {
            this.generateTree(level, pos, state, random);
        }
    }
    
    private void generateTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        // Remove sapling first
        level.removeBlock(pos, false);
        
        // Use the same ForestHeartwoodTreeFeature as world generation
        ForestHeartwoodTreeFeature treeFeature = new ForestHeartwoodTreeFeature(NoneFeatureConfiguration.CODEC);
        
        // Create a FeaturePlaceContext for the tree generation
        net.minecraft.world.level.levelgen.feature.FeaturePlaceContext<NoneFeatureConfiguration> context = 
            new net.minecraft.world.level.levelgen.feature.FeaturePlaceContext<>(
                java.util.Optional.empty(),
                level,
                level.getChunkSource().getGenerator(),
                random,
                pos,
                NoneFeatureConfiguration.INSTANCE
            );
        
        // Try to place the tree
        if (!treeFeature.place(context)) {
            // If tree generation failed, restore the sapling
            level.setBlock(pos, state, 3);
        }
    }
    
    // BonemealableBlock implementation
    
    @Override
    public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }
    
    @Override
    public boolean isBonemealSuccess(net.minecraft.world.level.Level level, RandomSource random, BlockPos pos, BlockState state) {
        return (double)level.random.nextFloat() < 0.45D;
    }
    
    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        this.advanceTree(level, pos, state, random);
    }
}
