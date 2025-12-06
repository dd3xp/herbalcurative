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
 * Pyrisage flower block - grows on nylium or soul sand (dirt-based Nether blocks)
 * Generates in Warped Forest, Crimson Forest, and Soul Sand Valley biomes with flame particle effects
 */
public class PyrisageFlowerBlock extends HerbFlowerBlock {
    
    public PyrisageFlowerBlock(Holder<MobEffect> effect, float seconds, Properties properties) {
        super(effect, seconds, properties);
    }
    
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        // Allow placement on Warped Nylium, Crimson Nylium, or Soul Sand (dirt-like blocks)
        return state.is(Blocks.WARPED_NYLIUM) || state.is(Blocks.CRIMSON_NYLIUM) || state.is(Blocks.SOUL_SAND);
    }
    
    /**
     * Generate flame particles around the herb head to create a burning effect
     * Particles don't rise up, just flicker around the top of the herb
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.4 + random.nextDouble() * 0.2;
        double z = pos.getZ() + 0.5;
        
        double offsetX = (random.nextDouble() - 0.5) * 0.6;
        double offsetZ = (random.nextDouble() - 0.5) * 0.6;
        
        if (random.nextInt(10) < 3) {
            level.addParticle(
                ParticleTypes.FLAME,
                x + offsetX,
                y,
                z + offsetZ,
                0, 0, 0
            );
        }
    }
}
