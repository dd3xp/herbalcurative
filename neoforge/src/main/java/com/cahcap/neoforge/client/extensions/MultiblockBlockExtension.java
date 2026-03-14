package com.cahcap.neoforge.client.extensions;

import com.cahcap.common.blockentity.MultiblockPartBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;

/**
 * Generic client extension for all multiblock structure blocks.
 * Displays the original block particles when destroyed or hit.
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

    @Override
    public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
        // Suppress default hit particles for multiblock blocks (they would use the invisible block texture)
        if (target instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            if (level.getBlockEntity(pos) instanceof MultiblockPartBlockEntity be && be.isFormed()) {
                return true; // Suppress default particles
            }
        }
        return false;
    }

    // ==================== Static helpers for Block.addLandingEffects / addRunningEffects ====================

    public static boolean handleLandingEffects(Level level, BlockPos pos, LivingEntity entity, int numberOfParticles) {
        if (level.getBlockEntity(pos) instanceof MultiblockPartBlockEntity be) {
            BlockState original = be.getOriginalBlockState();
            if (original != null && !original.isAir() && level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, original),
                        entity.getX(), entity.getY(), entity.getZ(), numberOfParticles, 0, 0, 0, 0.15);
                return true;
            }
        }
        return false;
    }

    public static boolean handleRunningEffects(Level level, BlockPos pos, Entity entity) {
        if (level.getBlockEntity(pos) instanceof MultiblockPartBlockEntity be) {
            BlockState original = be.getOriginalBlockState();
            if (original != null && !original.isAir()) {
                level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, original),
                        entity.getX() + (level.random.nextDouble() - 0.5) * entity.getBbWidth(),
                        entity.getY() + 0.1,
                        entity.getZ() + (level.random.nextDouble() - 0.5) * entity.getBbWidth(),
                        -entity.getDeltaMovement().x * 4, 1.5, -entity.getDeltaMovement().z * 4);
                return true;
            }
        }
        return false;
    }
}
