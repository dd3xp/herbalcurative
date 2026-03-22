package com.cahcap.neoforge.common.datagen.recipes.builder;

import com.cahcap.common.recipe.HerbPotGrowingRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builder for Herb Pot Growing recipes.
 */
public class HerbPotGrowingRecipeBuilder {
    
    private Ingredient seedling = Ingredient.EMPTY;
    private Ingredient soil = Ingredient.EMPTY;
    private final List<Map.Entry<Item, Integer>> herbs = new ArrayList<>();
    private ItemStack result = ItemStack.EMPTY;
    private int growthTime = 6000;
    
    private HerbPotGrowingRecipeBuilder() {}
    
    public static HerbPotGrowingRecipeBuilder builder() {
        return new HerbPotGrowingRecipeBuilder();
    }
    
    public HerbPotGrowingRecipeBuilder seedling(ItemLike item) {
        this.seedling = Ingredient.of(item);
        return this;
    }
    
    public HerbPotGrowingRecipeBuilder seedling(Ingredient ingredient) {
        this.seedling = ingredient;
        return this;
    }
    
    public HerbPotGrowingRecipeBuilder soil(ItemLike item) {
        this.soil = Ingredient.of(item);
        return this;
    }
    
    public HerbPotGrowingRecipeBuilder soil(Ingredient ingredient) {
        this.soil = ingredient;
        return this;
    }
    
    public HerbPotGrowingRecipeBuilder herb(ItemLike herb, int count) {
        this.herbs.add(Map.entry(herb.asItem(), count));
        return this;
    }
    
    public HerbPotGrowingRecipeBuilder result(ItemLike item) {
        this.result = new ItemStack(item);
        return this;
    }
    
    public HerbPotGrowingRecipeBuilder result(ItemLike item, int count) {
        this.result = new ItemStack(item, count);
        return this;
    }
    
    public HerbPotGrowingRecipeBuilder result(ItemStack stack) {
        this.result = stack;
        return this;
    }
    
    public HerbPotGrowingRecipeBuilder growthTime(int ticks) {
        this.growthTime = ticks;
        return this;
    }
    
    public HerbPotGrowingRecipeBuilder growthTimeSeconds(int seconds) {
        this.growthTime = seconds * 20;
        return this;
    }
    
    public HerbPotGrowingRecipeBuilder growthTimeMinutes(int minutes) {
        this.growthTime = minutes * 20 * 60;
        return this;
    }
    
    public void build(RecipeOutput output, String name) {
        if (seedling.isEmpty()) {
            throw new IllegalStateException("Seedling is required for herb pot growing recipe: " + name);
        }
        if (result.isEmpty()) {
            throw new IllegalStateException("Result is required for herb pot growing recipe: " + name);
        }
        if (herbs.isEmpty()) {
            throw new IllegalStateException("At least one herb is required for herb pot growing recipe: " + name);
        }
        
        HerbPotGrowingRecipe recipe = new HerbPotGrowingRecipe(
                seedling,
                soil,
                herbs,
                result,
                growthTime
        );
        
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("herbalcurative", "herb_pot_growing/" + name);
        output.accept(id, recipe, null);
    }
}
