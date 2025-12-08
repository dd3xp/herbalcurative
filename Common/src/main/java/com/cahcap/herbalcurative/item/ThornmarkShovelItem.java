package com.cahcap.herbalcurative.item;

import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tiers;

/**
 * Thornmark Shovel
 * 80 durability, same stats as iron shovel
 * Breaking gravel has 50% chance to drop flint (instead of vanilla 10%)
 * 
 * The flint drop behavior is handled by GravelFlintDropHandler event
 * Any subclass of ThornmarkShovelItem will automatically inherit this behavior
 */
public class ThornmarkShovelItem extends ShovelItem {

    public ThornmarkShovelItem(Properties properties) {
        super(Tiers.IRON, properties);
    }
}

