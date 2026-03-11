package com.cahcap.common.recipe;

import com.cahcap.common.item.IncensePowderItem;
import com.cahcap.common.registry.ModRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap;

/**
 * Recipe for Incense Burner mob summoning.
 * 
 * Defines:
 * - Required powder type (by entity type)
 * - Required herbs (list of item + count)
 * - Entity to spawn
 * - Burn time in ticks
 */
public class IncenseBurningRecipe implements Recipe<IncenseBurningRecipe.BurnerInput> {
    
    private final ResourceLocation entityType;
    private final List<HerbRequirement> herbRequirements;
    private final int burnTime;
    
    public IncenseBurningRecipe(ResourceLocation entityType, List<HerbRequirement> herbRequirements, int burnTime) {
        this.entityType = entityType;
        this.herbRequirements = herbRequirements;
        this.burnTime = burnTime;
    }
    
    public ResourceLocation getEntityType() {
        return entityType;
    }
    
    public List<Map.Entry<Item, Integer>> getHerbRequirements() {
        List<Map.Entry<Item, Integer>> result = new ArrayList<>();
        for (HerbRequirement req : herbRequirements) {
            Item item = BuiltInRegistries.ITEM.get(req.item);
            result.add(new AbstractMap.SimpleEntry<>(item, req.count));
        }
        return result;
    }
    
    public List<HerbRequirement> getRawHerbRequirements() {
        return herbRequirements;
    }
    
    public int getBurnTime() {
        return burnTime;
    }
    
    @Override
    public boolean matches(BurnerInput input, Level level) {
        ItemStack powder = input.powder();
        if (powder.isEmpty() || !(powder.getItem() instanceof IncensePowderItem powderItem)) {
            return false;
        }
        
        // Check if powder matches the entity type
        if (!powderItem.getEntityTypeId().equals(entityType)) {
            return false;
        }
        
        // Check if all required herbs are present
        Map<Item, Integer> herbs = input.herbs();
        for (HerbRequirement req : herbRequirements) {
            Item requiredItem = BuiltInRegistries.ITEM.get(req.item);
            int required = req.count;
            int have = herbs.getOrDefault(requiredItem, 0);
            if (have < required) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public ItemStack assemble(BurnerInput input, HolderLookup.Provider registries) {
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
        return ModRegistries.INCENSE_BURNING_SERIALIZER.get();
    }
    
    @Override
    public RecipeType<?> getType() {
        return ModRegistries.INCENSE_BURNING_RECIPE_TYPE.get();
    }
    
    public record BurnerInput(ItemStack powder, Map<Item, Integer> herbs) implements RecipeInput {
        @Override
        public ItemStack getItem(int slot) {
            return slot == 0 ? powder : ItemStack.EMPTY;
        }
        
        @Override
        public int size() {
            return 1;
        }
    }
    
    public record HerbRequirement(ResourceLocation item, int count) {
        public static final Codec<HerbRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(HerbRequirement::item),
                Codec.INT.fieldOf("count").forGetter(HerbRequirement::count)
        ).apply(instance, HerbRequirement::new));
        
        public static final StreamCodec<RegistryFriendlyByteBuf, HerbRequirement> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, HerbRequirement::item,
                ByteBufCodecs.VAR_INT, HerbRequirement::count,
                HerbRequirement::new
        );
    }
    
    public static class Serializer implements RecipeSerializer<IncenseBurningRecipe> {
        
        public static final MapCodec<IncenseBurningRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("entity_type").forGetter(IncenseBurningRecipe::getEntityType),
                HerbRequirement.CODEC.listOf().fieldOf("herbs").forGetter(IncenseBurningRecipe::getRawHerbRequirements),
                Codec.INT.optionalFieldOf("burn_time", 160).forGetter(IncenseBurningRecipe::getBurnTime)
        ).apply(instance, IncenseBurningRecipe::new));
        
        public static final StreamCodec<RegistryFriendlyByteBuf, IncenseBurningRecipe> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, IncenseBurningRecipe::getEntityType,
                HerbRequirement.STREAM_CODEC.apply(ByteBufCodecs.list()), IncenseBurningRecipe::getRawHerbRequirements,
                ByteBufCodecs.VAR_INT, IncenseBurningRecipe::getBurnTime,
                IncenseBurningRecipe::new
        );
        
        @Override
        public MapCodec<IncenseBurningRecipe> codec() {
            return CODEC;
        }
        
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, IncenseBurningRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
