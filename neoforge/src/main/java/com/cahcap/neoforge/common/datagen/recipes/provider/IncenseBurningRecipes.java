package com.cahcap.neoforge.common.datagen.recipes.provider;

import com.cahcap.neoforge.common.datagen.recipes.builder.*;

import com.cahcap.neoforge.common.registry.ModItems;
import net.minecraft.data.recipes.RecipeOutput;

/**
 * Incense Burning recipes.
 */
public class IncenseBurningRecipes {

    public void build(RecipeOutput output) {
        IncenseBurningRecipeBuilder.create("minecraft:wither_skeleton")
                .herb(ModItems.SCALEPLATE.get(), 32)
                .herb(ModItems.DEWPETAL.get(), 32)
                .herb(ModItems.ZEPHYR_BLOSSOM.get(), 32)
                .herb(ModItems.CRYST_SPINE.get(), 32)
                .herb(ModItems.PYRO_NODE.get(), 32)
                .herb(ModItems.STELLAR_MOTE.get(), 32)
                .burnTime(160)
                .save(output, "wither_skeleton");
    }
}
