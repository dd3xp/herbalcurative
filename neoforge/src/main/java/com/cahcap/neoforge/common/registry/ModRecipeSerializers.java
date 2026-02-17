package com.cahcap.neoforge.common.registry;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.recipe.CauldronBrewingRecipe;
import com.cahcap.common.recipe.CauldronInfusingRecipe;
import com.cahcap.common.recipe.HerbalBlendingRecipe;
import com.cahcap.common.recipe.WorkbenchRecipe;
import com.cahcap.common.registry.ModRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * NeoForge registration for recipe serializers.
 */
public class ModRecipeSerializers {
    
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = 
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, HerbalCurativeCommon.MOD_ID);
    
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<HerbalBlendingRecipe>> HERBAL_BLENDING = 
            RECIPE_SERIALIZERS.register("herbal_blending", () -> HerbalBlendingRecipe.Serializer.INSTANCE);
    
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<WorkbenchRecipe>> WORKBENCH = 
            RECIPE_SERIALIZERS.register("workbench", WorkbenchRecipe.Serializer::new);
    
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CauldronInfusingRecipe>> CAULDRON_INFUSING = 
            RECIPE_SERIALIZERS.register("cauldron_infusing", () -> CauldronInfusingRecipe.Serializer.INSTANCE);
    
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CauldronBrewingRecipe>> CAULDRON_BREWING = 
            RECIPE_SERIALIZERS.register("cauldron_brewing", () -> CauldronBrewingRecipe.Serializer.INSTANCE);
    
    public static void register(IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
    }
    
    /**
     * Initialize common recipe serializer references.
     * Call this after registration.
     */
    public static void initCommonReferences() {
        ModRegistries.WORKBENCH_RECIPE_SERIALIZER = WORKBENCH;
    }
}
