package com.cahcap.herbalcurative.neoforge.common.registry;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.common.recipe.HerbalBlendingRecipe;
import com.cahcap.herbalcurative.common.recipe.HerbalBlendingRecipeSerializer;
import com.cahcap.herbalcurative.common.recipe.ModRecipeSerializerHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * NeoForge registration for recipe serializers.
 */
public class ModRecipeSerializers {
    
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = 
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, HerbalCurativeCommon.MOD_ID);
    
    public static final Supplier<RecipeSerializer<HerbalBlendingRecipe>> HERBAL_BLENDING = 
            RECIPE_SERIALIZERS.register("herbal_blending", () -> HerbalBlendingRecipeSerializer.INSTANCE);
    
    public static void register(IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
    }
    
    /**
     * Initialize common recipe serializer references.
     * Call this after registration.
     */
    public static void initCommonReferences() {
        ModRecipeSerializerHolder.HERBAL_BLENDING = HERBAL_BLENDING;
    }
}
