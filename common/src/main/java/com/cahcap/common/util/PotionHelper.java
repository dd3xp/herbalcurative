package com.cahcap.common.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

/**
 * Utility class for potion-related helpers shared across items.
 */
public class PotionHelper {

    /**
     * Get effect holder from registry ID string.
     * Uses dynamic registry lookup instead of hardcoded switch.
     */
    public static Holder<MobEffect> getEffectForType(String type) {
        // Try to parse as ResourceLocation
        ResourceLocation id = ResourceLocation.tryParse(type);
        if (id == null) return null;

        MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(id);
        if (effect == null) return null;

        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);
    }

    /**
     * Check if an effect is instantaneous (like heal/harm) using vanilla API.
     */
    public static boolean isInstantEffect(Holder<MobEffect> effect) {
        return effect != null && effect.value().isInstantenous();
    }
}
