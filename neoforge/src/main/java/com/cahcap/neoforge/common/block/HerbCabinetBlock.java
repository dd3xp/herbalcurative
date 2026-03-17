package com.cahcap.neoforge.common.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

/**
 * NeoForge-specific HerbCabinetBlock with platform-specific overrides.
 * Left-click interception is now handled by HerbStorageLeftClickHandler.
 */
public class HerbCabinetBlock extends com.cahcap.common.block.HerbCabinetBlock {

    public HerbCabinetBlock(Properties properties) {
        super(properties);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }
}
