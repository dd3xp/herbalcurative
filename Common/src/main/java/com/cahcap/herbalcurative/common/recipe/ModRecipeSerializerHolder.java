package com.cahcap.herbalcurative.common.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

/**
 * Common recipe serializer references.
 * Platform-specific modules should populate these during initialization.
 */
public class ModRecipeSerializerHolder {
    
    public static Supplier<RecipeSerializer<HerbalBlendingRecipe>> HERBAL_BLENDING;
}
