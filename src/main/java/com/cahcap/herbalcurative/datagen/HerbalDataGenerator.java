package com.cahcap.herbalcurative.datagen;

import com.cahcap.herbalcurative.HerbalCurative;
import com.cahcap.herbalcurative.datagen.loot.HerbalBlockLootProvider;
import com.cahcap.herbalcurative.datagen.recipes.HerbalRecipeProvider;
import com.cahcap.herbalcurative.datagen.tags.HerbalBlockTagsProvider;
import com.cahcap.herbalcurative.datagen.tags.HerbalItemTagsProvider;
import com.cahcap.herbalcurative.datagen.worldgen.HerbalWorldGenProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Main entry point for data generation
 * Run 'gradlew runData' to generate all data files to src/generated/resources/
 */
@EventBusSubscriber(modid = HerbalCurative.MODID, bus = EventBusSubscriber.Bus.MOD)
public class HerbalDataGenerator {
    
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        
        // Server-side data generation
        if (event.includeServer()) {
            // Loot Tables
            generator.addProvider(true, new HerbalBlockLootProvider(packOutput, lookupProvider));
            
            // Recipes
            generator.addProvider(true, new HerbalRecipeProvider(packOutput, lookupProvider));
            
            // Tags
            BlockTagsProvider blockTagsProvider = new HerbalBlockTagsProvider(packOutput, lookupProvider, existingFileHelper);
            generator.addProvider(true, blockTagsProvider);
            generator.addProvider(true, new HerbalItemTagsProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));
            
            // World Generation + Biome Modifiers
            HerbalWorldGenProvider.addProviders(generator, packOutput, lookupProvider, existingFileHelper);
        }
        
        HerbalCurative.LOGGER.info("Herbal Curative data generation setup complete!");
    }
}

