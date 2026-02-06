package com.cahcap.herbalcurative.neoforge.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;

/**
 * NeoForge-specific CauldronBlock with platform-specific overrides
 */
public class CauldronBlock extends com.cahcap.herbalcurative.common.block.CauldronBlock {
    
    public CauldronBlock(Properties properties) {
        super(properties);
    }
    
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        // Use common logic to check if destruction should be prevented
        if (!handleBlockDestruction(state, level, pos, player, fluid)) {
            return false; // Prevent block removal
        }
        
        // For other cases, use standard behavior
        state.getBlock().destroy(level, pos, state);
        return level.setBlock(pos, fluid.createLegacyBlock(), 
                level.isClientSide ? Block.UPDATE_ALL_IMMEDIATE : Block.UPDATE_ALL);
    }
    
    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }
}
