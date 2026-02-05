package com.cahcap.herbalcurative.neoforge.common.datagen.recipes;

import com.cahcap.herbalcurative.neoforge.common.registry.ModBlocks;
import com.cahcap.herbalcurative.neoforge.common.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.concurrent.CompletableFuture;

/**
 * Recipe provider for all recipes:
 * - Vanilla crafting recipes
 * - Herbal Blending Rack recipes
 * - Workbench recipes
 */
public class ModRecipeProvider extends net.minecraft.data.recipes.RecipeProvider {
    
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;
    
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
        this.lookupProvider = lookupProvider;
    }
    
    @Override
    protected void buildRecipes(RecipeOutput output) {
        buildCraftingRecipes(output);
        buildHerbalBlendingRecipes(output);
        
        // Get registries for enchantments
        HolderLookup.Provider registries;
        try {
            registries = lookupProvider.get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get registries", e);
        }
        buildWorkbenchRecipes(output, registries);
    }
    
    /**
     * Vanilla crafting table recipes
     */
    private void buildCraftingRecipes(RecipeOutput output) {
        // ==================== Herb to Seed Recipes (Shapeless) ====================
        // All herbs: 1 flower -> 3 seeds
        
        // Crystbud: crystbud -> crystbud_seed x3
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CRYSTBUD_SEED.get(), 3)
            .requires(ModBlocks.CRYSTBUD.get())
            .unlockedBy("has_crystbud", has(ModBlocks.CRYSTBUD.get()))
            .save(output);
        
        // Dewpetal: dewpetal -> dewpetal_seed x3
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.DEWPETAL_SEED.get(), 3)
            .requires(ModBlocks.DEWPETAL.get())
            .unlockedBy("has_dewpetal", has(ModBlocks.DEWPETAL.get()))
            .save(output);
        
        // Pyrisage: pyrisage -> pyrisage_seed x3
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.PYRISAGE_SEED.get(), 3)
            .requires(ModBlocks.PYRISAGE.get())
            .unlockedBy("has_pyrisage", has(ModBlocks.PYRISAGE.get()))
            .save(output);
        
        // Rosynia: rosynia -> rosynia_seed x3
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ROSYNIA_SEED.get(), 3)
            .requires(ModBlocks.ROSYNIA.get())
            .unlockedBy("has_rosynia", has(ModBlocks.ROSYNIA.get()))
            .save(output);
        
        // Verdscale Fern: verdscale_fern -> verdscale_fern_seed x3
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.VERDSCALE_FERN_SEED.get(), 3)
            .requires(ModBlocks.VERDSCALE_FERN.get())
            .unlockedBy("has_verdscale_fern", has(ModBlocks.VERDSCALE_FERN.get()))
            .save(output);
        
        // Zephyr Lily: zephyr_lily -> zephyr_lily_seed x3
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ZEPHYR_LILY_SEED.get(), 3)
            .requires(ModBlocks.ZEPHYR_LILY.get())
            .unlockedBy("has_zephyr_lily", has(ModBlocks.ZEPHYR_LILY.get()))
            .save(output);
        
        // ==================== Red Cherry Series Recipes ====================
        
        // Red Cherry Planks: 1 log -> 4 planks (shapeless)
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_CHERRY_PLANKS.get(), 4)
            .requires(ModBlocks.RED_CHERRY_LOG.get())
            .unlockedBy("has_red_cherry_log", has(ModBlocks.RED_CHERRY_LOG.get()))
            .save(output);
        
        // Red Cherry Stick: 2 planks -> 4 sticks (shaped, vertical)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.STICK, 4)
            .define('#', ModBlocks.RED_CHERRY_PLANKS.get())
            .pattern("#")
            .pattern("#")
            .unlockedBy("has_red_cherry_planks", has(ModBlocks.RED_CHERRY_PLANKS.get()))
            .save(output, "herbalcurative:red_cherry_stick");
    }
    
    /**
     * Herbal Blending Rack multiblock recipes.
     * Structure: 6 Herb Baskets (sides) + 9 Red Cherry Shelves (center 3x3)
     * Use Flowweave Ring to trigger crafting.
     * 
     * Shelf positions (like crafting table 3x3):
     * [0][1][2]
     * [3][4][5]  <- 4 is center, output replaces item at this position
     * [6][7][8]
     */
    private void buildHerbalBlendingRecipes(RecipeOutput output) {
        // Red Cherry Sapling from any sapling
        // Input: 4 Dewpetal Shard in basket
        //        Any sapling on center shelf
        // Output: 1 Red Cherry Sapling
        HerbalBlendingRecipeBuilder.builder()
                .basketInput(ModItems.DEWPETAL_SHARD.get(), 4)
                .pattern("   ", " S ", "   ")
                .define('S', ItemTags.SAPLINGS)
                .output(ModBlocks.RED_CHERRY_SAPLING.get())
                .build(output, "red_cherry_sapling");
    }
    
    /**
     * Workbench recipes.
     * Structure: 3-block wide workbench
     * - Left block: 4 tool slots
     * - Center block: 1 input slot
     * - Right block: 6 material slots (LIFO stack)
     * 
     * Tool slots (looking down at left block):
     * [0: top-left] [1: top-right]
     * [2: bot-left] [3: bot-right]
     */
    private void buildWorkbenchRecipes(RecipeOutput output, HolderLookup.Provider registries) {
        // Efficiency V Enchanted Book
        // Tools: Feather Quill, Forge Hammer
        // Input: Book
        // Materials: 8 Obsidian
        HolderLookup.RegistryLookup<Enchantment> enchantmentRegistry = registries.lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> efficiency = enchantmentRegistry.getOrThrow(Enchantments.EFFICIENCY);
        
        ItemStack efficiencyBook = new ItemStack(Items.ENCHANTED_BOOK);
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        enchantments.set(efficiency, 5);
        efficiencyBook.set(DataComponents.STORED_ENCHANTMENTS, enchantments.toImmutable());
        
        WorkbenchRecipeBuilder.builder()
                .tool(ModItems.FEATHER_QUILL.get())
                .tool(ModItems.FORGE_HAMMER.get())
                .input(Items.BOOK)
                .material(Items.OBSIDIAN, 8)
                .result(efficiencyBook)
                .build(output, "efficiency_5_enchanted_book");
        
        // Smite V Enchanted Book
        // Tools: Feather Quill, Cutting Knife
        // Input: Book
        // Materials: 16 Rotten Flesh, 16 Bone
        Holder<Enchantment> smite = enchantmentRegistry.getOrThrow(Enchantments.SMITE);
        
        ItemStack smiteBook = new ItemStack(Items.ENCHANTED_BOOK);
        ItemEnchantments.Mutable smiteEnchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        smiteEnchantments.set(smite, 5);
        smiteBook.set(DataComponents.STORED_ENCHANTMENTS, smiteEnchantments.toImmutable());
        
        WorkbenchRecipeBuilder.builder()
                .tool(ModItems.FEATHER_QUILL.get())
                .tool(ModItems.CUTTING_KNIFE.get())
                .input(Items.BOOK)
                .material(Items.ROTTEN_FLESH, 16)
                .material(Items.BONE, 16)
                .result(smiteBook)
                .build(output, "smite_5_enchanted_book");
    }
}
