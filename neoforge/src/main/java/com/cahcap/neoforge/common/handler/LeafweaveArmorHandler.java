package com.cahcap.neoforge.common.handler;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.neoforge.common.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
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
 * Event handler for Leafweave Armor special effects
 * Note: Durability regeneration is handled by DurabilityRegenHandler
 * 
 * BOOTS EFFECTS ARE PRESERVED BUT DISABLED FOR FUTURE JOURNAL IMPLEMENTATION
 * 
 * @EventBusSubscriber annotation removed because all events are currently disabled
 */
// @EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID)
public class LeafweaveArmorHandler {

    // ==================== BOOTS EFFECTS - PRESERVED FOR JOURNAL IMPLEMENTATION ====================
    // The following code implements boots special effects but is currently disabled
    // These will be re-enabled through the journal/research system in future updates
    
    /*
    // ResourceLocation for the step height attribute modifier (1.21 uses ResourceLocation instead of UUID)
    private static final ResourceLocation STEP_HEIGHT_MODIFIER_ID = 
        ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "leafweave_boots_step_height");

    /**
     * Handle Leafweave Boots special effects (for players only)
     * - Movement speed boost (30% on ground, 15% in air)
     * - Auto-step (can walk up 1 block without jumping)
     */
    /*
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        boolean hasBoots = boots.is(ModItems.LEAFWEAVE_BOOTS.get());
        
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
     * Handle fall damage reduction for boots (for players only)
     * Boots: Increase safe fall height from 3 to 4 blocks
     */
    /*
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        float fallDistance = event.getDistance();

        // If wearing boots, increase safe fall height by 1 block (from 3 to 4 blocks)
        // Since boots allow jumping to 2 blocks, safe fall height should be 4 blocks
        if (boots.is(ModItems.LEAFWEAVE_BOOTS.get()) && !boots.isEmpty()) {
            if (fallDistance <= 4.0F) {
                event.setCanceled(true);
                return;
            }
        }
    }

    /**
     * Handle jump height increase for boots (for players only)
     * Allows jumping to 2 blocks height by increasing vertical jump velocity by 50%
     */
    /*
    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

        if (boots.is(ModItems.LEAFWEAVE_BOOTS.get()) && !boots.isEmpty()) {
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

            // Note: Horizontal movement speed during jump is handled by onPlayerTick
            // using moveRelative with half the speed boost
        }
    }
    */
    // ==================== END BOOTS EFFECTS ====================
}

