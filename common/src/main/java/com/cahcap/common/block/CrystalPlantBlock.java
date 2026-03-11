package com.cahcap.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Crystal Plant block that can be placed on the ground and in flower pots.
 * Uses a grayscale texture that gets tinted to the ore color.
 */
public class CrystalPlantBlock extends FlowerBlock {
    
    private final int color;
    private final String oreType;
    
    public CrystalPlantBlock(String oreType, int color, Properties properties) {
        super(MobEffects.REGENERATION, 5.0F, properties);
        this.oreType = oreType;
        this.color = color;
    }
    
    /**
     * Get the color for this crystal plant (ARGB format).
     */
    public int getColor() {
        return color;
    }
    
    /**
     * Get the ore type this plant produces.
     */
    public String getOreType() {
        return oreType;
    }
    
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || 
               state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL) ||
               state.is(Blocks.FARMLAND) || state.is(Blocks.ROOTED_DIRT) ||
               state.is(Blocks.MUD) || state.is(Blocks.MUDDY_MANGROVE_ROOTS);
    }
}
