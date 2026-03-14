package com.cahcap.neoforge.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import com.cahcap.neoforge.client.extensions.MultiblockBlockExtension;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.PushReaction;

/**
 * NeoForge-specific HerbVaultBlock with platform-specific overrides.
 */
public class HerbVaultBlock extends com.cahcap.common.block.HerbVaultBlock {

    public HerbVaultBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (!handleBlockDestruction(state, level, pos, player, fluid)) {
            return false;
        }
        state.getBlock().destroy(level, pos, state);
        return level.setBlock(pos, fluid.createLegacyBlock(),
                level.isClientSide ? Block.UPDATE_ALL_IMMEDIATE : Block.UPDATE_ALL);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public boolean addLandingEffects(BlockState state, ServerLevel level, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        return MultiblockBlockExtension.handleLandingEffects(level, pos, entity, numberOfParticles);
    }

    @Override
    public boolean addRunningEffects(BlockState state, Level level, BlockPos pos, Entity entity) {
        return MultiblockBlockExtension.handleRunningEffects(level, pos, entity);
    }
}
