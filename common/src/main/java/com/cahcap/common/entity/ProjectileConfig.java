package com.cahcap.common.entity;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

import java.util.List;

public record ProjectileConfig(
    List<Holder<MobEffect>> effects,
    int duration,
    int amplifier,
    int color,
    boolean lingering,
    boolean isInstant
) {}
