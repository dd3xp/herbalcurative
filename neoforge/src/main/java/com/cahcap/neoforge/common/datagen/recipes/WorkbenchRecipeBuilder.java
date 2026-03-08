package com.cahcap.neoforge.common.datagen.recipes;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.recipe.WorkbenchRecipe;
import com.cahcap.common.recipe.WorkbenchRecipe.MaterialRequirement;
import com.cahcap.common.recipe.WorkbenchRecipe.ToolRequirement;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
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
 * Tools can be placed in any slot on the workbench.
 * Material stack: LIFO order (first added = bottom, last added = top)
 * Supports Tag matching for tools, input, and materials.
 */
public class WorkbenchRecipeBuilder {
    
    private final List<ToolRequirement> tools = new ArrayList<>();
    private Ingredient input = Ingredient.EMPTY;
    private final List<MaterialRequirement> materials = new ArrayList<>();
    private ItemStack result = ItemStack.EMPTY;
    private int experienceCost = 0;
    
    private WorkbenchRecipeBuilder() {
    }
    
    public static WorkbenchRecipeBuilder builder() {
        return new WorkbenchRecipeBuilder();
    }
    
    /**
     * Add a tool requirement using an Item.
     * @param item The tool item
     * @param damage Durability consumed per craft (default 1)
     */
    public WorkbenchRecipeBuilder tool(ItemLike item, int damage) {
        tools.add(new ToolRequirement(Ingredient.of(item), damage));
        return this;
    }
    
    /**
     * Add a tool requirement with default 1 damage.
     * @param item The tool item
     */
    public WorkbenchRecipeBuilder tool(ItemLike item) {
        return tool(item, 1);
    }
    
    /**
     * Add a tool requirement using a Tag.
     * @param tag The tool tag
     * @param damage Durability consumed per craft
     */
    public WorkbenchRecipeBuilder tool(TagKey<Item> tag, int damage) {
        tools.add(new ToolRequirement(Ingredient.of(tag), damage));
        return this;
    }
    
    /**
     * Add a tool requirement using a Tag with default 1 damage.
     * @param tag The tool tag
     */
    public WorkbenchRecipeBuilder tool(TagKey<Item> tag) {
        return tool(tag, 1);
    }
    
    /**
     * Add a tool requirement using an Ingredient.
     * @param ingredient The tool ingredient
     * @param damage Durability consumed per craft
     */
    public WorkbenchRecipeBuilder tool(Ingredient ingredient, int damage) {
        tools.add(new ToolRequirement(ingredient, damage));
        return this;
    }
    
    /**
     * Add a tool requirement using an Ingredient with default 1 damage.
     * @param ingredient The tool ingredient
     */
    public WorkbenchRecipeBuilder tool(Ingredient ingredient) {
        return tool(ingredient, 1);
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
     * Set the input using a Tag (center slot).
     * @param tag The input tag
     */
    public WorkbenchRecipeBuilder input(TagKey<Item> tag) {
        this.input = Ingredient.of(tag);
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
     * Add a material requirement using an Item (order matters, first = bottom of stack).
     * @param item The material item
     * @param count Amount required per craft
     */
    public WorkbenchRecipeBuilder material(ItemLike item, int count) {
        if (materials.size() >= 9) {
            throw new IllegalStateException("Cannot add more than 9 materials!");
        }
        materials.add(new MaterialRequirement(Ingredient.of(item), count));
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
     * Add a material requirement using a Tag.
     * @param tag The material tag
     * @param count Amount required per craft
     */
    public WorkbenchRecipeBuilder material(TagKey<Item> tag, int count) {
        if (materials.size() >= 9) {
            throw new IllegalStateException("Cannot add more than 9 materials!");
        }
        materials.add(new MaterialRequirement(Ingredient.of(tag), count));
        return this;
    }
    
    /**
     * Add a material requirement using a Tag with count 1.
     * @param tag The material tag
     */
    public WorkbenchRecipeBuilder material(TagKey<Item> tag) {
        return material(tag, 1);
    }
    
    /**
     * Add a material requirement using an Ingredient.
     * @param ingredient The material ingredient
     * @param count Amount required per craft
     */
    public WorkbenchRecipeBuilder material(Ingredient ingredient, int count) {
        if (materials.size() >= 9) {
            throw new IllegalStateException("Cannot add more than 9 materials!");
        }
        materials.add(new MaterialRequirement(ingredient, count));
        return this;
    }
    
    /**
     * Add a material requirement using an Ingredient with count 1.
     * @param ingredient The material ingredient
     */
    public WorkbenchRecipeBuilder material(Ingredient ingredient) {
        return material(ingredient, 1);
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
     * Set the experience cost for this recipe.
     * The player must have at least this many experience points to craft.
     * @param cost The experience point cost (not levels, but points)
     */
    public WorkbenchRecipeBuilder experience(int cost) {
        this.experienceCost = cost;
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
                result.copy(),
                experienceCost
        );
        
        recipeOutput.accept(id, recipe, null);
    }
}
