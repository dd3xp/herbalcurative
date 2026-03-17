package com.cahcap.neoforge.common.datagen.recipes;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.recipe.CauldronInfusingRecipe;
import com.cahcap.common.recipe.CauldronInfusingRecipe.IngredientWithCount;
import com.cahcap.common.registry.ModRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for Cauldron infusing recipes.
 * Used in data generation to create recipes that infuse items in fluid/potion.
 * 
 * Infusing process:
 * 1. Fill cauldron with fluid (water, lava, etc.) or brew a potion
 * 2. Add EXACT materials (must match recipe exactly - same items, same counts)
 * 3. Wait 5 seconds
 * 4. Result: Output item floats in the cauldron (fluid converts to water)
 * 
 * STRICT MATCHING: Materials must EXACTLY match the recipe.
 * Supports Tag matching for inputs.
 */
public class CauldronInfusingRecipeBuilder {
    
    private final List<IngredientWithCount> inputs = new ArrayList<>();
    private ItemStack output = ItemStack.EMPTY;
    private String fluidType = "";      // For fluid-based infusing (e.g., "minecraft:water")
    private String potionType = "";     // For potion-based infusing (e.g., "minecraft:instant_health")
    private int minDuration = 0;        // Minimum potion duration (in seconds)
    private int minLevel = 1;           // Minimum potion level (1 = level I)
    private boolean isFlowweaveRingBinding = false;  // Special: dynamic output for Flowweave Ring
    private boolean isFlowweaveRingUnbinding = false; // Special: clear ring binding in water
    private int fluidCost = 32;                        // Potion units consumed (0-32)
    
    private CauldronInfusingRecipeBuilder() {
    }
    
    public static CauldronInfusingRecipeBuilder builder() {
        return new CauldronInfusingRecipeBuilder();
    }
    
    /**
     * Add a required input item (count = 1).
     * @param item The input item
     */
    public CauldronInfusingRecipeBuilder input(ItemLike item) {
        this.inputs.add(new IngredientWithCount(Ingredient.of(item), 1));
        return this;
    }
    
    /**
     * Add a required input item with specific count.
     * @param item The input item
     * @param count The required count
     */
    public CauldronInfusingRecipeBuilder input(ItemLike item, int count) {
        this.inputs.add(new IngredientWithCount(Ingredient.of(item), count));
        return this;
    }
    
    /**
     * Add a required input using a Tag (count = 1).
     * @param tag The input tag
     */
    public CauldronInfusingRecipeBuilder input(TagKey<Item> tag) {
        this.inputs.add(new IngredientWithCount(Ingredient.of(tag), 1));
        return this;
    }
    
    /**
     * Add a required input using a Tag with specific count.
     * @param tag The input tag
     * @param count The required count
     */
    public CauldronInfusingRecipeBuilder input(TagKey<Item> tag, int count) {
        this.inputs.add(new IngredientWithCount(Ingredient.of(tag), count));
        return this;
    }
    
    /**
     * Add a required input using an Ingredient (count = 1).
     * @param ingredient The input ingredient
     */
    public CauldronInfusingRecipeBuilder input(Ingredient ingredient) {
        this.inputs.add(new IngredientWithCount(ingredient, 1));
        return this;
    }
    
    /**
     * Add a required input using an Ingredient with specific count.
     * @param ingredient The input ingredient
     * @param count The required count
     */
    public CauldronInfusingRecipeBuilder input(Ingredient ingredient, int count) {
        this.inputs.add(new IngredientWithCount(ingredient, count));
        return this;
    }
    
    /**
     * Set the output item.
     * @param item The output item
     */
    public CauldronInfusingRecipeBuilder output(ItemLike item) {
        this.output = new ItemStack(item);
        return this;
    }
    
    /**
     * Set the output item with count.
     * @param item The output item
     * @param count The output count
     */
    public CauldronInfusingRecipeBuilder output(ItemLike item, int count) {
        this.output = new ItemStack(item, count);
        return this;
    }
    
    /**
     * Set the output item stack.
     * @param stack The output item stack
     */
    public CauldronInfusingRecipeBuilder output(ItemStack stack) {
        this.output = stack.copy();
        return this;
    }
    
