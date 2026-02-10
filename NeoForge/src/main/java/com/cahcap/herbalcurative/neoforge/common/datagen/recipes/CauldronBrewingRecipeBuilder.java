package com.cahcap.herbalcurative.neoforge.common.datagen.recipes;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.common.recipe.CauldronBrewingRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for Cauldron brewing recipes.
 * Used in data generation to create recipes that brew potions from materials.
 * 
 * Brewing process:
 * 1. Fill cauldron with water
 * 2. Add materials
 * 3. Use Flowweave Ring to start brewing
 * 4. Add herbs to increase duration/amplifier
 * 5. Use Flowweave Ring to finish brewing
 * 6. Result: Potion with specified effect(s)
 * 
 * Supports multiple effects per recipe (e.g., Speed + Jump Boost for "Travel Potion")
 */
public class CauldronBrewingRecipeBuilder {
    
    private final List<Ingredient> materials = new ArrayList<>();
    private final List<String> effectIds = new ArrayList<>();
    private int baseColor = 0x3F76E4;
    private int defaultDuration = 120;  // Default: 2 minutes
    private int defaultAmplifier = 0;   // Default: level 1
    private int maxDuration = 480;      // Default: 8 minutes (for binding requirement)
    private int maxAmplifier = 1;       // Default: level 2 (for binding requirement)
    private int durationPerHerb = 30;   // Default: 30 seconds per overworld herb
    private int herbsPerLevel = 12;     // Default: 12 nether/end herbs per level
    
    private CauldronBrewingRecipeBuilder() {
    }
    
    public static CauldronBrewingRecipeBuilder builder() {
        return new CauldronBrewingRecipeBuilder();
    }
    
    /**
     * Add a material ingredient.
     * @param item The material item
     * @param count How many times to add this ingredient
     */
    public CauldronBrewingRecipeBuilder material(ItemLike item, int count) {
        Ingredient ingredient = Ingredient.of(item);
        for (int i = 0; i < count; i++) {
            materials.add(ingredient);
        }
        return this;
    }
    
    /**
     * Add a material ingredient with count 1.
     * @param item The material item
     */
    public CauldronBrewingRecipeBuilder material(ItemLike item) {
        return material(item, 1);
    }
    
    /**
     * Add a material ingredient.
     * @param ingredient The ingredient
     * @param count How many times to add this ingredient
     */
    public CauldronBrewingRecipeBuilder material(Ingredient ingredient, int count) {
        for (int i = 0; i < count; i++) {
            materials.add(ingredient);
        }
        return this;
    }
    
    /**
     * Add a potion effect ID.
     * Can be called multiple times to add multiple effects.
     * @param effectId The effect registry ID (e.g., "minecraft:instant_health")
     */
    public CauldronBrewingRecipeBuilder effect(String effectId) {
        this.effectIds.add(effectId);
        return this;
    }
    
    /**
     * Add multiple potion effect IDs at once.
     * @param effectIds The effect registry IDs
     */
    public CauldronBrewingRecipeBuilder effects(String... effectIds) {
        for (String effectId : effectIds) {
            this.effectIds.add(effectId);
        }
        return this;
    }
    
    /**
     * Set the base potion color.
     * @param color The color in RGB format (e.g., 0xF82423 for healing potion)
     */
    public CauldronBrewingRecipeBuilder color(int color) {
        this.baseColor = color;
        return this;
    }
    
    /**
     * Set the default duration when first brewed (in seconds).
     * @param defaultDuration Default duration in seconds
     */
    public CauldronBrewingRecipeBuilder defaultDuration(int defaultDuration) {
        this.defaultDuration = defaultDuration;
        return this;
    }
    
    /**
     * Set the default amplifier when first brewed.
     * 0 = level 1, 1 = level 2, etc.
     * @param defaultAmplifier Default amplifier (0-based)
     */
    public CauldronBrewingRecipeBuilder defaultAmplifier(int defaultAmplifier) {
        this.defaultAmplifier = defaultAmplifier;
        return this;
    }
    
    /**
     * Set the maximum duration for this potion (in seconds).
     * Used for Flowweave Ring binding requirement.
     * Set to 0 for instant effects.
     * @param maxDuration Maximum duration in seconds
     */
    public CauldronBrewingRecipeBuilder maxDuration(int maxDuration) {
        this.maxDuration = maxDuration;
        return this;
    }
    
    /**
     * Set the maximum amplifier for this potion.
     * Used for Flowweave Ring binding requirement.
     * 0 = level 1, 1 = level 2, etc.
     * @param maxAmplifier Maximum amplifier (0-based)
     */
    public CauldronBrewingRecipeBuilder maxAmplifier(int maxAmplifier) {
        this.maxAmplifier = maxAmplifier;
        return this;
    }
    
    /**
     * Set seconds added per overworld herb.
     * @param durationPerHerb Seconds per herb (default: 30)
     */
    public CauldronBrewingRecipeBuilder durationPerHerb(int durationPerHerb) {
        this.durationPerHerb = durationPerHerb;
        return this;
    }
    
    /**
     * Set number of nether/end herbs needed for +1 level.
     * @param herbsPerLevel Herbs per level (default: 12)
     */
    public CauldronBrewingRecipeBuilder herbsPerLevel(int herbsPerLevel) {
        this.herbsPerLevel = herbsPerLevel;
        return this;
    }
    
    /**
     * Build and save the recipe.
     * @param recipeOutput The recipe output
     * @param name The recipe name (without namespace)
     */
    public void build(RecipeOutput recipeOutput, String name) {
        build(recipeOutput, ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "cauldron_brewing/" + name));
    }
    
    /**
     * Build and save the recipe with full resource location.
     * @param recipeOutput The recipe output
     * @param id The full recipe resource location
     */
    public void build(RecipeOutput recipeOutput, ResourceLocation id) {
        if (materials.isEmpty()) {
            throw new IllegalStateException("Recipe " + id + " has no materials!");
        }
        if (effectIds.isEmpty()) {
            throw new IllegalStateException("Recipe " + id + " has no effects!");
        }
        
        CauldronBrewingRecipe recipe = new CauldronBrewingRecipe(
                new ArrayList<>(materials),
                new ArrayList<>(effectIds),
                baseColor,
                defaultDuration,
                defaultAmplifier,
                maxDuration,
                maxAmplifier,
                durationPerHerb,
                herbsPerLevel
        );
        
        recipeOutput.accept(id, recipe, null);
    }
}
