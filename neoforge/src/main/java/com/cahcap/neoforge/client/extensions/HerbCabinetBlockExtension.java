package com.cahcap.neoforge.client.extensions;

import com.cahcap.common.blockentity.HerbCabinetBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;

/**
 * Client extension for HerbCabinetBlock to display the original block particles
 * when the multiblock structure is destroyed.
 */
public class HerbCabinetBlockExtension implements IClientBlockExtensions {

    public static final HerbCabinetBlockExtension INSTANCE = new HerbCabinetBlockExtension();

    private HerbCabinetBlockExtension() {
    }

    @Override
    public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        if (level.getBlockEntity(pos) instanceof HerbCabinetBlockEntity be) {
            BlockState originalState = be.getOriginalBlockState();
            if (originalState != null && !originalState.isAir()) {
                Minecraft.getInstance().particleEngine.destroy(pos, originalState);
                return true;
            }
        }
        return false;
    }
}
