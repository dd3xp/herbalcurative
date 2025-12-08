package com.cahcap.herbalcurative.neoforge.datagen.tags;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.neoforge.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import org.jetbrains.annotations.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Block tags provider
 * Generates minecraft:flowers, leaves, logs, planks, saplings and other tags
 */
public class ModBlockTagsProvider extends net.neoforged.neoforge.common.data.BlockTagsProvider {
    
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, HerbalCurativeCommon.MOD_ID, existingFileHelper);
    }
    
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // ==================== Minecraft vanilla tags ====================
        
        // minecraft:flowers - All herb flowers
        this.tag(BlockTags.FLOWERS).add(
            ModBlocks.VERDSCALE_FERN.get(),
            ModBlocks.DEWPETAL.get(),
            ModBlocks.ZEPHYR_LILY.get(),
            ModBlocks.CRYSTBUD.get(),
            ModBlocks.PYRISAGE.get(),
            ModBlocks.ROSYNIA.get()
        );
        
        // minecraft:leaves - Forest Heartwood leaves
        this.tag(BlockTags.LEAVES).add(
            ModBlocks.FOREST_HEARTWOOD_LEAVES.get()
        );
        
        // minecraft:logs - Forest Heartwood logs
        this.tag(BlockTags.LOGS).add(
            ModBlocks.FOREST_HEARTWOOD_LOG.get()
        );
        
        // minecraft:planks - Forest Heartwood planks
        this.tag(BlockTags.PLANKS).add(
            ModBlocks.FOREST_HEARTWOOD_PLANKS.get()
        );
        
        // minecraft:saplings - Forest Heartwood saplings
        this.tag(BlockTags.SAPLINGS).add(
            ModBlocks.FOREST_HEARTWOOD_SAPLING.get()
        );
        
        // ==================== Mineable tags ====================
        
        // minecraft:mineable/axe - Blocks mineable with axe
        this.tag(BlockTags.MINEABLE_WITH_AXE).add(
            ModBlocks.FOREST_HEARTWOOD_LOG.get(),
            ModBlocks.FOREST_HEARTWOOD_PLANKS.get(),
            ModBlocks.FOREST_HEARTWOOD_LEAVES.get()
        );
        
        // minecraft:mineable/hoe - Blocks mineable with hoe (all herbs and crops)
        this.tag(BlockTags.MINEABLE_WITH_HOE).add(
            // Herb flowers
            ModBlocks.VERDSCALE_FERN.get(),
            ModBlocks.DEWPETAL.get(),
            ModBlocks.ZEPHYR_LILY.get(),
            ModBlocks.CRYSTBUD.get(),
            ModBlocks.PYRISAGE.get(),
            ModBlocks.ROSYNIA.get(),
            // Herb crops
            ModBlocks.VERDSCALE_FERN_CROP.get(),
            ModBlocks.DEWPETAL_CROP.get(),
            ModBlocks.ZEPHYR_LILY_CROP.get(),
            ModBlocks.CRYSTBUD_CROP.get(),
            ModBlocks.PYRISAGE_CROP.get(),
            ModBlocks.ROSYNIA_CROP.get(),
            // Forest Berry Bush
            ModBlocks.FOREST_BERRY_BUSH.get()
        );
    }
}
