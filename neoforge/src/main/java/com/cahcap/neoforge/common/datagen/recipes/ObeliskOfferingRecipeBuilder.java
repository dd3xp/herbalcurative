package com.cahcap.neoforge.common.datagen.recipes;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.recipe.ObeliskOfferingRecipe;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/**
 * Builder for Obelisk Offering recipes.
 */
public class ObeliskOfferingRecipeBuilder {

    private Ingredient ingredient = Ingredient.EMPTY;
    private ResourceLocation entityType;
    private int waitTicks = 100;
    private int spawnDistance = 1;

    private ObeliskOfferingRecipeBuilder(ResourceLocation entityType) {
        this.entityType = entityType;
    }

    public static ObeliskOfferingRecipeBuilder create(ResourceLocation entityType) {
        return new ObeliskOfferingRecipeBuilder(entityType);
    }

    public static ObeliskOfferingRecipeBuilder create(String entityType) {
        return new ObeliskOfferingRecipeBuilder(ResourceLocation.parse(entityType));
    }

    public ObeliskOfferingRecipeBuilder ingredient(ItemLike item) {
        this.ingredient = Ingredient.of(item);
        return this;
    }

    public ObeliskOfferingRecipeBuilder waitTicks(int ticks) {
        this.waitTicks = ticks;
        return this;
    }

    public ObeliskOfferingRecipeBuilder spawnDistance(int distance) {
        this.spawnDistance = distance;
        return this;
    }

    public void save(RecipeOutput output, String name) {
        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(
                HerbalCurativeCommon.MOD_ID, "obelisk_offering/" + name);

        ObeliskOfferingRecipe recipe = new ObeliskOfferingRecipe(
                ingredient, entityType, waitTicks, spawnDistance);

        output.accept(recipeId, recipe, (AdvancementHolder) null);
    }
}
