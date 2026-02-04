package com.cahcap.herbalcurative.neoforge.common.datagen.recipes;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.common.recipe.WorkbenchRecipe;
import com.cahcap.herbalcurative.common.recipe.WorkbenchRecipe.MaterialRequirement;
import com.cahcap.herbalcurative.common.recipe.WorkbenchRecipe.ToolRequirement;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for Workbench recipes.
 * Used in data generation to create recipes for the Workbench.
 * 
 * Tool slots (looking down at the left block):
 * [0: top-left] [1: top-right]
 * [2: bot-left] [3: bot-right]
 * 
 * Material stack: LIFO order (first added = bottom, last added = top)
 */
public class WorkbenchRecipeBuilder {
    
    private final List<ToolRequirement> tools = new ArrayList<>();
    private Ingredient input = Ingredient.EMPTY;
    private final List<MaterialRequirement> materials = new ArrayList<>();
    private ItemStack result = ItemStack.EMPTY;
    
    private WorkbenchRecipeBuilder() {
    }
    
    public static WorkbenchRecipeBuilder builder() {
        return new WorkbenchRecipeBuilder();
    }
    
    /**
     * Add a tool requirement.
     * @param slot Tool slot (0-3)
     * @param item The tool item
     * @param damage Durability consumed per craft (default 1)
     */
    public WorkbenchRecipeBuilder tool(int slot, ItemLike item, int damage) {
        if (slot < 0 || slot > 3) {
            throw new IllegalArgumentException("Tool slot must be 0-3!");
        }
        tools.add(new ToolRequirement(slot, item.asItem(), damage));
        return this;
    }
    
    /**
     * Add a tool requirement with default 1 damage.
     * @param slot Tool slot (0-3)
     * @param item The tool item
     */
    public WorkbenchRecipeBuilder tool(int slot, ItemLike item) {
        return tool(slot, item, 1);
    }
    
    /**
     * Set the input item (center slot).
     * @param item The input item
     */
    public WorkbenchRecipeBuilder input(ItemLike item) {
        this.input = Ingredient.of(item);
        return this;
    }
    
    /**
     * Set the input ingredient (center slot).
     * @param ingredient The input ingredient
     */
    public WorkbenchRecipeBuilder input(Ingredient ingredient) {
        this.input = ingredient;
        return this;
    }
    
    /**
     * Add a material requirement (order matters, first = bottom of stack).
     * @param item The material item
     * @param count Amount required per craft
     */
    public WorkbenchRecipeBuilder material(ItemLike item, int count) {
        if (materials.size() >= 6) {
            throw new IllegalStateException("Cannot add more than 6 materials!");
        }
        materials.add(new MaterialRequirement(item.asItem(), count));
        return this;
    }
    
    /**
     * Add a material requirement with count 1.
     * @param item The material item
     */
    public WorkbenchRecipeBuilder material(ItemLike item) {
        return material(item, 1);
    }
    
    /**
     * Set the output item.
     * @param item The output item
     */
    public WorkbenchRecipeBuilder result(ItemLike item) {
        this.result = new ItemStack(item);
        return this;
    }
    
    /**
     * Set the output item with count.
     * @param item The output item
     * @param count The output count
     */
    public WorkbenchRecipeBuilder result(ItemLike item, int count) {
        this.result = new ItemStack(item, count);
        return this;
    }
    
    /**
     * Set the output item stack (for items with components like enchanted books).
     * @param stack The output item stack
     */
    public WorkbenchRecipeBuilder result(ItemStack stack) {
        this.result = stack.copy();
        return this;
    }
    
    /**
     * Build and save the recipe.
     * @param recipeOutput The recipe output
     * @param name The recipe name (without namespace)
     */
    public void build(RecipeOutput recipeOutput, String name) {
        build(recipeOutput, ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "workbench/" + name));
    }
    
    /**
     * Build and save the recipe with full resource location.
     * @param recipeOutput The recipe output
     * @param id The full recipe resource location
     */
    public void build(RecipeOutput recipeOutput, ResourceLocation id) {
        if (result.isEmpty()) {
            throw new IllegalStateException("Recipe " + id + " has no result!");
        }
        if (input.isEmpty()) {
            throw new IllegalStateException("Recipe " + id + " has no input!");
        }
        if (materials.isEmpty()) {
            throw new IllegalStateException("Recipe " + id + " has no materials!");
        }
        
        WorkbenchRecipe recipe = new WorkbenchRecipe(
                new ArrayList<>(tools),
                input,
                new ArrayList<>(materials),
                result.copy()
        );
        
        recipeOutput.accept(id, recipe, null);
    }
}
