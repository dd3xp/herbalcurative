package com.cahcap.neoforge.client.extensions;

import com.cahcap.common.blockentity.CauldronBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;

/**
 * Client extension for CauldronBlock to display the original block particles
 * when the multiblock structure is destroyed.
 * 
 * Uses CauldronBlockEntity.getOriginalBlockState() to determine the correct
 * original block for each position in the multiblock.
 */
public class CauldronBlockExtension implements IClientBlockExtensions {

    public static final CauldronBlockExtension INSTANCE = new CauldronBlockExtension();

    private CauldronBlockExtension() {
    }

    @Override
    public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        if (level.getBlockEntity(pos) instanceof CauldronBlockEntity be) {
            BlockState originalState = be.getOriginalBlockState();
            if (originalState != null && !originalState.isAir()) {
                Minecraft.getInstance().particleEngine.destroy(pos, originalState);
                return true;
            }
            return true;
        }
        return false;
    }
}
