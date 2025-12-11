package com.cahcap.herbalcurative.neoforge.client.handler;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.neoforge.common.registry.ModBlocks;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Client-side handler for herb flower particle effects
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID, value = Dist.CLIENT)
public class HerbFlowerParticleHandler {

    @SubscribeEvent
    public static void onClientTick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ClientLevel level)) {
            return;
        }

        // Only process every 20 ticks (once per second) to reduce performance impact
        if (level.getGameTime() % 20 != 0) {
            return;
        }

        // Randomly spawn particles for herb flowers
        RandomSource random = level.random;

        // Get random block position near player
        var players = level.players();
        if (players.isEmpty()) {
            return;
        }

        var player = players.get(random.nextInt(players.size()));
        int range = 16;
        
        for (int i = 0; i < 3; i++) {
            BlockPos pos = player.blockPosition().offset(
                random.nextInt(range * 2) - range,
                random.nextInt(range) - range/2,
                random.nextInt(range * 2) - range
            );

            BlockState state = level.getBlockState(pos);
            
            // Crystbud - purple redstone particles (crystallization effect)
            if (state.is(ModBlocks.CRYSTBUD.get())) {
                spawnCrystbudParticles(level, pos, random);
            }
            // Pyrisage - flame particles
            else if (state.is(ModBlocks.PYRISAGE.get())) {
                spawnPyrisageParticles(level, pos, random);
            }
            // Rosynia - portal particles (starry sky effect)
            else if (state.is(ModBlocks.ROSYNIA.get())) {
                spawnRosyniaParticles(level, pos, random);
            }
        }
    }

    private static void spawnCrystbudParticles(ClientLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) < 3) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.4 + random.nextDouble() * 0.2;
            double z = pos.getZ() + 0.5;
            
            double offsetX = (random.nextDouble() - 0.5) * 0.6;
            double offsetZ = (random.nextDouble() - 0.5) * 0.6;
            
            // Purple color: RGB(100, 57, 181) normalized to 0-1
            level.addParticle(
                new DustParticleOptions(
                    new org.joml.Vector3f(100f/255f, 57f/255f, 181f/255f),
                    1.0f
                ),
                x + offsetX,
                y,
                z + offsetZ,
                0, 0, 0
            );
        }
    }

    private static void spawnPyrisageParticles(ClientLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) < 3) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.4 + random.nextDouble() * 0.2;
            double z = pos.getZ() + 0.5;
            
            double offsetX = (random.nextDouble() - 0.5) * 0.6;
            double offsetZ = (random.nextDouble() - 0.5) * 0.6;
            
            level.addParticle(
                ParticleTypes.FLAME,
                x + offsetX,
                y,
                z + offsetZ,
                0, 0, 0
            );
        }
    }

    private static void spawnRosyniaParticles(ClientLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(2) == 0) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.5;
            
            double offsetX = (random.nextDouble() - 0.5) * 0.6;
            double offsetZ = (random.nextDouble() - 0.5) * 0.6;
            
            level.addParticle(
                ParticleTypes.PORTAL,
                x + offsetX,
                y,
                z + offsetZ,
                (random.nextDouble() - 0.5) * 0.1,
                0.05 + random.nextDouble() * 0.05,
                (random.nextDouble() - 0.5) * 0.1
            );
            
            // Additional particle 1/3 of the time
            if (random.nextInt(3) == 0) {
                double offsetX2 = (random.nextDouble() - 0.5) * 0.6;
                double offsetZ2 = (random.nextDouble() - 0.5) * 0.6;
                
                level.addParticle(
                    ParticleTypes.PORTAL,
                    x + offsetX2,
                    y,
                    z + offsetZ2,
                    (random.nextDouble() - 0.5) * 0.1,
                    0.05 + random.nextDouble() * 0.05,
                    (random.nextDouble() - 0.5) * 0.1
                );
            }
        }
    }
}

