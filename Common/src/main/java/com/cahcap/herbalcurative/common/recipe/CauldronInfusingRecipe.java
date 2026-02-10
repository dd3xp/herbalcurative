package com.cahcap.herbalcurative.common.recipe;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.common.blockentity.CauldronBlockEntity.CauldronFluid;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Recipe for infusing items inside the Cauldron with fluid or potion.
 * 
 * Supports matching:
 * - Specific fluid (e.g., "minecraft:water", "minecraft:lava")
 * - Potion with optional effect/duration/level requirements
 * - Any fluid (when fluidType is empty and potionType is empty)
 * 
 * STRICT MATCHING: Materials in cauldron must EXACTLY match the recipe
 * - Same items with same counts
 * - No extra items allowed
 * - No missing items allowed
 * 
 * After infusing completes (5 seconds), the output is added to materials
 * and the fluid converts to water.
 */
public class CauldronInfusingRecipe implements Recipe<SingleRecipeInput> {
    
    public static final int INFUSING_TIME_TICKS = 100;  // 5 seconds
    
    private final List<ItemStack> inputs;     // List of required materials with exact counts
    private final ItemStack output;
    private final String fluidType;      // Fluid registry name (e.g., "minecraft:water") or empty for any/potion
    private final String potionType;     // Effect registry name (e.g., "minecraft:instant_health") or empty
    private final int minDuration;
    private final int minLevel;
    private final boolean isFlowweaveRingBinding;   // Special: dynamic output for Flowweave Ring binding
    private final boolean isFlowweaveRingUnbinding; // Special: clear ring binding in water
    
    public CauldronInfusingRecipe(List<ItemStack> inputs, ItemStack output, String fluidType, String potionType, 
                          int minDuration, int minLevel, boolean isFlowweaveRingBinding, boolean isFlowweaveRingUnbinding) {
        this.inputs = new ArrayList<>(inputs);
        this.output = output;
        this.fluidType = fluidType;
        this.potionType = potionType;
        this.minDuration = minDuration;
        this.minLevel = minLevel;
        this.isFlowweaveRingBinding = isFlowweaveRingBinding;
        this.isFlowweaveRingUnbinding = isFlowweaveRingUnbinding;
    }
    
    // Constructor with only binding flag (for compatibility)
    public CauldronInfusingRecipe(List<ItemStack> inputs, ItemStack output, String fluidType, String potionType, 
                          int minDuration, int minLevel, boolean isFlowweaveRingBinding) {
        this(inputs, output, fluidType, potionType, minDuration, minLevel, isFlowweaveRingBinding, false);
    }
    
    // Convenience constructor without flowweave flags
    public CauldronInfusingRecipe(List<ItemStack> inputs, ItemStack output, String fluidType, String potionType, 
                          int minDuration, int minLevel) {
        this(inputs, output, fluidType, potionType, minDuration, minLevel, false, false);
    }
    
