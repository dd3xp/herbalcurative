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
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Recipe for brewing potions in the Cauldron.
 * 
 * Materials (items added to water) determine what potion type is produced.
 * Herbs added during brewing determine duration and amplifier.
 * 
 * Example: Glistering Melon Slice -> Healing Potion
 */
public class CauldronBrewingRecipe implements Recipe<RecipeInput> {
    
    private final List<Ingredient> materials;
    private final String effectId;    // MobEffect registry name (e.g., "minecraft:instant_health")
    private final int baseColor;      // Potion color
    
    public CauldronBrewingRecipe(List<Ingredient> materials, String effectId, int baseColor) {
        this.materials = materials;
        this.effectId = effectId;
        this.baseColor = baseColor;
    }
    
    @Override
    public boolean matches(RecipeInput input, Level level) {
        // Check if the input contains all required materials
        // This is a simple check - all materials must be present
        if (input.size() < materials.size()) {
            return false;
        }
        
        // Create a copy of materials to track what we've matched
        List<Ingredient> remaining = new ArrayList<>(materials);
        
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            
            // Try to match this stack with a remaining ingredient
            boolean matched = false;
            for (int j = 0; j < remaining.size(); j++) {
                if (remaining.get(j).test(stack)) {
                    remaining.remove(j);
                    matched = true;
                    break;
                }
            }
            
            // If we found an item that doesn't match any ingredient, recipe fails
            if (!matched) {
                return false;
            }
        }
        
        // All required ingredients must be matched
        return remaining.isEmpty();
    }
    
    /**
     * Check if the given list of ItemStacks matches this recipe.
     * Takes into account item counts - e.g., 4 Nether Wart can be 
     * one stack of 4 or four stacks of 1.
     */
    public boolean matchesMaterials(List<ItemStack> inputMaterials) {
        // Count total items in input
        int totalInputCount = 0;
        for (ItemStack stack : inputMaterials) {
            if (!stack.isEmpty()) {
                totalInputCount += stack.getCount();
            }
        }
        
        // Recipe requires exactly materials.size() items total
        if (totalInputCount != materials.size()) {
            return false;
        }
        
        // Track how many of each ingredient we still need
        // Use a list to track remaining needed count for each unique ingredient pattern
        List<IngredientCount> remaining = new ArrayList<>();
        for (Ingredient ingredient : materials) {
            // Try to find existing entry for same ingredient
            boolean found = false;
            for (IngredientCount ic : remaining) {
                if (ingredientsEqual(ic.ingredient, ingredient)) {
                    ic.count++;
                    found = true;
                    break;
                }
            }
            if (!found) {
                remaining.add(new IngredientCount(ingredient, 1));
            }
        }
        
        // Match input stacks against remaining ingredients
        for (ItemStack stack : inputMaterials) {
            if (stack.isEmpty()) continue;
            
            int stackCount = stack.getCount();
            boolean matched = false;
            
            for (IngredientCount ic : remaining) {
                if (ic.count > 0 && ic.ingredient.test(stack)) {
                    int toConsume = Math.min(stackCount, ic.count);
                    ic.count -= toConsume;
                    stackCount -= toConsume;
                    matched = true;
                    if (stackCount <= 0) break;
                }
            }
            
            // If we couldn't match all items in this stack, fail
            if (stackCount > 0) {
                return false;
            }
        }
        
        // Check all ingredients are fully matched
        for (IngredientCount ic : remaining) {
            if (ic.count > 0) {
                return false;
            }
        }
        
        return true;
    }
    
    private static class IngredientCount {
        Ingredient ingredient;
        int count;
        
        IngredientCount(Ingredient ingredient, int count) {
            this.ingredient = ingredient;
            this.count = count;
        }
    }
    
    private boolean ingredientsEqual(Ingredient a, Ingredient b) {
        // Simple comparison - check if they accept the same items
        ItemStack[] itemsA = a.getItems();
        ItemStack[] itemsB = b.getItems();
        if (itemsA.length != itemsB.length) return false;
        for (int i = 0; i < itemsA.length; i++) {
            if (!ItemStack.isSameItem(itemsA[i], itemsB[i])) return false;
        }
        return true;
    }
    
    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY; // Brewing doesn't produce an item directly
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
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.addAll(materials);
        return list;
    }
    
    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }
    
    @Override
    public RecipeType<?> getType() {
        return com.cahcap.herbalcurative.common.registry.ModRegistries.CAULDRON_BREWING_RECIPE_TYPE.get();
    }
    
    // Getters
    public List<Ingredient> getMaterials() { return materials; }
    public String getEffectId() { return effectId; }
    public int getBaseColor() { return baseColor; }
    
    /**
     * Get the MobEffect from the effect ID
     */
    public MobEffect getEffect() {
        ResourceLocation id = ResourceLocation.tryParse(effectId);
        if (id == null) return null;
        return BuiltInRegistries.MOB_EFFECT.get(id);
    }
    
    // Recipe Type
    public static class Type implements RecipeType<CauldronBrewingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "cauldron_brewing");
    }
    
    // Recipe Serializer
    public static class Serializer implements RecipeSerializer<CauldronBrewingRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "cauldron_brewing");
        
        private static final MapCodec<CauldronBrewingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("materials").forGetter(CauldronBrewingRecipe::getMaterials),
                Codec.STRING.fieldOf("effect").forGetter(CauldronBrewingRecipe::getEffectId),
                Codec.INT.optionalFieldOf("color", 0x3F76E4).forGetter(CauldronBrewingRecipe::getBaseColor)
            ).apply(instance, CauldronBrewingRecipe::new)
        );
        
        private static final StreamCodec<RegistryFriendlyByteBuf, CauldronBrewingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), CauldronBrewingRecipe::getMaterials,
            ByteBufCodecs.STRING_UTF8, CauldronBrewingRecipe::getEffectId,
            ByteBufCodecs.VAR_INT, CauldronBrewingRecipe::getBaseColor,
            CauldronBrewingRecipe::new
        );
        
        @Override
        public MapCodec<CauldronBrewingRecipe> codec() {
            return CODEC;
        }
        
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CauldronBrewingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
