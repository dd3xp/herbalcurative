package com.cahcap.herbalcurative.common.item;

import com.cahcap.herbalcurative.common.block.CauldronBlock;
import com.cahcap.herbalcurative.common.block.WorkbenchBlock;
import com.cahcap.herbalcurative.common.blockentity.CauldronBlockEntity;
import com.cahcap.herbalcurative.common.blockentity.WorkbenchBlockEntity;
import com.cahcap.herbalcurative.common.entity.FlowweaveProjectile;
import com.cahcap.herbalcurative.common.multiblock.MultiblockCauldron;
import com.cahcap.herbalcurative.common.multiblock.MultiblockHerbCabinet;
import com.cahcap.herbalcurative.common.multiblock.MultiblockHerbalBlending;
import com.cahcap.herbalcurative.common.multiblock.MultiblockHerbalBlending.BlendingStructure;
import com.cahcap.herbalcurative.common.recipe.WorkbenchRecipe;
import com.cahcap.herbalcurative.common.registry.ModRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Flowweave Ring
 * Magical tool with no durability (permanent use, like a wand)
 * Can be held in offhand
 * Has same attack attributes as iron sword (6 attack damage, -2.4 attack speed)
 * Can be used to:
 * - Form Herb Cabinet multiblock structure
 * - Trigger Herbal Blending Rack crafting
 * - Trigger Workbench crafting
 * - Bind to potion when placed in 8+ min potion in cauldron
 * - Apply bound potion effect when right-clicked in casting mode (consumes herbs)
 * 
 * Three casting modes:
 * - INFUSION: Apply effect to self
 * - BURST: Shoot projectile, create explosion effect at impact, apply buff to all entities in range
 * - LINGERING: Shoot projectile, create explosion effect at impact, spawn lingering cloud
 */
public class FlowweaveRingItem extends Item {
    
    /**
     * Casting modes for the Flowweave Ring
     */
    public enum CastingMode {
        INFUSION(1.0f, "Infusion"),      // Apply to self, 1x herb cost
        BURST(1.5f, "Burst"),            // Shoot projectile, AOE buff, 1.5x herb cost (rounded down)
        LINGERING(2.0f, "Lingering");    // Shoot projectile, lingering cloud, 2x herb cost
        
        private final float herbMultiplier;
        private final String displayName;
        
        CastingMode(float herbMultiplier, String displayName) {
            this.herbMultiplier = herbMultiplier;
            this.displayName = displayName;
        }
        
        public float getHerbMultiplier() { return herbMultiplier; }
        public String getDisplayName() { return displayName; }
        
        public CastingMode next() {
            CastingMode[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }
    }
    
    // NBT keys for bound potion
    private static final String TAG_BOUND = "BoundPotion";
    private static final String TAG_POTION_TYPE = "PotionType";
    private static final String TAG_POTION_COLOR = "PotionColor";
    private static final String TAG_DURATION = "Duration";
    private static final String TAG_LEVEL = "Level";
    private static final String TAG_HERB_COST = "HerbCost";
    private static final String TAG_CASTING_MODE = "CastingMode";
    
    // Minimum duration for binding (8 minutes = 480 seconds)
    public static final int MIN_BIND_DURATION = 480;
    
    // Projectile settings
    public static final float PROJECTILE_SPEED = 3.0f;  // 3 blocks per tick = fast wave
    public static final int MAX_PROJECTILE_DISTANCE = 64;
    
    public FlowweaveRingItem(Properties properties) {
        super(properties);
    }
    
    // ==================== Potion Binding ====================
    
    /**
     * Check if this Flowweave Ring has a bound potion
     */
    public static boolean hasBoundPotion(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }
        return customData.copyTag().getBoolean(TAG_BOUND);
    }
    
