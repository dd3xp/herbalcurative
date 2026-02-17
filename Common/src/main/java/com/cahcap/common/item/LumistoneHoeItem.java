package com.cahcap.common.item;

import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Tiers;

/**
 * Lumistone Hoe
 * - 80 durability, same stats as iron hoe
 * - Regenerates 1 durability per second
 * - Auto-pickup: Broken block drops go directly to inventory
 * 
 * Auto-pickup is handled by LumistoneToolHandler event
 */
public class LumistoneHoeItem extends HoeItem {

    public LumistoneHoeItem(Properties properties) {
        super(Tiers.IRON, properties);
    }
    
    @Override
    public int getEnchantmentValue() {
        return 5; // Stone tool enchantability (despite iron stats)
    }
}

