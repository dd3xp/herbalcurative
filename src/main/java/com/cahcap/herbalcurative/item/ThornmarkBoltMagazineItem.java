package com.cahcap.herbalcurative.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Thornmark Bolt Magazine
 * 80 durability
 * When in inventory, crossbow prioritizes using bolt magazine instead of arrows
 * Arrows fired from bolt magazine cannot be picked up
 */
public class ThornmarkBoltMagazineItem extends Item {
    
    public ThornmarkBoltMagazineItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }
    
    @Override
    public int getEnchantmentValue() {
        return 1;
    }
}
