package com.cahcap.common.multiblock;

import com.cahcap.common.registry.ModRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Cauldron Multiblock Structure (3x3x2)
 * <p>
 * Layout (default facing SOUTH):
 * <pre>
 * Layer y=0 (master layer):
 *   [LumiBricks]     [LumiBrickSlab(top)] [LumiBricks]
 *   [LumiBrickSlab(top)] [MASTER slab(top)] [LumiBrickSlab(top)]
 *   [LumiBricks]     [LumiBrickSlab(top)] [LumiBricks]
 *
 * Layer y=1:
 *   [LumiBricks][LumiBricks][LumiBricks]
 *   [LumiBricks][   Air    ][LumiBricks]
 *   [LumiBricks][LumiBricks][LumiBricks]
 * </pre>
 * Trigger: any Lumistone Brick position.
 */
public class MultiblockCauldron {

    public static final Multiblock BLUEPRINT = Multiblock.builder()
            .layer(0,
                    "BCB",
                    "CMC",
                    "BCB")
            .layer(1,
                    "BBB",
                    "B.B",
                    "BBB")
            .define('B', state -> state.is(ModRegistries.LUMISTONE_BRICKS.get()))
            .define('C', state -> state.is(ModRegistries.LUMISTONE_BRICK_SLAB.get())
                    && state.hasProperty(SlabBlock.TYPE)
                    && state.getValue(SlabBlock.TYPE) == SlabType.TOP)
            .define('M', state -> state.is(ModRegistries.LUMISTONE_BRICK_SLAB.get())
                    && state.hasProperty(SlabBlock.TYPE)
                    && state.getValue(SlabBlock.TYPE) == SlabType.TOP)
            .define('.', state -> state.isAir())
            .master('M')
            .trigger('B')
            .result(() -> ModRegistries.CAULDRON.get())
            .sound(SoundEvents.STONE_PLACE, 1.0f, 0.8f)
            .build();
}
