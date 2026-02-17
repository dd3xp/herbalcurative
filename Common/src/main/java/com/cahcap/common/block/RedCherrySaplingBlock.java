package com.cahcap.common.block;

import com.cahcap.common.worldgen.RedCherryTreeFeature;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Optional;

public class RedCherrySaplingBlock extends BushBlock implements BonemealableBlock {
    
    public static final MapCodec<RedCherrySaplingBlock> CODEC = simpleCodec(RedCherrySaplingBlock::new);
    
    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }
    
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 1);
    protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);
    
    public RedCherrySaplingBlock(Properties properties) {
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
        level.removeBlock(pos, false);
        
        RedCherryTreeFeature treeFeature = new RedCherryTreeFeature(NoneFeatureConfiguration.CODEC);
        
        FeaturePlaceContext<NoneFeatureConfiguration> context = 
            new FeaturePlaceContext<>(
                Optional.empty(),
                level,
                level.getChunkSource().getGenerator(),
                random,
                pos,
                NoneFeatureConfiguration.INSTANCE
            );
        
        if (!treeFeature.place(context)) {
            level.setBlock(pos, state, 3);
        }
    }
    
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
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

