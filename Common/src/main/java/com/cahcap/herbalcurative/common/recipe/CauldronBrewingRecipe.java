package com.cahcap.herbalcurative.common.recipe;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Recipe for brewing potions in the Cauldron.
 * 
 * Materials (items added to water) determine what potion type is produced.
 * Herbs added during brewing determine duration and amplifier.
 * 
 * Supports multiple effects per recipe (e.g., Speed + Jump Boost for "Travel Potion")
 * 
 * Example: Glistering Melon Slice -> Healing Potion
 */
public class CauldronBrewingRecipe implements Recipe<RecipeInput> {
    
    private final List<Ingredient> materials;
    private final List<String> effectIds;  // List of MobEffect registry names (e.g., ["minecraft:speed", "minecraft:jump_boost"])
    private final int baseColor;           // Potion color
    private final int defaultDuration;     // Default duration in seconds when first brewed
    private final int defaultAmplifier;    // Default amplifier when first brewed (0 = level 1)
    private final int maxDuration;         // Maximum duration in seconds (0 for instant effects)
    private final int maxAmplifier;        // Maximum amplifier (0 = level 1, 1 = level 2, etc.)
    private final int durationPerHerb;     // Seconds added per overworld herb (default: 30)
    private final int herbsPerLevel;       // Number of nether/end herbs needed for +1 level (default: 12)
    
    public CauldronBrewingRecipe(List<Ingredient> materials, List<String> effectIds, int baseColor, 
                                  int defaultDuration, int defaultAmplifier, int maxDuration, int maxAmplifier,
                                  int durationPerHerb, int herbsPerLevel) {
        this.materials = materials;
        this.effectIds = effectIds;
        this.baseColor = baseColor;
        this.defaultDuration = defaultDuration;
        this.defaultAmplifier = defaultAmplifier;
        this.maxDuration = maxDuration;
        this.maxAmplifier = maxAmplifier;
        this.durationPerHerb = durationPerHerb;
        this.herbsPerLevel = herbsPerLevel;
    }
    
    // Compatibility constructor without herb parameters (uses defaults)
    public CauldronBrewingRecipe(List<Ingredient> materials, List<String> effectIds, int baseColor, 
                                  int defaultDuration, int defaultAmplifier, int maxDuration, int maxAmplifier) {
        this(materials, effectIds, baseColor, defaultDuration, defaultAmplifier, maxDuration, maxAmplifier, 30, 12);
    }
    
    // Compatibility constructor for single effect (backwards compatible)
    public CauldronBrewingRecipe(List<Ingredient> materials, String effectId, int baseColor, 
                                  int defaultDuration, int defaultAmplifier, int maxDuration, int maxAmplifier) {
        this(materials, List.of(effectId), baseColor, defaultDuration, defaultAmplifier, maxDuration, maxAmplifier, 30, 12);
    }
    
    // Simple constructor with defaults
    public CauldronBrewingRecipe(List<Ingredient> materials, List<String> effectIds, int baseColor) {
        this(materials, effectIds, baseColor, 120, 0, 480, 1, 30, 12);  // Default: 2 min start, 8 min max, level 2 max
    }
    
    @Override
    public boolean matches(RecipeInput input, Level level) {
        // Check if the input contains all required materials
        // This is a simple check - all materials must be present
        if (input.size() < materials.size()) {
            return false;
        }
        
        // Create a copy of materials to track what we've matched
        List<Ingredient> remaining = new ArrayList<>(materials);
        
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            
            // Try to match this stack with a remaining ingredient
            boolean matched = false;
            for (int j = 0; j < remaining.size(); j++) {
                if (remaining.get(j).test(stack)) {
                    remaining.remove(j);
                    matched = true;
                    break;
                }
            }
            
            // If we found an item that doesn't match any ingredient, recipe fails
            if (!matched) {
                return false;
            }
        }
        
