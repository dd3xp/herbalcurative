package com.cahcap.common.blockentity.cauldron;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the fluid content of a Cauldron.
 * Can be empty, a vanilla/modded fluid, or a potion with custom properties.
 */
public class CauldronFluid {

    public enum FluidType {
        EMPTY,          // No fluid
        FLUID,          // Vanilla/modded fluid (water, lava, etc.)
        BOILING_POTION, // Brewing in progress - effect determined, duration/level pending
        POTION          // Finished potion with effect properties
    }

    public static final int MAX_POTION_UNITS = 32;

    private FluidType type = FluidType.EMPTY;
    private int amount = 0;  // In millibuckets (1000 = 1 bucket)
    private int potionUnits = 0;  // Potion dose units (0-32), only meaningful for POTION type

    // For FLUID type
    private Fluid fluid = null;

    // For POTION type - now supports multiple effects
    private List<MobEffect> effects = new ArrayList<>();
    private int duration = 0;      // In seconds
    private int amplifier = 0;     // Potion level (0 = level 1)
    private int color = 0x3F76E4;  // Default water color

    // Private constructor - use factory methods
    private CauldronFluid() {}

    // ==================== Factory Methods ====================

    public static CauldronFluid empty() {
        return new CauldronFluid();
    }

    public static CauldronFluid ofFluid(Fluid fluid, int amount) {
        CauldronFluid cf = new CauldronFluid();
        cf.type = FluidType.FLUID;
        cf.fluid = fluid;
        cf.amount = amount;
        return cf;
    }

    public static CauldronFluid ofWater(int amount) {
        return ofFluid(Fluids.WATER, amount);
    }

    public static CauldronFluid ofPotion(List<MobEffect> effects, int duration, int amplifier, int color, int amount) {
        CauldronFluid cf = new CauldronFluid();
        cf.type = FluidType.POTION;
        cf.effects = new ArrayList<>(effects);
        cf.duration = duration;
        cf.amplifier = amplifier;
        cf.color = color;
        cf.amount = amount;
        cf.potionUnits = MAX_POTION_UNITS;
        return cf;
    }

    // Single effect version for backwards compatibility
    public static CauldronFluid ofPotion(MobEffect effect, int duration, int amplifier, int color, int amount) {
        return ofPotion(List.of(effect), duration, amplifier, color, amount);
    }

    public static CauldronFluid ofBoilingPotion(List<MobEffect> effects, int color, int amount) {
        CauldronFluid cf = new CauldronFluid();
        cf.type = FluidType.BOILING_POTION;
        cf.effects = new ArrayList<>(effects);
        cf.duration = 0;   // To be determined by herbs
        cf.amplifier = 0;  // To be determined by herbs
        cf.color = color;
        cf.amount = amount;
        return cf;
    }

    // Single effect version for backwards compatibility
    public static CauldronFluid ofBoilingPotion(MobEffect effect, int color, int amount) {
        return ofBoilingPotion(List.of(effect), color, amount);
    }

    // ==================== Getters ====================

    public FluidType getType() {
        return type;
    }

    public boolean isEmpty() {
        return type == FluidType.EMPTY || amount <= 0;
    }

    public boolean isFluid() {
        return type == FluidType.FLUID;
    }

    public boolean isPotion() {
        return type == FluidType.POTION;
    }

    public boolean isBoilingPotion() {
        return type == FluidType.BOILING_POTION;
    }

    public boolean isWater() {
        return type == FluidType.FLUID && fluid == Fluids.WATER;
    }

    public int getAmount() {
        return amount;
    }

    public int getPotionUnits() {
        return potionUnits;
    }

    /**
     * Consume potion units. If units reach 0, convert to water.
     * @param units number of units to consume
     */
    public void consumeUnits(int units) {
        if (!isPotion()) return;
        potionUnits = Math.max(0, potionUnits - units);
        if (potionUnits <= 0) {
            convertToWater();
        }
    }

    public Fluid getFluid() {
        return fluid;
    }

    /**
     * Get the first effect (for backwards compatibility)
     */
    public MobEffect getEffect() {
        return effects.isEmpty() ? null : effects.get(0);
    }

    /**
     * Get all effects
     */
    public List<MobEffect> getEffects() {
        return effects;
    }

    public int getDuration() {
        return duration;
    }

