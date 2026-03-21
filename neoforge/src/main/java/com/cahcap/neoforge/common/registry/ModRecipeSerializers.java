package com.cahcap.neoforge.common.registry;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.recipe.CauldronBrewingRecipe;
import com.cahcap.common.recipe.CauldronInfusingRecipe;
import com.cahcap.common.recipe.HerbalBlendingRecipe;
import com.cahcap.common.recipe.HerbPotGrowingRecipe;
import com.cahcap.common.recipe.IncenseBurningRecipe;
import com.cahcap.common.recipe.KilnCatalystRecipe;
import com.cahcap.common.recipe.KilnSmeltingRecipe;
import com.cahcap.common.recipe.ObeliskOfferingRecipe;
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
    
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<HerbPotGrowingRecipe>> HERB_POT_GROWING = 
            RECIPE_SERIALIZERS.register("herb_pot_growing", () -> HerbPotGrowingRecipe.Serializer.INSTANCE);
    
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<IncenseBurningRecipe>> INCENSE_BURNING =
            RECIPE_SERIALIZERS.register("incense_burning", IncenseBurningRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<KilnSmeltingRecipe>> KILN_SMELTING =
            RECIPE_SERIALIZERS.register("kiln_smelting", () -> KilnSmeltingRecipe.Serializer.INSTANCE);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<KilnCatalystRecipe>> KILN_CATALYST =
            RECIPE_SERIALIZERS.register("kiln_catalyst", () -> KilnCatalystRecipe.Serializer.INSTANCE);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ObeliskOfferingRecipe>> OBELISK_OFFERING =
            RECIPE_SERIALIZERS.register("obelisk_offering", () -> ObeliskOfferingRecipe.Serializer.INSTANCE);

    public static void register(IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
    }
    
    /**
     * Initialize common recipe serializer references.
     * Call this after registration.
     */
    public static void initCommonReferences() {
        ModRegistries.WORKBENCH_RECIPE_SERIALIZER = WORKBENCH;
        ModRegistries.INCENSE_BURNING_SERIALIZER = INCENSE_BURNING;
        ModRegistries.OBELISK_OFFERING_SERIALIZER = OBELISK_OFFERING;
    }
}
