package com.cahcap.herbalcurative.handler;

import com.cahcap.herbalcurative.HerbalCurative;
import com.cahcap.herbalcurative.item.WeaveleafArmorItem;
import com.cahcap.herbalcurative.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Event handler for Weaveleaf Armor special effects
 * Note: Durability regeneration is handled by DurabilityRegenHandler
 */
@EventBusSubscriber(modid = HerbalCurative.MODID)
public class WeaveleafArmorHandler {

    private static final int HEALTH_REGEN_TICK_INTERVAL = 100; // 5 seconds (100 ticks)
    
    // ResourceLocation for the step height attribute modifier (1.21 uses ResourceLocation instead of UUID)
    private static final ResourceLocation STEP_HEIGHT_MODIFIER_ID = 
        ResourceLocation.fromNamespaceAndPath(HerbalCurative.MODID, "weaveleaf_boots_step_height");

    /**
     * Handle Weaveleaf Armor special effects (for players only)
     * - Health regeneration (1 HP per 5 seconds for helmet)
     * - Haste 1 buff (permanent for chestplate)
     * - Movement speed boost and auto-step (for boots)
     * Note: Durability regeneration is handled by DurabilityRegenHandler
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        
        // Quick check: if player has no Weaveleaf armor at all, skip everything
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        
        boolean hasHelmet = helmet.is(ModItems.WEAVELEAF_HELMET.get());
        boolean hasChestplate = chestplate.is(ModItems.WEAVELEAF_CHESTPLATE.get());
        boolean hasBoots = boots.is(ModItems.WEAVELEAF_BOOTS.get());
        
        // Early exit if no Weaveleaf armor equipped
        if (!hasHelmet && !hasChestplate && !hasBoots) {
            // Remove step height modifier if it was applied before
            var stepHeightAttribute = player.getAttribute(Attributes.STEP_HEIGHT);
            if (stepHeightAttribute != null && stepHeightAttribute.getModifier(STEP_HEIGHT_MODIFIER_ID) != null) {
                stepHeightAttribute.removeModifier(STEP_HEIGHT_MODIFIER_ID);
            }
            return;
        }

        // Server-side effects (health regen, haste) - only check occasionally
        if (!player.level().isClientSide()) {
            int tickMod = player.tickCount % HEALTH_REGEN_TICK_INTERVAL;
            
            // Health regeneration (1 HP per 5 seconds for helmet)
            if (hasHelmet && tickMod == 0) {
                if (player.getHealth() < player.getMaxHealth()) {
                    player.heal(1.0F);
                }
            }

            // Haste 1 buff (permanent for chestplate) - refresh every 1.5 seconds
            if (hasChestplate && tickMod % 30 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 40, 0, false, false));
            }
        }

        // Handle boots effects (auto-step and movement speed)
        if (hasBoots) {
            // Auto-step using attribute modifiers (more efficient and compatible)
            var stepHeightAttribute = player.getAttribute(Attributes.STEP_HEIGHT);
            if (stepHeightAttribute != null) {
                boolean shouldStep = !player.isCrouching() && player.zza >= 0F;
                boolean hasModifier = stepHeightAttribute.getModifier(STEP_HEIGHT_MODIFIER_ID) != null;
                
                if (shouldStep && !hasModifier) {
                    // Add +0.4 to base 0.6 = 1.0 total (1.21 uses ResourceLocation)
                    stepHeightAttribute.addTransientModifier(new AttributeModifier(
                        STEP_HEIGHT_MODIFIER_ID,
                        0.4,
                        AttributeModifier.Operation.ADD_VALUE
                    ));
                } else if (!shouldStep && hasModifier) {
                    stepHeightAttribute.removeModifier(STEP_HEIGHT_MODIFIER_ID);
                }
            }

            // Apply movement speed boost on client side (only when moving)
            if (player.level().isClientSide() && player.zza > 0F) {
                boolean inWater = player.isInWater() || player.isInFluidType();
                
                if (!inWater) {
                    if (player.onGround()) {
                        // 30% speed boost
                        player.moveRelative(0.03F, new Vec3(0, 0, 1));
                    } else if (!player.getAbilities().flying) {
                        // Half speed boost in air (15%)
                        player.moveRelative(0.015F, new Vec3(0, 0, 1));
                    }
                }
            }
        } else {
            // Remove step height modifier if boots are removed
            var stepHeightAttribute = player.getAttribute(Attributes.STEP_HEIGHT);
            if (stepHeightAttribute != null && stepHeightAttribute.getModifier(STEP_HEIGHT_MODIFIER_ID) != null) {
                stepHeightAttribute.removeModifier(STEP_HEIGHT_MODIFIER_ID);
            }
        }
    }

    /**
     * Handle fall damage reduction (for players only)
     * Boots: Increase safe fall height from 3 to 4 blocks
     * Leggings: Reduce fall damage by 30%
     */
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

        float fallDistance = event.getDistance();

        // If wearing boots, increase safe fall height by 1 block (from 3 to 4 blocks)
        // Since boots allow jumping to 2 blocks, safe fall height should be 4 blocks
        if (boots.is(ModItems.WEAVELEAF_BOOTS.get()) && !boots.isEmpty()) {
            if (fallDistance <= 4.0F) {
                event.setCanceled(true);
                return;
            }
        }

        // Reduce fall damage by 30% if wearing leggings
        if (leggings.is(ModItems.WEAVELEAF_LEGGINGS.get()) && !leggings.isEmpty()) {
            event.setDamageMultiplier(0.7F);
        }
    }


    /**
     * Handle jump height increase (for players only)
     * Allows jumping to 2 blocks height by increasing vertical jump velocity by 50%
     */
    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

        if (boots.is(ModItems.WEAVELEAF_BOOTS.get()) && !boots.isEmpty()) {
            // Increase jump height by 50% (multiply vertical velocity by 1.5)
            // This allows jumping to ~2 blocks height
            double currentMotionY = player.getDeltaMovement().y;
            if (currentMotionY > 0.0D) {
                player.setDeltaMovement(
                    player.getDeltaMovement().x,
                    currentMotionY * 1.5D,
                    player.getDeltaMovement().z
                );
            }

            // Note: Horizontal movement speed during jump is handled by onLivingUpdate
            // using moveRelative with half the speed boost
        }
    }
}
