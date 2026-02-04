package com.cahcap.herbalcurative.common.recipe;

import net.minecraft.world.item.crafting.RecipeType;

import java.util.function.Supplier;

/**
 * Common recipe type references.
 * Platform-specific modules should populate these during initialization.
 */
public class ModRecipeTypeHolder {
    
    public static Supplier<RecipeType<HerbalBlendingRecipe>> HERBAL_BLENDING;
}
