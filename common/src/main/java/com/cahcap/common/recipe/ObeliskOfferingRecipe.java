package com.cahcap.common.recipe;

import com.cahcap.common.registry.ModRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/**
 * Recipe for Obelisk offering — sacrifice an item to summon a mob.
 *
 * Fields:
 * - ingredient:     the item the player offers
 * - entityType:     ResourceLocation of the mob to spawn
 * - waitTicks:      ticks to wait before spawning
 * - spawnDistance:   blocks in front of the offering table to spawn the mob
 */
public class ObeliskOfferingRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient ingredient;
    private final ResourceLocation entityType;
    private final int waitTicks;
    private final int spawnDistance;

    public ObeliskOfferingRecipe(Ingredient ingredient, ResourceLocation entityType,
                                 int waitTicks, int spawnDistance) {
        this.ingredient = ingredient;
        this.entityType = entityType;
        this.waitTicks = waitTicks;
        this.spawnDistance = spawnDistance;
    }

    public Ingredient getIngredient() { return ingredient; }
    public ResourceLocation getEntityType() { return entityType; }
    public int getWaitTicks() { return waitTicks; }
    public int getSpawnDistance() { return spawnDistance; }

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

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRegistries.OBELISK_OFFERING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRegistries.OBELISK_OFFERING_RECIPE_TYPE.get();
    }

    // ==================== Serializer ====================

    public static class Serializer implements RecipeSerializer<ObeliskOfferingRecipe> {

        public static final Serializer INSTANCE = new Serializer();

        public static final MapCodec<ObeliskOfferingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(ObeliskOfferingRecipe::getIngredient),
                ResourceLocation.CODEC.fieldOf("entity_type").forGetter(ObeliskOfferingRecipe::getEntityType),
                Codec.INT.optionalFieldOf("wait_ticks", 100).forGetter(ObeliskOfferingRecipe::getWaitTicks),
                Codec.INT.optionalFieldOf("spawn_distance", 1).forGetter(ObeliskOfferingRecipe::getSpawnDistance)
        ).apply(instance, ObeliskOfferingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ObeliskOfferingRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, ObeliskOfferingRecipe::getIngredient,
                ResourceLocation.STREAM_CODEC, ObeliskOfferingRecipe::getEntityType,
                ByteBufCodecs.VAR_INT, ObeliskOfferingRecipe::getWaitTicks,
                ByteBufCodecs.VAR_INT, ObeliskOfferingRecipe::getSpawnDistance,
                ObeliskOfferingRecipe::new
        );

        @Override
        public MapCodec<ObeliskOfferingRecipe> codec() { return CODEC; }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ObeliskOfferingRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