    /**
     * Require a specific fluid (not potion).
     * @param fluid The required fluid
     */
    public CauldronInfusingRecipeBuilder requireFluid(Fluid fluid) {
        this.fluidType = BuiltInRegistries.FLUID.getKey(fluid).toString();
        return this;
    }
    
    /**
     * Require a specific fluid by registry ID.
     * @param fluidId The fluid registry ID (e.g., "minecraft:water")
     */
    public CauldronInfusingRecipeBuilder requireFluid(String fluidId) {
        this.fluidType = fluidId;
        return this;
    }
    
    /**
     * Require a potion with specific effect.
     * @param effectId The effect registry ID (e.g., "minecraft:instant_health")
     */
    public CauldronInfusingRecipeBuilder requirePotion(String effectId) {
        this.potionType = effectId;
        return this;
    }
    
    /**
     * Require a potion with specific effect and minimum duration.
     * @param effectId The effect registry ID
     * @param minDuration Minimum duration in minutes
     */
    public CauldronInfusingRecipeBuilder requirePotion(String effectId, int minDuration) {
        this.potionType = effectId;
        this.minDuration = minDuration;
        return this;
    }
    
    /**
     * Require a potion with specific effect, minimum duration, and minimum level.
     * @param effectId The effect registry ID
     * @param minDuration Minimum duration in minutes
     * @param minLevel Minimum potion level (1 = level I, 2 = level II)
     */
    public CauldronInfusingRecipeBuilder requirePotion(String effectId, int minDuration, int minLevel) {
        this.potionType = effectId;
        this.minDuration = minDuration;
        this.minLevel = minLevel;
        return this;
    }
    
    /**
     * Set this recipe as a Flowweave Ring binding recipe.
     * The output will be dynamically generated based on the potion in the cauldron.
     * Input is automatically set to 1 Flowweave Ring.
     */
    public CauldronInfusingRecipeBuilder flowweaveRingBinding() {
        this.isFlowweaveRingBinding = true;
        return this;
    }
    
    /**
     * Set this recipe as a Flowweave Ring unbinding recipe.
     * Soaking a ring in water will clear its binding.
     * Input is automatically set to 1 Flowweave Ring.
     */
    /**
     * Set how many potion units this recipe consumes (default 32 = all).
     * Water-based recipes never consume fluid regardless of this value.
     */
    public CauldronInfusingRecipeBuilder fluidCost(int cost) {
        this.fluidCost = cost;
        return this;
    }

    public CauldronInfusingRecipeBuilder flowweaveRingUnbinding() {
        this.isFlowweaveRingUnbinding = true;
        return this;
    }
    
    /**
     * Build and save the recipe.
     * @param recipeOutput The recipe output
     * @param name The recipe name (without namespace)
     */
    public void build(RecipeOutput recipeOutput, String name) {
        build(recipeOutput, ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "cauldron_infusing/" + name));
    }
    
    /**
     * Build and save the recipe with full resource location.
     * @param recipeOutput The recipe output
     * @param id The full recipe resource location
     */
    public void build(RecipeOutput recipeOutput, ResourceLocation id) {
        List<IngredientWithCount> recipeInputs = inputs;
        ItemStack recipeOutput2 = output;
        
        // Special handling for Flowweave Ring binding/unbinding
        if (isFlowweaveRingBinding || isFlowweaveRingUnbinding) {
            // Input is 1 Flowweave Ring
            recipeInputs = new ArrayList<>();
            recipeInputs.add(new IngredientWithCount(Ingredient.of(ModRegistries.FLOWWEAVE_RING.get()), 1));
            // Output is a placeholder (actual output is dynamic for binding, or unbound ring for unbinding)
            recipeOutput2 = new ItemStack(ModRegistries.FLOWWEAVE_RING.get(), 1);
        } else {
            if (recipeInputs.isEmpty()) {
                throw new IllegalStateException("Recipe " + id + " has no inputs!");
            }
            if (recipeOutput2.isEmpty()) {
                throw new IllegalStateException("Recipe " + id + " has no output!");
            }
        }
        
        CauldronInfusingRecipe recipe = new CauldronInfusingRecipe(
                recipeInputs,
                recipeOutput2.copy(),
                fluidType,
                potionType,
                minDuration,
                minLevel,
                isFlowweaveRingBinding,
                isFlowweaveRingUnbinding,
                fluidCost
        );
        
        recipeOutput.accept(id, recipe, null);
    }
}
