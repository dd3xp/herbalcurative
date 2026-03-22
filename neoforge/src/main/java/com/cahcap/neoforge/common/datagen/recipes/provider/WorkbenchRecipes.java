package com.cahcap.neoforge.common.datagen.recipes.provider;

import com.cahcap.neoforge.common.datagen.recipes.builder.*;

import com.cahcap.neoforge.common.registry.ModBlocks;
import com.cahcap.neoforge.common.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.concurrent.CompletableFuture;

/**
 * Workbench recipes.
 */
public class WorkbenchRecipes {

    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public WorkbenchRecipes(CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.lookupProvider = lookupProvider;
    }

    public void build(RecipeOutput output) {
        HolderLookup.Provider registries;
        try {
            registries = lookupProvider.get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get registries", e);
        }

        HolderLookup.RegistryLookup<Enchantment> enchantmentRegistry = registries.lookupOrThrow(Registries.ENCHANTMENT);

        ItemStack efficiencyBook = createEnchantedBook(enchantmentRegistry.getOrThrow(Enchantments.EFFICIENCY), 5);
        WorkbenchRecipeBuilder.builder()
                .tool(ModItems.FEATHER_QUILL.get()).tool(ModItems.CUTTING_KNIFE.get())
                .input(Items.BOOK)
                .material(Items.FLINT, 16).material(Items.LAPIS_LAZULI, 5)
                .result(efficiencyBook)
                .build(output, "efficiency_5_enchanted_book");

        ItemStack smiteBook = createEnchantedBook(enchantmentRegistry.getOrThrow(Enchantments.SMITE), 5);
        WorkbenchRecipeBuilder.builder()
                .tool(ModItems.FEATHER_QUILL.get()).tool(ModItems.FORGE_HAMMER.get())
                .input(Items.BOOK)
                .material(Items.ROTTEN_FLESH, 16).material(Items.BONE, 16).material(Items.LAPIS_LAZULI, 5)
                .result(smiteBook)
                .build(output, "smite_5_enchanted_book");

        ItemStack unbreakingBook = createEnchantedBook(enchantmentRegistry.getOrThrow(Enchantments.UNBREAKING), 3);
        WorkbenchRecipeBuilder.builder()
                .tool(ModItems.FEATHER_QUILL.get()).tool(ModItems.FORGE_HAMMER.get())
                .input(Items.BOOK)
                .material(Items.OBSIDIAN, 8).material(Items.LAPIS_LAZULI, 3)
                .result(unbreakingBook)
                .build(output, "unbreaking_3_enchanted_book");

        ItemStack sharpnessBook = createEnchantedBook(enchantmentRegistry.getOrThrow(Enchantments.SHARPNESS), 5);
        WorkbenchRecipeBuilder.builder()
                .tool(ModItems.FEATHER_QUILL.get()).tool(ModItems.CUTTING_KNIFE.get())
                .input(Items.BOOK)
                .material(Items.QUARTZ, 16).material(Items.LAPIS_LAZULI, 5)
                .result(sharpnessBook)
                .build(output, "sharpness_5_enchanted_book");

        WorkbenchRecipeBuilder.builder()
                .tool(ModItems.CUTTING_KNIFE.get())
                .input(ModBlocks.LUMISTONE_BRICKS.get())
                .material(Items.GLOWSTONE_DUST, 4)
                .result(new ItemStack(ModBlocks.RUNE_STONE_BRICKS.get()))
                .build(output, "rune_stone_bricks");
    }

    private static ItemStack createEnchantedBook(Holder<Enchantment> enchantment, int level) {
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        enchantments.set(enchantment, level);
        book.set(DataComponents.STORED_ENCHANTMENTS, enchantments.toImmutable());
        return book;
    }
}
