package com.cahcap.herbalcurative.item;

import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Tiers;

/**
 * Thornmark Hoe
 * 80 durability, same stats as iron hoe
 * Right-click on mature crops to harvest and replant with 1 seed (keeping extra seeds and all produce)
 * 
 * The harvest behavior is handled by ThornmarkToolHandler event
 * Any subclass of ThornmarkHoeItem will automatically inherit this behavior
 */
public class ThornmarkHoeItem extends HoeItem {

    public ThornmarkHoeItem(Properties properties) {
        super(Tiers.IRON, properties);
    }
}