    /**
     * Bind a potion to this Flowweave Ring
     * Preserves the existing casting mode if set
     */
    public static void bindPotion(ItemStack stack, String potionType, int color, 
                                   int duration, int level, Map<Item, Integer> herbCost) {
        // Get existing tag to preserve casting mode
        CustomData existingData = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = existingData != null ? existingData.copyTag() : new CompoundTag();
        
        tag.putBoolean(TAG_BOUND, true);
        tag.putString(TAG_POTION_TYPE, potionType);
        tag.putInt(TAG_POTION_COLOR, color);
        tag.putInt(TAG_DURATION, duration);
        tag.putInt(TAG_LEVEL, level);
        
        // Store herb costs
        CompoundTag herbTag = new CompoundTag();
        for (Map.Entry<Item, Integer> entry : herbCost.entrySet()) {
            String key = entry.getKey().builtInRegistryHolder().key().location().toString();
            herbTag.putInt(key, entry.getValue());
        }
        tag.put(TAG_HERB_COST, herbTag);
        
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
    
    /**
     * Unbind the potion from this Flowweave Ring (clear all binding data)
     */
    public static void unbindPotion(ItemStack stack) {
        // Remove custom data component to clear binding
        stack.remove(DataComponents.CUSTOM_DATA);
    }
    
    /**
     * Get bound potion type
     */
    public static String getBoundPotionType(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return "";
        return customData.copyTag().getString(TAG_POTION_TYPE);
    }
    
    /**
     * Get bound potion color
     */
    public static int getBoundPotionColor(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return 0x3F76E4;
        return customData.copyTag().getInt(TAG_POTION_COLOR);
    }
    
    /**
     * Get bound potion duration (seconds)
     */
    public static int getBoundDuration(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return 0;
        return customData.copyTag().getInt(TAG_DURATION);
    }
    
    /**
     * Get bound potion level
     */
    public static int getBoundLevel(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return 1;
        return customData.copyTag().getInt(TAG_LEVEL);
    }
    
    /**
     * Get required herb costs
     */
    public static Map<Item, Integer> getHerbCost(ItemStack stack) {
        Map<Item, Integer> costs = new HashMap<>();
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return costs;
        
        CompoundTag tag = customData.copyTag();
        if (!tag.contains(TAG_HERB_COST)) return costs;
        
        CompoundTag herbTag = tag.getCompound(TAG_HERB_COST);
        for (String key : herbTag.getAllKeys()) {
            Item herb = getHerbFromKey(key);
            if (herb != null) {
                costs.put(herb, herbTag.getInt(key));
            }
        }
        return costs;
    }
    
    /**
     * Get the current casting mode
     */
    public static CastingMode getCastingMode(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return CastingMode.INFUSION;
        
        CompoundTag tag = customData.copyTag();
        if (!tag.contains(TAG_CASTING_MODE)) return CastingMode.INFUSION;
        
        int ordinal = tag.getInt(TAG_CASTING_MODE);
        CastingMode[] modes = CastingMode.values();
        if (ordinal >= 0 && ordinal < modes.length) {
            return modes[ordinal];
        }
        return CastingMode.INFUSION;
    }
    
    /**
     * Set the casting mode
     */
    public static void setCastingMode(ItemStack stack, CastingMode mode) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData != null ? customData.copyTag() : new CompoundTag();
        tag.putInt(TAG_CASTING_MODE, mode.ordinal());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
    
    /**
     * Cycle to the next casting mode
     */
    public static CastingMode cycleMode(ItemStack stack) {
        CastingMode current = getCastingMode(stack);
        CastingMode next = current.next();
        setCastingMode(stack, next);
        return next;
    }
    
    private static Item getHerbFromKey(String key) {
        if (key.contains("scaleplate")) return ModRegistries.SCALEPLATE.get();
        if (key.contains("dewpetal_shard")) return ModRegistries.DEWPETAL_SHARD.get();
        if (key.contains("golden_lilybell")) return ModRegistries.GOLDEN_LILYBELL.get();
        if (key.contains("cryst_spine")) return ModRegistries.CRYST_SPINE.get();
        if (key.contains("burnt_node")) return ModRegistries.BURNT_NODE.get();
        if (key.contains("heart_of_stardream")) return ModRegistries.HEART_OF_STARDREAM.get();
        return null;
    }
    
    /**
     * Right-click in air:
     * - Shift+right-click: cycle casting mode
     * - Normal right-click: cast if bound potion exists
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        // Shift+right-click in air: cycle casting mode
        if (player.isShiftKeyDown() && hasBoundPotion(stack)) {
            if (!level.isClientSide) {
                CastingMode newMode = cycleMode(stack);
                // Send message to player about mode change
                player.displayClientMessage(
                    Component.translatable("item.herbalcurative.flowweave_ring.mode_changed", newMode.getDisplayName())
                        .withStyle(ChatFormatting.AQUA), 
                    true);  // action bar
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.5F, 1.2F);
            }
            return InteractionResultHolder.success(stack);
        }
        
        if (!hasBoundPotion(stack)) {
            return InteractionResultHolder.pass(stack);
        }
        
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        
        // Try to cast based on current mode
        if (tryCastPotion(level, player, stack)) {
            return InteractionResultHolder.success(stack);
        }
        
        return InteractionResultHolder.fail(stack);
    }
    
    /**
     * Try to cast the bound potion effect based on current mode
     */
    private boolean tryCastPotion(Level level, Player player, ItemStack stack) {
        if (!hasBoundPotion(stack)) {
            return false;
        }
        
        CastingMode mode = getCastingMode(stack);
        
        // Calculate adjusted herb cost based on mode
        Map<Item, Integer> baseHerbCost = getHerbCost(stack);
        Map<Item, Integer> adjustedCost = calculateAdjustedHerbCost(baseHerbCost, mode);
        
        // Check if player has the required herbs
        if (!hasRequiredHerbs(player, adjustedCost)) {
            // Play failure sound
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
            return false;
        }
        
        // Consume herbs
        if (!player.isCreative()) {
            consumeHerbs(player, adjustedCost);
        }
        
        // Get potion data
        String potionType = getBoundPotionType(stack);
        int amplifier = getBoundLevel(stack) - 1; // 0-based amplifier
        int color = getBoundPotionColor(stack);
        
        Holder<MobEffect> effect = getEffectForType(potionType);
        if (effect == null) {
            return false;
        }
        
        // Check if this is an instant effect
        boolean isInstantEffect = potionType.contains("instant_health") || potionType.contains("instant_damage");
        
        // For instant effects, use duration of 1 tick; for others, convert seconds to ticks
        int duration = isInstantEffect ? 1 : getBoundDuration(stack) * 20;
        
        switch (mode) {
            case INFUSION:
                // Apply effect to self
                if (isInstantEffect) {
                    // For instant effects, apply immediately using the effect's value
                    applyInstantEffect(player, effect, amplifier);
                } else {
                    player.addEffect(new MobEffectInstance(effect, duration, amplifier));
                }
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
                break;
                
            case BURST:
                // Shoot projectile that applies AOE buff on impact
                shootProjectile(level, player, effect, duration, amplifier, color, false, isInstantEffect);
                break;
                
            case LINGERING:
                // Shoot projectile that creates lingering cloud on impact
                shootProjectile(level, player, effect, duration, amplifier, color, true, isInstantEffect);
                break;
        }
        
        return true;
    }
    
    
    /**
     * Apply an instant effect (heal or harm) directly to a target using vanilla logic
     */
    private void applyInstantEffect(LivingEntity target, Holder<MobEffect> effect, int amplifier) {
        // For instant effects, adding a MobEffectInstance with duration 1 triggers immediate application
        // Minecraft handles instant effects specially in MobEffectInstance.tick()
        target.addEffect(new MobEffectInstance(effect, 1, amplifier, false, true));
    }
    
    /**
     * Shoot a projectile for BURST or LINGERING mode
     */
    private void shootProjectile(Level level, Player player, Holder<MobEffect> effect, 
                                  int duration, int amplifier, int color, boolean lingering, boolean isInstant) {
        // Create and spawn the projectile entity
        FlowweaveProjectile projectile = new FlowweaveProjectile(level, player);
        projectile.setEffect(effect, duration, amplifier);
        projectile.setColor(color);
        projectile.setLingering(lingering);
        projectile.setInstant(isInstant);
        
        // Calculate direction from player's look direction (NOT affected by player movement)
        Vec3 lookVec = player.getLookAngle();
        projectile.setDeltaMovement(lookVec.scale(PROJECTILE_SPEED));
        
        // Set rotation to match look direction
        projectile.setYRot((float)(Math.atan2(lookVec.x, lookVec.z) * (180.0 / Math.PI)));
        projectile.setXRot((float)(Math.atan2(lookVec.y, Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z)) * (-180.0 / Math.PI)));
        
        level.addFreshEntity(projectile);
        
        // Play shoot sound
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_PEARL_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
    }
    
