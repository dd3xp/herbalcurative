package com.cahcap.herbalcurative.common.recipe;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/**
 * Recipe for crafting items inside the Cauldron with potion.
 * 
 * Example: Apple + Healing 2 (8+ min) -> Enchanted Golden Apple
 * 
 * Input: The item placed into the potion
 * Output: The resulting item after transformation
 * Minimum Duration: Required minimum potion duration (in minutes)
 * Minimum Level: Required minimum potion level
 * Potion Type: Required potion type (empty for any)
 * Processing Time: Ticks to complete transformation
 */
public class CauldronRecipe implements Recipe<SingleRecipeInput> {
    
    private final Ingredient input;
    private final ItemStack output;
    private final String potionType;
    private final int minDuration;
    private final int minLevel;
    private final int processingTime;
    
    public CauldronRecipe(Ingredient input, ItemStack output, String potionType, 
                          int minDuration, int minLevel, int processingTime) {
        this.input = input;
        this.output = output;
        this.potionType = potionType;
        this.minDuration = minDuration;
        this.minLevel = minLevel;
        this.processingTime = processingTime;
    }
    
    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.getItem(0));
    }
    
    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
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
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(input);
        return list;
    }
    
    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }
    
    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }
    
    // Getters
    public Ingredient getInput() { return input; }
    public ItemStack getOutput() { return output.copy(); }
    public String getPotionType() { return potionType; }
    public int getMinDuration() { return minDuration; }
    public int getMinLevel() { return minLevel; }
    public int getProcessingTime() { return processingTime; }
    
    /**
     * Check if this recipe matches the given potion properties
     */
    public boolean matchesPotion(String potionType, int duration, int level) {
        if (!this.potionType.isEmpty() && !this.potionType.equals(potionType)) {
            return false;
        }
        return duration >= this.minDuration && level >= this.minLevel;
    }
    
    // Recipe Type
    public static class Type implements RecipeType<CauldronRecipe> {
        public static final Type INSTANCE = new Type();
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "cauldron");
    }
    
    // Recipe Serializer
    public static class Serializer implements RecipeSerializer<CauldronRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "cauldron");
        
        private static final MapCodec<CauldronRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(CauldronRecipe::getInput),
                ItemStack.CODEC.fieldOf("output").forGetter(CauldronRecipe::getOutput),
                Codec.STRING.optionalFieldOf("potion_type", "").forGetter(CauldronRecipe::getPotionType),
                Codec.INT.optionalFieldOf("min_duration", 0).forGetter(CauldronRecipe::getMinDuration),
                Codec.INT.optionalFieldOf("min_level", 1).forGetter(CauldronRecipe::getMinLevel),
                Codec.INT.optionalFieldOf("processing_time", 200).forGetter(CauldronRecipe::getProcessingTime)
            ).apply(instance, CauldronRecipe::new)
        );
        
        private static final StreamCodec<RegistryFriendlyByteBuf, CauldronRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, CauldronRecipe::getInput,
            ItemStack.STREAM_CODEC, CauldronRecipe::getOutput,
            ByteBufCodecs.STRING_UTF8, CauldronRecipe::getPotionType,
            ByteBufCodecs.VAR_INT, CauldronRecipe::getMinDuration,
            ByteBufCodecs.VAR_INT, CauldronRecipe::getMinLevel,
            ByteBufCodecs.VAR_INT, CauldronRecipe::getProcessingTime,
            CauldronRecipe::new
        );
        
        @Override
        public MapCodec<CauldronRecipe> codec() {
            return CODEC;
        }
        
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CauldronRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
