package com.cahcap.neoforge.common.datagen.worldgen;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.neoforge.common.datagen.tags.ModBiomeTagsProvider;
import com.cahcap.neoforge.common.registry.ModFeatures;
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
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRYSEL_CONFIGURED = 
        createConfiguredFeatureKey("crysel");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DORELLA_CONFIGURED = 
        createConfiguredFeatureKey("dorella");
    public static final ResourceKey<ConfiguredFeature<?, ?>> RED_CHERRY_TREE_CONFIGURED = 
        createConfiguredFeatureKey("red_cherry_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PYRAZE_CONFIGURED = 
        createConfiguredFeatureKey("pyraze");
    public static final ResourceKey<ConfiguredFeature<?, ?>> STELLIA_CONFIGURED = 
        createConfiguredFeatureKey("stellia");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SCLERIS_CONFIGURED = 
        createConfiguredFeatureKey("scleris");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SEPHREL_CONFIGURED = 
        createConfiguredFeatureKey("sephrel");
    
    // Placed Features
    public static final ResourceKey<PlacedFeature> CRYSEL_PLACED = 
        createPlacedFeatureKey("crysel_placed");
    public static final ResourceKey<PlacedFeature> DORELLA_PLACED = 
        createPlacedFeatureKey("dorella_placed");
    public static final ResourceKey<PlacedFeature> RED_CHERRY_TREE_PLACED = 
        createPlacedFeatureKey("red_cherry_tree_placed");
    public static final ResourceKey<PlacedFeature> PYRAZE_PLACED = 
        createPlacedFeatureKey("pyraze_placed");
    public static final ResourceKey<PlacedFeature> STELLIA_PLACED = 
        createPlacedFeatureKey("stellia_placed");
    public static final ResourceKey<PlacedFeature> SCLERIS_PLACED = 
        createPlacedFeatureKey("scleris_placed");
    public static final ResourceKey<PlacedFeature> SEPHREL_PLACED = 
        createPlacedFeatureKey("sephrel_placed");
    
    // Biome Modifiers - Organized by dimension
    // Overworld
    public static final ResourceKey<BiomeModifier> DORELLA_OVERWORLD = 
        createBiomeModifierKey("overworld/dorella");
    public static final ResourceKey<BiomeModifier> SCLERIS_OVERWORLD = 
        createBiomeModifierKey("overworld/scleris");
    public static final ResourceKey<BiomeModifier> SEPHREL_OVERWORLD = 
        createBiomeModifierKey("overworld/sephrel");
    public static final ResourceKey<BiomeModifier> RED_CHERRY_TREE_OVERWORLD = 
        createBiomeModifierKey("overworld/red_cherry_tree");
    
    // Nether
    public static final ResourceKey<BiomeModifier> CRYSEL_NETHER = 
        createBiomeModifierKey("nether/crysel");
    public static final ResourceKey<BiomeModifier> PYRAZE_NETHER = 
        createBiomeModifierKey("nether/pyraze");
    
    // End
    public static final ResourceKey<BiomeModifier> STELLIA_END = 
        createBiomeModifierKey("end/stellia");
    
    // Biome Tags
    public static final TagKey<Biome> HAS_CRYSEL = 
        createBiomeTag("has_crysel");
    public static final TagKey<Biome> HAS_DORELLA = 
        createBiomeTag("has_dorella");
    public static final TagKey<Biome> HAS_RED_CHERRY_TREES = 
        createBiomeTag("has_red_cherry_trees");
    public static final TagKey<Biome> HAS_PYRAZE = 
        createBiomeTag("has_pyraze");
    public static final TagKey<Biome> HAS_STELLIA = 
        createBiomeTag("has_stellia");
    public static final TagKey<Biome> HAS_SCLERIS = 
        createBiomeTag("has_scleris");
    public static final TagKey<Biome> HAS_SEPHREL = 
        createBiomeTag("has_sephrel");
    
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
        
        // Crysel (Nether)
        context.register(CRYSEL_CONFIGURED, new ConfiguredFeature<>(
            ModFeatures.CRYSEL_PATCH.get(),
            NoneFeatureConfiguration.INSTANCE
        ));
        
        // Dorella (Overworld)
        context.register(DORELLA_CONFIGURED, new ConfiguredFeature<>(
            ModFeatures.DORELLA_PATCH.get(),
            NoneFeatureConfiguration.INSTANCE
        ));
        
        // Red Cherry Tree (Overworld)
        context.register(RED_CHERRY_TREE_CONFIGURED, new ConfiguredFeature<>(
            ModFeatures.RED_CHERRY_TREE.get(),
            NoneFeatureConfiguration.INSTANCE
        ));
        
        // Pyraze (Nether)
        context.register(PYRAZE_CONFIGURED, new ConfiguredFeature<>(
            ModFeatures.PYRAZE_PATCH.get(),
            NoneFeatureConfiguration.INSTANCE
        ));
        
        // Stellia (End)
        context.register(STELLIA_CONFIGURED, new ConfiguredFeature<>(
            ModFeatures.STELLIA_PATCH.get(),
            NoneFeatureConfiguration.INSTANCE
        ));
        
        // Scleris (Overworld)
        context.register(SCLERIS_CONFIGURED, new ConfiguredFeature<>(
            ModFeatures.SCLERIS_PATCH.get(),
            NoneFeatureConfiguration.INSTANCE
        ));
        
        // Sephrel (Overworld)
        context.register(SEPHREL_CONFIGURED, new ConfiguredFeature<>(
            ModFeatures.SEPHREL_PATCH.get(),
            NoneFeatureConfiguration.INSTANCE
        ));
    }
    
    // ==================== Placed Features ====================
    
    private static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        
        // ==================== Individual Herb Placement Configurations ====================
        
        // Dorella - Rare (16 chunks, half frequency)
        List<PlacementModifier> dorellaPlacement = List.of(
            RarityFilter.onAverageOnceEvery(16),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
            BiomeFilter.biome()
        );
        
        // Scleris - Normal (8 chunks, unchanged)
        List<PlacementModifier> sclerisPlacement = List.of(
            RarityFilter.onAverageOnceEvery(8),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
            BiomeFilter.biome()
        );
        
        // Sephrel - Rare (16 chunks, half frequency)
        List<PlacementModifier> sephrelPlacement = List.of(
            RarityFilter.onAverageOnceEvery(16),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
            BiomeFilter.biome()
        );
        
        // Crysel - Common (4 chunks, double frequency)
        List<PlacementModifier> cryselPlacement = List.of(
            RarityFilter.onAverageOnceEvery(4),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
            BiomeFilter.biome()
        );
        
        // Pyraze - Common (4 chunks, double frequency)
        List<PlacementModifier> pyrazePlacement = List.of(
            RarityFilter.onAverageOnceEvery(4),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
            BiomeFilter.biome()
        );
        
        // Stellia - Normal (8 chunks, unchanged)
        List<PlacementModifier> stelliaPlacement = List.of(
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
        context.register(DORELLA_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(DORELLA_CONFIGURED), dorellaPlacement));
        
        context.register(SCLERIS_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(SCLERIS_CONFIGURED), sclerisPlacement));
        
        context.register(SEPHREL_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(SEPHREL_CONFIGURED), sephrelPlacement));
        
        // Nether herbs
        context.register(CRYSEL_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(CRYSEL_CONFIGURED), cryselPlacement));
        
        context.register(PYRAZE_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(PYRAZE_CONFIGURED), pyrazePlacement));
        
        // End herbs
        context.register(STELLIA_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(STELLIA_CONFIGURED), stelliaPlacement));
        
        // Trees
        context.register(RED_CHERRY_TREE_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(RED_CHERRY_TREE_CONFIGURED), treePlacement));
    }
    
    // ==================== Biome Modifiers ====================
    
    private static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        
        // Overworld herbs - Add to overworld biomes
        context.register(DORELLA_OVERWORLD, new BiomeModifiers.AddFeaturesBiomeModifier(
            biomes.getOrThrow(HAS_DORELLA),
            HolderSet.direct(placedFeatures.getOrThrow(DORELLA_PLACED)),
            GenerationStep.Decoration.VEGETAL_DECORATION
        ));
        
        context.register(SCLERIS_OVERWORLD, new BiomeModifiers.AddFeaturesBiomeModifier(
            biomes.getOrThrow(HAS_SCLERIS),
            HolderSet.direct(placedFeatures.getOrThrow(SCLERIS_PLACED)),
            GenerationStep.Decoration.VEGETAL_DECORATION
        ));
        
        context.register(SEPHREL_OVERWORLD, new BiomeModifiers.AddFeaturesBiomeModifier(
            biomes.getOrThrow(HAS_SEPHREL),
            HolderSet.direct(placedFeatures.getOrThrow(SEPHREL_PLACED)),
            GenerationStep.Decoration.VEGETAL_DECORATION
        ));
        
        // Red Cherry Trees - Add to overworld forest biomes
        context.register(RED_CHERRY_TREE_OVERWORLD, new BiomeModifiers.AddFeaturesBiomeModifier(
            biomes.getOrThrow(HAS_RED_CHERRY_TREES),
            HolderSet.direct(placedFeatures.getOrThrow(RED_CHERRY_TREE_PLACED)),
            GenerationStep.Decoration.VEGETAL_DECORATION
        ));
        
        // Nether herbs - Add to nether biomes
        context.register(CRYSEL_NETHER, new BiomeModifiers.AddFeaturesBiomeModifier(
            biomes.getOrThrow(HAS_CRYSEL),
            HolderSet.direct(placedFeatures.getOrThrow(CRYSEL_PLACED)),
            GenerationStep.Decoration.VEGETAL_DECORATION
        ));
        
        context.register(PYRAZE_NETHER, new BiomeModifiers.AddFeaturesBiomeModifier(
            biomes.getOrThrow(HAS_PYRAZE),
            HolderSet.direct(placedFeatures.getOrThrow(PYRAZE_PLACED)),
            GenerationStep.Decoration.VEGETAL_DECORATION
        ));
        
        // End herbs - Add to end biomes
        context.register(STELLIA_END, new BiomeModifiers.AddFeaturesBiomeModifier(
            biomes.getOrThrow(HAS_STELLIA),
            HolderSet.direct(placedFeatures.getOrThrow(STELLIA_PLACED)),
            GenerationStep.Decoration.VEGETAL_DECORATION
        ));
    }
}
