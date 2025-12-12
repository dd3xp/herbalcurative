package com.cahcap.herbalcurative.common.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Red Cherry Bolt Magazine
 * 10 durability
 * When in inventory, crossbow prioritizes using bolt magazine instead of arrows
 * Arrows fired from bolt magazine cannot be picked up
 * Can only be enchanted with Unbreaking (naturally limited by vanilla enchantment system)
 */
public class RedCherryBoltMagazineItem extends Item {
    
    public RedCherryBoltMagazineItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }
    
    @Override
    public int getEnchantmentValue() {
        return 1; // Same as vanilla crossbow
    }
}

