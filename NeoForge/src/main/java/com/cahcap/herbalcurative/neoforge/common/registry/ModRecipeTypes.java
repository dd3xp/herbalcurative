package com.cahcap.herbalcurative.neoforge.common.registry;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.common.recipe.HerbalBlendingRecipe;
import com.cahcap.herbalcurative.common.recipe.ModRecipeTypeHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * NeoForge registration for recipe types.
 */
public class ModRecipeTypes {
    
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = 
            DeferredRegister.create(Registries.RECIPE_TYPE, HerbalCurativeCommon.MOD_ID);
    
    public static final Supplier<RecipeType<HerbalBlendingRecipe>> HERBAL_BLENDING = 
            RECIPE_TYPES.register("herbal_blending", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "herbal_blending").toString();
                }
            });
    
    public static void register(IEventBus modEventBus) {
        RECIPE_TYPES.register(modEventBus);
    }
    
    /**
     * Initialize common recipe type references.
     * Call this after registration.
     */
    public static void initCommonReferences() {
        ModRecipeTypeHolder.HERBAL_BLENDING = HERBAL_BLENDING;
    }
}
