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
 * 6. Result: Potion with specified effect
 */
public class CauldronBrewingRecipeBuilder {
    
    private final List<Ingredient> materials = new ArrayList<>();
    private String effectId = "";
    private int baseColor = 0x3F76E4;
    private int maxDuration = 480;    // Default: 8 minutes (for binding requirement)
    private int maxAmplifier = 3;     // Default: level 4 (for binding requirement)
    
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
     * Set the potion effect ID.
     * @param effectId The effect registry ID (e.g., "minecraft:instant_health")
     */
    public CauldronBrewingRecipeBuilder effect(String effectId) {
        this.effectId = effectId;
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
        if (effectId.isEmpty()) {
            throw new IllegalStateException("Recipe " + id + " has no effect!");
        }
        
        CauldronBrewingRecipe recipe = new CauldronBrewingRecipe(
                new ArrayList<>(materials),
                effectId,
                baseColor,
                maxDuration,
                maxAmplifier
        );
        
        recipeOutput.accept(id, recipe, null);
    }
}
