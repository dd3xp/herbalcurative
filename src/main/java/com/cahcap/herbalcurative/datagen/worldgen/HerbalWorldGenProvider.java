package com.cahcap.herbalcurative.datagen.worldgen;

import com.cahcap.herbalcurative.HerbalCurative;
import com.cahcap.herbalcurative.registry.ModFeatures;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
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
public class HerbalWorldGenProvider {
    
    // ==================== Resource Keys ====================
    
    // Configured Features
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRYSTBUD_CONFIGURED = 
        createConfiguredFeatureKey("crystbud");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DEWPETAL_CONFIGURED = 
        createConfiguredFeatureKey("dewpetal");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FOREST_HEARTWOOD_TREE_CONFIGURED = 
        createConfiguredFeatureKey("forest_heartwood_tree");
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
    public static final ResourceKey<PlacedFeature> FOREST_HEARTWOOD_TREE_PLACED = 
        createPlacedFeatureKey("forest_heartwood_tree_placed");
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
    public static final ResourceKey<BiomeModifier> FOREST_HEARTWOOD_OVERWORLD = 
        createBiomeModifierKey("overworld/forest_heartwood_tree");
    
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
    public static final TagKey<Biome> HAS_FOREST_HEARTWOOD_TREES = 
        createBiomeTag("has_forest_heartwood_trees");
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
            ResourceLocation.fromNamespaceAndPath(HerbalCurative.MODID, name));
    }
    
    private static ResourceKey<PlacedFeature> createPlacedFeatureKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, 
            ResourceLocation.fromNamespaceAndPath(HerbalCurative.MODID, name));
    }
    
    private static ResourceKey<BiomeModifier> createBiomeModifierKey(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, 
            ResourceLocation.fromNamespaceAndPath(HerbalCurative.MODID, name));
    }
    
    private static TagKey<Biome> createBiomeTag(String name) {
        return TagKey.create(Registries.BIOME, 
            ResourceLocation.fromNamespaceAndPath(HerbalCurative.MODID, name));
    }
    
    // ==================== Data Provider Registration ====================
    
    public static void addProviders(DataGenerator generator, PackOutput output, 
                                   CompletableFuture<HolderLookup.Provider> lookupProvider,
                                   ExistingFileHelper existingFileHelper) {
        RegistrySetBuilder registryBuilder = new RegistrySetBuilder();
        registryBuilder.add(Registries.CONFIGURED_FEATURE, HerbalWorldGenProvider::bootstrapConfiguredFeatures);
        registryBuilder.add(Registries.PLACED_FEATURE, HerbalWorldGenProvider::bootstrapPlacedFeatures);
        registryBuilder.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, HerbalWorldGenProvider::bootstrapBiomeModifiers);
        
        generator.addProvider(true, new DatapackBuiltinEntriesProvider(
            output, lookupProvider, registryBuilder, Set.of(HerbalCurative.MODID)
        ));
        
        // Biome tags
        generator.addProvider(true, new HerbalBiomeTagsProvider(output, lookupProvider, existingFileHelper));
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
        
        // Forest Heartwood Tree (Overworld)
        context.register(FOREST_HEARTWOOD_TREE_CONFIGURED, new ConfiguredFeature<>(
            ModFeatures.FOREST_HEARTWOOD_TREE.get(),
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
        
        // Common placement modifiers (rarity, distribution, height, biome filter)
        List<PlacementModifier> herbPlacement = List.of(
            RarityFilter.onAverageOnceEvery(16),  // Try once every 16 chunks
            InSquarePlacement.spread(),             // Random position in chunk
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE, // Surface height
            BiomeFilter.biome()                     // Biome filter
        );
        
        // Tree placement modifiers (rarer)
        List<PlacementModifier> treePlacement = List.of(
            RarityFilter.onAverageOnceEvery(32),  // Try once every 32 chunks (trees are rarer)
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
            BiomeFilter.biome()
        );
        
        // Overworld herbs
        context.register(DEWPETAL_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(DEWPETAL_CONFIGURED), herbPlacement));
        
        context.register(VERDSCALE_FERN_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(VERDSCALE_FERN_CONFIGURED), herbPlacement));
        
        context.register(ZEPHYR_LILY_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(ZEPHYR_LILY_CONFIGURED), herbPlacement));
        
        // Nether herbs
        context.register(CRYSTBUD_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(CRYSTBUD_CONFIGURED), herbPlacement));
        
        context.register(PYRISAGE_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(PYRISAGE_CONFIGURED), herbPlacement));
        
        // End herbs
        context.register(ROSYNIA_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(ROSYNIA_CONFIGURED), herbPlacement));
        
        // Trees
        context.register(FOREST_HEARTWOOD_TREE_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(FOREST_HEARTWOOD_TREE_CONFIGURED), treePlacement));
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
        
        // Forest Heartwood Trees - Add to overworld forest biomes
        context.register(FOREST_HEARTWOOD_OVERWORLD, new BiomeModifiers.AddFeaturesBiomeModifier(
            biomes.getOrThrow(HAS_FOREST_HEARTWOOD_TREES),
            HolderSet.direct(placedFeatures.getOrThrow(FOREST_HEARTWOOD_TREE_PLACED)),
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