    public int getAmplifier() {
        return amplifier;
    }

    /**
     * Get the display color (interpolated for potions based on remaining units).
     * Use for rendering the fluid in the cauldron and tooltip.
     */
    public int getColor() {
        if (type == FluidType.EMPTY) {
            return 0;
        } else if (type == FluidType.FLUID) {
            return getFluidColor(fluid);
        } else if (type == FluidType.POTION && potionUnits < MAX_POTION_UNITS && potionUnits > 0) {
            float ratio = (float) potionUnits / MAX_POTION_UNITS;
            return lerpColor(WATER_COLOR, color, ratio);
        } else {
            return color;
        }
    }

    /**
     * Get the original undiluted potion color (not interpolated).
     * Use when storing to pot or serialization that needs the base color.
     */
    public int getBaseColor() {
        return color;
    }

    private static final int WATER_COLOR = 0x3F76E4;

    private static int lerpColor(int from, int to, float ratio) {
        int fr = (from >> 16) & 0xFF, fg = (from >> 8) & 0xFF, fb = from & 0xFF;
        int tr = (to >> 16) & 0xFF, tg = (to >> 8) & 0xFF, tb = to & 0xFF;
        int r = (int) (fr + (tr - fr) * ratio);
        int g = (int) (fg + (tg - fg) * ratio);
        int b = (int) (fb + (tb - fb) * ratio);
        return (r << 16) | (g << 8) | b;
    }

