package com.cahcap.herbalcurative.neoforge.common.datagen.tags;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.neoforge.common.registry.ModItems;
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
    
    // Mod-specific tags
    public static final TagKey<Item> HERB_PRODUCTS = ItemTags.create(
            ResourceLocation.fromNamespaceAndPath("herbalcurative", "herb_products"));
    
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
        
        // minecraft:slabs - Auto-copy from block tags
        copy(BlockTags.SLABS, ItemTags.SLABS);
        
        // minecraft:stairs - Auto-copy from block tags
        copy(BlockTags.STAIRS, ItemTags.STAIRS);
        
        // ==================== Custom item tags ====================
        
        // Add forest heartwood stick to c:rods/wooden tag (allows use in vanilla recipes)
        tag(STICKS).add(ModItems.RED_CHERRY_STICK.get());
        
        // herbalcurative:herb_products - All herb products (scaleplate, dewpetal_shard, etc.)
        // Used in recipes that accept any herb product (e.g. crafting Lumistone)
        tag(HERB_PRODUCTS).add(
            ModItems.SCALEPLATE.get(),
            ModItems.DEWPETAL_SHARD.get(),
            ModItems.GOLDEN_LILYBELL.get(),
            ModItems.CRYST_SPINE.get(),
            ModItems.BURNT_NODE.get(),
            ModItems.HEART_OF_STARDREAM.get()
        );
        
        // ==================== Minecraft tool/armor tags (required for enchanting) ====================
        
        // Lumistone Tools - Add to minecraft tool tags so they can be enchanted
        tag(ItemTags.SWORDS).add(ModItems.LUMISTONE_SWORD.get());
        tag(ItemTags.PICKAXES).add(ModItems.LUMISTONE_PICKAXE.get());
        tag(ItemTags.AXES).add(ModItems.LUMISTONE_AXE.get());
        tag(ItemTags.SHOVELS).add(ModItems.LUMISTONE_SHOVEL.get());
        tag(ItemTags.HOES).add(ModItems.LUMISTONE_HOE.get());
        
        // Leafweave Armor - Add to minecraft armor tags so they can be enchanted
        tag(ItemTags.HEAD_ARMOR).add(ModItems.LEAFWEAVE_HELMET.get());
        tag(ItemTags.CHEST_ARMOR).add(ModItems.LEAFWEAVE_CHESTPLATE.get());
        tag(ItemTags.LEG_ARMOR).add(ModItems.LEAFWEAVE_LEGGINGS.get());
        tag(ItemTags.FOOT_ARMOR).add(ModItems.LEAFWEAVE_BOOTS.get());
        
        // Red Cherry Crossbow - Add to enchantable tag
        tag(ItemTags.CROSSBOW_ENCHANTABLE).add(ModItems.RED_CHERRY_CROSSBOW.get());
        
        // Red Cherry Bolt Magazine - Add to enchantable/durability tag (generic enchantable items)
        tag(ItemTags.DURABILITY_ENCHANTABLE).add(ModItems.RED_CHERRY_BOLT_MAGAZINE.get());
    }
}
