package com.cahcap.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

/**
 * Crystbud flower block - grows on basalt or netherrack (stone-based Nether blocks)
 * Generates in Basalt Deltas and Nether Wastes biomes with crystallization particle effects
 */
public class CrystbudFlowerBlock extends HerbFlowerBlock {
    
    public CrystbudFlowerBlock(Holder<MobEffect> effect, float seconds, Properties properties) {
        super(effect, seconds, properties);
    }
    
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        // Only allow placement on Basalt or Netherrack (Nether stone-like blocks)
        return state.is(Blocks.BASALT) || state.is(Blocks.NETHERRACK) || 
               state.is(Blocks.BLACKSTONE) || state.is(Blocks.POLISHED_BASALT);
    }
    
    /**
     * Generate purple redstone particles around the herb to create a crystallization effect
     * Particles float around the top of the herb
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.4 + random.nextDouble() * 0.2;
        double z = pos.getZ() + 0.5;
        
        double offsetX = (random.nextDouble() - 0.5) * 0.6;
        double offsetZ = (random.nextDouble() - 0.5) * 0.6;
        
        if (random.nextInt(10) < 3) {
            // Purple color: RGB(100, 57, 181) normalized to 0-1
            level.addParticle(
                new DustParticleOptions(
                    new Vector3f(100f/255f, 57f/255f, 181f/255f),
                    1.0f
                ),
                x + offsetX,
                y,
                z + offsetZ,
                0, 0, 0
            );
        }
    }
}

