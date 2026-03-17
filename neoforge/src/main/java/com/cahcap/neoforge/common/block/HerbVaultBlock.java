package com.cahcap.neoforge.common.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

/**
 * NeoForge-specific HerbVaultBlock with platform-specific overrides.
 * Left-click interception is now handled by HerbStorageLeftClickHandler.
 */
public class HerbVaultBlock extends com.cahcap.common.block.HerbVaultBlock {

    public HerbVaultBlock(Properties properties) {
        super(properties);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }
}
