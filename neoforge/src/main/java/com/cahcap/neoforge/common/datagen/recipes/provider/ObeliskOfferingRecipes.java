package com.cahcap.neoforge.common.datagen.recipes.provider;

import com.cahcap.neoforge.common.datagen.recipes.builder.*;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;

/**
 * Obelisk Offering recipes.
 */
public class ObeliskOfferingRecipes {

    public void build(RecipeOutput output) {
        ObeliskOfferingRecipeBuilder.create("minecraft:villager")
                .ingredient(Items.EMERALD_BLOCK)
                .waitTicks(100)
                .spawnDistance(1)
                .save(output, "villager");
    }
}
