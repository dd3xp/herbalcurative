package com.cahcap.common.multiblock;

import com.cahcap.common.registry.ModRegistries;
import net.minecraft.sounds.SoundEvents;

/**
 * Herb Cabinet Multiblock Structure (3x2, wall-mounted)
 * <p>
 * Layout (default facing SOUTH, viewed from front):
 * <pre>
 * Layer y=1: [Log][Log][Log]
 * Layer y=0: [Log][Log (MASTER/TRIGGER)][Log]
 * </pre>
 */
public class MultiblockHerbCabinet {

    public static final Multiblock BLUEPRINT = Multiblock.builder()
            .layer(0, "LML")
            .layer(1, "LLL")
            .define('L', state -> state.is(ModRegistries.RED_CHERRY_LOG.get()))
            .define('M', state -> state.is(ModRegistries.RED_CHERRY_LOG.get()))
            .master('M')
            .trigger('M')
            .result(() -> ModRegistries.HERB_CABINET.get())
            .sound(SoundEvents.WOOD_PLACE, 1.0f, 1.0f)
            .build();
}
