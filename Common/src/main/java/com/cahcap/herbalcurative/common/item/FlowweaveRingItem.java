package com.cahcap.herbalcurative.common.item;

import com.cahcap.herbalcurative.common.block.CauldronBlock;
import com.cahcap.herbalcurative.common.block.WorkbenchBlock;
import com.cahcap.herbalcurative.common.blockentity.CauldronBlockEntity;
import com.cahcap.herbalcurative.common.blockentity.WorkbenchBlockEntity;
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
 */
public class FlowweaveRingItem extends Item {
    
    // NBT keys for bound potion
    private static final String TAG_BOUND = "BoundPotion";
    private static final String TAG_POTION_TYPE = "PotionType";
    private static final String TAG_POTION_COLOR = "PotionColor";
    private static final String TAG_DURATION = "Duration";
    private static final String TAG_LEVEL = "Level";
    private static final String TAG_HERB_COST = "HerbCost";
    
    // Minimum duration for binding (8 minutes = 480 seconds)
    public static final int MIN_BIND_DURATION = 480;
    
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
     */
    public static void bindPotion(ItemStack stack, String potionType, int color, 
                                   int duration, int level, Map<Item, Integer> herbCost) {
        CompoundTag tag = new CompoundTag();
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
     * Right-click in air - cast if bound potion exists
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!hasBoundPotion(stack)) {
            return InteractionResultHolder.pass(stack);
        }
        
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        
        // Try to cast
        if (tryCastPotion(level, player, stack)) {
            return InteractionResultHolder.success(stack);
        }
        
        return InteractionResultHolder.fail(stack);
    }
    
    /**
     * Try to cast the bound potion effect
     */
    private boolean tryCastPotion(Level level, Player player, ItemStack stack) {
        if (!hasBoundPotion(stack)) {
            return false;
        }
        
        // Check if player has the required herbs
        Map<Item, Integer> herbCost = getHerbCost(stack);
        if (!hasRequiredHerbs(player, herbCost)) {
            // Play failure sound
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
            return false;
        }
        
        // Consume herbs
        if (!player.isCreative()) {
            consumeHerbs(player, herbCost);
        }
        
        // Apply effect
        String potionType = getBoundPotionType(stack);
        int duration = getBoundDuration(stack) * 20; // Convert seconds to ticks
        int amplifier = getBoundLevel(stack) - 1; // 0-based amplifier
        
        Holder<MobEffect> effect = getEffectForType(potionType);
        if (effect != null) {
            player.addEffect(new MobEffectInstance(effect, duration, amplifier));
        }
        
        // Play success sound
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
        
        return true;
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
            
            // Duration is stored in seconds, display as "mm:ss"
            int minutes = duration / 60;
            int seconds = duration % 60;
            String durationText = String.format("%02d:%02d", minutes, seconds);
            tooltip.add(Component.literal("Duration: " + durationText)
                    .withStyle(ChatFormatting.GRAY));
            
            // Show herb cost
            Map<Item, Integer> herbCost = getHerbCost(stack);
            if (!herbCost.isEmpty()) {
                tooltip.add(Component.literal("Herb Cost:")
                        .withStyle(ChatFormatting.YELLOW));
                for (Map.Entry<Item, Integer> entry : herbCost.entrySet()) {
                    tooltip.add(Component.literal("  " + entry.getValue() + "x " + 
                            entry.getKey().getDescription().getString())
                            .withStyle(ChatFormatting.GRAY));
                }
            }
        }
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
        
        // If no other action triggered and ring has bound potion, try to cast
        if (player != null && hasBoundPotion(stack)) {
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
