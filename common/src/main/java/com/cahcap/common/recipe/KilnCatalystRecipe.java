package com.cahcap.common.recipe;

import com.cahcap.common.registry.ModRegistries;
import com.mojang.serialization.Codec;
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
 * Defines a kiln catalyst fuel.
 * Each catalyst has:
 * - ingredient: what item acts as catalyst
 * - affectedInputs: which smelting inputs get output multiplier (others only get speed boost)
 * - outputMultiplier: product count multiplier (e.g. 2 = double output), only for affected inputs
 * - speedMultiplier: smelting speed multiplier (e.g. 4 = 4x faster), always applies
 * - usesPerItem: how many smelts one catalyst item supports
 */
public class KilnCatalystRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient ingredient;
    private final Ingredient affectedInputs;
    private final int outputMultiplier;
    private final int speedMultiplier;
    private final int usesPerItem;

    public KilnCatalystRecipe(Ingredient ingredient, Ingredient affectedInputs, int outputMultiplier, int speedMultiplier, int usesPerItem) {
        this.ingredient = ingredient;
        this.affectedInputs = affectedInputs;
        this.outputMultiplier = outputMultiplier;
        this.speedMultiplier = speedMultiplier;
        this.usesPerItem = usesPerItem;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public Ingredient getAffectedInputs() {
        return affectedInputs;
    }

    /**
     * Check if the given smelting input should receive the output multiplier.
     */
    public boolean isInputAffected(ItemStack input) {
        return affectedInputs.isEmpty() || affectedInputs.test(input);
    }

    public int getOutputMultiplier() {
        return outputMultiplier;
    }

    public int getSpeedMultiplier() {
        return speedMultiplier;
    }

    public int getUsesPerItem() {
        return usesPerItem;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRegistries.KILN_CATALYST_RECIPE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<KilnCatalystRecipe> {

        public static final Serializer INSTANCE = new Serializer();

        private static final MapCodec<KilnCatalystRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(r -> r.ingredient),
                        Ingredient.CODEC.optionalFieldOf("affected_inputs", Ingredient.EMPTY).forGetter(r -> r.affectedInputs),
                        Codec.INT.fieldOf("output_multiplier").forGetter(r -> r.outputMultiplier),
                        Codec.INT.fieldOf("speed_multiplier").forGetter(r -> r.speedMultiplier),
                        Codec.INT.fieldOf("uses_per_item").forGetter(r -> r.usesPerItem)
                ).apply(instance, KilnCatalystRecipe::new)
        );

        private static final StreamCodec<RegistryFriendlyByteBuf, KilnCatalystRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        private static KilnCatalystRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            boolean hasAffected = buf.readBoolean();
            Ingredient affectedInputs = hasAffected ? Ingredient.CONTENTS_STREAM_CODEC.decode(buf) : Ingredient.EMPTY;
            int outputMultiplier = buf.readVarInt();
            int speedMultiplier = buf.readVarInt();
            int usesPerItem = buf.readVarInt();
            return new KilnCatalystRecipe(ingredient, affectedInputs, outputMultiplier, speedMultiplier, usesPerItem);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, KilnCatalystRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.ingredient);
            boolean hasAffected = !recipe.affectedInputs.isEmpty();
            buf.writeBoolean(hasAffected);
            if (hasAffected) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.affectedInputs);
            }
            buf.writeVarInt(recipe.outputMultiplier);
            buf.writeVarInt(recipe.speedMultiplier);
            buf.writeVarInt(recipe.usesPerItem);
        }

        @Override
        public MapCodec<KilnCatalystRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, KilnCatalystRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
