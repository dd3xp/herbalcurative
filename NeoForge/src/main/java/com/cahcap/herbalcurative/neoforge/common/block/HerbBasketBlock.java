package com.cahcap.herbalcurative.neoforge.common.block;

import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * NeoForge-specific HerbBasketBlock with platform-specific overrides
 */
public class HerbBasketBlock extends com.cahcap.herbalcurative.common.block.HerbBasketBlock {
    
    public HerbBasketBlock(Properties properties) {
        super(properties);
    }
    
    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }
}
