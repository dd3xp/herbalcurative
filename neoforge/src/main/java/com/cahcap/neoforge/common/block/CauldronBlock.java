package com.cahcap.neoforge.common.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

/**
 * NeoForge-specific CauldronBlock with platform-specific overrides.
 */
public class CauldronBlock extends com.cahcap.common.block.CauldronBlock {

    public CauldronBlock(Properties properties) {
        super(properties);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }
}
