package com.cahcap.herbalcurative.common.item;

import com.cahcap.herbalcurative.common.blockentity.CauldronBlockEntity;
import com.cahcap.herbalcurative.common.registry.ModRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Pot Item - Used to collect and store potions from the Cauldron.
 * 
 * Can store:
 * - Potion type (healing, strength, etc.)
 * - Potion color
 * - Effect duration (in minutes)
 * - Effect level
 * 
 * When used (right-click while holding):
 * - Applies the stored potion effect to the player
 * - Empties the pot
 */
public class PotItem extends Item {
    
    // NBT keys for potion data stored in CustomData component
    private static final String TAG_POTION_TYPE = "PotionType";
    private static final String TAG_POTION_COLOR = "PotionColor";
    private static final String TAG_DURATION = "Duration";
    private static final String TAG_LEVEL = "Level";
    private static final String TAG_FILLED = "Filled";
    
    public PotItem(Properties properties) {
        super(properties.stacksTo(1));
    }
    
    /**
     * Use on a cauldron to collect potion
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        
        // Check if clicking on cauldron
        if (!state.is(ModRegistries.CAULDRON.get())) {
            return InteractionResult.PASS;
        }
        
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        
        if (!(level.getBlockEntity(pos) instanceof CauldronBlockEntity be)) {
            return InteractionResult.PASS;
        }
        
        CauldronBlockEntity master = be.getMaster();
        if (master == null) {
            return InteractionResult.PASS;
        }
        
        // Can only collect from phase 3 (complete)
        if (master.getPhase() != CauldronBlockEntity.PHASE_COMPLETE) {
            return InteractionResult.PASS;
        }
        
        ItemStack stack = context.getItemInHand();
        
        // Check if pot is already filled
        if (isFilled(stack)) {
            return InteractionResult.PASS;
        }
        
        // Fill the pot with potion
        fillPot(stack, master.getPotionType(), master.getPotionColor(), 
                master.getPotionDuration(), master.getPotionLevel());
        
        // Empty the cauldron
        master.resetCauldron();
        
        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        
        return InteractionResult.SUCCESS;
    }
    
    /**
     * Drink the potion when used
     */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            if (isFilled(stack)) {
                // Apply effect
                applyPotionEffect(stack, player);
                
                // Empty the pot
                emptyPot(stack);
                
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
        return stack;
    }
    
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return isFilled(stack) ? 32 : 0;
    }
    
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return isFilled(stack) ? UseAnim.DRINK : UseAnim.NONE;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isFilled(stack)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.pass(stack);
    }
    
    // ==================== NBT Helpers ====================
    
    public static boolean isFilled(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }
        return customData.copyTag().getBoolean(TAG_FILLED);
    }
    
    public static void fillPot(ItemStack stack, String potionType, int color, int duration, int level) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(TAG_FILLED, true);
        tag.putString(TAG_POTION_TYPE, potionType);
        tag.putInt(TAG_POTION_COLOR, color);
        tag.putInt(TAG_DURATION, duration);
        tag.putInt(TAG_LEVEL, level);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
    
    public static void emptyPot(ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(TAG_FILLED, false);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
    
    public static String getPotionType(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return "";
        }
        return customData.copyTag().getString(TAG_POTION_TYPE);
    }
    
    public static int getPotionColor(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return 0x3F76E4;
        }
        return customData.copyTag().getInt(TAG_POTION_COLOR);
    }
    
    public static int getDuration(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return 0;
        }
        return customData.copyTag().getInt(TAG_DURATION);
    }
    
    public static int getLevel(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return 1;
        }
        return customData.copyTag().getInt(TAG_LEVEL);
    }
    
    private void applyPotionEffect(ItemStack stack, Player player) {
        String potionType = getPotionType(stack);
        int duration = getDuration(stack) * 60 * 20; // Convert minutes to ticks
        int level = getLevel(stack) - 1; // MobEffectInstance uses 0-based amplifier
        
        Holder<MobEffect> effect = getEffectForType(potionType);
        if (effect != null) {
            player.addEffect(new MobEffectInstance(effect, duration, level));
        }
    }
    
    private Holder<MobEffect> getEffectForType(String type) {
        return switch (type) {
            case "healing" -> MobEffects.HEAL;
            case "regeneration" -> MobEffects.REGENERATION;
            case "strength" -> MobEffects.DAMAGE_BOOST;
            case "speed" -> MobEffects.MOVEMENT_SPEED;
            case "fire_resistance" -> MobEffects.FIRE_RESISTANCE;
            case "night_vision" -> MobEffects.NIGHT_VISION;
            case "invisibility" -> MobEffects.INVISIBILITY;
            case "water_breathing" -> MobEffects.WATER_BREATHING;
            case "jump_boost" -> MobEffects.JUMP;
            case "slow_falling" -> MobEffects.SLOW_FALLING;
            default -> null;
        };
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (isFilled(stack)) {
            String type = getPotionType(stack);
            int duration = getDuration(stack);
            int level = getLevel(stack);
            
            // Potion type
            String typeName = switch (type) {
                case "healing" -> "Healing";
                case "regeneration" -> "Regeneration";
                case "strength" -> "Strength";
                case "speed" -> "Speed";
                case "fire_resistance" -> "Fire Resistance";
                case "night_vision" -> "Night Vision";
                case "invisibility" -> "Invisibility";
                case "water_breathing" -> "Water Breathing";
                case "jump_boost" -> "Jump Boost";
                case "slow_falling" -> "Slow Falling";
                default -> "Unknown";
            };
            
            tooltip.add(Component.literal("Potion of " + typeName)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            
            // Level
            if (level > 1) {
                tooltip.add(Component.literal("Level " + level)
                        .withStyle(ChatFormatting.BLUE));
            }
            
            // Duration
            tooltip.add(Component.literal("Duration: " + duration + " min")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal("Empty")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return isFilled(stack);
    }
}
