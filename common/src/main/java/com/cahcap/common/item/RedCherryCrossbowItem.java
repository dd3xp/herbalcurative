package com.cahcap.common.item;

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
 * Red Cherry Crossbow
 * Supports all bolt magazines (BoltMagazineItem subclasses) as ammo
 * - Arrows from bolt magazine cannot be picked up (by default)
 * - Arrows from regular arrows can be picked up
 * - Bolt magazine is consumed via durability when loading (not when shooting)
 * 
 * Architecture:
 * - Magazines define what projectile to load (arrow, firework, etc.) via createProjectile()
 * - Magazines consume durability when loading via consumeOnLoad()
 * - Magazines can apply special effects after shooting via onProjectileShot()
 */
public class RedCherryCrossbowItem extends CrossbowItem {
    
    public RedCherryCrossbowItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public int getEnchantmentValue() {
        return 1; // Same as vanilla crossbow
    }
    
    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return (stack) -> stack.is(Items.ARROW) || 
                          stack.is(Items.SPECTRAL_ARROW) || 
                          stack.is(Items.TIPPED_ARROW) ||
                          stack.getItem() instanceof BoltMagazineItem;
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
            
            // Check if using a bolt magazine
            if (!ammo.isEmpty() && ammo.getItem() instanceof BoltMagazineItem magazine) {
                // Check if magazine has durability remaining
                if (magazine.canUse(ammo)) {
                    int magazineSlot = findItemSlot(player, ammo);
                    if (magazineSlot >= 0) {
                        // 1. Get the projectile from the magazine (could be arrow, firework, etc.)
                        ItemStack projectile = magazine.createProjectile(ammo, crossbow, player);
                        
                        // 2. CONSUME MAGAZINE DURABILITY WHEN LOADING (not when shooting)
                        magazine.consumeOnLoad(ammo, player);
                        
                        // 3. Temporarily replace magazine with the projectile in inventory
                        ItemStack originalMagazine = ammo.copy();
                        player.getInventory().setItem(magazineSlot, projectile);
                        
                        // 4. Load crossbow (will consume the temporary projectile)
                        super.releaseUsing(crossbow, level, entity, timeLeft);
                        
                        // 5. Restore the magazine to inventory
                        player.getInventory().setItem(magazineSlot, originalMagazine);
                        
                        return;
                    }
                }
            }
        }
        
        // For regular arrows or other cases, use vanilla behavior
        super.releaseUsing(crossbow, level, entity, timeLeft);
    }
    
    /**
     * Find the slot of a specific item stack in player's inventory
     */
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
        
        // Apply magazine special effects to the projectile after shooting
        if (projectile instanceof AbstractArrow arrow && shooter instanceof Player player) {
            ItemStack magazine = findBoltMagazine(player);
            if (magazine != null && !magazine.isEmpty() && magazine.getItem() instanceof BoltMagazineItem boltMag) {
                // Let the magazine apply its special effects (e.g., make arrow non-pickupable)
                boltMag.onProjectileShot(arrow, magazine, player);
            }
        }
    }
    
    /**
     * Find a bolt magazine in player's inventory with remaining durability
     */
    private ItemStack findBoltMagazine(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BoltMagazineItem magazine) {
                if (magazine.canUse(stack)) {
                    return stack;
                }
            }
        }
        return null;
    }
}

