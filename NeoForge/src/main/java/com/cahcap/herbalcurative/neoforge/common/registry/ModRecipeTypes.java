package com.cahcap.herbalcurative.neoforge.common.registry;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.common.recipe.CauldronBrewingRecipe;
import com.cahcap.herbalcurative.common.recipe.CauldronInfusingRecipe;
import com.cahcap.herbalcurative.common.recipe.HerbalBlendingRecipe;
import com.cahcap.herbalcurative.common.recipe.WorkbenchRecipe;
import com.cahcap.herbalcurative.common.registry.ModRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * NeoForge registration for recipe types.
 */
public class ModRecipeTypes {
    
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = 
            DeferredRegister.create(Registries.RECIPE_TYPE, HerbalCurativeCommon.MOD_ID);
    
    public static final DeferredHolder<RecipeType<?>, RecipeType<HerbalBlendingRecipe>> HERBAL_BLENDING = 
            RECIPE_TYPES.register("herbal_blending", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "herbal_blending").toString();
                }
            });
    
    public static final DeferredHolder<RecipeType<?>, RecipeType<WorkbenchRecipe>> WORKBENCH = 
            RECIPE_TYPES.register("workbench", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "workbench").toString();
                }
            });
    
    public static final DeferredHolder<RecipeType<?>, RecipeType<CauldronInfusingRecipe>> CAULDRON_INFUSING = 
            RECIPE_TYPES.register("cauldron_infusing", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "cauldron_infusing").toString();
                }
            });
    
    public static final DeferredHolder<RecipeType<?>, RecipeType<CauldronBrewingRecipe>> CAULDRON_BREWING = 
            RECIPE_TYPES.register("cauldron_brewing", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "cauldron_brewing").toString();
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
        ModRegistries.HERBAL_BLENDING_RECIPE_TYPE = HERBAL_BLENDING;
        ModRegistries.WORKBENCH_RECIPE_TYPE = WORKBENCH;
        ModRegistries.CAULDRON_INFUSING_RECIPE_TYPE = CAULDRON_INFUSING;
        ModRegistries.CAULDRON_BREWING_RECIPE_TYPE = CAULDRON_BREWING;
    }
}
