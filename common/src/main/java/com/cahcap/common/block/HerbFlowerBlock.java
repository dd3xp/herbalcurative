package com.cahcap.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Base class for herb flower blocks
 * Slightly larger than vanilla flowers, emits light
 */
public class HerbFlowerBlock extends FlowerBlock {
    
    // Slightly larger than vanilla flowers (0.2-0.8 vs 0.3-0.7)
    protected static final VoxelShape SHAPE = box(3.2D, 0.0D, 3.2D, 12.8D, 8.0D, 12.8D);
    
    public HerbFlowerBlock(Holder<MobEffect> effect, float seconds, Properties properties) {
        super(effect, seconds, properties);
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}

