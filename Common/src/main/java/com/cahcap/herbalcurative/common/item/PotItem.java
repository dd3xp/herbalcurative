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
 * - Effect duration (in seconds)
 * - Effect level
 * 
 * When used (right-click while holding):
 * - Applies the stored potion effect to the player
 * - Empties the pot
 */
public class PotItem extends Item {
    
    // NBT keys for potion data stored in CustomData component
    private static final String TAG_POTION_TYPES = "PotionTypes";  // List of effect IDs
    private static final String TAG_POTION_TYPE = "PotionType";    // Legacy single effect
    private static final String TAG_POTION_COLOR = "PotionColor";
    private static final String TAG_DURATION = "Duration";
    private static final String TAG_LEVEL = "Level";
    private static final String TAG_FILLED = "Filled";
    private static final String TAG_USES = "Uses";
    
    // Max uses per pot
    public static final int MAX_USES = 32;
    
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
        
        // Can only collect potion (not regular fluid)
        if (!master.getFluid().isPotion()) {
            return InteractionResult.PASS;
        }
        
        ItemStack stack = context.getItemInHand();
        
        // Check if pot is already filled
        if (isFilled(stack)) {
            return InteractionResult.PASS;
        }
        
        // Get effect IDs for pot
        java.util.List<String> effectIds = new java.util.ArrayList<>();
        for (net.minecraft.world.effect.MobEffect effect : master.getFluid().getEffects()) {
            net.minecraft.resources.ResourceLocation effectResLoc = net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect);
            if (effectResLoc != null) {
                effectIds.add(effectResLoc.toString());
            }
        }
        
        // Fill the pot with potion
        fillPot(stack, effectIds, master.getFluidColor(), 
                master.getPotionDuration(), master.getPotionLevel());
        
        // Return any floating materials and output slot to player
        Player player = context.getPlayer();
        if (player != null) {
            // Return materials
            for (ItemStack material : master.getMaterials()) {
                if (!material.isEmpty()) {
                    if (!player.getInventory().add(material.copy())) {
                        player.drop(material.copy(), false);
                    }
                }
            }
            // Return output slot
            if (master.hasOutputSlotItems()) {
                ItemStack output = master.extractFromOutputSlot();
                if (!output.isEmpty()) {
                    if (!player.getInventory().add(output)) {
                        player.drop(output, false);
                    }
                }
            }
        }
        
        // Clear the cauldron fluid (this also clears materials and output slot)
        master.clearFluid();
        
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
                
                // Decrease uses
                int uses = getUses(stack) - 1;
                if (uses <= 0) {
                    // Empty the pot when all uses are consumed
                    emptyPot(stack);
                } else {
                    // Update uses count
                    setUses(stack, uses);
                }
                
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
    
    /**
     * Fill pot with multiple effects
     */
    public static void fillPot(ItemStack stack, java.util.List<String> potionTypes, int color, int duration, int level) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(TAG_FILLED, true);
        
        // Store effects as a list
        net.minecraft.nbt.ListTag effectsList = new net.minecraft.nbt.ListTag();
        for (String effectId : potionTypes) {
            effectsList.add(net.minecraft.nbt.StringTag.valueOf(effectId));
        }
        tag.put(TAG_POTION_TYPES, effectsList);
        
        // Also store first effect for backwards compatibility
        if (!potionTypes.isEmpty()) {
            tag.putString(TAG_POTION_TYPE, potionTypes.get(0));
        }
        
        tag.putInt(TAG_POTION_COLOR, color);
        tag.putInt(TAG_DURATION, duration);
        tag.putInt(TAG_LEVEL, level);
        tag.putInt(TAG_USES, MAX_USES);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
    
    /**
     * Fill pot with single effect (backwards compatible)
     */
    public static void fillPot(ItemStack stack, String potionType, int color, int duration, int level) {
        fillPot(stack, java.util.List.of(potionType), color, duration, level);
    }
    
    public static void emptyPot(ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(TAG_FILLED, false);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
    
    public static int getUses(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return 0;
        }
        int uses = customData.copyTag().getInt(TAG_USES);
        return uses > 0 ? uses : MAX_USES; // Default to MAX_USES for old pots
    }
    
    public static void setUses(ItemStack stack, int uses) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return;
        
        CompoundTag tag = customData.copyTag();
        tag.putInt(TAG_USES, uses);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
    
    /**
     * Get all effect IDs from the pot
     */
    public static java.util.List<String> getPotionTypes(ItemStack stack) {
        java.util.List<String> effectIds = new java.util.ArrayList<>();
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return effectIds;
        }
        
        CompoundTag tag = customData.copyTag();
        
        // Try new list format first
        if (tag.contains(TAG_POTION_TYPES)) {
            net.minecraft.nbt.ListTag effectsList = tag.getList(TAG_POTION_TYPES, net.minecraft.nbt.Tag.TAG_STRING);
            for (int i = 0; i < effectsList.size(); i++) {
                effectIds.add(effectsList.getString(i));
            }
        } else if (tag.contains(TAG_POTION_TYPE)) {
            // Fallback to legacy single effect
            String legacyType = tag.getString(TAG_POTION_TYPE);
            if (!legacyType.isEmpty()) {
                effectIds.add(legacyType);
            }
        }
        
        return effectIds;
    }
    
    /**
     * Get first effect ID (for backwards compatibility)
     */
    public static String getPotionType(ItemStack stack) {
        java.util.List<String> types = getPotionTypes(stack);
        return types.isEmpty() ? "" : types.get(0);
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
        java.util.List<String> potionTypes = getPotionTypes(stack);
        int amplifier = getLevel(stack) - 1; // MobEffectInstance uses 0-based amplifier
        int durationTicks = getDuration(stack) * 20; // Convert seconds to ticks
        
        for (String potionType : potionTypes) {
            Holder<MobEffect> effect = getEffectForType(potionType);
            if (effect != null) {
                // Check if this is an instant effect using vanilla API
                boolean isInstant = isInstantEffect(effect);
                
                if (isInstant) {
                    // Use vanilla's applyInstantenousEffect for immediate application
                    // Parameters: source entity, owner entity, target, amplifier, proximity (1.0 = full effect)
                    effect.value().applyInstantenousEffect(null, player, player, amplifier, 1.0);
                } else {
                    player.addEffect(new MobEffectInstance(effect, durationTicks, amplifier));
                }
            }
        }
    }
    
    /**
     * Get effect holder from registry ID string.
     * Uses dynamic registry lookup instead of hardcoded switch.
     */
    private Holder<MobEffect> getEffectForType(String type) {
        // Try to parse as ResourceLocation
        ResourceLocation id = ResourceLocation.tryParse(type);
        if (id == null) return null;
        
        MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(id);
        if (effect == null) return null;
        
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);
    }
    
    /**
     * Check if an effect is instantaneous (like heal/harm) using vanilla API.
     */
    private boolean isInstantEffect(Holder<MobEffect> effect) {
        return effect != null && effect.value().isInstantenous();
    }
    
    /**
     * Get effect display name from registry.
     */
    private String getEffectDisplayName(String type) {
        ResourceLocation id = ResourceLocation.tryParse(type);
        if (id != null) {
            MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(id);
            if (effect != null) {
                return effect.getDisplayName().getString();
            }
        }
        return "Unknown";
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (isFilled(stack)) {
            java.util.List<String> types = getPotionTypes(stack);
            int duration = getDuration(stack);
            int level = getLevel(stack);
            int uses = getUses(stack);
            
            // Potion effects - each effect on its own line
            tooltip.add(Component.literal("Potion of")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            for (String type : types) {
                String typeName = getEffectDisplayName(type);
                tooltip.add(Component.literal("  " + typeName)
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            
            // Level
            if (level > 1) {
                tooltip.add(Component.literal("Level " + level)
                        .withStyle(ChatFormatting.BLUE));
            }
            
            // Check if any effect is instant (don't show duration if any effect is instant)
            boolean isInstant = false;
            for (String type : types) {
                Holder<MobEffect> effect = getEffectForType(type);
                if (isInstantEffect(effect)) {
                    isInstant = true;
                    break;
                }
            }
            
            if (!isInstant) {
                // Duration is stored in seconds, display as "mm:ss"
                int minutes = duration / 60;
                int seconds = duration % 60;
                String durationText = String.format("%02d:%02d", minutes, seconds);
                tooltip.add(Component.literal("Duration: " + durationText)
                        .withStyle(ChatFormatting.GRAY));
            }
            
            // Uses remaining
            tooltip.add(Component.literal("Uses: " + uses + "/" + MAX_USES)
                    .withStyle(ChatFormatting.AQUA));
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
