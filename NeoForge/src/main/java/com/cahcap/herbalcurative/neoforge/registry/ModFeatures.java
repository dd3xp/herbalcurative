package com.cahcap.herbalcurative.neoforge.registry;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.worldgen.ForestHeartwoodTreeFeature;
import com.cahcap.herbalcurative.worldgen.HerbFlowerPatchFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModFeatures {
    
    public static final DeferredRegister<Feature<?>> FEATURES = 
        DeferredRegister.create(Registries.FEATURE, HerbalCurativeCommon.MOD_ID);

    // Tree features
    public static final DeferredHolder<Feature<?>, ForestHeartwoodTreeFeature> FOREST_HEARTWOOD_TREE = 
        FEATURES.register("forest_heartwood_tree", 
            () -> new ForestHeartwoodTreeFeature(NoneFeatureConfiguration.CODEC));
    
    // Herb flower patch features (2-3 per cluster)
    
    // Overworld herbs
    public static final DeferredHolder<Feature<?>, HerbFlowerPatchFeature> VERDSCALE_FERN_PATCH = 
        FEATURES.register("verdscale_fern_patch", 
            () -> HerbFlowerPatchFeature.verdscaleFern(NoneFeatureConfiguration.CODEC));
    
    public static final DeferredHolder<Feature<?>, HerbFlowerPatchFeature> ZEPHYR_LILY_PATCH = 
        FEATURES.register("zephyr_lily_patch", 
            () -> HerbFlowerPatchFeature.zephyrLily(NoneFeatureConfiguration.CODEC));
    
    public static final DeferredHolder<Feature<?>, HerbFlowerPatchFeature> DEWPETAL_PATCH = 
        FEATURES.register("dewpetal_patch", 
            () -> HerbFlowerPatchFeature.dewpetal(NoneFeatureConfiguration.CODEC));
    
    // Nether herbs
    public static final DeferredHolder<Feature<?>, HerbFlowerPatchFeature> CRYSTBUD_PATCH = 
        FEATURES.register("crystbud_patch", 
            () -> HerbFlowerPatchFeature.crystbud(NoneFeatureConfiguration.CODEC));
    
    public static final DeferredHolder<Feature<?>, HerbFlowerPatchFeature> PYRISAGE_PATCH = 
        FEATURES.register("pyrisage_patch", 
            () -> HerbFlowerPatchFeature.pyrisage(NoneFeatureConfiguration.CODEC));
    
    // End herb
    public static final DeferredHolder<Feature<?>, HerbFlowerPatchFeature> ROSYNIA_PATCH = 
        FEATURES.register("rosynia_patch", 
            () -> HerbFlowerPatchFeature.rosynia(NoneFeatureConfiguration.CODEC));
}

