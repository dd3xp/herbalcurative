package com.cahcap.herbalcurative.common.recipe;

import com.cahcap.herbalcurative.common.recipe.HerbalBlendingRecipe.IngredientWithCount;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Serializer for HerbalBlendingRecipe.
 * Handles JSON serialization/deserialization and network sync.
 */
public class HerbalBlendingRecipeSerializer implements RecipeSerializer<HerbalBlendingRecipe> {
    
    public static final HerbalBlendingRecipeSerializer INSTANCE = new HerbalBlendingRecipeSerializer();
    
    // Codec for IngredientWithCount
    private static final Codec<IngredientWithCount> INGREDIENT_WITH_COUNT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(IngredientWithCount::ingredient),
                    Codec.INT.fieldOf("count").orElse(1).forGetter(IngredientWithCount::count)
            ).apply(instance, IngredientWithCount::new)
    );
    
    // StreamCodec for IngredientWithCount (network)
    private static final StreamCodec<RegistryFriendlyByteBuf, IngredientWithCount> INGREDIENT_WITH_COUNT_STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, IngredientWithCount::ingredient,
                    ByteBufCodecs.INT, IngredientWithCount::count,
                    IngredientWithCount::new
            );
    
    // Helper to convert List to NonNullList with exactly 9 elements
    private static NonNullList<Ingredient> listToShelfPattern(List<Ingredient> list) {
        NonNullList<Ingredient> pattern = NonNullList.withSize(9, Ingredient.EMPTY);
        for (int i = 0; i < Math.min(list.size(), 9); i++) {
            pattern.set(i, list.get(i));
        }
        return pattern;
    }
    
    // MapCodec for the recipe
    private static final MapCodec<HerbalBlendingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    INGREDIENT_WITH_COUNT_CODEC.listOf().fieldOf("basket_inputs").forGetter(HerbalBlendingRecipe::getBasketInputs),
                    Ingredient.CODEC.listOf().fieldOf("shelf_pattern").forGetter(r -> new ArrayList<>(r.getShelfPattern())),
                    ItemStack.CODEC.fieldOf("result").forGetter(HerbalBlendingRecipe::getOutput)
            ).apply(instance, (baskets, shelves, output) -> new HerbalBlendingRecipe(baskets, listToShelfPattern(shelves), output))
    );
    
    // StreamCodec for network
    private static final StreamCodec<RegistryFriendlyByteBuf, HerbalBlendingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    INGREDIENT_WITH_COUNT_STREAM_CODEC.apply(ByteBufCodecs.list()), HerbalBlendingRecipe::getBasketInputs,
                    Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), r -> new ArrayList<>(r.getShelfPattern()),
                    ItemStack.STREAM_CODEC, HerbalBlendingRecipe::getOutput,
                    (baskets, shelves, output) -> new HerbalBlendingRecipe(baskets, listToShelfPattern(shelves), output)
            );
    
    private HerbalBlendingRecipeSerializer() {}
    
    @Override
    public MapCodec<HerbalBlendingRecipe> codec() {
        return CODEC;
    }
    
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, HerbalBlendingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
