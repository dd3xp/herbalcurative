package com.cahcap.neoforge.common.datagen.recipes.provider;

import com.cahcap.neoforge.common.registry.ModBlocks;
import com.cahcap.neoforge.common.registry.ModItems;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Vanilla crafting table recipes.
 */
public class CraftingRecipes {

    private static final TagKey<Item> HERB_PRODUCTS = ItemTags.create(
            ResourceLocation.fromNamespaceAndPath("herbalcurative", "herb_products"));

    private final ModRecipeProvider p;

    public CraftingRecipes(ModRecipeProvider provider) {
        this.p = provider;
    }

    public void build(RecipeOutput output) {
        // ==================== Herb to Seed Recipes (Shapeless) ====================
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CRYSTBUD_SEED.get(), 3)
            .requires(ModBlocks.CRYSTBUD.get())
            .unlockedBy("has_crystbud", p.criterion(ModBlocks.CRYSTBUD.get()))
            .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.DEWPETAL_SEED.get(), 3)
            .requires(ModBlocks.DEWPETAL.get())
            .unlockedBy("has_dewpetal", p.criterion(ModBlocks.DEWPETAL.get()))
            .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.PYRISAGE_SEED.get(), 3)
            .requires(ModBlocks.PYRISAGE.get())
            .unlockedBy("has_pyrisage", p.criterion(ModBlocks.PYRISAGE.get()))
            .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ROSYNIA_SEED.get(), 3)
            .requires(ModBlocks.ROSYNIA.get())
            .unlockedBy("has_rosynia", p.criterion(ModBlocks.ROSYNIA.get()))
            .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.VERDSCALE_FERN_SEED.get(), 3)
            .requires(ModBlocks.VERDSCALE_FERN.get())
            .unlockedBy("has_verdscale_fern", p.criterion(ModBlocks.VERDSCALE_FERN.get()))
            .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ZEPHYR_LILY_SEED.get(), 3)
            .requires(ModBlocks.ZEPHYR_LILY.get())
            .unlockedBy("has_zephyr_lily", p.criterion(ModBlocks.ZEPHYR_LILY.get()))
            .save(output);

        // ==================== Red Cherry Series ====================
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_CHERRY_PLANKS.get(), 4)
            .requires(ModBlocks.RED_CHERRY_LOG.get())
            .unlockedBy("has_red_cherry_log", p.criterion(ModBlocks.RED_CHERRY_LOG.get()))
            .save(output, "herbalcurative:red_cherry_planks_from_log");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_CHERRY_PLANKS.get(), 4)
            .requires(ModBlocks.STRIPPED_RED_CHERRY_LOG.get())
            .unlockedBy("has_stripped_red_cherry_log", p.criterion(ModBlocks.STRIPPED_RED_CHERRY_LOG.get()))
            .save(output, "herbalcurative:red_cherry_planks_from_stripped_log");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_CHERRY_PLANKS.get(), 4)
            .requires(ModBlocks.RED_CHERRY_WOOD.get())
            .unlockedBy("has_red_cherry_wood", p.criterion(ModBlocks.RED_CHERRY_WOOD.get()))
            .save(output, "herbalcurative:red_cherry_planks_from_wood");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_CHERRY_PLANKS.get(), 4)
            .requires(ModBlocks.STRIPPED_RED_CHERRY_WOOD.get())
            .unlockedBy("has_stripped_red_cherry_wood", p.criterion(ModBlocks.STRIPPED_RED_CHERRY_WOOD.get()))
            .save(output, "herbalcurative:red_cherry_planks_from_stripped_wood");

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_CHERRY_WOOD.get(), 3)
            .define('#', ModBlocks.RED_CHERRY_LOG.get())
            .pattern("##").pattern("##")
            .unlockedBy("has_red_cherry_log", p.criterion(ModBlocks.RED_CHERRY_LOG.get()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STRIPPED_RED_CHERRY_WOOD.get(), 3)
            .define('#', ModBlocks.STRIPPED_RED_CHERRY_LOG.get())
            .pattern("##").pattern("##")
            .unlockedBy("has_stripped_red_cherry_log", p.criterion(ModBlocks.STRIPPED_RED_CHERRY_LOG.get()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_CHERRY_STAIRS.get(), 4)
            .define('#', ModBlocks.RED_CHERRY_PLANKS.get())
            .pattern("#  ").pattern("## ").pattern("###")
            .unlockedBy("has_red_cherry_planks", p.criterion(ModBlocks.RED_CHERRY_PLANKS.get()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_CHERRY_SLAB.get(), 6)
            .define('#', ModBlocks.RED_CHERRY_PLANKS.get())
            .pattern("###")
            .unlockedBy("has_red_cherry_planks", p.criterion(ModBlocks.RED_CHERRY_PLANKS.get()))
            .save(output);

        TagKey<Item> woodenRods = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "rods/wooden"));
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.RED_CHERRY_FENCE.get(), 3)
            .define('#', ModBlocks.RED_CHERRY_PLANKS.get()).define('S', woodenRods)
            .pattern("#S#").pattern("#S#")
            .unlockedBy("has_red_cherry_planks", p.criterion(ModBlocks.RED_CHERRY_PLANKS.get()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RED_CHERRY_FENCE_GATE.get(), 1)
            .define('#', ModBlocks.RED_CHERRY_PLANKS.get()).define('S', woodenRods)
            .pattern("S#S").pattern("S#S")
            .unlockedBy("has_red_cherry_planks", p.criterion(ModBlocks.RED_CHERRY_PLANKS.get()))
            .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ModBlocks.RED_CHERRY_BUTTON.get(), 1)
            .requires(ModBlocks.RED_CHERRY_PLANKS.get())
            .unlockedBy("has_red_cherry_planks", p.criterion(ModBlocks.RED_CHERRY_PLANKS.get()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RED_CHERRY_PRESSURE_PLATE.get(), 1)
            .define('#', ModBlocks.RED_CHERRY_PLANKS.get())
            .pattern("##")
            .unlockedBy("has_red_cherry_planks", p.criterion(ModBlocks.RED_CHERRY_PLANKS.get()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.STICK, 4)
            .define('#', ModBlocks.RED_CHERRY_PLANKS.get())
            .pattern("#").pattern("#")
            .unlockedBy("has_red_cherry_planks", p.criterion(ModBlocks.RED_CHERRY_PLANKS.get()))
            .save(output, "herbalcurative:red_cherry_stick");

        // ==================== Lumistone Series ====================
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.LUMISTONE.get(), 8)
            .define('S', Items.STONE).define('H', HERB_PRODUCTS)
            .pattern("SSS").pattern("SHS").pattern("SSS")
            .unlockedBy("has_herb_product", p.criterion(HERB_PRODUCTS))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.LUMISTONE_SLAB.get(), 6)
            .define('L', ModBlocks.LUMISTONE.get()).pattern("LLL")
            .unlockedBy("has_lumistone", p.criterion(ModBlocks.LUMISTONE.get()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.LUMISTONE_STAIRS.get(), 4)
            .define('L', ModBlocks.LUMISTONE.get())
            .pattern("L  ").pattern("LL ").pattern("LLL")
            .unlockedBy("has_lumistone", p.criterion(ModBlocks.LUMISTONE.get()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.LUMISTONE_WALL.get(), 6)
            .define('L', ModBlocks.LUMISTONE.get())
            .pattern("LLL").pattern("LLL")
            .unlockedBy("has_lumistone", p.criterion(ModBlocks.LUMISTONE.get()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.LUMISTONE_PRESSURE_PLATE.get(), 1)
            .define('L', ModBlocks.LUMISTONE.get()).pattern("LL")
            .unlockedBy("has_lumistone", p.criterion(ModBlocks.LUMISTONE.get()))
            .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ModBlocks.LUMISTONE_BUTTON.get(), 1)
            .requires(ModBlocks.LUMISTONE.get())
            .unlockedBy("has_lumistone", p.criterion(ModBlocks.LUMISTONE.get()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.LUMISTONE_BRICKS.get(), 4)
            .define('L', ModBlocks.LUMISTONE.get())
            .pattern("LL").pattern("LL")
            .unlockedBy("has_lumistone", p.criterion(ModBlocks.LUMISTONE.get()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.LUMISTONE_BRICK_STAIRS.get(), 4)
            .define('L', ModBlocks.LUMISTONE_BRICKS.get())
            .pattern("L  ").pattern("LL ").pattern("LLL")
            .unlockedBy("has_lumistone_bricks", p.criterion(ModBlocks.LUMISTONE_BRICKS.get()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.LUMISTONE_BRICK_SLAB.get(), 6)
            .define('L', ModBlocks.LUMISTONE_BRICKS.get()).pattern("LLL")
            .unlockedBy("has_lumistone_bricks", p.criterion(ModBlocks.LUMISTONE_BRICKS.get()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.LUMISTONE_BRICK_WALL.get(), 6)
            .define('L', ModBlocks.LUMISTONE_BRICKS.get())
            .pattern("LLL").pattern("LLL")
            .unlockedBy("has_lumistone_bricks", p.criterion(ModBlocks.LUMISTONE_BRICKS.get()))
            .save(output);

        // ==================== Magic Alloy ====================
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.MAGIC_ALLOY_BLOCK.get(), 1)
            .define('M', ModItems.MAGIC_ALLOY_INGOT.get())
            .pattern("MMM").pattern("MMM").pattern("MMM")
            .unlockedBy("has_magic_alloy_ingot", p.criterion(ModItems.MAGIC_ALLOY_INGOT.get()))
            .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.MAGIC_ALLOY_INGOT.get(), 9)
            .requires(ModBlocks.MAGIC_ALLOY_BLOCK.get())
            .unlockedBy("has_magic_alloy_block", p.criterion(ModBlocks.MAGIC_ALLOY_BLOCK.get()))
            .save(output, "herbalcurative:magic_alloy_ingot_from_block");
    }
}
