package com.cahcap.herbalcurative.neoforge.handler;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.item.ThornmarkAxeItem;
import com.cahcap.herbalcurative.item.ThornmarkHoeItem;
import com.cahcap.herbalcurative.item.ThornmarkShovelItem;
import com.cahcap.herbalcurative.item.ThornmarkSwordItem;
import com.cahcap.herbalcurative.neoforge.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/**
 * Event handler for Thornmark Tools special effects
 * Uses instanceof checks to support inheritance - all subclasses automatically get the same behaviors
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID)
public class ThornmarkToolHandler {

    /**
     * Handle shovel: increase flint drop chance from 10% to 50% when breaking gravel
     * Handle axe: make leaves and vines drop themselves (like shears)
     */
    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        // Get the entity that broke the block
        if (!(event.getBreaker() instanceof Player player)) {
            return;
        }

        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty()) {
            return;
        }

        Item tool = heldItem.getItem();
        BlockState state = event.getState();
        Block block = state.getBlock();

        // Handle shovel: increase flint drop chance from 10% to 50%
        if (tool instanceof ThornmarkShovelItem && block == Blocks.GRAVEL) {
            // Vanilla gravel drops flint 10% of the time, gravel 90% of the time
            // With Thornmark Shovel (or any subclass), increase flint chance to 50%
            // Clear existing drops and add either flint (50%) or gravel (50%)
            event.getDrops().clear();
            if (player.level().random.nextFloat() < 0.5F) { // 50% chance for flint
                event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(
                    player.level(),
                    event.getPos().getX() + 0.5,
                    event.getPos().getY() + 0.5,
                    event.getPos().getZ() + 0.5,
                    new ItemStack(Items.FLINT, 1)
                ));
            } else {
                event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(
                    player.level(),
                    event.getPos().getX() + 0.5,
                    event.getPos().getY() + 0.5,
                    event.getPos().getZ() + 0.5,
                    new ItemStack(Blocks.GRAVEL, 1)
                ));
            }
        }

        // Handle axe: make leaves and vines drop themselves (like shears)
        if (tool instanceof ThornmarkAxeItem && (block instanceof LeavesBlock || block instanceof VineBlock)) {
            // Clear normal drops and add the block itself (silk touch effect)
            event.getDrops().clear();

            // Create ItemStack from block (like silk touch)
            ItemStack blockItem = new ItemStack(block.asItem(), 1);
            event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(
                player.level(),
                event.getPos().getX() + 0.5,
                event.getPos().getY() + 0.5,
                event.getPos().getZ() + 0.5,
                blockItem
            ));
        }
    }
    
    /**
     * Handle hoe: right-click to harvest crops
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack heldItem = player.getItemInHand(event.getHand());
        
        if (heldItem.isEmpty() || !(heldItem.getItem() instanceof ThornmarkHoeItem)) {
            return;
        }
        
        Level world = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        
        // Check if it's a crop that can be harvested
        if (block instanceof CropBlock) {
            CropBlock crop = (CropBlock) block;
            
            // Check if crop is fully grown
            if (crop.isMaxAge(state)) {
                if (!world.isClientSide()) {
                    // Get the drops from the crop
                    java.util.List<ItemStack> drops = Block.getDrops(state, (ServerLevel) world, pos, null, player, heldItem);
                    
                    // Identify the seed item by comparing to the crop's clone item stack
                    ItemStack seedStack = crop.getCloneItemStack(world, pos, state);
                    Item seedItem = seedStack.getItem();
                    
                    // Count how many seeds are in the drops
                    int seedCount = 0;
                    for (ItemStack drop : drops) {
                        if (drop.getItem() == seedItem) {
                            seedCount += drop.getCount();
                        }
                    }
                    
                    // Drop all items
                    for (ItemStack drop : drops) {
                        Block.popResource(world, pos, drop);
                    }
                    
                    // Only consume 1 seed (replant), so add back (seedCount - 1) seeds
                    if (seedCount > 1) {
                        Block.popResource(world, pos, new ItemStack(seedItem, seedCount - 1));
                    }
                    
                    // Reset crop to initial growth stage
                    world.setBlock(pos, crop.getStateForAge(0), 2);
                    
                    // Damage the hoe
                    heldItem.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
                }
                
                event.setCanceled(true);
            }
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

            // Check if player is holding Thornmark Sword or any subclass
            if (!heldItem.isEmpty() && heldItem.getItem() instanceof ThornmarkSwordItem) {
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

