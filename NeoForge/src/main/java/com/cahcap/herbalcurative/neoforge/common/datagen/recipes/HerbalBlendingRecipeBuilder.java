package com.cahcap.herbalcurative.neoforge.common.datagen.recipes;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.common.recipe.HerbalBlendingRecipe;
import com.cahcap.herbalcurative.common.recipe.HerbalBlendingRecipe.IngredientWithCount;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for Herbal Blending recipes.
 * Used in data generation to create recipes for the Herbal Blending Rack multiblock.
 * 
 * Shelf positions (like crafting table 3x3):
 * [0][1][2]
 * [3][4][5]  <- 4 is center, output replaces this position
 * [6][7][8]
 */
public class HerbalBlendingRecipeBuilder {
    
    private final List<IngredientWithCount> basketInputs = new ArrayList<>();
    private final Map<Character, Ingredient> ingredientMap = new HashMap<>();
    private String[] pattern = new String[3];
    private ItemStack output = ItemStack.EMPTY;
    
    private HerbalBlendingRecipeBuilder() {
        // Initialize pattern with empty spaces
        pattern[0] = "   ";
        pattern[1] = "   ";
        pattern[2] = "   ";
    }
    
    public static HerbalBlendingRecipeBuilder builder() {
        return new HerbalBlendingRecipeBuilder();
    }
    
    /**
     * Add a herb input (goes into a basket).
     * @param item The herb item
     * @param count Amount required
     */
    public HerbalBlendingRecipeBuilder basketInput(ItemLike item, int count) {
        basketInputs.add(IngredientWithCount.of(Ingredient.of(item), count));
        return this;
    }
    
    /**
     * Add a herb input using a tag.
     * @param tag The herb item tag
     * @param count Amount required
     */
    public HerbalBlendingRecipeBuilder basketInput(TagKey<Item> tag, int count) {
        basketInputs.add(IngredientWithCount.of(Ingredient.of(tag), count));
        return this;
    }
    
    /**
     * Add a herb input using an ingredient.
     * @param ingredient The ingredient
     * @param count Amount required
     */
    public HerbalBlendingRecipeBuilder basketInput(Ingredient ingredient, int count) {
        basketInputs.add(IngredientWithCount.of(ingredient, count));
        return this;
    }
    
    /**
     * Define the shelf pattern using 3 strings (like shaped crafting).
     * Each string is 3 characters, representing one row.
     * Space ' ' means empty slot.
     * 
     * Example:
     * pattern("   ", " S ", "   ") - only center has item 'S'
     * 
     * @param row0 Top row
     * @param row1 Middle row (center is position 1)
     * @param row2 Bottom row
     */
    public HerbalBlendingRecipeBuilder pattern(String row0, String row1, String row2) {
        if (row0.length() != 3 || row1.length() != 3 || row2.length() != 3) {
            throw new IllegalArgumentException("Each pattern row must be exactly 3 characters!");
        }
        this.pattern[0] = row0;
        this.pattern[1] = row1;
        this.pattern[2] = row2;
        return this;
    }
    
    /**
     * Define what ingredient a character represents.
     * @param key The character used in pattern
     * @param item The item
     */
    public HerbalBlendingRecipeBuilder define(char key, ItemLike item) {
        ingredientMap.put(key, Ingredient.of(item));
        return this;
    }
    
    /**
     * Define what ingredient a character represents using a tag.
     * @param key The character used in pattern
     * @param tag The item tag
     */
    public HerbalBlendingRecipeBuilder define(char key, TagKey<Item> tag) {
        ingredientMap.put(key, Ingredient.of(tag));
        return this;
    }
    
    /**
     * Define what ingredient a character represents.
     * @param key The character used in pattern
     * @param ingredient The ingredient
     */
    public HerbalBlendingRecipeBuilder define(char key, Ingredient ingredient) {
        ingredientMap.put(key, ingredient);
        return this;
    }
    
    /**
     * Set the output item.
     * @param item The output item
     */
    public HerbalBlendingRecipeBuilder output(ItemLike item) {
        this.output = new ItemStack(item);
        return this;
    }
    
    /**
     * Set the output item with count.
     * @param item The output item
     * @param count The output count
     */
    public HerbalBlendingRecipeBuilder output(ItemLike item, int count) {
        this.output = new ItemStack(item, count);
        return this;
    }
    
    /**
     * Set the output item stack.
     * @param stack The output item stack
     */
    public HerbalBlendingRecipeBuilder output(ItemStack stack) {
        this.output = stack.copy();
        return this;
    }
    
    /**
     * Build and save the recipe.
     * @param recipeOutput The recipe output
     * @param name The recipe name (without namespace)
     */
    public void build(RecipeOutput recipeOutput, String name) {
        build(recipeOutput, ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "herbal_blending/" + name));
    }
    
    /**
     * Build and save the recipe with full resource location.
     * @param recipeOutput The recipe output
     * @param id The full recipe resource location
     */
    public void build(RecipeOutput recipeOutput, ResourceLocation id) {
        if (output.isEmpty()) {
            throw new IllegalStateException("Recipe " + id + " has no output!");
        }
        if (basketInputs.size() > 6) {
            throw new IllegalStateException("Recipe " + id + " has more than 6 basket inputs (max is 6 baskets)!");
        }
        
        // Build shelf pattern from pattern strings
        NonNullList<Ingredient> shelfPattern = NonNullList.withSize(9, Ingredient.EMPTY);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                char c = pattern[row].charAt(col);
                int index = row * 3 + col;
                if (c != ' ') {
                    Ingredient ingredient = ingredientMap.get(c);
                    if (ingredient == null) {
                        throw new IllegalStateException("Recipe " + id + " uses undefined key '" + c + "' in pattern!");
                    }
                    shelfPattern.set(index, ingredient);
                }
            }
        }
        
        HerbalBlendingRecipe recipe = new HerbalBlendingRecipe(
                new ArrayList<>(basketInputs),
                shelfPattern,
                output.copy()
        );
        
        recipeOutput.accept(id, recipe, null);
    }
}
