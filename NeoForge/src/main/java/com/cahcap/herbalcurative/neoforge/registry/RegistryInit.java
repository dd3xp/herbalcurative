package com.cahcap.herbalcurative.neoforge.registry;

import com.cahcap.herbalcurative.registry.ModRegistries;

/**
 * Initializes the common registry references with NeoForge implementations.
 * This must be called during mod initialization before any common code tries to access registries.
 */
public class RegistryInit {
    
    public static void init() {
        // Blocks
        ModRegistries.VERDSCALE_FERN = () -> ModBlocks.VERDSCALE_FERN.get();
        ModRegistries.DEWPETAL = () -> ModBlocks.DEWPETAL.get();
        ModRegistries.ZEPHYR_LILY = () -> ModBlocks.ZEPHYR_LILY.get();
        ModRegistries.CRYSTBUD = () -> ModBlocks.CRYSTBUD.get();
        ModRegistries.PYRISAGE = () -> ModBlocks.PYRISAGE.get();
        ModRegistries.ROSYNIA = () -> ModBlocks.ROSYNIA.get();
        
        ModRegistries.VERDSCALE_FERN_CROP = () -> ModBlocks.VERDSCALE_FERN_CROP.get();
        ModRegistries.DEWPETAL_CROP = () -> ModBlocks.DEWPETAL_CROP.get();
        ModRegistries.ZEPHYR_LILY_CROP = () -> ModBlocks.ZEPHYR_LILY_CROP.get();
        ModRegistries.CRYSTBUD_CROP = () -> ModBlocks.CRYSTBUD_CROP.get();
        ModRegistries.PYRISAGE_CROP = () -> ModBlocks.PYRISAGE_CROP.get();
        ModRegistries.ROSYNIA_CROP = () -> ModBlocks.ROSYNIA_CROP.get();
        
        ModRegistries.RED_CHERRY_LOG = () -> ModBlocks.RED_CHERRY_LOG.get();
        ModRegistries.RED_CHERRY_PLANKS = () -> ModBlocks.RED_CHERRY_PLANKS.get();
        ModRegistries.RED_CHERRY_LEAVES = () -> ModBlocks.RED_CHERRY_LEAVES.get();
        ModRegistries.RED_CHERRY_SAPLING = () -> ModBlocks.RED_CHERRY_SAPLING.get();
        ModRegistries.RED_CHERRY_BUSH = () -> ModBlocks.RED_CHERRY_BUSH.get();
        ModRegistries.HERB_CABINET = () -> ModBlocks.HERB_CABINET.get();
        
        // Items
        ModRegistries.SCALEPLATE = () -> ModItems.SCALEPLATE.get();
        ModRegistries.DEWPETAL_SHARD = () -> ModItems.DEWPETAL_SHARD.get();
        ModRegistries.GOLDEN_LILYBELL = () -> ModItems.GOLDEN_LILYBELL.get();
        ModRegistries.CRYST_SPINE = () -> ModItems.CRYST_SPINE.get();
        ModRegistries.BURNT_NODE = () -> ModItems.BURNT_NODE.get();
        ModRegistries.HEART_OF_STARDREAM = () -> ModItems.HEART_OF_STARDREAM.get();
        
        ModRegistries.VERDSCALE_FERN_SEED = () -> ModItems.VERDSCALE_FERN_SEED.get();
        ModRegistries.DEWPETAL_SEED = () -> ModItems.DEWPETAL_SEED.get();
        ModRegistries.ZEPHYR_LILY_SEED = () -> ModItems.ZEPHYR_LILY_SEED.get();
        ModRegistries.CRYSTBUD_SEED = () -> ModItems.CRYSTBUD_SEED.get();
        ModRegistries.PYRISAGE_SEED = () -> ModItems.PYRISAGE_SEED.get();
        ModRegistries.ROSYNIA_SEED = () -> ModItems.ROSYNIA_SEED.get();
        
        ModRegistries.RED_CHERRY_STICK = () -> ModItems.RED_CHERRY_STICK.get();
        ModRegistries.RED_CHERRY = () -> ModItems.RED_CHERRY.get();
        
        ModRegistries.WEAVELEAF_HELMET = () -> ModItems.WEAVELEAF_HELMET.get();
        ModRegistries.WEAVELEAF_CHESTPLATE = () -> ModItems.WEAVELEAF_CHESTPLATE.get();
        ModRegistries.WEAVELEAF_LEGGINGS = () -> ModItems.WEAVELEAF_LEGGINGS.get();
        ModRegistries.WEAVELEAF_BOOTS = () -> ModItems.WEAVELEAF_BOOTS.get();
        
        ModRegistries.THORNMARK_SWORD = () -> ModItems.THORNMARK_SWORD.get();
        ModRegistries.THORNMARK_PICKAXE = () -> ModItems.THORNMARK_PICKAXE.get();
        ModRegistries.THORNMARK_AXE = () -> ModItems.THORNMARK_AXE.get();
        ModRegistries.THORNMARK_SHOVEL = () -> ModItems.THORNMARK_SHOVEL.get();
        ModRegistries.THORNMARK_HOE = () -> ModItems.THORNMARK_HOE.get();
        ModRegistries.THORNMARK_CROSSBOW = () -> ModItems.THORNMARK_CROSSBOW.get();
        ModRegistries.THORNMARK_BOLT_MAGAZINE = () -> ModItems.THORNMARK_BOLT_MAGAZINE.get();
        
        ModRegistries.WEAVEFLOW_LOOP = () -> ModItems.WEAVEFLOW_LOOP.get();
        ModRegistries.HERB_BOX = () -> ModItems.HERB_BOX.get();
        ModRegistries.HERB_CABINET_ITEM = () -> ModItems.HERB_CABINET.get();
        
        // Armor Materials
        ModRegistries.WEAVELEAF_ARMOR_MATERIAL = () -> ModArmorMaterials.WEAVELEAF;
        
        // Block Entities
        ModRegistries.HERB_CABINET_BE = () -> ModBlockEntities.HERB_CABINET.get();
    }
}

