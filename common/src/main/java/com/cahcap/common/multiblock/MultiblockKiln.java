package com.cahcap.common.multiblock;

import com.cahcap.common.registry.ModRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Kiln Multiblock Structure (3x3x3)
 * <p>
 * Layout (default facing NORTH, front = -Z):
 * <pre>
 * Layer y=-1 (bottom):
 *   [LumiBricks][LumiBricks]  [LumiBricks]
 *   [LumiBricks][PyrisageSoil][LumiBricks]
 *   [LumiBricks][LumiBricks]  [LumiBricks]
 *
 * Layer y=0 (master layer):
 *   [LumiBricks][Slab(top, TRIGGER)][LumiBricks]   ← front (z=0)
 *   [LumiBricks][Pyrisage (MASTER)] [LumiBricks]
 *   [LumiBricks][LumiBricks]        [LumiBricks]   ← back (z=2)
 *
 * Layer y=1 (top):
 *   [Slab(bottom)][LumiBricks][Slab(bottom)]
 *   [Slab(bottom)][LumiBricks][Slab(bottom)]
 *   [Slab(bottom)][LumiBricks][Slab(bottom)]
 * </pre>
 * Trigger: Lumistone Brick Slab (top half) — the front opening.
 */
public class MultiblockKiln {

    public static final Multiblock BLUEPRINT = Multiblock.builder()
            .layer(-1,
                    "BBB",
                    "BPB",
                    "BBB")
            .layer(0,
                    "BTB",
                    "B#B",
                    "BBB")
            .layer(1,
                    "SbS",
                    "SbS",
                    "SbS")
            .define('B', state -> state.is(ModRegistries.LUMISTONE_BRICKS.get()))
            .define('P', state -> state.is(Blocks.WARPED_NYLIUM) || state.is(Blocks.CRIMSON_NYLIUM)
                    || state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL))
            .define('#', state -> state.is(ModRegistries.PYRISAGE.get()))
            .define('T', state -> state.is(ModRegistries.LUMISTONE_BRICK_SLAB.get())
                    && state.hasProperty(SlabBlock.TYPE)
                    && state.getValue(SlabBlock.TYPE) == SlabType.TOP)
            .define('b', state -> state.is(ModRegistries.LUMISTONE_BRICKS.get()))
            .define('S', state -> state.is(ModRegistries.LUMISTONE_BRICK_SLAB.get())
                    && state.hasProperty(SlabBlock.TYPE)
                    && state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM)
            .master('#')
            .trigger('T')
            .mirrorable()
            .result(() -> ModRegistries.KILN.get())
            .sound(SoundEvents.STONE_PLACE, 1.0f, 0.8f)
            .build();
}
