package com.cahcap.common.recipe;

import com.cahcap.common.registry.ModRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * Recipe for growing plants in the Herb Pot.
 * 
 * Defines:
 * - Seedling (what plant to grow)
 * - Soil type (optional, defaults to any valid soil)
 * - Required herbs (herb type -> count)
 * - Output item
 * - Growth time (in ticks)
 */
public class HerbPotGrowingRecipe implements Recipe<HerbPotGrowingRecipe.PotInput> {
    
    private final Ingredient seedling;
    private final Ingredient soil;
    private final List<Map.Entry<Item, Integer>> herbRequirements;
    private final ItemStack output;
    private final int growthTime;
    
    public HerbPotGrowingRecipe(Ingredient seedling, Ingredient soil, 
                                List<Map.Entry<Item, Integer>> herbRequirements,
                                ItemStack output, int growthTime) {
        this.seedling = seedling;
        this.soil = soil;
        this.herbRequirements = herbRequirements;
        this.output = output;
        this.growthTime = growthTime;
    }
    
    public Ingredient getSeedling() {
        return seedling;
    }
    
    public Ingredient getSoil() {
        return soil;
    }
    
    public List<Map.Entry<Item, Integer>> getHerbRequirements() {
        return herbRequirements;
    }
    
    public ItemStack getOutput() {
        return output.copy();
    }
    
    public int getGrowthTime() {
        return growthTime;
    }
    
    @Override
    public boolean matches(PotInput input, Level level) {
        if (!seedling.test(input.seedling())) {
            return false;
        }
        
        if (!soil.isEmpty() && !soil.test(input.soil())) {
            return false;
        }
        
        for (Map.Entry<Item, Integer> req : herbRequirements) {
            int available = input.herbs().getOrDefault(req.getKey(), 0);
            if (available < req.getValue()) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public ItemStack assemble(PotInput input, HolderLookup.Provider registries) {
        return output.copy();
    }
    
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }
    
    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.copy();
    }
    
    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }
    
    @Override
    public RecipeType<?> getType() {
        return ModRegistries.HERB_POT_GROWING_RECIPE_TYPE.get();
    }
    
    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(seedling);
        if (!soil.isEmpty()) {
            ingredients.add(soil);
        }
        for (Map.Entry<Item, Integer> req : herbRequirements) {
            ingredients.add(Ingredient.of(req.getKey()));
        }
        return ingredients;
    }
    
    public record PotInput(ItemStack seedling, ItemStack soil, Map<Item, Integer> herbs) implements RecipeInput {
        @Override
        public ItemStack getItem(int index) {
            if (index == 0) return seedling;
            if (index == 1) return soil;
            return ItemStack.EMPTY;
        }
        
        @Override
        public int size() {
            return 2 + herbs.size();
        }
    }
    
    public record HerbRequirement(Item herb, int count) {
        public static final Codec<HerbRequirement> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        BuiltInRegistries.ITEM.byNameCodec().fieldOf("herb").forGetter(HerbRequirement::herb),
                        Codec.INT.fieldOf("count").orElse(1).forGetter(HerbRequirement::count)
                ).apply(instance, HerbRequirement::new)
        );
        
        public static final StreamCodec<RegistryFriendlyByteBuf, HerbRequirement> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.fromCodec(BuiltInRegistries.ITEM.byNameCodec()), HerbRequirement::herb,
                        ByteBufCodecs.INT, HerbRequirement::count,
                        HerbRequirement::new
                );
        
        public Map.Entry<Item, Integer> toEntry() {
            return Map.entry(herb, count);
        }
        
        public static HerbRequirement fromEntry(Map.Entry<Item, Integer> entry) {
            return new HerbRequirement(entry.getKey(), entry.getValue());
        }
    }
    
    public static class Serializer implements RecipeSerializer<HerbPotGrowingRecipe> {
        
        public static final Serializer INSTANCE = new Serializer();
        
        private static final MapCodec<HerbPotGrowingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Ingredient.CODEC.fieldOf("seedling").forGetter(HerbPotGrowingRecipe::getSeedling),
                        Ingredient.CODEC.optionalFieldOf("soil", Ingredient.EMPTY).forGetter(HerbPotGrowingRecipe::getSoil),
                        HerbRequirement.CODEC.listOf().fieldOf("herbs").forGetter(r -> 
                                r.getHerbRequirements().stream().map(HerbRequirement::fromEntry).toList()),
                        ItemStack.CODEC.fieldOf("result").forGetter(HerbPotGrowingRecipe::getOutput),
                        Codec.INT.fieldOf("growth_time").orElse(6000).forGetter(HerbPotGrowingRecipe::getGrowthTime)
                ).apply(instance, (seedling, soil, herbs, result, time) -> 
                        new HerbPotGrowingRecipe(seedling, soil, 
                                herbs.stream().map(HerbRequirement::toEntry).toList(), 
                                result, time))
        );
        
        private static final StreamCodec<RegistryFriendlyByteBuf, HerbPotGrowingRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, HerbPotGrowingRecipe::getSeedling,
                        Ingredient.CONTENTS_STREAM_CODEC, HerbPotGrowingRecipe::getSoil,
                        HerbRequirement.STREAM_CODEC.apply(ByteBufCodecs.list()), r -> 
                                r.getHerbRequirements().stream().map(HerbRequirement::fromEntry).toList(),
                        ItemStack.STREAM_CODEC, HerbPotGrowingRecipe::getOutput,
                        ByteBufCodecs.INT, HerbPotGrowingRecipe::getGrowthTime,
                        (seedling, soil, herbs, result, time) -> 
                                new HerbPotGrowingRecipe(seedling, soil,
                                        herbs.stream().map(HerbRequirement::toEntry).toList(),
                                        result, time)
                );
        
        private Serializer() {}
        
        @Override
        public MapCodec<HerbPotGrowingRecipe> codec() {
            return CODEC;
        }
        
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, HerbPotGrowingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
