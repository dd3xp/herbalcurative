package com.cahcap.neoforge.common.datagen.recipes.provider;

import com.cahcap.neoforge.common.datagen.recipes.builder.*;

import com.cahcap.neoforge.common.registry.ModBlocks;
import com.cahcap.neoforge.common.registry.ModItems;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;

/**
 * Herb Pot Growing recipes.
 */
public class HerbPotRecipes {

    public void build(RecipeOutput output) {
        // ==================== Herb Plant Cultivation ====================
        HerbPotGrowingRecipeBuilder.builder()
                .seedling(ModBlocks.VERDSCALE_FERN.get()).herb(ModItems.SCALEPLATE.get(), 1)
                .result(ModBlocks.VERDSCALE_FERN.get()).growthTimeSeconds(10).build(output, "verdscale_fern");

        HerbPotGrowingRecipeBuilder.builder()
                .seedling(ModBlocks.DEWPETAL.get()).herb(ModItems.DEWPETAL_SHARD.get(), 1)
                .result(ModBlocks.DEWPETAL.get()).growthTimeSeconds(10).build(output, "dewpetal");

        HerbPotGrowingRecipeBuilder.builder()
                .seedling(ModBlocks.ZEPHYR_LILY.get()).herb(ModItems.GOLDEN_LILYBELL.get(), 1)
                .result(ModBlocks.ZEPHYR_LILY.get()).growthTimeSeconds(10).build(output, "zephyr_lily");

        HerbPotGrowingRecipeBuilder.builder()
                .seedling(ModBlocks.CRYSTBUD.get()).herb(ModItems.CRYST_SPINE.get(), 1)
                .result(ModBlocks.CRYSTBUD.get()).growthTimeSeconds(10).build(output, "crystbud");

        HerbPotGrowingRecipeBuilder.builder()
                .seedling(ModBlocks.PYRISAGE.get()).herb(ModItems.BURNT_NODE.get(), 1)
                .result(ModBlocks.PYRISAGE.get()).growthTimeSeconds(10).build(output, "pyrisage");

        HerbPotGrowingRecipeBuilder.builder()
                .seedling(ModBlocks.ROSYNIA.get()).herb(ModItems.HEART_OF_STARDREAM.get(), 1)
                .result(ModBlocks.ROSYNIA.get()).growthTimeSeconds(10).build(output, "rosynia");

        // ==================== Crystal Plant Cultivation ====================
        HerbPotGrowingRecipeBuilder.builder()
                .seedling(ModItems.IRON_CRYST_PLANT.get())
                .herb(ModItems.SCALEPLATE.get(), 4).herb(ModItems.GOLDEN_LILYBELL.get(), 4)
                .result(Items.IRON_INGOT).growthTimeMinutes(2).build(output, "iron_cryst_plant");
    }
}