    private boolean hasRequiredHerbs(Player player, Map<Item, Integer> herbCost) {
        for (Map.Entry<Item, Integer> entry : herbCost.entrySet()) {
            int count = countItemTotal(player, entry.getKey());
            if (count < entry.getValue()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Count total available herbs from HerbBox items + player inventory
     */
    private int countItemTotal(Player player, Item item) {
        int count = 0;
        
        // First count from HerbBox items in inventory
        count += countItemInHerbBoxes(player, item);
        
        // Then count from player inventory (loose herbs)
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }
    
    /**
     * Get the herbKey for an Item, used by HerbBoxItem storage
     */
    private String getHerbKeyForItem(Item item) {
        if (item == ModRegistries.SCALEPLATE.get()) return "scaleplate";
        if (item == ModRegistries.DEWPETAL_SHARD.get()) return "dewpetal_shard";
        if (item == ModRegistries.GOLDEN_LILYBELL.get()) return "golden_lilybell";
        if (item == ModRegistries.CRYST_SPINE.get()) return "cryst_spine";
        if (item == ModRegistries.BURNT_NODE.get()) return "burnt_node";
        if (item == ModRegistries.HEART_OF_STARDREAM.get()) return "heart_of_stardream";
        return null;
    }
    
    /**
     * Count herbs available in HerbBox items in player inventory
     */
    private int countItemInHerbBoxes(Player player, Item item) {
        String herbKey = getHerbKeyForItem(item);
        if (herbKey == null) return 0;
        
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof HerbBoxItem) {
                count += HerbBoxItem.getHerbAmount(stack, herbKey);
            }
        }
        return count;
    }
    
    /**
     * Consume herbs: first from HerbBox items, then from player inventory
     */
    private void consumeHerbs(Player player, Map<Item, Integer> herbCost) {
        for (Map.Entry<Item, Integer> entry : herbCost.entrySet()) {
            int remaining = entry.getValue();
            
            // First try to consume from HerbBox items
            remaining = consumeFromHerbBoxes(player, entry.getKey(), remaining);
            
            // If still need more, consume from player inventory (loose herbs)
            if (remaining > 0) {
                for (ItemStack stack : player.getInventory().items) {
                    if (stack.is(entry.getKey())) {
                        int toRemove = Math.min(remaining, stack.getCount());
                        stack.shrink(toRemove);
                        remaining -= toRemove;
                        if (remaining <= 0) break;
                    }
                }
            }
        }
    }
    
    /**
     * Consume herbs from HerbBox items in player inventory
     * @return remaining amount that couldn't be consumed
     */
    private int consumeFromHerbBoxes(Player player, Item item, int amount) {
        String herbKey = getHerbKeyForItem(item);
        if (herbKey == null) return amount;
        
        int remaining = amount;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof HerbBoxItem && remaining > 0) {
                int available = HerbBoxItem.getHerbAmount(stack, herbKey);
                if (available > 0) {
                    int toRemove = Math.min(remaining, available);
                    HerbBoxItem.removeHerb(stack, herbKey, toRemove);
                    remaining -= toRemove;
                }
            }
        }
        return remaining;
    }
    
    private Holder<MobEffect> getEffectForType(String type) {
        // type is a full registry ID like "minecraft:instant_health"
        return switch (type) {
            case "minecraft:instant_health" -> MobEffects.HEAL;
            case "minecraft:regeneration" -> MobEffects.REGENERATION;
            case "minecraft:strength" -> MobEffects.DAMAGE_BOOST;
            case "minecraft:speed" -> MobEffects.MOVEMENT_SPEED;
            case "minecraft:fire_resistance" -> MobEffects.FIRE_RESISTANCE;
            case "minecraft:night_vision" -> MobEffects.NIGHT_VISION;
            case "minecraft:invisibility" -> MobEffects.INVISIBILITY;
            case "minecraft:water_breathing" -> MobEffects.WATER_BREATHING;
            case "minecraft:jump_boost" -> MobEffects.JUMP;
            case "minecraft:slow_falling" -> MobEffects.SLOW_FALLING;
            case "minecraft:poison" -> MobEffects.POISON;
            case "minecraft:instant_damage" -> MobEffects.HARM;
            case "minecraft:weakness" -> MobEffects.WEAKNESS;
            case "minecraft:slowness" -> MobEffects.MOVEMENT_SLOWDOWN;
            default -> null;
        };
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (hasBoundPotion(stack)) {
            String type = getBoundPotionType(stack);
            int duration = getBoundDuration(stack);
            int level = getBoundLevel(stack);
            
            // type is a full registry ID like "minecraft:instant_health"
            String typeName = switch (type) {
                case "minecraft:instant_health" -> "Healing";
                case "minecraft:regeneration" -> "Regeneration";
                case "minecraft:strength" -> "Strength";
                case "minecraft:speed" -> "Speed";
                case "minecraft:fire_resistance" -> "Fire Resistance";
                case "minecraft:night_vision" -> "Night Vision";
                case "minecraft:invisibility" -> "Invisibility";
                case "minecraft:water_breathing" -> "Water Breathing";
                case "minecraft:jump_boost" -> "Jump Boost";
                case "minecraft:slow_falling" -> "Slow Falling";
                case "minecraft:poison" -> "Poison";
                case "minecraft:instant_damage" -> "Harming";
                case "minecraft:weakness" -> "Weakness";
                case "minecraft:slowness" -> "Slowness";
                default -> type.contains(":") ? type.substring(type.indexOf(":") + 1) : type;
            };
            
            tooltip.add(Component.literal("Bound: " + typeName)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            
            if (level > 1) {
                tooltip.add(Component.literal("Level " + level)
                        .withStyle(ChatFormatting.BLUE));
            }
            
            // Check if this is an instant effect (don't show duration for instant effects)
            boolean isInstantEffect = type.contains("instant_health") || type.contains("instant_damage");
            
            if (!isInstantEffect) {
                // Duration is stored in seconds, display as "mm:ss"
                int minutes = duration / 60;
                int seconds = duration % 60;
                String durationText = String.format("%02d:%02d", minutes, seconds);
                tooltip.add(Component.literal("Duration: " + durationText)
                        .withStyle(ChatFormatting.GRAY));
            }
            
            // Show current casting mode
            CastingMode mode = getCastingMode(stack);
            String modeKey = switch (mode) {
                case INFUSION -> "item.herbalcurative.flowweave_ring.mode.infusion";
                case BURST -> "item.herbalcurative.flowweave_ring.mode.burst";
                case LINGERING -> "item.herbalcurative.flowweave_ring.mode.lingering";
            };
            tooltip.add(Component.translatable("item.herbalcurative.flowweave_ring.mode", 
                    Component.translatable(modeKey))
                    .withStyle(ChatFormatting.AQUA));
            
            // Show herb cost (adjusted for current mode)
            Map<Item, Integer> baseHerbCost = getHerbCost(stack);
            Map<Item, Integer> adjustedCost = calculateAdjustedHerbCost(baseHerbCost, mode);
            if (!adjustedCost.isEmpty()) {
                String costLabel = mode.getHerbMultiplier() > 1.0f 
                    ? String.format("Herb Cost (x%.1f):", mode.getHerbMultiplier())
                    : "Herb Cost:";
                tooltip.add(Component.literal(costLabel)
                        .withStyle(ChatFormatting.YELLOW));
                for (Map.Entry<Item, Integer> entry : adjustedCost.entrySet()) {
                    tooltip.add(Component.literal("  " + entry.getValue() + "x " + 
                            entry.getKey().getDescription().getString())
                            .withStyle(ChatFormatting.GRAY));
                }
            }
            
            // Hint for mode switching
            tooltip.add(Component.translatable("item.herbalcurative.flowweave_ring.mode_hint")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
    
    /**
     * Static version of calculateAdjustedHerbCost for use in tooltip
     */
    private static Map<Item, Integer> calculateAdjustedHerbCost(Map<Item, Integer> baseCost, CastingMode mode) {
        if (mode.getHerbMultiplier() == 1.0f) {
            return baseCost;
        }
        
        Map<Item, Integer> adjusted = new HashMap<>();
        for (Map.Entry<Item, Integer> entry : baseCost.entrySet()) {
            int adjustedCount = (int) (entry.getValue() * mode.getHerbMultiplier());
            adjusted.put(entry.getKey(), Math.max(1, adjustedCount));
        }
        return adjusted;
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return hasBoundPotion(stack);
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockState clickedState = level.getBlockState(context.getClickedPos());
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        
        // Check if this click would trigger any action (for both client and server)
        boolean wouldTriggerAction = wouldTriggerAction(context, clickedState);
        
        if (level.isClientSide()) {
            // Client: only show swing animation if action would be triggered
            return wouldTriggerAction ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        
        // Server-side: actually perform the actions
        
        // Try to form Herb Cabinet multiblock
        if (MultiblockHerbCabinet.INSTANCE.isBlockTrigger(clickedState)) {
            if (MultiblockHerbCabinet.INSTANCE.createStructure(
                    level,
                    context.getClickedPos(),
                    context.getClickedFace(),
                    player)) {
                return InteractionResult.SUCCESS;
            }
        }
        
        // Try to form Cauldron multiblock
        if (MultiblockCauldron.INSTANCE.isBlockTrigger(clickedState)) {
            if (MultiblockCauldron.INSTANCE.createStructure(
                    level,
                    context.getClickedPos(),
                    player)) {
                return InteractionResult.SUCCESS;
            }
        }
        
        // Handle formed Cauldron - start/finish brewing or force clear
        if (clickedState.is(ModRegistries.CAULDRON.get()) && clickedState.getValue(CauldronBlock.FORMED)) {
            if (level.getBlockEntity(context.getClickedPos()) instanceof CauldronBlockEntity be) {
                CauldronBlockEntity master = be.getMaster();
                if (master != null) {
                    if (player != null && player.isShiftKeyDown()) {
                        master.onFlowweaveRingShiftUse(player);
                        level.playSound(null, context.getClickedPos(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1.0F);
                    } else {
                        master.onFlowweaveRingUse(player);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        
        // Try to trigger Herbal Blending Rack crafting (requires shift + right click)
        if (player != null && player.isShiftKeyDown() 
                && MultiblockHerbalBlending.INSTANCE.isBlockTrigger(clickedState)) {
            BlendingStructure structure = MultiblockHerbalBlending.INSTANCE.findStructure(
                    level,
                    context.getClickedPos(),
                    context.getClickedFace(),
                    player);
            
            if (structure != null) {
                if (MultiblockHerbalBlending.INSTANCE.tryCraft(level, structure, player)) {
                    return InteractionResult.SUCCESS;
                }
            }
        }
        
        // Try to trigger Workbench crafting (right-click center block)
        if (clickedState.is(ModRegistries.WORKBENCH.get())) {
            if (clickedState.getValue(WorkbenchBlock.PART) == WorkbenchBlock.WorkbenchPart.CENTER) {
                boolean isShift = player != null && player.isShiftKeyDown();
                if (tryWorkbenchCraft(level, context.getClickedPos(), player, isShift)) {
                    return InteractionResult.SUCCESS;
                }
            }
        }
        
        // If no other action triggered and ring has bound potion
        if (player != null && hasBoundPotion(stack)) {
            // Shift+right-click on non-trigger block: cycle mode
            if (player.isShiftKeyDown()) {
                CastingMode newMode = cycleMode(stack);
                player.displayClientMessage(
                    Component.translatable("item.herbalcurative.flowweave_ring.mode_changed", newMode.getDisplayName())
                        .withStyle(ChatFormatting.AQUA), 
                    true);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.5F, 1.2F);
                return InteractionResult.SUCCESS;
            }
            
            // Normal right-click: try to cast
            if (tryCastPotion(level, player, stack)) {
                return InteractionResult.SUCCESS;
            }
        }
        
        return InteractionResult.PASS;
    }
    
    /**
     * Check if clicking would trigger any action (used for client-side swing animation)
     */
    private boolean wouldTriggerAction(UseOnContext context, BlockState clickedState) {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        
        // Check multiblock triggers
        if (MultiblockHerbCabinet.INSTANCE.isBlockTrigger(clickedState)) {
            return true;
        }
        if (MultiblockCauldron.INSTANCE.isBlockTrigger(clickedState)) {
            return true;
        }
        // Check formed Cauldron
        if (clickedState.is(ModRegistries.CAULDRON.get()) && clickedState.getValue(CauldronBlock.FORMED)) {
            return true;
        }
        if (player != null && player.isShiftKeyDown() 
                && MultiblockHerbalBlending.INSTANCE.isBlockTrigger(clickedState)) {
            return true;
        }
        if (clickedState.is(ModRegistries.WORKBENCH.get()) 
                && clickedState.getValue(WorkbenchBlock.PART) == WorkbenchBlock.WorkbenchPart.CENTER) {
            return true;
        }
        
        // Check if casting would trigger (has bound potion)
        if (hasBoundPotion(stack)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Try to craft using the workbench.
     * @param level The world level
     * @param centerPos The position of the center workbench block
     * @param player The player performing the craft (can be null)
     * @param craftAll If true, craft as many as possible; if false, craft one
     * @return true if crafting was successful
     */
    private boolean tryWorkbenchCraft(Level level, BlockPos centerPos, Player player, boolean craftAll) {
        BlockEntity be = level.getBlockEntity(centerPos);
        if (!(be instanceof WorkbenchBlockEntity workbench)) {
            return false;
        }
        
        // Create recipe input from workbench state
        WorkbenchRecipe.WorkbenchInput input = new WorkbenchRecipe.WorkbenchInput(workbench);
        
        // Find matching recipe
        Optional<RecipeHolder<WorkbenchRecipe>> recipeHolder = level.getRecipeManager()
                .getRecipeFor(ModRegistries.WORKBENCH_RECIPE_TYPE.get(), input, level);
        
        if (recipeHolder.isEmpty()) {
            return false;
        }
        
        WorkbenchRecipe recipe = recipeHolder.get().value();
        
        // Calculate how many to craft
        int craftCount = craftAll ? recipe.getMaxCraftCount(input) : 1;
        if (craftCount <= 0) {
            return false;
        }
        
        // Check experience cost (if recipe requires experience and player exists)
        int expCost = recipe.getExperienceCost();
        if (expCost > 0 && player != null && !player.isCreative()) {
            // Calculate total experience cost
            int totalExpCost = expCost * craftCount;
            int playerExp = getTotalExperience(player);
            
            if (playerExp < totalExpCost) {
                // Not enough experience - limit craft count to what player can afford
                craftCount = playerExp / expCost;
                if (craftCount <= 0) {
                    // Play failure sound
                    level.playSound(null, centerPos, SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return false;
                }
            }
        }
        
        // Perform crafting
        for (int i = 0; i < craftCount; i++) {
            // Damage tools by item type (tools can be in any slot)
            for (WorkbenchRecipe.ToolRequirement tool : recipe.getTools()) {
                for (int d = 0; d < tool.damage(); d++) {
                    workbench.damageToolByItem(tool.item());
                }
            }
            
            // Consume materials by type
            for (WorkbenchRecipe.MaterialRequirement req : recipe.getMaterials()) {
                workbench.consumeMaterialByType(req.item(), req.count());
            }
            
            // Consume input
            workbench.consumeInput(1);
            
            // Consume experience
            if (expCost > 0 && player != null && !player.isCreative()) {
                player.giveExperiencePoints(-expCost);
            }
        }
        
        // Create result and drop it
        ItemStack result = recipe.getResult();
        result.setCount(result.getCount() * craftCount);
        
        // Drop the result (pop out from center)
        ItemEntity itemEntity = new ItemEntity(level, 
                centerPos.getX() + 0.5, centerPos.getY() + 1.0, centerPos.getZ() + 0.5, result);
        itemEntity.setDeltaMovement(0, 0.2, 0); // Small upward velocity
        level.addFreshEntity(itemEntity);
        
        // Play success sound
        level.playSound(null, centerPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        
        return true;
    }
    
    /**
     * Calculate total experience points the player has.
     * Experience is stored as levels + progress, need to convert to total points.
     */
    private int getTotalExperience(Player player) {
        int level = player.experienceLevel;
        float progress = player.experienceProgress;
        
        // Calculate points needed to reach current level
        int totalPoints;
        if (level <= 16) {
            totalPoints = level * level + 6 * level;
        } else if (level <= 31) {
            totalPoints = (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            totalPoints = (int) (4.5 * level * level - 162.5 * level + 2220);
        }
        
        // Add progress towards next level
        int pointsForNextLevel = getExperienceForLevel(level + 1) - getExperienceForLevel(level);
        totalPoints += (int) (progress * pointsForNextLevel);
        
        return totalPoints;
    }
    
    /**
     * Get total experience points needed to reach a specific level.
     */
    private int getExperienceForLevel(int level) {
        if (level <= 16) {
            return level * level + 6 * level;
        } else if (level <= 31) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
    }
    
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
