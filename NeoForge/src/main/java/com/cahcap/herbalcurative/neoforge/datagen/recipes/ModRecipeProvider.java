package com.cahcap.herbalcurative.neoforge.datagen.recipes;

import com.cahcap.herbalcurative.neoforge.registry.ModBlocks;
import com.cahcap.herbalcurative.neoforge.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

/**
 * Recipe provider
 * Generates 8 crafting recipes
 */
public class ModRecipeProvider extends net.minecraft.data.recipes.RecipeProvider {
    
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }
    
    @Override
    protected void buildRecipes(RecipeOutput output) {
        // ==================== Herb to Seed Recipes (Shapeless) ====================
        // All herbs: 1 flower → 3 seeds
        
        // Crystbud: crystbud → crystbud_seed x3
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CRYSTBUD_SEED.get(), 3)
            .requires(ModBlocks.CRYSTBUD.get())
            .unlockedBy("has_crystbud", has(ModBlocks.CRYSTBUD.get()))
            .save(output);
        
        // Dewpetal: dewpetal → dewpetal_seed x3
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.DEWPETAL_SEED.get(), 3)
            .requires(ModBlocks.DEWPETAL.get())
            .unlockedBy("has_dewpetal", has(ModBlocks.DEWPETAL.get()))
            .save(output);
        
        // Pyrisage: pyrisage → pyrisage_seed x3
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.PYRISAGE_SEED.get(), 3)
            .requires(ModBlocks.PYRISAGE.get())
            .unlockedBy("has_pyrisage", has(ModBlocks.PYRISAGE.get()))
            .save(output);
        
        // Rosynia: rosynia → rosynia_seed x3
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ROSYNIA_SEED.get(), 3)
            .requires(ModBlocks.ROSYNIA.get())
            .unlockedBy("has_rosynia", has(ModBlocks.ROSYNIA.get()))
            .save(output);
        
        // Verdscale Fern: verdscale_fern → verdscale_fern_seed x3
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.VERDSCALE_FERN_SEED.get(), 3)
            .requires(ModBlocks.VERDSCALE_FERN.get())
            .unlockedBy("has_verdscale_fern", has(ModBlocks.VERDSCALE_FERN.get()))
            .save(output);
        
        // Zephyr Lily: zephyr_lily → zephyr_lily_seed x3
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ZEPHYR_LILY_SEED.get(), 3)
            .requires(ModBlocks.ZEPHYR_LILY.get())
            .unlockedBy("has_zephyr_lily", has(ModBlocks.ZEPHYR_LILY.get()))
            .save(output);
        
        // ==================== Forest Heartwood Recipes ====================
        
        // Forest Heartwood Planks: 1 log → 4 planks (shapeless)
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.FOREST_HEARTWOOD_PLANKS.get(), 4)
            .requires(ModBlocks.FOREST_HEARTWOOD_LOG.get())
            .unlockedBy("has_forest_heartwood_log", has(ModBlocks.FOREST_HEARTWOOD_LOG.get()))
            .save(output);
        
        // Forest Heartwood Stick: 2 planks → 4 sticks (shaped, vertical)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.STICK, 4)
            .define('#', ModBlocks.FOREST_HEARTWOOD_PLANKS.get())
            .pattern("#")
            .pattern("#")
            .unlockedBy("has_forest_heartwood_planks", has(ModBlocks.FOREST_HEARTWOOD_PLANKS.get()))
            .save(output, "herbalcurative:forest_heartwood_stick");
    }
}
