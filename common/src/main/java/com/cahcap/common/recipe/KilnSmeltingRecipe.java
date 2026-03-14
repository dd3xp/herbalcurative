package com.cahcap.common.recipe;

import com.cahcap.common.registry.ModRegistries;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * Custom kiln smelting recipe.
 * Used for items that should only be smeltable in the kiln, not in vanilla furnaces.
 * The kiln also supports all vanilla smelting recipes as fallback.
 */
public class KilnSmeltingRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient ingredient;
    private final ItemStack result;
    private final int smeltTime;

    public KilnSmeltingRecipe(Ingredient ingredient, ItemStack result, int smeltTime) {
        this.ingredient = ingredient;
        this.result = result;
        this.smeltTime = smeltTime;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public int getSmeltTime() {
        return smeltTime;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRegistries.KILN_SMELTING_RECIPE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<KilnSmeltingRecipe> {

        public static final Serializer INSTANCE = new Serializer();

        private static final MapCodec<KilnSmeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(r -> r.ingredient),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.result),
                        com.mojang.serialization.Codec.INT.optionalFieldOf("smelt_time", 200).forGetter(r -> r.smeltTime)
                ).apply(instance, KilnSmeltingRecipe::new)
        );

        private static final StreamCodec<RegistryFriendlyByteBuf, KilnSmeltingRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        private static KilnSmeltingRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
            int smeltTime = buf.readVarInt();
            return new KilnSmeltingRecipe(ingredient, result, smeltTime);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, KilnSmeltingRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.ingredient);
            ItemStack.STREAM_CODEC.encode(buf, recipe.result);
            buf.writeVarInt(recipe.smeltTime);
        }

        @Override
        public MapCodec<KilnSmeltingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, KilnSmeltingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
