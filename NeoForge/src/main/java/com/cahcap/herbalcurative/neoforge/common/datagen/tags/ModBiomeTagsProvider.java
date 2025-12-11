package com.cahcap.herbalcurative.neoforge.common.datagen.tags;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.neoforge.common.datagen.worldgen.ModWorldGenProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import org.jetbrains.annotations.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Biome tags provider
 * Defines which biomes will generate which herbs
 */
public class ModBiomeTagsProvider extends net.minecraft.data.tags.BiomeTagsProvider {
    
    public ModBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, HerbalCurativeCommon.MOD_ID, existingFileHelper);
    }
    
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // ==================== Overworld Herbs ====================
        
        // Dewpetal - Generates in cold/snowy biomes (8 biomes, no overlap)
        this.tag(ModWorldGenProvider.HAS_DEWPETAL).add(
            Biomes.GROVE,
            Biomes.SNOWY_PLAINS,
            Biomes.ICE_SPIKES,
            Biomes.SNOWY_SLOPES,
            Biomes.FROZEN_RIVER,
            Biomes.SNOWY_BEACH,
            Biomes.JAGGED_PEAKS,
            Biomes.FROZEN_PEAKS
        );
        
        // Verdscale Fern - Generates in forest biomes
        this.tag(ModWorldGenProvider.HAS_VERDSCALE_FERN).add(
            Biomes.FOREST,
            Biomes.BIRCH_FOREST,
            Biomes.DARK_FOREST,
            Biomes.FLOWER_FOREST,
            Biomes.OLD_GROWTH_BIRCH_FOREST,
            Biomes.TAIGA,
            Biomes.OLD_GROWTH_PINE_TAIGA,
            Biomes.OLD_GROWTH_SPRUCE_TAIGA,
            Biomes.SNOWY_TAIGA,
            Biomes.WINDSWEPT_FOREST,
            Biomes.CHERRY_GROVE
        );
        
        // Zephyr Lily - Generates in plains biomes
        this.tag(ModWorldGenProvider.HAS_ZEPHYR_LILY).add(
            Biomes.PLAINS,
            Biomes.SUNFLOWER_PLAINS,
            Biomes.MEADOW,
            Biomes.SAVANNA,
            Biomes.SAVANNA_PLATEAU,
            Biomes.WINDSWEPT_SAVANNA,
            Biomes.WINDSWEPT_HILLS,
            Biomes.WINDSWEPT_GRAVELLY_HILLS
        );
        
        // Red Cherry Trees - Generates in all forest biomes
        this.tag(ModWorldGenProvider.HAS_RED_CHERRY_TREES).add(
            Biomes.FOREST,
            Biomes.BIRCH_FOREST,
            Biomes.DARK_FOREST,
            Biomes.FLOWER_FOREST,
            Biomes.OLD_GROWTH_BIRCH_FOREST,
            Biomes.TAIGA,
            Biomes.OLD_GROWTH_PINE_TAIGA,
            Biomes.OLD_GROWTH_SPRUCE_TAIGA,
            Biomes.SNOWY_TAIGA,
            Biomes.WINDSWEPT_FOREST,
            Biomes.CHERRY_GROVE
        );
        
        // ==================== Nether Herbs ====================
        
        // Crystbud - Generates in Basalt Deltas and Nether Wastes (stone-like blocks)
        this.tag(ModWorldGenProvider.HAS_CRYSTBUD).add(
            Biomes.BASALT_DELTAS,
            Biomes.NETHER_WASTES
        );
        
        // Pyrisage - Generates in Warped Forest, Crimson Forest, Soul Sand Valley (dirt-like blocks)
        this.tag(ModWorldGenProvider.HAS_PYRISAGE).add(
            Biomes.WARPED_FOREST,
            Biomes.CRIMSON_FOREST,
            Biomes.SOUL_SAND_VALLEY
        );
        
        // ==================== End Herbs ====================
        
        // Rosynia - Generates in all End biomes
        this.tag(ModWorldGenProvider.HAS_ROSYNIA).add(
            Biomes.THE_END,
            Biomes.END_HIGHLANDS,
            Biomes.END_MIDLANDS,
            Biomes.END_BARRENS,
            Biomes.SMALL_END_ISLANDS
        );
    }
}

