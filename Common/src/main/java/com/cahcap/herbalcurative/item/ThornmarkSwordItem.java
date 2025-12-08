package com.cahcap.herbalcurative.item;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

/**
 * Thornmark Sword
 * 80 durability, same stats as iron sword
 * On hit: deals 2 magic damage with 2-second cooldown
 * 
 * The magic damage behavior is handled by ThornmarkToolHandler event
 * Any subclass of ThornmarkSwordItem will automatically inherit this behavior
 */
public class ThornmarkSwordItem extends SwordItem {

    public ThornmarkSwordItem(Properties properties) {
        super(Tiers.IRON, properties);
    }
}

