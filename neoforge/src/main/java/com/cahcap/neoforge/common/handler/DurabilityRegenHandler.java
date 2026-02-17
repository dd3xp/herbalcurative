package com.cahcap.neoforge.common.handler;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.item.RedCherryCrossbowItem;
import com.cahcap.neoforge.common.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handler for durability regeneration
 * Leafweave Armor and Lumistone Tools regenerate 1 durability per second
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID)
public class DurabilityRegenHandler {
    
    private static final int DURABILITY_REGEN_TICK_INTERVAL = 20;
    private static final Map<UUID, Integer> pendingCrossbowRepair = new HashMap<>();
    private static final Map<UUID, Boolean> wasChargingCrossbow = new HashMap<>();
    
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        
        if (player.level().isClientSide()) {
            return;
        }
        
        UUID playerUUID = player.getUUID();
        
        boolean isUsingItem = player.isUsingItem();
        ItemStack activeItem = player.getUseItem();
        boolean isChargingCrossbow = isUsingItem && !activeItem.isEmpty() && 
                                     activeItem.getItem() instanceof RedCherryCrossbowItem &&
                                     !CrossbowItem.isCharged(activeItem);
        
        Boolean wasCharging = wasChargingCrossbow.getOrDefault(playerUUID, false);
        if (wasCharging && !isChargingCrossbow) {
            Integer pendingRepair = pendingCrossbowRepair.get(playerUUID);
            if (pendingRepair != null && pendingRepair > 0) {
                applyPendingCrossbowRepair(player, pendingRepair);
                pendingCrossbowRepair.put(playerUUID, 0);
            }
        }
        
        wasChargingCrossbow.put(playerUUID, isChargingCrossbow);
        
        if (player.tickCount % DURABILITY_REGEN_TICK_INTERVAL != 0) {
            return;
        }
        
        for (ItemStack stack : player.getArmorSlots()) {
            if (!stack.isEmpty() && stack.isDamaged() && isRegenerableItem(stack)) {
                stack.setDamageValue(Math.max(0, stack.getDamageValue() - 1));
            }
        }
        
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty() && offhand.isDamaged() && isRegenerableItem(offhand)) {
            if (isChargingCrossbow && offhand.getItem() instanceof RedCherryCrossbowItem) {
                int pending = pendingCrossbowRepair.getOrDefault(playerUUID, 0);
                pendingCrossbowRepair.put(playerUUID, pending + 1);
            } else {
                offhand.setDamageValue(Math.max(0, offhand.getDamageValue() - 1));
            }
        }
        
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.isDamaged() && isRegenerableItem(stack)) {
                if (isChargingCrossbow && stack.getItem() instanceof RedCherryCrossbowItem) {
                    int pending = pendingCrossbowRepair.getOrDefault(playerUUID, 0);
                    pendingCrossbowRepair.put(playerUUID, pending + 1);
                    continue;
                }
                
                stack.setDamageValue(Math.max(0, stack.getDamageValue() - 1));
            }
        }
    }
    
    private static void applyPendingCrossbowRepair(Player player, int repairPoints) {
        ItemStack mainhand = player.getMainHandItem();
        if (!mainhand.isEmpty() && mainhand.getItem() instanceof RedCherryCrossbowItem && mainhand.isDamaged()) {
            mainhand.setDamageValue(Math.max(0, mainhand.getDamageValue() - repairPoints));
            return;
        }
        
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty() && offhand.getItem() instanceof RedCherryCrossbowItem && offhand.isDamaged()) {
            offhand.setDamageValue(Math.max(0, offhand.getDamageValue() - repairPoints));
            return;
        }
        
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof RedCherryCrossbowItem && stack.isDamaged()) {
                stack.setDamageValue(Math.max(0, stack.getDamageValue() - repairPoints));
                return;
            }
        }
    }
    
    private static boolean isRegenerableItem(ItemStack stack) {
        Item item = stack.getItem();
        return item == ModItems.LEAFWEAVE_HELMET.get() ||
               item == ModItems.LEAFWEAVE_CHESTPLATE.get() ||
               item == ModItems.LEAFWEAVE_LEGGINGS.get() ||
               item == ModItems.LEAFWEAVE_BOOTS.get() ||
               item == ModItems.LUMISTONE_SWORD.get() ||
               item == ModItems.LUMISTONE_PICKAXE.get() ||
               item == ModItems.LUMISTONE_AXE.get() ||
               item == ModItems.LUMISTONE_SHOVEL.get() ||
               item == ModItems.LUMISTONE_HOE.get() ||
               item == ModItems.RED_CHERRY_CROSSBOW.get() ||
               item == ModItems.RED_CHERRY_BOLT_MAGAZINE.get();
    }
}