    // Legacy single-input constructor for compatibility
    public CauldronInfusingRecipe(Ingredient input, ItemStack output, String fluidType, String potionType, 
                          int minDuration, int minLevel, int processingTime) {
        this.inputs = new ArrayList<>();
        // Convert Ingredient to ItemStack (use first item in ingredient)
        for (ItemStack stack : input.getItems()) {
            this.inputs.add(stack.copy());
            break;  // Only take first
        }
        this.output = output;
        this.fluidType = fluidType;
        this.potionType = potionType;
        this.minDuration = minDuration;
        this.minLevel = minLevel;
        this.isFlowweaveRingBinding = false;
        this.isFlowweaveRingUnbinding = false;
    }
    
    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        // For SingleRecipeInput, just check if the single item matches any input
        for (ItemStack required : inputs) {
            if (ItemStack.isSameItem(required, input.getItem(0))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if the given materials EXACTLY match this recipe's requirements.
     * Must have same items with same counts, no more, no less.
     */
    public boolean matchesMaterialsExactly(List<ItemStack> materials) {
        if (materials.isEmpty() && inputs.isEmpty()) {
            return true;
        }
        if (materials.isEmpty() || inputs.isEmpty()) {
            return false;
        }
        
        // Build a map of required items -> count
        Map<Item, Integer> required = new HashMap<>();
        for (ItemStack input : inputs) {
            required.merge(input.getItem(), input.getCount(), Integer::sum);
        }
        
        // Build a map of actual materials -> count
        Map<Item, Integer> actual = new HashMap<>();
        for (ItemStack stack : materials) {
            if (!stack.isEmpty()) {
                actual.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }
        
        // Must match exactly
        return required.equals(actual);
    }
    
    /**
     * Check if the given materials CONTAIN this recipe's required item types.
     * Does not require exact counts - just checks if the required item types are present.
     * Used for relaxed matching where any quantity is acceptable.
     */
    public boolean matchesMaterialsContains(List<ItemStack> materials) {
        if (inputs.isEmpty()) {
            return materials.isEmpty();
        }
        if (materials.isEmpty()) {
            return false;
        }
        
        // Build a set of required item types
        java.util.Set<Item> required = new java.util.HashSet<>();
        for (ItemStack input : inputs) {
            required.add(input.getItem());
        }
        
        // Build a set of actual material types
        java.util.Set<Item> actual = new java.util.HashSet<>();
        for (ItemStack stack : materials) {
            if (!stack.isEmpty()) {
                actual.add(stack.getItem());
            }
        }
        
        // Actual must contain all required types AND no extra types
        return actual.equals(required);
    }
    
    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return output.copy();
    }
    
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }
    
    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.copy();
    }
    
    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        for (ItemStack input : inputs) {
            list.add(Ingredient.of(input));
        }
        return list;
    }
    
    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }
    
    @Override
    public RecipeType<?> getType() {
        return com.cahcap.herbalcurative.common.registry.ModRegistries.CAULDRON_INFUSING_RECIPE_TYPE.get();
    }
    
    // Getters
    public List<ItemStack> getInputs() { return new ArrayList<>(inputs); }
    public Ingredient getInput() { 
        // Legacy getter - returns first input as Ingredient
        if (inputs.isEmpty()) return Ingredient.EMPTY;
        return Ingredient.of(inputs.get(0));
    }
    public ItemStack getOutput() { return output.copy(); }
    public String getFluidType() { return fluidType; }
    public String getPotionType() { return potionType; }
    public int getMinDuration() { return minDuration; }
    public int getMinLevel() { return minLevel; }
    public int getProcessingTime() { return INFUSING_TIME_TICKS; }
    public boolean isFlowweaveRingBinding() { return isFlowweaveRingBinding; }
    public boolean isFlowweaveRingUnbinding() { return isFlowweaveRingUnbinding; }
    
    // Fluid/Potion matching helpers
    public boolean requiresFluid() {
        return !fluidType.isEmpty();
    }
    
    public boolean requiresPotion() {
        return !potionType.isEmpty();
    }
    
    public Fluid getRequiredFluid() {
        if (fluidType.isEmpty()) return null;
        ResourceLocation id = ResourceLocation.tryParse(fluidType);
        if (id == null) return null;
        return BuiltInRegistries.FLUID.get(id);
    }
    
    public MobEffect getRequiredEffect() {
        if (potionType.isEmpty()) return null;
        ResourceLocation id = ResourceLocation.tryParse(potionType);
        if (id == null) return null;
        return BuiltInRegistries.MOB_EFFECT.get(id);
    }
    
    /**
     * Check if this recipe matches the given CauldronFluid
     */
    public boolean matchesFluid(CauldronFluid cauldronFluid) {
        if (cauldronFluid.isEmpty()) {
            return false;
        }
        
        // If recipe requires specific fluid
        if (requiresFluid()) {
            if (!cauldronFluid.isFluid()) return false;
            Fluid required = getRequiredFluid();
            return required != null && cauldronFluid.matchesFluid(required);
        }
        
        // If recipe requires potion (specific effect or any potion with duration/level requirement)
        if (requiresPotion() || minDuration > 0 || minLevel > 1) {
            if (!cauldronFluid.isPotion()) return false;
            MobEffect required = getRequiredEffect();  // null if potionType is empty (any effect)
            // minLevel is 1-based (level 1 = amplifier 0), so convert to amplifier
            int minAmplifier = minLevel > 0 ? minLevel - 1 : 0;
            return cauldronFluid.matchesPotion(required, minDuration, minAmplifier);
        }
        
        // No specific requirement - matches any fluid (water, lava, potion, etc.)
        return true;
    }
    
    // Recipe Type
    public static class Type implements RecipeType<CauldronInfusingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "cauldron_infusing");
    }
    
    // Recipe Serializer
    public static class Serializer implements RecipeSerializer<CauldronInfusingRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "cauldron_infusing");
        
        private static final MapCodec<CauldronInfusingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                ItemStack.CODEC.listOf().fieldOf("inputs").forGetter(CauldronInfusingRecipe::getInputs),
                ItemStack.CODEC.fieldOf("output").forGetter(CauldronInfusingRecipe::getOutput),
                Codec.STRING.optionalFieldOf("fluid", "").forGetter(CauldronInfusingRecipe::getFluidType),
                Codec.STRING.optionalFieldOf("potion", "").forGetter(CauldronInfusingRecipe::getPotionType),
                Codec.INT.optionalFieldOf("min_duration", 0).forGetter(CauldronInfusingRecipe::getMinDuration),
                Codec.INT.optionalFieldOf("min_level", 1).forGetter(CauldronInfusingRecipe::getMinLevel),
                Codec.BOOL.optionalFieldOf("flowweave_ring_binding", false).forGetter(CauldronInfusingRecipe::isFlowweaveRingBinding),
                Codec.BOOL.optionalFieldOf("flowweave_ring_unbinding", false).forGetter(CauldronInfusingRecipe::isFlowweaveRingUnbinding)
            ).apply(instance, CauldronInfusingRecipe::new)
        );
        
        private static final StreamCodec<RegistryFriendlyByteBuf, CauldronInfusingRecipe> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public CauldronInfusingRecipe decode(RegistryFriendlyByteBuf buf) {
                int inputCount = ByteBufCodecs.VAR_INT.decode(buf);
                List<ItemStack> inputs = new ArrayList<>();
                for (int i = 0; i < inputCount; i++) {
                    inputs.add(ItemStack.STREAM_CODEC.decode(buf));
                }
                ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
                String fluidType = ByteBufCodecs.STRING_UTF8.decode(buf);
                String potionType = ByteBufCodecs.STRING_UTF8.decode(buf);
                int minDuration = ByteBufCodecs.VAR_INT.decode(buf);
                int minLevel = ByteBufCodecs.VAR_INT.decode(buf);
                boolean isFlowweaveRingBinding = ByteBufCodecs.BOOL.decode(buf);
                boolean isFlowweaveRingUnbinding = ByteBufCodecs.BOOL.decode(buf);
                return new CauldronInfusingRecipe(inputs, output, fluidType, potionType, minDuration, minLevel, isFlowweaveRingBinding, isFlowweaveRingUnbinding);
            }
            
            @Override
            public void encode(RegistryFriendlyByteBuf buf, CauldronInfusingRecipe recipe) {
                List<ItemStack> inputs = recipe.getInputs();
                ByteBufCodecs.VAR_INT.encode(buf, inputs.size());
                for (ItemStack input : inputs) {
                    ItemStack.STREAM_CODEC.encode(buf, input);
                }
                ItemStack.STREAM_CODEC.encode(buf, recipe.getOutput());
                ByteBufCodecs.STRING_UTF8.encode(buf, recipe.getFluidType());
                ByteBufCodecs.STRING_UTF8.encode(buf, recipe.getPotionType());
                ByteBufCodecs.VAR_INT.encode(buf, recipe.getMinDuration());
                ByteBufCodecs.VAR_INT.encode(buf, recipe.getMinLevel());
                ByteBufCodecs.BOOL.encode(buf, recipe.isFlowweaveRingBinding());
                ByteBufCodecs.BOOL.encode(buf, recipe.isFlowweaveRingUnbinding());
            }
        };
        
        @Override
        public MapCodec<CauldronInfusingRecipe> codec() {
            return CODEC;
        }
        
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CauldronInfusingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
