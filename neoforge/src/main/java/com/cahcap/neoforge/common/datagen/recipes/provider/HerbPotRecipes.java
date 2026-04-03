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
                .seedling(ModBlocks.SCLERIS.get()).herb(ModItems.SCALEPLATE.get(), 1)
                .result(ModBlocks.SCLERIS.get()).growthTimeSeconds(10).build(output, "scleris");

        HerbPotGrowingRecipeBuilder.builder()
                .seedling(ModBlocks.DORELLA.get()).herb(ModItems.DEWPETAL.get(), 1)
                .result(ModBlocks.DORELLA.get()).growthTimeSeconds(10).build(output, "dorella");

        HerbPotGrowingRecipeBuilder.builder()
                .seedling(ModBlocks.SEPHREL.get()).herb(ModItems.ZEPHYR_BLOSSOM.get(), 1)
                .result(ModBlocks.SEPHREL.get()).growthTimeSeconds(10).build(output, "sephrel");

        HerbPotGrowingRecipeBuilder.builder()
                .seedling(ModBlocks.CRYSEL.get()).herb(ModItems.CRYST_SPINE.get(), 1)
                .result(ModBlocks.CRYSEL.get()).growthTimeSeconds(10).build(output, "crysel");

        HerbPotGrowingRecipeBuilder.builder()
                .seedling(ModBlocks.PYRAZE.get()).herb(ModItems.PYRO_NODE.get(), 1)
                .result(ModBlocks.PYRAZE.get()).growthTimeSeconds(10).build(output, "pyraze");

        HerbPotGrowingRecipeBuilder.builder()
                .seedling(ModBlocks.STELLIA.get()).herb(ModItems.STELLAR_MOTE.get(), 1)
                .result(ModBlocks.STELLIA.get()).growthTimeSeconds(10).build(output, "stellia");

        // ==================== Crystal Plant Cultivation ====================
        HerbPotGrowingRecipeBuilder.builder()
                .seedling(ModItems.IRON_CRYST_PLANT.get())
                .herb(ModItems.SCALEPLATE.get(), 4).herb(ModItems.ZEPHYR_BLOSSOM.get(), 4)
                .result(Items.IRON_INGOT).growthTimeMinutes(2).build(output, "iron_cryst_plant");
    }
}
