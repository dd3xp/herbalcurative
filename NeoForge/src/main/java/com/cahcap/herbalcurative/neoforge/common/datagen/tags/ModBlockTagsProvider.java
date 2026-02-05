package com.cahcap.herbalcurative.neoforge.common.datagen.tags;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.neoforge.common.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import org.jetbrains.annotations.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Block tags provider
 * Generates minecraft:flowers, leaves, logs, planks, saplings and other tags
 */
public class ModBlockTagsProvider extends net.neoforged.neoforge.common.data.BlockTagsProvider {
    
    // Common tags (c: namespace) for cross-mod compatibility
    private static final TagKey<Block> C_STONES = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, 
            ResourceLocation.fromNamespaceAndPath("c", "stones"));
    private static final TagKey<Block> C_STONE_BRICKS = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, 
            ResourceLocation.fromNamespaceAndPath("c", "stone_bricks"));
    
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
        
        // minecraft:leaves - Red Cherry leaves
        this.tag(BlockTags.LEAVES).add(
            ModBlocks.RED_CHERRY_LEAVES.get()
        );
        
        // minecraft:logs - Red Cherry logs (all log variants)
        this.tag(BlockTags.LOGS).add(
            ModBlocks.RED_CHERRY_LOG.get(),
            ModBlocks.STRIPPED_RED_CHERRY_LOG.get(),
            ModBlocks.RED_CHERRY_WOOD.get(),
            ModBlocks.STRIPPED_RED_CHERRY_WOOD.get()
        );
        
        // minecraft:planks - Red Cherry planks
        this.tag(BlockTags.PLANKS).add(
            ModBlocks.RED_CHERRY_PLANKS.get()
        );
        
        // minecraft:saplings - Red Cherry saplings
        this.tag(BlockTags.SAPLINGS).add(
            ModBlocks.RED_CHERRY_SAPLING.get()
        );
        
        // ==================== Mineable tags ====================
        
        // minecraft:mineable/axe - Blocks mineable with axe
        this.tag(BlockTags.MINEABLE_WITH_AXE).add(
            ModBlocks.RED_CHERRY_LOG.get(),
            ModBlocks.STRIPPED_RED_CHERRY_LOG.get(),
            ModBlocks.RED_CHERRY_WOOD.get(),
            ModBlocks.STRIPPED_RED_CHERRY_WOOD.get(),
            ModBlocks.RED_CHERRY_PLANKS.get(),
            ModBlocks.RED_CHERRY_STAIRS.get(),
            ModBlocks.RED_CHERRY_SLAB.get(),
            ModBlocks.RED_CHERRY_LEAVES.get(),
            ModBlocks.HERB_CABINET.get(),
            ModBlocks.HERB_BASKET.get(),
            ModBlocks.RED_CHERRY_SHELF.get(),
            ModBlocks.WORKBENCH.get()
        );
        
        // minecraft:mineable/pickaxe - Blocks mineable with pickaxe
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
            ModBlocks.LUMISTONE.get(),
            ModBlocks.LUMISTONE_BRICKS.get(),
            ModBlocks.RUNE_STONE_BRICKS.get(),
            ModBlocks.LUMISTONE_BRICK_SLAB.get(),
            ModBlocks.LUMISTONE_BRICK_STAIRS.get()
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
            // Red Cherry Bush
            ModBlocks.RED_CHERRY_BUSH.get()
        );
        
        // ==================== Minecraft structure tags ====================
        
        // minecraft:slabs - All slab blocks
        this.tag(BlockTags.SLABS).add(
            ModBlocks.RED_CHERRY_SLAB.get(),
            ModBlocks.LUMISTONE_BRICK_SLAB.get()
        );
        
        // minecraft:stairs - All stair blocks
        this.tag(BlockTags.STAIRS).add(
            ModBlocks.RED_CHERRY_STAIRS.get(),
            ModBlocks.LUMISTONE_BRICK_STAIRS.get()
        );
        
        // minecraft:wooden_slabs - Wooden slab blocks
        this.tag(BlockTags.WOODEN_SLABS).add(
            ModBlocks.RED_CHERRY_SLAB.get()
        );
        
        // minecraft:wooden_stairs - Wooden stair blocks
        this.tag(BlockTags.WOODEN_STAIRS).add(
            ModBlocks.RED_CHERRY_STAIRS.get()
        );
        
        // ==================== Common tags (c: namespace) for cross-mod compatibility ====================
        
        // c:stones - Stone-like blocks (for crafting recipes that accept any stone)
        this.tag(C_STONES).add(
            ModBlocks.LUMISTONE.get()
        );
        
        // c:stone_bricks - Stone brick blocks
        this.tag(C_STONE_BRICKS).add(
            ModBlocks.LUMISTONE_BRICKS.get(),
            ModBlocks.RUNE_STONE_BRICKS.get()
        );
    }
}
