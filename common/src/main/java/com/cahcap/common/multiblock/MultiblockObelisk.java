package com.cahcap.common.multiblock;

import com.cahcap.common.registry.ModRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Obelisk Multiblock Structure (3x3x3)
 * <p>
 * Layout (default facing NORTH, front = -Z):
 * <pre>
 * Layer y=-1 (bottom):
 *   [Slab(b)][LumiBricks][Slab(b)]
 *   [LumiBricks][Lumistone][LumiBricks]
 *   [Slab(b)][LumiBricks][Slab(b)]
 *
 * Layer y=0 (master layer):
 *   [Air][Air][Air]
 *   [Air][Lumistone (MASTER)][Air]
 *   [Air][Air][Air]
 *
 * Layer y=1 (top):
 *   [Slab(b)][Slab(b)]  [Slab(b)]
 *   [Slab(b)][Lumistone][Slab(b)]
 *   [Slab(b)][Slab(b)]  [Slab(b)]
 * </pre>
 * Right-click any of the 4 edge LumiBricks on layer -1 to assemble.
 */
public class MultiblockObelisk {

    public static final Multiblock BLUEPRINT = Multiblock.builder()
            .layer(-1,
                    "STS",
                    "TLT",
                    "STS")
            .layer(0,
                    "...",
                    ".#.",
                    "...")
            .layer(1,
                    "sss",
                    "sLs",
                    "sss")
            .define('s', state -> state.is(ModRegistries.LUMISTONE_SLAB.get())
                    && state.hasProperty(SlabBlock.TYPE)
                    && state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM)
            .define('S', state -> state.is(ModRegistries.LUMISTONE_BRICK_SLAB.get())
                    && state.hasProperty(SlabBlock.TYPE)
                    && state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM)
            .define('T', state -> state.is(ModRegistries.LUMISTONE_BRICKS.get()))
            .define('L', state -> state.is(ModRegistries.LUMISTONE.get()))
            .define('#', state -> state.is(ModRegistries.LUMISTONE.get()))
            .define('.', state -> true)
            .master('#')
            .trigger('T')
            .result(() -> ModRegistries.OBELISK.get())
            .sound(SoundEvents.STONE_PLACE, 1.0f, 0.8f)
            .build();
}
