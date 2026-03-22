package com.cahcap.neoforge.common.datagen.recipes.provider;

import com.cahcap.neoforge.common.datagen.recipes.builder.*;

import com.cahcap.neoforge.common.registry.ModItems;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Kiln catalyst recipes.
 */
public class KilnRecipes {

    public void build(RecipeOutput output) {
        TagKey<Item> kilnCatalyzable = ItemTags.create(
                ResourceLocation.fromNamespaceAndPath("herbalcurative", "kiln_catalyzable"));
        KilnCatalystRecipeBuilder.builder()
                .ingredient(ModItems.BURNT_NODE.get())
                .affectedInputs(kilnCatalyzable)
                .outputMultiplier(2)
                .speedMultiplier(4)
                .usesPerItem(8)
                .build(output, "burnt_node");
    }
}
