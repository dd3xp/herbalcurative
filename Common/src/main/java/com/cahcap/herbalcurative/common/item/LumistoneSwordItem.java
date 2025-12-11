package com.cahcap.herbalcurative.common.item;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

/**
 * Lumistone Sword
 * 80 durability, same stats as iron sword
 * On hit: deals 2 magic damage with 2-second cooldown
 * 
 * The magic damage behavior is handled by LumistoneToolHandler event
 * Any subclass of LumistoneSwordItem will automatically inherit this behavior
 */
public class LumistoneSwordItem extends SwordItem {

    public LumistoneSwordItem(Properties properties) {
        super(Tiers.IRON, properties);
    }
    
    @Override
    public int getEnchantmentValue() {
        return 5; // Stone tool enchantability (despite iron stats)
    }
}

