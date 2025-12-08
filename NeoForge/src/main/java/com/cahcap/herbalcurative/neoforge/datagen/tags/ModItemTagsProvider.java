package com.cahcap.herbalcurative.neoforge.datagen.tags;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.neoforge.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import org.jetbrains.annotations.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Item tags provider
 * Most tags are copied from block tags
 */
public class ModItemTagsProvider extends net.minecraft.data.tags.ItemTagsProvider {
    
    // Common tag for sticks (allows mod sticks to be used in vanilla recipes)
    private static final TagKey<Item> STICKS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "rods/wooden"));
    
    public ModItemTagsProvider(
            PackOutput output, 
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            CompletableFuture<TagLookup<Block>> blockTagsProvider,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTagsProvider, HerbalCurativeCommon.MOD_ID, existingFileHelper);
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
        
        // ==================== Custom item tags ====================
        
        // Add forest heartwood stick to c:rods/wooden tag (allows use in vanilla recipes)
        tag(STICKS).add(ModItems.RED_CHERRY_STICK.get());
    }
}
