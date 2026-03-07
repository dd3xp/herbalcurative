package com.cahcap.neoforge.client.extensions;

import com.cahcap.common.blockentity.MultiblockPartBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;

/**
 * Generic client extension for all multiblock structure blocks.
 * Displays the original block particles when destroyed.
 * 
 * Works with any block that has a MultiblockPartBlockEntity.
 */
public class MultiblockBlockExtension implements IClientBlockExtensions {

    public static final MultiblockBlockExtension INSTANCE = new MultiblockBlockExtension();

    private MultiblockBlockExtension() {
    }

    @Override
    public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        if (level.getBlockEntity(pos) instanceof MultiblockPartBlockEntity be) {
            BlockState originalState = be.getOriginalBlockState();
            if (originalState != null && !originalState.isAir()) {
                Minecraft.getInstance().particleEngine.destroy(pos, originalState);
            }
            return true;
        }
        return false;
    }
}
