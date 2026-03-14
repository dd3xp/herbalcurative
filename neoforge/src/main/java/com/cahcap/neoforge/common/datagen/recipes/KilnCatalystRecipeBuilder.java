package com.cahcap.neoforge.common.datagen.recipes;

import com.cahcap.common.recipe.KilnCatalystRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/**
 * Builder for Kiln Catalyst recipes.
 */
public class KilnCatalystRecipeBuilder {

    private Ingredient ingredient = Ingredient.EMPTY;
    private Ingredient affectedInputs = Ingredient.EMPTY;
    private int outputMultiplier = 2;
    private int speedMultiplier = 4;
    private int usesPerItem = 8;

    private KilnCatalystRecipeBuilder() {}

    public static KilnCatalystRecipeBuilder builder() {
        return new KilnCatalystRecipeBuilder();
    }

    public KilnCatalystRecipeBuilder ingredient(ItemLike item) {
        this.ingredient = Ingredient.of(item);
        return this;
    }

    public KilnCatalystRecipeBuilder ingredient(Ingredient ingredient) {
        this.ingredient = ingredient;
        return this;
    }

    public KilnCatalystRecipeBuilder affectedInputs(TagKey<Item> tag) {
        this.affectedInputs = Ingredient.of(tag);
        return this;
    }

    public KilnCatalystRecipeBuilder affectedInputs(Ingredient ingredient) {
        this.affectedInputs = ingredient;
        return this;
    }

    public KilnCatalystRecipeBuilder outputMultiplier(int multiplier) {
        this.outputMultiplier = multiplier;
        return this;
    }

    public KilnCatalystRecipeBuilder speedMultiplier(int multiplier) {
        this.speedMultiplier = multiplier;
        return this;
    }

    public KilnCatalystRecipeBuilder usesPerItem(int uses) {
        this.usesPerItem = uses;
        return this;
    }

    public void build(RecipeOutput output, String name) {
        if (ingredient.isEmpty()) {
            throw new IllegalStateException("Ingredient is required for kiln catalyst recipe: " + name);
        }

        KilnCatalystRecipe recipe = new KilnCatalystRecipe(ingredient, affectedInputs, outputMultiplier, speedMultiplier, usesPerItem);
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("herbalcurative", "kiln_catalyst/" + name);
        output.accept(id, recipe, null);
    }
}
