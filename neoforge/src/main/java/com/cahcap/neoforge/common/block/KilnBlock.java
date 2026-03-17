package com.cahcap.neoforge.common.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

/**
 * NeoForge-specific KilnBlock with platform-specific overrides.
 */
public class KilnBlock extends com.cahcap.common.block.KilnBlock {

    public KilnBlock(Properties properties) {
        super(properties);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }
}