        // All required ingredients must be matched
        return remaining.isEmpty();
    }
    
    /**
     * Check if the given list of ItemStacks matches this recipe.
     * Takes into account item counts - e.g., 4 Nether Wart can be 
     * one stack of 4 or four stacks of 1.
     */
    public boolean matchesMaterials(List<ItemStack> inputMaterials) {
        // Count total items in input
        int totalInputCount = 0;
        for (ItemStack stack : inputMaterials) {
            if (!stack.isEmpty()) {
                totalInputCount += stack.getCount();
            }
        }
        
        // Recipe requires exactly materials.size() items total
        if (totalInputCount != materials.size()) {
            return false;
        }
        
        // Track how many of each ingredient we still need
        // Use a list to track remaining needed count for each unique ingredient pattern
        List<IngredientCount> remaining = new ArrayList<>();
        for (Ingredient ingredient : materials) {
            // Try to find existing entry for same ingredient
            boolean found = false;
            for (IngredientCount ic : remaining) {
                if (ingredientsEqual(ic.ingredient, ingredient)) {
                    ic.count++;
                    found = true;
                    break;
                }
            }
            if (!found) {
                remaining.add(new IngredientCount(ingredient, 1));
            }
        }
        
        // Match input stacks against remaining ingredients
        for (ItemStack stack : inputMaterials) {
            if (stack.isEmpty()) continue;
            
            int stackCount = stack.getCount();
            boolean matched = false;
            
            for (IngredientCount ic : remaining) {
                if (ic.count > 0 && ic.ingredient.test(stack)) {
                    int toConsume = Math.min(stackCount, ic.count);
                    ic.count -= toConsume;
                    stackCount -= toConsume;
                    matched = true;
                    if (stackCount <= 0) break;
                }
            }
            
            // If we couldn't match all items in this stack, fail
            if (stackCount > 0) {
                return false;
            }
        }
        
        // Check all ingredients are fully matched
        for (IngredientCount ic : remaining) {
            if (ic.count > 0) {
                return false;
            }
        }
        
        return true;
    }
    
    private static class IngredientCount {
        Ingredient ingredient;
        int count;
        
        IngredientCount(Ingredient ingredient, int count) {
            this.ingredient = ingredient;
            this.count = count;
        }
    }
    
    private boolean ingredientsEqual(Ingredient a, Ingredient b) {
        // Simple comparison - check if they accept the same items
        ItemStack[] itemsA = a.getItems();
        ItemStack[] itemsB = b.getItems();
        if (itemsA.length != itemsB.length) return false;
        for (int i = 0; i < itemsA.length; i++) {
            if (!ItemStack.isSameItem(itemsA[i], itemsB[i])) return false;
        }
        return true;
    }
    
    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY; // Brewing doesn't produce an item directly
    }
    
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }
    
    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.addAll(materials);
        return list;
    }
    
    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }
    
    @Override
    public RecipeType<?> getType() {
        return com.cahcap.herbalcurative.common.registry.ModRegistries.CAULDRON_BREWING_RECIPE_TYPE.get();
    }
    
    // Getters
    public List<Ingredient> getMaterials() { return materials; }
    public List<String> getEffectIds() { return effectIds; }
    public int getBaseColor() { return baseColor; }
    public int getDefaultDuration() { return defaultDuration; }
    public int getDefaultAmplifier() { return defaultAmplifier; }
    public int getMaxDuration() { return maxDuration; }
    public int getMaxAmplifier() { return maxAmplifier; }
    public int getDurationPerHerb() { return durationPerHerb; }
    public int getHerbsPerLevel() { return herbsPerLevel; }
    
    /**
     * Get the first effect ID (for backwards compatibility)
     */
    public String getEffectId() { 
        return effectIds.isEmpty() ? "" : effectIds.get(0); 
    }
    
    /**
     * Get the first MobEffect from the effect IDs (for backwards compatibility)
     */
    public MobEffect getEffect() {
        if (effectIds.isEmpty()) return null;
        ResourceLocation id = ResourceLocation.tryParse(effectIds.get(0));
        if (id == null) return null;
        return BuiltInRegistries.MOB_EFFECT.get(id);
    }
    
    /**
     * Get all MobEffects from the effect IDs
     */
    public List<MobEffect> getEffects() {
        List<MobEffect> effects = new ArrayList<>();
        for (String effectId : effectIds) {
            ResourceLocation id = ResourceLocation.tryParse(effectId);
            if (id != null) {
                MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(id);
                if (effect != null) {
                    effects.add(effect);
                }
            }
        }
        return effects;
    }
    
    /**
     * Check if this is an instant effect potion (any effect is instant)
     */
    public boolean isInstantEffect() {
        for (MobEffect effect : getEffects()) {
            if (effect.isInstantenous()) {
                return true;
            }
        }
        return false;
    }
    
    // Recipe Type
    public static class Type implements RecipeType<CauldronBrewingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "cauldron_brewing");
    }
    
    // Recipe Serializer
    public static class Serializer implements RecipeSerializer<CauldronBrewingRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "cauldron_brewing");
        
        private static final MapCodec<CauldronBrewingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("materials").forGetter(CauldronBrewingRecipe::getMaterials),
                Codec.STRING.listOf().fieldOf("effects").forGetter(CauldronBrewingRecipe::getEffectIds),
                Codec.INT.optionalFieldOf("color", 0x3F76E4).forGetter(CauldronBrewingRecipe::getBaseColor),
                Codec.INT.optionalFieldOf("default_duration", 120).forGetter(CauldronBrewingRecipe::getDefaultDuration),
                Codec.INT.optionalFieldOf("default_amplifier", 0).forGetter(CauldronBrewingRecipe::getDefaultAmplifier),
                Codec.INT.optionalFieldOf("max_duration", 480).forGetter(CauldronBrewingRecipe::getMaxDuration),
                Codec.INT.optionalFieldOf("max_amplifier", 1).forGetter(CauldronBrewingRecipe::getMaxAmplifier),
                Codec.INT.optionalFieldOf("duration_per_herb", 30).forGetter(CauldronBrewingRecipe::getDurationPerHerb),
                Codec.INT.optionalFieldOf("herbs_per_level", 12).forGetter(CauldronBrewingRecipe::getHerbsPerLevel)
            ).apply(instance, CauldronBrewingRecipe::new)
        );
        
        // Custom StreamCodec since composite only supports up to 6 fields
        private static final StreamCodec<RegistryFriendlyByteBuf, CauldronBrewingRecipe> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public CauldronBrewingRecipe decode(RegistryFriendlyByteBuf buf) {
                List<Ingredient> materials = Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
                List<String> effectIds = ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).decode(buf);
                int color = ByteBufCodecs.VAR_INT.decode(buf);
                int defaultDuration = ByteBufCodecs.VAR_INT.decode(buf);
                int defaultAmplifier = ByteBufCodecs.VAR_INT.decode(buf);
                int maxDuration = ByteBufCodecs.VAR_INT.decode(buf);
                int maxAmplifier = ByteBufCodecs.VAR_INT.decode(buf);
                int durationPerHerb = ByteBufCodecs.VAR_INT.decode(buf);
                int herbsPerLevel = ByteBufCodecs.VAR_INT.decode(buf);
                return new CauldronBrewingRecipe(materials, effectIds, color, defaultDuration, defaultAmplifier, maxDuration, maxAmplifier, durationPerHerb, herbsPerLevel);
            }
            
            @Override
            public void encode(RegistryFriendlyByteBuf buf, CauldronBrewingRecipe recipe) {
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.getMaterials());
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).encode(buf, recipe.getEffectIds());
                ByteBufCodecs.VAR_INT.encode(buf, recipe.getBaseColor());
                ByteBufCodecs.VAR_INT.encode(buf, recipe.getDefaultDuration());
                ByteBufCodecs.VAR_INT.encode(buf, recipe.getDefaultAmplifier());
                ByteBufCodecs.VAR_INT.encode(buf, recipe.getMaxDuration());
                ByteBufCodecs.VAR_INT.encode(buf, recipe.getMaxAmplifier());
                ByteBufCodecs.VAR_INT.encode(buf, recipe.getDurationPerHerb());
                ByteBufCodecs.VAR_INT.encode(buf, recipe.getHerbsPerLevel());
            }
        };
        
        @Override
        public MapCodec<CauldronBrewingRecipe> codec() {
            return CODEC;
        }
        
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CauldronBrewingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
