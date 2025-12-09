package com.cahcap.herbalcurative.neoforge.handler;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.item.*;
import com.cahcap.herbalcurative.neoforge.registry.ModBlocks;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/**
 * Event handler for Lumistone Tools special effects
 * - Pickaxe, Axe, Shovel, Hoe: Auto-pickup broken block drops
 * - Sword: Magic damage with particle effects
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID)
public class LumistoneToolHandler {

    /**
     * Auto-pickup for Lumistone Tools (except sword)
     * When breaking blocks with pickaxe/axe/shovel/hoe, drops go directly to inventory
     */
    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        // Only process for players
        if (!(event.getBreaker() instanceof Player player)) {
            return;
        }

        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty()) {
            return;
        }

        Item tool = heldItem.getItem();
        
        // Check if using a Lumistone tool (excluding sword)
        boolean isLumistoneTool = tool instanceof LumistonePickaxeItem ||
                                  tool instanceof LumistoneAxeItem ||
                                  tool instanceof LumistoneShovelItem ||
                                  tool instanceof LumistoneHoeItem;
        
        if (!isLumistoneTool) {
            return;
        }
        
        // Auto-pickup: add all drops directly to player inventory
        boolean pickedUpAny = false;
        
        for (ItemEntity itemEntity : event.getDrops()) {
            ItemStack drop = itemEntity.getItem();
        
            // Try to add to player inventory
            if (!player.getInventory().add(drop)) {
                // If inventory is full, drop it normally (don't delete items)
                continue;
            }
            
            // Successfully added to inventory, remove from world drops
            drop.setCount(0);
            pickedUpAny = true;
        }
        
        // Remove all items that were successfully picked up (count = 0)
        event.getDrops().removeIf(itemEntity -> itemEntity.getItem().isEmpty());
                    
        // Play pickup sound if any items were picked up
        if (pickedUpAny && !player.level().isClientSide()) {
            player.level().playSound(
                null, // null = broadcast to all nearby players
                player.getX(), 
                player.getY(), 
                player.getZ(),
                SoundEvents.ITEM_PICKUP, // Original vanilla pickup sound
                SoundSource.PLAYERS,
                0.25F, // Volume (0.25 = subtle, not too loud)
                ((player.level().random.nextFloat() - player.level().random.nextFloat()) * 0.7F + 1.0F) * 2.0F // Pitch variation
            );
        }
    }

    /**
     * Handle sword: add magic damage on hit with cooldown
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        DamageSource source = event.getSource();
        LivingEntity target = event.getEntity();
        Level level = target.level();

        // Only process on server side
        if (level.isClientSide()) {
            return;
        }

        // Check if damage source is from a player (melee attack)
        if (source.getEntity() instanceof Player && source.getDirectEntity() == source.getEntity()) {
            Player player = (Player) source.getEntity();
            ItemStack heldItem = player.getMainHandItem();

            // Check if player is holding Lumistone Sword or any subclass
            if (!heldItem.isEmpty() && heldItem.getItem() instanceof LumistoneSwordItem) {
                Item sword = heldItem.getItem();

                // Check cooldown
                if (!player.getCooldowns().isOnCooldown(sword)) {
                    // Add magic damage by increasing event amount
                    float currentDamage = event.getOriginalDamage();
                    float magicDamage = 2.0F;
                    event.setNewDamage(currentDamage + magicDamage);

                    // Set cooldown: 40 ticks = 2 seconds
                    player.getCooldowns().addCooldown(sword, 40);

                    // Spawn particle effect on server side
                    if (level instanceof ServerLevel serverLevel) {
                        BlockState particleState = ModBlocks.RED_CHERRY_LEAVES.get() != null ?
                            ModBlocks.RED_CHERRY_LEAVES.get().defaultBlockState() : Blocks.OAK_LEAVES.defaultBlockState();

                        // Spawn particles around the target for a powerful hit effect
                        double centerX = target.getX();
                        double centerY = target.getY() + target.getBbHeight() / 2.0;
                        double centerZ = target.getZ();

                        // Main burst at center
                        serverLevel.sendParticles(
                            new BlockParticleOption(ParticleTypes.BLOCK, particleState),
                            centerX, centerY, centerZ,
                            30, // particle count
                            target.getBbWidth() * 0.4, target.getBbHeight() * 0.4, target.getBbWidth() * 0.4, // spread
                            0.2 // speed
                        );

                        // Additional burst above for upward effect
                        serverLevel.sendParticles(
                            new BlockParticleOption(ParticleTypes.BLOCK, particleState),
                            centerX, centerY + target.getBbHeight() * 0.2, centerZ,
                            15, // additional particles
                            target.getBbWidth() * 0.3, target.getBbHeight() * 0.3, target.getBbWidth() * 0.3, // spread
                            0.15 // speed
                        );

                        // Burst below for downward effect
                        serverLevel.sendParticles(
                            new BlockParticleOption(ParticleTypes.BLOCK, particleState),
                            centerX, centerY - target.getBbHeight() * 0.15, centerZ,
                            15, // additional particles
                            target.getBbWidth() * 0.3, target.getBbHeight() * 0.3, target.getBbWidth() * 0.3, // spread
                            0.15 // speed
                        );
                    }
                }
            }
        }
    }
}

