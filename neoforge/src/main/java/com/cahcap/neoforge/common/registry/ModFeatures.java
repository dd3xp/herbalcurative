package com.cahcap.neoforge.common.registry;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.worldgen.RedCherryTreeFeature;
import com.cahcap.common.worldgen.HerbFlowerPatchFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModFeatures {
    
    public static final DeferredRegister<Feature<?>> FEATURES = 
        DeferredRegister.create(Registries.FEATURE, HerbalCurativeCommon.MOD_ID);

    // Tree features
    public static final DeferredHolder<Feature<?>, RedCherryTreeFeature> RED_CHERRY_TREE = 
        FEATURES.register("red_cherry_tree", 
            () -> new RedCherryTreeFeature(NoneFeatureConfiguration.CODEC));
    
    // Herb flower patch features (2-3 per cluster)
    
    // Overworld herbs
    public static final DeferredHolder<Feature<?>, HerbFlowerPatchFeature> SCLERIS_PATCH = 
        FEATURES.register("scleris_patch", 
            () -> HerbFlowerPatchFeature.scleris(NoneFeatureConfiguration.CODEC));
    
    public static final DeferredHolder<Feature<?>, HerbFlowerPatchFeature> SEPHREL_PATCH = 
        FEATURES.register("sephrel_patch", 
            () -> HerbFlowerPatchFeature.sephrel(NoneFeatureConfiguration.CODEC));
    
    public static final DeferredHolder<Feature<?>, HerbFlowerPatchFeature> DORELLA_PATCH = 
        FEATURES.register("dorella_patch", 
            () -> HerbFlowerPatchFeature.dorella(NoneFeatureConfiguration.CODEC));
    
    // Nether herbs
    public static final DeferredHolder<Feature<?>, HerbFlowerPatchFeature> CRYSEL_PATCH = 
        FEATURES.register("crysel_patch", 
            () -> HerbFlowerPatchFeature.crysel(NoneFeatureConfiguration.CODEC));
    
    public static final DeferredHolder<Feature<?>, HerbFlowerPatchFeature> PYRAZE_PATCH = 
        FEATURES.register("pyraze_patch", 
            () -> HerbFlowerPatchFeature.pyraze(NoneFeatureConfiguration.CODEC));
    
    // End herb
    public static final DeferredHolder<Feature<?>, HerbFlowerPatchFeature> STELLIA_PATCH = 
        FEATURES.register("stellia_patch", 
            () -> HerbFlowerPatchFeature.stellia(NoneFeatureConfiguration.CODEC));
}

