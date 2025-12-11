package com.cahcap.herbalcurative.neoforge.common.datagen;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.neoforge.common.datagen.loot.ModBlockLootProvider;
import com.cahcap.herbalcurative.neoforge.common.datagen.recipes.ModRecipeProvider;
import com.cahcap.herbalcurative.neoforge.common.datagen.tags.ModBlockTagsProvider;
import com.cahcap.herbalcurative.neoforge.common.datagen.tags.ModItemTagsProvider;
import com.cahcap.herbalcurative.neoforge.common.datagen.worldgen.ModWorldGenProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Main entry point for data generation
 * Run 'gradlew runData' to generate all data files to src/generated/resources/
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModDataGenerator {
    
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        net.minecraft.data.DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        
        // Server-side data generation
        if (event.includeServer()) {
            // Loot Tables
            generator.addProvider(true, new ModBlockLootProvider(packOutput, lookupProvider));
            
            // Recipes
            generator.addProvider(true, new ModRecipeProvider(packOutput, lookupProvider));
            
            // Tags
            ModBlockTagsProvider blockTagsProvider = new ModBlockTagsProvider(packOutput, lookupProvider, existingFileHelper);
            generator.addProvider(true, blockTagsProvider);
            generator.addProvider(true, new ModItemTagsProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));
            
            // World Generation + Biome Modifiers
            ModWorldGenProvider.addProviders(generator, packOutput, lookupProvider, existingFileHelper);
        }
        
        HerbalCurativeCommon.LOGGER.info("Herbal Curative data generation setup complete!");
    }
}