    private static int getFluidColor(Fluid fluid) {
        if (fluid == null || fluid == Fluids.EMPTY) return 0;
        if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) return 0x3F76E4;
        if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) return 0xFF5500;
        return 0xFFFFFF;
    }

    // ==================== Setters ====================

    public void setAmount(int amount) {
        this.amount = amount;
        if (amount <= 0) {
            clear();
        }
    }

    public void clear() {
        this.type = FluidType.EMPTY;
        this.amount = 0;
        this.potionUnits = 0;
        this.fluid = null;
        this.effects.clear();
        this.duration = 0;
        this.amplifier = 0;
        this.color = 0x3F76E4;
    }

    /**
     * Convert this fluid to water (used after infusing completes)
     */
    public void convertToWater() {
        this.type = FluidType.FLUID;
        this.fluid = Fluids.WATER;
        this.potionUnits = 0;
        this.effects.clear();
        this.duration = 0;
        this.amplifier = 0;
        this.color = 0x3F76E4;
        // Keep the same amount
    }

    /**
     * Convert water to boiling potion (used when brewing starts)
     * Effect types are determined, but duration/amplifier are pending (based on herbs added later)
     */
    public void convertToBoilingPotion(List<MobEffect> effects, int color) {
        this.type = FluidType.BOILING_POTION;
        this.fluid = null;
        this.effects = new ArrayList<>(effects);
        this.duration = 0;   // To be determined
        this.amplifier = 0;  // To be determined
        this.color = color;
        // Keep the same amount
    }

    // Single effect version for backwards compatibility
    public void convertToBoilingPotion(MobEffect effect, int color) {
        convertToBoilingPotion(List.of(effect), color);
    }

    /**
     * Convert boiling potion to finished potion (used when brewing completes)
     */
    public void convertToPotion(int duration, int amplifier) {
        if (this.type != FluidType.BOILING_POTION) return;
        this.type = FluidType.POTION;
        this.duration = duration;
        this.amplifier = amplifier;
        this.potionUnits = MAX_POTION_UNITS;
        // Set color from first effect (boiling used water color)
        if (!this.effects.isEmpty()) {
            this.color = this.effects.get(0).getColor();
        }
    }

    /**
     * Convert boiling potion to finished potion with custom color
     */
    public void convertToPotion(int duration, int amplifier, int color) {
        if (this.type != FluidType.BOILING_POTION) return;
        this.type = FluidType.POTION;
        this.potionUnits = MAX_POTION_UNITS;
        this.duration = duration;
        this.amplifier = amplifier;
        this.color = color;
    }

    /**
     * Convert water to potion directly (used for legacy compatibility)
     */
    public void convertToPotion(MobEffect effect, int duration, int amplifier, int color) {
        this.type = FluidType.POTION;
        this.fluid = null;
        this.effects = new ArrayList<>(List.of(effect));
        this.duration = duration;
        this.amplifier = amplifier;
        this.color = color;
        // Keep the same amount
    }

    // ==================== NBT Serialization ====================

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", type.name());
        tag.putInt("Amount", amount);
        tag.putInt("PotionUnits", potionUnits);

        if (type == FluidType.FLUID && fluid != null) {
            tag.putString("Fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());
        } else if ((type == FluidType.POTION || type == FluidType.BOILING_POTION) && !effects.isEmpty()) {
            // Save effects as a list
            ListTag effectsList = new ListTag();
            for (MobEffect effect : effects) {
                ResourceLocation effectId = BuiltInRegistries.MOB_EFFECT.getKey(effect);
                if (effectId != null) {
                    StringTag effectTag = StringTag.valueOf(effectId.toString());
                    effectsList.add(effectTag);
                }
            }
            tag.put("Effects", effectsList);
            tag.putInt("Duration", duration);
            tag.putInt("Amplifier", amplifier);
            tag.putInt("Color", color);
        }

        return tag;
    }

    public static CauldronFluid load(CompoundTag tag, HolderLookup.Provider registries) {
        CauldronFluid cf = new CauldronFluid();

        String typeName = tag.getString("Type");
        cf.type = FluidType.valueOf(typeName.isEmpty() ? "EMPTY" : typeName);
        cf.amount = tag.getInt("Amount");
        cf.potionUnits = tag.getInt("PotionUnits");

        if (cf.type == FluidType.FLUID && tag.contains("Fluid")) {
            ResourceLocation fluidId = ResourceLocation.tryParse(tag.getString("Fluid"));
            if (fluidId != null) {
                cf.fluid = BuiltInRegistries.FLUID.get(fluidId);
            }
        } else if ((cf.type == FluidType.POTION || cf.type == FluidType.BOILING_POTION)) {
            // Load effects - support both old single effect and new list format
            if (tag.contains("Effects")) {
                ListTag effectsList = tag.getList("Effects", Tag.TAG_STRING);
                for (int i = 0; i < effectsList.size(); i++) {
                    ResourceLocation effectId = ResourceLocation.tryParse(effectsList.getString(i));
                    if (effectId != null) {
                        MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(effectId);
                        if (effect != null) {
                            cf.effects.add(effect);
                        }
                    }
                }
            } else if (tag.contains("Effect")) {
                // Legacy single effect format
                ResourceLocation effectId = ResourceLocation.tryParse(tag.getString("Effect"));
                if (effectId != null) {
                    MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(effectId);
                    if (effect != null) {
                        cf.effects.add(effect);
                    }
                }
            }
            cf.duration = tag.getInt("Duration");
            cf.amplifier = tag.getInt("Amplifier");
            cf.color = tag.getInt("Color");
        }

        return cf;
    }

    // ==================== Matching ====================

    /**
     * Check if this fluid matches a specific vanilla/modded fluid
     */
    public boolean matchesFluid(Fluid fluid) {
        return type == FluidType.FLUID && this.fluid == fluid;
    }

    /**
     * Check if this fluid matches potion requirements
     */
    public boolean matchesPotion(MobEffect requiredEffect, int minDuration, int minAmplifier) {
        if (type != FluidType.POTION) return false;
        if (requiredEffect != null && !this.effects.contains(requiredEffect)) return false;
        return this.duration >= minDuration && this.amplifier >= minAmplifier;
    }

    /**
     * Check if this potion has any of the given effects
     */
    public boolean hasAnyEffect(List<MobEffect> requiredEffects) {
        if (type != FluidType.POTION && type != FluidType.BOILING_POTION) return false;
        for (MobEffect effect : requiredEffects) {
            if (this.effects.contains(effect)) return true;
        }
        return false;
    }

    /**
     * Check if this matches any fluid (water or potion)
     */
    public boolean matchesAny() {
        return !isEmpty();
    }

    // ==================== Copy ====================

    public CauldronFluid copy() {
        CauldronFluid cf = new CauldronFluid();
        cf.type = this.type;
        cf.amount = this.amount;
        cf.fluid = this.fluid;
        cf.effects = new ArrayList<>(this.effects);
        cf.duration = this.duration;
        cf.amplifier = this.amplifier;
        cf.color = this.color;
        return cf;
    }
}
