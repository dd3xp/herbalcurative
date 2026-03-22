package com.cahcap.neoforge.common.datagen.recipes.provider;

import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.concurrent.CompletableFuture;

/**
 * Main recipe provider that delegates to per-type providers.
 */
public class ModRecipeProvider extends RecipeProvider {

    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
        this.lookupProvider = lookupProvider;
    }

    /** Expose has() for delegate recipe classes. */
    public Criterion<?> criterion(ItemLike item) { return has(item); }
    public Criterion<?> criterion(TagKey<Item> tag) { return has(tag); }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        new CraftingRecipes(this).build(output);
        new HerbalBlendingRecipes().build(output);
        new CauldronRecipes().build(output);
        new HerbPotRecipes().build(output);
        new IncenseBurningRecipes().build(output);
        new ObeliskOfferingRecipes().build(output);
        new KilnRecipes().build(output);
        new WorkbenchRecipes(lookupProvider).build(output);
    }
}
