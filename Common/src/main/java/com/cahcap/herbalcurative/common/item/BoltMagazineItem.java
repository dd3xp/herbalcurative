package com.cahcap.herbalcurative.common.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Base class for all bolt magazine items
 * Provides common functionality and extensibility for different magazine types:
 * - Red Cherry Bolt Magazine: Basic magazine with standard arrows
 * - Magic Bolt Magazine: Arrows apply random debuffs on hit
 * - Floral Bolt Magazine: Can switch to celebration mode to shoot fireworks
 */
public abstract class BoltMagazineItem extends Item {
    
    public BoltMagazineItem(Properties properties) {
        super(properties);
    }
    
    /**
     * Called when loading the crossbow to create the projectile
     * This is where the magazine determines what gets loaded (arrow, firework, etc.)
     * 
     * @param magazine The magazine item stack
     * @param crossbow The crossbow being loaded
     * @param player The player loading the crossbow
     * @return The projectile to load into the crossbow
     */
    public abstract ItemStack createProjectile(ItemStack magazine, ItemStack crossbow, Player player);
    
    /**
     * Called when loading the crossbow to consume magazine durability
     * Default implementation: consume 1 durability
     * Override if you need different consumption logic
     * 
     * @param magazine The magazine item stack
     * @param player The player loading the crossbow
     */
    public void consumeOnLoad(ItemStack magazine, Player player) {
        magazine.hurtAndBreak(1, player, player.getEquipmentSlotForItem(magazine));
    }
    
    /**
     * Called after the projectile is shot to apply special effects
     * This is where magazines can modify arrow properties (pickup status, effects, etc.)
     * 
     * @param arrow The arrow entity that was shot
     * @param magazine The magazine that provided the projectile
     * @param shooter The player who shot the projectile
     */
    public void onProjectileShot(AbstractArrow arrow, ItemStack magazine, Player shooter) {
        // Default behavior: arrows from magazines cannot be picked up
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
    }
    
    /**
     * Check if the magazine can be used (has durability remaining)
     * 
     * @param magazine The magazine item stack
     * @return true if the magazine can be used
     */
    public boolean canUse(ItemStack magazine) {
        return magazine.getDamageValue() < magazine.getMaxDamage();
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
