package com.cahcap.herbalcurative.neoforge.common.datagen.worldgen;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.neoforge.common.datagen.tags.ModBiomeTagsProvider;
import com.cahcap.herbalcurative.neoforge.common.registry.ModFeatures;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;

/**
 * World generation data provider
 * Generates:
 * - 7 configured_features
 * - 7 placed_features
 * - 7 biome_modifiers
 * - 7 biome tags
 */
public class ModWorldGenProvider {
    
    // ==================== Resource Keys ====================
    
    // Configured Features
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRYSTBUD_CONFIGURED = 
        createConfiguredFeatureKey("crystbud");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DEWPETAL_CONFIGURED = 
        createConfiguredFeatureKey("dewpetal");
    public static final ResourceKey<ConfiguredFeature<?, ?>> RED_CHERRY_TREE_CONFIGURED = 
        createConfiguredFeatureKey("red_cherry_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PYRISAGE_CONFIGURED = 
        createConfiguredFeatureKey("pyrisage");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ROSYNIA_CONFIGURED = 
        createConfiguredFeatureKey("rosynia");
    public static final ResourceKey<ConfiguredFeature<?, ?>> VERDSCALE_FERN_CONFIGURED = 
        createConfiguredFeatureKey("verdscale_fern");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ZEPHYR_LILY_CONFIGURED = 
        createConfiguredFeatureKey("zephyr_lily");
    
    // Placed Features
    public static final ResourceKey<PlacedFeature> CRYSTBUD_PLACED = 
        createPlacedFeatureKey("crystbud_placed");
    public static final ResourceKey<PlacedFeature> DEWPETAL_PLACED = 
        createPlacedFeatureKey("dewpetal_placed");
    public static final ResourceKey<PlacedFeature> RED_CHERRY_TREE_PLACED = 
        createPlacedFeatureKey("red_cherry_tree_placed");
    public static final ResourceKey<PlacedFeature> PYRISAGE_PLACED = 
        createPlacedFeatureKey("pyrisage_placed");
    public static final ResourceKey<PlacedFeature> ROSYNIA_PLACED = 
        createPlacedFeatureKey("rosynia_placed");
    public static final ResourceKey<PlacedFeature> VERDSCALE_FERN_PLACED = 
        createPlacedFeatureKey("verdscale_fern_placed");
    public static final ResourceKey<PlacedFeature> ZEPHYR_LILY_PLACED = 
        createPlacedFeatureKey("zephyr_lily_placed");
    
    // Biome Modifiers - Organized by dimension
    // Overworld
    public static final ResourceKey<BiomeModifier> DEWPETAL_OVERWORLD = 
        createBiomeModifierKey("overworld/dewpetal");
    public static final ResourceKey<BiomeModifier> VERDSCALE_FERN_OVERWORLD = 
        createBiomeModifierKey("overworld/verdscale_fern");
    public static final ResourceKey<BiomeModifier> ZEPHYR_LILY_OVERWORLD = 
        createBiomeModifierKey("overworld/zephyr_lily");
    public static final ResourceKey<BiomeModifier> RED_CHERRY_TREE_OVERWORLD = 
        createBiomeModifierKey("overworld/red_cherry_tree");
    
    // Nether
    public static final ResourceKey<BiomeModifier> CRYSTBUD_NETHER = 
        createBiomeModifierKey("nether/crystbud");
    public static final ResourceKey<BiomeModifier> PYRISAGE_NETHER = 
        createBiomeModifierKey("nether/pyrisage");
    
    // End
    public static final ResourceKey<BiomeModifier> ROSYNIA_END = 
        createBiomeModifierKey("end/rosynia");
    
    // Biome Tags
    public static final TagKey<Biome> HAS_CRYSTBUD = 
        createBiomeTag("has_crystbud");
    public static final TagKey<Biome> HAS_DEWPETAL = 
        createBiomeTag("has_dewpetal");
    public static final TagKey<Biome> HAS_RED_CHERRY_TREES = 
        createBiomeTag("has_red_cherry_trees");
    public static final TagKey<Biome> HAS_PYRISAGE = 
        createBiomeTag("has_pyrisage");
    public static final TagKey<Biome> HAS_ROSYNIA = 
        createBiomeTag("has_rosynia");
    public static final TagKey<Biome> HAS_VERDSCALE_FERN = 
        createBiomeTag("has_verdscale_fern");
    public static final TagKey<Biome> HAS_ZEPHYR_LILY = 
        createBiomeTag("has_zephyr_lily");
    
    // ==================== Utility Methods ====================
    
    private static ResourceKey<ConfiguredFeature<?, ?>> createConfiguredFeatureKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, 
            ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, name));
    }
    
    private static ResourceKey<PlacedFeature> createPlacedFeatureKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, 
            ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, name));
    }
    
    private static ResourceKey<BiomeModifier> createBiomeModifierKey(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, 
            ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, name));
    }
    
    private static TagKey<Biome> createBiomeTag(String name) {
        return TagKey.create(Registries.BIOME, 
            ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, name));
    }
    
    // ==================== Data Provider Registration ====================
    
    public static void addProviders(net.minecraft.data.DataGenerator generator, PackOutput output, 
                                   CompletableFuture<HolderLookup.Provider> lookupProvider,
                                   ExistingFileHelper existingFileHelper) {
        RegistrySetBuilder registryBuilder = new RegistrySetBuilder();
        registryBuilder.add(Registries.CONFIGURED_FEATURE, ModWorldGenProvider::bootstrapConfiguredFeatures);
        registryBuilder.add(Registries.PLACED_FEATURE, ModWorldGenProvider::bootstrapPlacedFeatures);
        registryBuilder.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModWorldGenProvider::bootstrapBiomeModifiers);
        
        generator.addProvider(true, new DatapackBuiltinEntriesProvider(
            output, lookupProvider, registryBuilder, Set.of(HerbalCurativeCommon.MOD_ID)
        ));
        
        // Biome tags
        generator.addProvider(true, new ModBiomeTagsProvider(output, lookupProvider, existingFileHelper));
    }
    
    // ==================== Configured Features ====================
    
    private static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        // Use our already registered custom Feature types
        
        // Crystbud (Nether)
        context.register(CRYSTBUD_CONFIGURED, new ConfiguredFeature<>(
            ModFeatures.CRYSTBUD_PATCH.get(),
            NoneFeatureConfiguration.INSTANCE
        ));
        
        // Dewpetal (Overworld)
        context.register(DEWPETAL_CONFIGURED, new ConfiguredFeature<>(
            ModFeatures.DEWPETAL_PATCH.get(),
            NoneFeatureConfiguration.INSTANCE
        ));
        
        // Red Cherry Tree (Overworld)
        context.register(RED_CHERRY_TREE_CONFIGURED, new ConfiguredFeature<>(
            ModFeatures.RED_CHERRY_TREE.get(),
            NoneFeatureConfiguration.INSTANCE
        ));
        
        // Pyrisage (Nether)
        context.register(PYRISAGE_CONFIGURED, new ConfiguredFeature<>(
            ModFeatures.PYRISAGE_PATCH.get(),
            NoneFeatureConfiguration.INSTANCE
        ));
        
        // Rosynia (End)
        context.register(ROSYNIA_CONFIGURED, new ConfiguredFeature<>(
            ModFeatures.ROSYNIA_PATCH.get(),
            NoneFeatureConfiguration.INSTANCE
        ));
        
        // Verdscale Fern (Overworld)
        context.register(VERDSCALE_FERN_CONFIGURED, new ConfiguredFeature<>(
            ModFeatures.VERDSCALE_FERN_PATCH.get(),
            NoneFeatureConfiguration.INSTANCE
        ));
        
        // Zephyr Lily (Overworld)
        context.register(ZEPHYR_LILY_CONFIGURED, new ConfiguredFeature<>(
            ModFeatures.ZEPHYR_LILY_PATCH.get(),
            NoneFeatureConfiguration.INSTANCE
        ));
    }
    
    // ==================== Placed Features ====================
    
    private static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        
        // ==================== Individual Herb Placement Configurations ====================
        
        // Dewpetal - Rare (16 chunks, half frequency)
        List<PlacementModifier> dewpetalPlacement = List.of(
            RarityFilter.onAverageOnceEvery(16),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
            BiomeFilter.biome()
        );
        
        // Verdscale Fern - Normal (8 chunks, unchanged)
        List<PlacementModifier> verdscaleFernPlacement = List.of(
            RarityFilter.onAverageOnceEvery(8),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
            BiomeFilter.biome()
        );
        
        // Zephyr Lily - Rare (16 chunks, half frequency)
        List<PlacementModifier> zephyrLilyPlacement = List.of(
            RarityFilter.onAverageOnceEvery(16),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
            BiomeFilter.biome()
        );
        
        // Crystbud - Common (4 chunks, double frequency)
        List<PlacementModifier> crystbudPlacement = List.of(
            RarityFilter.onAverageOnceEvery(4),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
            BiomeFilter.biome()
        );
        
        // Pyrisage - Common (4 chunks, double frequency)
        List<PlacementModifier> pyrisagePlacement = List.of(
            RarityFilter.onAverageOnceEvery(4),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
            BiomeFilter.biome()
        );
        
        // Rosynia - Normal (8 chunks, unchanged)
        List<PlacementModifier> rosyniaPlacement = List.of(
            RarityFilter.onAverageOnceEvery(8),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
            BiomeFilter.biome()
        );
        
        // Tree placement modifiers - try once every 2 chunks
        List<PlacementModifier> treePlacement = List.of(
            RarityFilter.onAverageOnceEvery(2),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
            BiomeFilter.biome()
        );
        
        // ==================== Register Placed Features ====================
        
        // Overworld herbs
        context.register(DEWPETAL_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(DEWPETAL_CONFIGURED), dewpetalPlacement));
        
        context.register(VERDSCALE_FERN_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(VERDSCALE_FERN_CONFIGURED), verdscaleFernPlacement));
        
        context.register(ZEPHYR_LILY_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(ZEPHYR_LILY_CONFIGURED), zephyrLilyPlacement));
        
        // Nether herbs
        context.register(CRYSTBUD_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(CRYSTBUD_CONFIGURED), crystbudPlacement));
        
        context.register(PYRISAGE_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(PYRISAGE_CONFIGURED), pyrisagePlacement));
        
        // End herbs
        context.register(ROSYNIA_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(ROSYNIA_CONFIGURED), rosyniaPlacement));
        
        // Trees
        context.register(RED_CHERRY_TREE_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(RED_CHERRY_TREE_CONFIGURED), treePlacement));
    }
    
    // ==================== Biome Modifiers ====================
    
    private static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        
        // Overworld herbs - Add to overworld biomes
        context.register(DEWPETAL_OVERWORLD, new BiomeModifiers.AddFeaturesBiomeModifier(
            biomes.getOrThrow(HAS_DEWPETAL),
            HolderSet.direct(placedFeatures.getOrThrow(DEWPETAL_PLACED)),
            GenerationStep.Decoration.VEGETAL_DECORATION
        ));
        
        context.register(VERDSCALE_FERN_OVERWORLD, new BiomeModifiers.AddFeaturesBiomeModifier(
            biomes.getOrThrow(HAS_VERDSCALE_FERN),
            HolderSet.direct(placedFeatures.getOrThrow(VERDSCALE_FERN_PLACED)),
            GenerationStep.Decoration.VEGETAL_DECORATION
        ));
        
        context.register(ZEPHYR_LILY_OVERWORLD, new BiomeModifiers.AddFeaturesBiomeModifier(
            biomes.getOrThrow(HAS_ZEPHYR_LILY),
            HolderSet.direct(placedFeatures.getOrThrow(ZEPHYR_LILY_PLACED)),
            GenerationStep.Decoration.VEGETAL_DECORATION
        ));
        
        // Red Cherry Trees - Add to overworld forest biomes
        context.register(RED_CHERRY_TREE_OVERWORLD, new BiomeModifiers.AddFeaturesBiomeModifier(
            biomes.getOrThrow(HAS_RED_CHERRY_TREES),
            HolderSet.direct(placedFeatures.getOrThrow(RED_CHERRY_TREE_PLACED)),
            GenerationStep.Decoration.VEGETAL_DECORATION
        ));
        
        // Nether herbs - Add to nether biomes
        context.register(CRYSTBUD_NETHER, new BiomeModifiers.AddFeaturesBiomeModifier(
            biomes.getOrThrow(HAS_CRYSTBUD),
            HolderSet.direct(placedFeatures.getOrThrow(CRYSTBUD_PLACED)),
            GenerationStep.Decoration.VEGETAL_DECORATION
        ));
        
        context.register(PYRISAGE_NETHER, new BiomeModifiers.AddFeaturesBiomeModifier(
            biomes.getOrThrow(HAS_PYRISAGE),
            HolderSet.direct(placedFeatures.getOrThrow(PYRISAGE_PLACED)),
            GenerationStep.Decoration.VEGETAL_DECORATION
        ));
        
        // End herbs - Add to end biomes
        context.register(ROSYNIA_END, new BiomeModifiers.AddFeaturesBiomeModifier(
            biomes.getOrThrow(HAS_ROSYNIA),
            HolderSet.direct(placedFeatures.getOrThrow(ROSYNIA_PLACED)),
            GenerationStep.Decoration.VEGETAL_DECORATION
        ));
    }
}
