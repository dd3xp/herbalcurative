package com.cahcap.herbalcurative.common.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Red Cherry Bolt Magazine
 * 10 durability
 * When in inventory, crossbow prioritizes using bolt magazine instead of arrows
 * Arrows fired from bolt magazine cannot be picked up
 * Can only be enchanted with Unbreaking (naturally limited by vanilla enchantment system)
 * 
 * This is the basic magazine type - future magazines can extend BoltMagazineItem
 * to provide special effects (debuffs, fireworks, etc.)
 */
public class RedCherryBoltMagazineItem extends BoltMagazineItem {
    
    public RedCherryBoltMagazineItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public ItemStack createProjectile(ItemStack magazine, ItemStack crossbow, Player player) {
        // Red Cherry Bolt Magazine: loads standard arrows
        return new ItemStack(Items.ARROW, 1);
    }
    
    // Inherits default behavior:
    // - consumeOnLoad: consumes 1 durability when loading
    // - onProjectileShot: makes arrows non-pickupable
    // - canUse: checks if magazine has durability remaining
}

