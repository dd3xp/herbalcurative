package com.cahcap.neoforge.common.datagen.recipes.provider;

import com.cahcap.neoforge.common.datagen.recipes.builder.*;

import com.cahcap.neoforge.common.registry.ModBlocks;
import com.cahcap.neoforge.common.registry.ModItems;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.ItemTags;

/**
 * Herbal Blending Rack recipes.
 */
public class HerbalBlendingRecipes {

    public void build(RecipeOutput output) {
        HerbalBlendingRecipeBuilder.builder()
                .basketInput(ModItems.DEWPETAL.get(), 4)
                .pattern("   ", " S ", "   ")
                .define('S', ItemTags.SAPLINGS)
                .output(ModBlocks.RED_CHERRY_SAPLING.get())
                .build(output, "red_cherry_sapling");
    }
}
