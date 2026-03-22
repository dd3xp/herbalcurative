package com.cahcap.neoforge.common.datagen.recipes.builder;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.recipe.IncenseBurningRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for Incense Burning recipes.
 */
public class IncenseBurningRecipeBuilder {
    
    private final ResourceLocation entityType;
    private final List<IncenseBurningRecipe.HerbRequirement> herbs = new ArrayList<>();
    private int burnTime = 160; // Default 8 seconds
    
    private IncenseBurningRecipeBuilder(ResourceLocation entityType) {
        this.entityType = entityType;
    }
    
    public static IncenseBurningRecipeBuilder create(ResourceLocation entityType) {
        return new IncenseBurningRecipeBuilder(entityType);
    }
    
    public static IncenseBurningRecipeBuilder create(String entityType) {
        return new IncenseBurningRecipeBuilder(ResourceLocation.parse(entityType));
    }
    
    public IncenseBurningRecipeBuilder herb(Item herb, int count) {
        ResourceLocation herbId = BuiltInRegistries.ITEM.getKey(herb);
        herbs.add(new IncenseBurningRecipe.HerbRequirement(herbId, count));
        return this;
    }
    
    public IncenseBurningRecipeBuilder burnTime(int ticks) {
        this.burnTime = ticks;
        return this;
    }
    
    public void save(RecipeOutput output, String name) {
        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, 
                "incense_burning/" + name);
        
        IncenseBurningRecipe recipe = new IncenseBurningRecipe(entityType, herbs, burnTime);
        
        output.accept(recipeId, recipe, (AdvancementHolder) null);
    }
}
