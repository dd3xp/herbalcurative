package com.cahcap.herbalcurative.datagen.tags;

import com.cahcap.herbalcurative.HerbalCurative;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Item tags provider
 * Most tags are copied from block tags
 */
public class HerbalItemTagsProvider extends ItemTagsProvider {
    
    public HerbalItemTagsProvider(
            PackOutput output, 
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            CompletableFuture<TagLookup<Block>> blockTagsProvider,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTagsProvider, HerbalCurative.MODID, existingFileHelper);
    }
    
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // ==================== Copy from block tags to item tags ====================
        
        // minecraft:flowers - Auto-copy from block tags
        copy(BlockTags.FLOWERS, ItemTags.FLOWERS);
        
        // minecraft:leaves - Auto-copy from block tags
        copy(BlockTags.LEAVES, ItemTags.LEAVES);
        
        // minecraft:logs - Auto-copy from block tags
        copy(BlockTags.LOGS, ItemTags.LOGS);
        
        // minecraft:planks - Auto-copy from block tags
        copy(BlockTags.PLANKS, ItemTags.PLANKS);
        
        // minecraft:saplings - Auto-copy from block tags
        copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);
    }
}

