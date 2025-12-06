package com.cahcap.herbalcurative.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Rosynia flower block - only grows on End Stone
 * Generates in The End dimension with portal particle effects
 */
public class RosyniaFlowerBlock extends HerbFlowerBlock {
    
    public RosyniaFlowerBlock(Holder<MobEffect> effect, float seconds, Properties properties) {
        super(effect, seconds, properties);
    }
    
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        // Only allow placement on End Stone
        return state.is(Blocks.END_STONE);
    }
    
    /**
     * Generate ender portal particles floating up from the herb
     * Increased particle count for more visible effect
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        
        double offsetX = (random.nextDouble() - 0.5) * 0.6;
        double offsetZ = (random.nextDouble() - 0.5) * 0.6;
        
        if (random.nextInt(2) == 0) {
            level.addParticle(
                ParticleTypes.PORTAL,
                x + offsetX,
                y,
                z + offsetZ,
                (random.nextDouble() - 0.5) * 0.1,
                0.05 + random.nextDouble() * 0.05,
                (random.nextDouble() - 0.5) * 0.1
            );
            
            // Additional particle 1/3 of the time
            if (random.nextInt(3) == 0) {
                double offsetX2 = (random.nextDouble() - 0.5) * 0.6;
                double offsetZ2 = (random.nextDouble() - 0.5) * 0.6;
                
                level.addParticle(
                    ParticleTypes.PORTAL,
                    x + offsetX2,
                    y,
                    z + offsetZ2,
                    (random.nextDouble() - 0.5) * 0.1,
                    0.05 + random.nextDouble() * 0.05,
                    (random.nextDouble() - 0.5) * 0.1
                );
            }
        }
    }
}
