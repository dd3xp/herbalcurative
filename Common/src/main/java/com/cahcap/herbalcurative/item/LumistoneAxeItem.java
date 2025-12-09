package com.cahcap.herbalcurative.item;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Tiers;

/**
 * Lumistone Axe
 * - 80 durability, same stats as iron axe
 * - Regenerates 1 durability per second
 * - Auto-pickup: Broken block drops go directly to inventory
 * 
 * Auto-pickup is handled by LumistoneToolHandler event
 */
public class LumistoneAxeItem extends AxeItem {

    public LumistoneAxeItem(Properties properties) {
        super(Tiers.IRON, properties);
    }
}

