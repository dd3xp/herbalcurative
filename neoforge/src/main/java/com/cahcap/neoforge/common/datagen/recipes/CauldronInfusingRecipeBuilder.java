package com.cahcap.neoforge.common.datagen.recipes;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.recipe.CauldronInfusingRecipe;
import com.cahcap.common.registry.ModRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
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
 */
public class CauldronInfusingRecipeBuilder {
    
    private final List<ItemStack> inputs = new ArrayList<>();
    private ItemStack output = ItemStack.EMPTY;
    private String fluidType = "";      // For fluid-based infusing (e.g., "minecraft:water")
    private String potionType = "";     // For potion-based infusing (e.g., "minecraft:instant_health")
    private int minDuration = 0;        // Minimum potion duration (in seconds)
    private int minLevel = 1;           // Minimum potion level (1 = level I)
    private boolean isFlowweaveRingBinding = false;  // Special: dynamic output for Flowweave Ring
    private boolean isFlowweaveRingUnbinding = false; // Special: clear ring binding in water
    
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
        this.inputs.add(new ItemStack(item, 1));
        return this;
    }
    
    /**
     * Add a required input item with specific count.
     * @param item The input item
     * @param count The required count
     */
    public CauldronInfusingRecipeBuilder input(ItemLike item, int count) {
        this.inputs.add(new ItemStack(item, count));
        return this;
    }
    
    /**
     * Add a required input item stack.
     * @param stack The input item stack
     */
    public CauldronInfusingRecipeBuilder input(ItemStack stack) {
        this.inputs.add(stack.copy());
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
        // Input is 1 Flowweave Ring (will be set in build)
        return this;
    }
    
    /**
     * Set this recipe as a Flowweave Ring unbinding recipe.
     * Soaking a ring in water will clear its binding.
     * Input is automatically set to 1 Flowweave Ring.
     */
    public CauldronInfusingRecipeBuilder flowweaveRingUnbinding() {
        this.isFlowweaveRingUnbinding = true;
        // Input is 1 Flowweave Ring (will be set in build)
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
        List<ItemStack> recipeInputs = inputs;
        ItemStack recipeOutput2 = output;
        
        // Special handling for Flowweave Ring binding/unbinding
        if (isFlowweaveRingBinding || isFlowweaveRingUnbinding) {
            // Input is 1 Flowweave Ring
            recipeInputs = new ArrayList<>();
            recipeInputs.add(new ItemStack(ModRegistries.FLOWWEAVE_RING.get(), 1));
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
                isFlowweaveRingUnbinding
        );
        
        recipeOutput.accept(id, recipe, null);
    }
}
