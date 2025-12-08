package com.cahcap.herbalcurative.item;

import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Tiers;

/**
 * Lumistone Hoe
 * 80 durability, same stats as iron hoe
 * Right-click on mature crops to harvest and replant with 1 seed (keeping extra seeds and all produce)
 * 
 * The harvest behavior is handled by LumistoneToolHandler event
 * Any subclass of LumistoneHoeItem will automatically inherit this behavior
 */
public class LumistoneHoeItem extends HoeItem {

    public LumistoneHoeItem(Properties properties) {
        super(Tiers.IRON, properties);
    }
}

