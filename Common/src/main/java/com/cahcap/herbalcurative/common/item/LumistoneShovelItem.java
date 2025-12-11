package com.cahcap.herbalcurative.common.item;

import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tiers;

/**
 * Lumistone Shovel
 * - 80 durability, same stats as iron shovel
 * - Regenerates 1 durability per second
 * - Auto-pickup: Broken block drops go directly to inventory
 * 
 * Auto-pickup is handled by LumistoneToolHandler event
 */
public class LumistoneShovelItem extends ShovelItem {

    public LumistoneShovelItem(Properties properties) {
        super(Tiers.IRON, properties);
    }
    
    @Override
    public int getEnchantmentValue() {
        return 5; // Stone tool enchantability (despite iron stats)
    }
}

