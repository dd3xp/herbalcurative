package com.cahcap.common.multiblock;

import com.cahcap.common.registry.ModRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Herb Vault Multiblock Structure (3x3x3)
 * <p>
 * Layout (default facing SOUTH, front = +Z):
 * <pre>
 * Layer y=-1 (bottom): all Lumistone Bricks
 *
 * Layer y=0 (master layer):
 *   [RedCherryLog] [LumiBrickWall]       [RedCherryLog]
 *   [LumiBrickWall][MagicAlloyBlock (MASTER)][LumiBrickWall]
 *   [RedCherryLog] [RedCherryFence (TRIGGER)][RedCherryLog]
 *
 * Layer y=1 (top):
 *   [Slab(bottom)][Slab(bottom)]  [Slab(bottom)]
 *   [Slab(bottom)][LumiBricks]    [Slab(bottom)]
 *   [Slab(bottom)][Slab(bottom)]  [Slab(bottom)]
 * </pre>
 * Trigger: Red Cherry Fence — the front door.
 */
public class MultiblockHerbVault {

    public static final Multiblock BLUEPRINT = Multiblock.builder()
            .layer(-1,
                    "BBB",
                    "BBB",
                    "BBB")
            .layer(0,
                    "LWL",
                    "W#W",
                    "LFL")
            .layer(1,
                    "SSS",
                    "SBS",
                    "SSS")
            .define('B', state -> state.is(ModRegistries.LUMISTONE_BRICKS.get()))
            .define('L', state -> state.is(ModRegistries.RED_CHERRY_LOG.get()))
            .define('W', state -> state.is(ModRegistries.LUMISTONE_BRICK_WALL.get()))
            .define('#', state -> state.is(ModRegistries.MAGIC_ALLOY_BLOCK.get()))
            .define('F', state -> state.is(ModRegistries.RED_CHERRY_FENCE.get()))
            .define('S', state -> state.is(ModRegistries.LUMISTONE_BRICK_SLAB.get())
                    && state.hasProperty(SlabBlock.TYPE)
                    && state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM)
            .master('#')
            .trigger('F')
            .result(() -> ModRegistries.HERB_VAULT.get())
            .sound(SoundEvents.STONE_PLACE, 1.0f, 0.8f)
            .build();
}
