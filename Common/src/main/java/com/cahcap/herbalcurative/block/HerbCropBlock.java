package com.cahcap.herbalcurative.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * Base class for herb crops
 * Has 10 growth stages (0-9), more than vanilla crops (0-7)
 * Uses parent CropBlock's randomTick for growth logic
 */
public class HerbCropBlock extends CropBlock {
    
    public static final int MAX_AGE = 9;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, MAX_AGE);
    
    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.6D, 16.0D),   // Stage 0
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 3.2D, 16.0D),   // Stage 1
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.8D, 16.0D),   // Stage 2
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.4D, 16.0D),   // Stage 3
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D),   // Stage 4
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 9.6D, 16.0D),   // Stage 5
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 11.2D, 16.0D),  // Stage 6
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.8D, 16.0D),  // Stage 7
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.4D, 16.0D),  // Stage 8
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)   // Stage 9 (mature)
    };
    
    private final Supplier<? extends ItemLike> seedSupplier;
    
    public HerbCropBlock(Properties properties, Supplier<? extends ItemLike> seedSupplier) {
        super(properties);
        this.seedSupplier = seedSupplier;
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_AGE[this.getAge(state)];
    }
    
    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }
    
    @Override
    public int getMaxAge() {
        return MAX_AGE;
    }
    
    @Override
    protected ItemLike getBaseSeedId() {
        return seedSupplier.get();
    }
    
    // Note: We don't override randomTick() - parent CropBlock handles growth correctly
    // using our getMaxAge() value of 9
}

