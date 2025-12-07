package com.cahcap.herbalcurative.datagen.worldgen;

import com.cahcap.herbalcurative.HerbalCurative;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Biome tags provider
 * Defines which biomes will generate which herbs
 */
public class HerbalBiomeTagsProvider extends BiomeTagsProvider {
    
    public HerbalBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, HerbalCurative.MODID, existingFileHelper);
    }
    
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // ==================== Overworld Herbs ====================
        
        // Dewpetal - Generates in overworld biomes
        this.tag(HerbalWorldGenProvider.HAS_DEWPETAL).add(
            Biomes.PLAINS,
            Biomes.FOREST,
            Biomes.BIRCH_FOREST,
            Biomes.DARK_FOREST,
            Biomes.TAIGA,
            Biomes.SNOWY_TAIGA,
            Biomes.MEADOW,
            Biomes.GROVE,
            Biomes.SNOWY_PLAINS,
            Biomes.ICE_SPIKES,
            Biomes.SNOWY_SLOPES
        );
        
        // Verdscale Fern - Generates in overworld forests and plains
        this.tag(HerbalWorldGenProvider.HAS_VERDSCALE_FERN).add(
            Biomes.PLAINS,
            Biomes.FOREST,
            Biomes.BIRCH_FOREST,
            Biomes.FLOWER_FOREST,
            Biomes.DARK_FOREST,
            Biomes.MEADOW,
            Biomes.SUNFLOWER_PLAINS
        );
        
        // Zephyr Lily - Generates in overworld biomes
        this.tag(HerbalWorldGenProvider.HAS_ZEPHYR_LILY).add(
            Biomes.PLAINS,
            Biomes.FOREST,
            Biomes.BIRCH_FOREST,
            Biomes.DARK_FOREST,
            Biomes.TAIGA,
            Biomes.MEADOW,
            Biomes.SUNFLOWER_PLAINS,
            Biomes.SAVANNA
        );
        
        // Forest Heartwood Trees - Generates in forest biomes
        this.tag(HerbalWorldGenProvider.HAS_FOREST_HEARTWOOD_TREES).add(
            Biomes.FOREST,
            Biomes.BIRCH_FOREST,
            Biomes.DARK_FOREST,
            Biomes.FLOWER_FOREST,
            Biomes.TAIGA
        );
        
        // ==================== Nether Herbs ====================
        
        // Crystbud - Generates in Basalt Deltas and Nether Wastes (stone-like blocks)
        this.tag(HerbalWorldGenProvider.HAS_CRYSTBUD).add(
            Biomes.BASALT_DELTAS,
            Biomes.NETHER_WASTES
        );
        
        // Pyrisage - Generates in Warped Forest, Crimson Forest, Soul Sand Valley (dirt-like blocks)
        this.tag(HerbalWorldGenProvider.HAS_PYRISAGE).add(
            Biomes.WARPED_FOREST,
            Biomes.CRIMSON_FOREST,
            Biomes.SOUL_SAND_VALLEY
        );
        
        // ==================== End Herbs ====================
        
        // Rosynia - Generates in all End biomes
        this.tag(HerbalWorldGenProvider.HAS_ROSYNIA).add(
            Biomes.THE_END,
            Biomes.END_HIGHLANDS,
            Biomes.END_MIDLANDS,
            Biomes.END_BARRENS,
            Biomes.SMALL_END_ISLANDS
        );
    }
}

