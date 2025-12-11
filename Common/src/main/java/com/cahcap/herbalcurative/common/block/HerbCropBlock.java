package com.cahcap.herbalcurative.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
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
 * - 4 growth stages (0-3)
 * - Fixed growth time: 5 minutes (6000 ticks) per stage
 * - Total time to mature: 15 minutes (3 stages × 5 minutes)
 * - Uses scheduleTick for deterministic growth (no randomness)
 */
public class HerbCropBlock extends CropBlock {
    
    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, MAX_AGE);
    
    /** Growth interval: 5 minutes = 6000 ticks */
    private static final int GROWTH_INTERVAL = 6000;
    
    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D),   // Stage 0
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D),   // Stage 1
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D),  // Stage 2
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)   // Stage 3 (mature)
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
    
    /**
     * When crop is placed, schedule first growth tick
     */
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        
        // Only schedule tick on server side and if not already mature
        if (!level.isClientSide() && !this.isMaxAge(state)) {
            level.scheduleTick(pos, this, GROWTH_INTERVAL);
        }
    }
    
    /**
     * Disable random tick growth - we use scheduled ticks instead
     */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Do nothing - we use scheduleTick for deterministic growth
    }
    
    /**
     * Fixed-time growth logic
     * Called every GROWTH_INTERVAL ticks (5 minutes)
     */
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        
        // Check light level (crops need light to grow)
        if (level.getRawBrightness(pos, 0) >= 9) {
            int currentAge = this.getAge(state);
            
            // Grow to next stage if not mature
            if (currentAge < this.getMaxAge()) {
                level.setBlock(pos, this.getStateForAge(currentAge + 1), 2);
                
                // Schedule next growth tick if not mature yet
                if (currentAge + 1 < this.getMaxAge()) {
                    level.scheduleTick(pos, this, GROWTH_INTERVAL);
                }
            }
        } else {
            // If not enough light, check again later
            level.scheduleTick(pos, this, GROWTH_INTERVAL);
        }
    }
}

