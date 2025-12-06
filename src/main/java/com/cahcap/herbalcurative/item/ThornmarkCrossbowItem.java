package com.cahcap.herbalcurative.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Thornmark Crossbow
 * Supports Thornmark Bolt Magazine as ammo
 * Arrows from bolt magazine cannot be picked up
 * Arrows from regular arrows can be picked up
 * Bolt magazine is consumed via durability, not item count
 */
public class ThornmarkCrossbowItem extends CrossbowItem {
    
    public ThornmarkCrossbowItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return (stack) -> stack.is(Items.ARROW) || 
                          stack.is(Items.SPECTRAL_ARROW) || 
                          stack.is(Items.TIPPED_ARROW) ||
                          stack.getItem() instanceof ThornmarkBoltMagazineItem;
    }
    
    @Override
    public void releaseUsing(ItemStack crossbow, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            super.releaseUsing(crossbow, level, entity, timeLeft);
            return;
        }
        
        // Check if crossbow is not already charged
        if (!isCharged(crossbow)) {
            // Find ammo
            ItemStack ammo = player.getProjectile(crossbow);
            
            if (!ammo.isEmpty() && ammo.getItem() instanceof ThornmarkBoltMagazineItem) {
                // For bolt magazine: temporarily replace with arrow, load, then restore magazine
                int magazineSlot = findItemSlot(player, ammo);
                if (magazineSlot >= 0) {
                    // Save magazine state
                    ItemStack originalMagazine = ammo.copy();
                    
                    // Temporarily replace with arrow
                    ItemStack tempArrow = new ItemStack(Items.ARROW, 1);
                    player.getInventory().setItem(magazineSlot, tempArrow);
                    
                    // Load crossbow (will consume the temp arrow)
                    super.releaseUsing(crossbow, level, entity, timeLeft);
                    
                    // Restore original magazine
                    player.getInventory().setItem(magazineSlot, originalMagazine);
                    return;
                }
            }
        }
        
        // For regular arrows or other cases, use vanilla behavior
        super.releaseUsing(crossbow, level, entity, timeLeft);
    }
    
    private int findItemSlot(Player player, ItemStack target) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack == target) {
                return i;
            }
        }
        return -1;
    }
    
    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity, 
                                    float inaccuracy, float angle, @Nullable LivingEntity target) {
        super.shootProjectile(shooter, projectile, index, velocity, inaccuracy, angle, target);
        
        // Handle bolt magazine consumption and arrow pickup status
        if (projectile instanceof AbstractArrow arrow && shooter instanceof Player player) {
            // Find and consume bolt magazine durability
            ItemStack magazine = findBoltMagazine(player);
            if (magazine != null && !magazine.isEmpty()) {
                // Consume magazine durability
                magazine.hurtAndBreak(1, player, player.getEquipmentSlotForItem(magazine));
                // Set arrow to not be pickupable
                arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
            }
        }
    }
    
    /**
     * Find a bolt magazine in player's inventory with remaining durability
     */
    private ItemStack findBoltMagazine(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ThornmarkBoltMagazineItem) {
                if (stack.getDamageValue() < stack.getMaxDamage()) {
                    return stack;
                }
            }
        }
        return null;
    }
}
