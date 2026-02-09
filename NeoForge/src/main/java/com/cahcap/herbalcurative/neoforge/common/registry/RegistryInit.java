package com.cahcap.herbalcurative.neoforge.common.registry;

import com.cahcap.herbalcurative.common.registry.ModRegistries;

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
        ModRegistries.HERB_BASKET = () -> ModBlocks.HERB_BASKET.get();
        ModRegistries.RED_CHERRY_SHELF = () -> ModBlocks.RED_CHERRY_SHELF.get();
        ModRegistries.WORKBENCH = () -> ModBlocks.WORKBENCH.get();
        ModRegistries.CAULDRON = () -> ModBlocks.CAULDRON.get();
        ModRegistries.LUMISTONE = () -> ModBlocks.LUMISTONE.get();
        ModRegistries.LUMISTONE_BRICKS = () -> ModBlocks.LUMISTONE_BRICKS.get();
        ModRegistries.LUMISTONE_BRICK_SLAB = () -> ModBlocks.LUMISTONE_BRICK_SLAB.get();
        
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
        
        ModRegistries.LEAFWEAVE_HELMET = () -> ModItems.LEAFWEAVE_HELMET.get();
        ModRegistries.LEAFWEAVE_CHESTPLATE = () -> ModItems.LEAFWEAVE_CHESTPLATE.get();
        ModRegistries.LEAFWEAVE_LEGGINGS = () -> ModItems.LEAFWEAVE_LEGGINGS.get();
        ModRegistries.LEAFWEAVE_BOOTS = () -> ModItems.LEAFWEAVE_BOOTS.get();
        
        ModRegistries.LUMISTONE_SWORD = () -> ModItems.LUMISTONE_SWORD.get();
        ModRegistries.LUMISTONE_PICKAXE = () -> ModItems.LUMISTONE_PICKAXE.get();
        ModRegistries.LUMISTONE_AXE = () -> ModItems.LUMISTONE_AXE.get();
        ModRegistries.LUMISTONE_SHOVEL = () -> ModItems.LUMISTONE_SHOVEL.get();
        ModRegistries.LUMISTONE_HOE = () -> ModItems.LUMISTONE_HOE.get();
        ModRegistries.RED_CHERRY_CROSSBOW = () -> ModItems.RED_CHERRY_CROSSBOW.get();
        ModRegistries.RED_CHERRY_BOLT_MAGAZINE = () -> ModItems.RED_CHERRY_BOLT_MAGAZINE.get();
        
        ModRegistries.FLOWWEAVE_RING = () -> ModItems.FLOWWEAVE_RING.get();
        ModRegistries.HERB_BOX = () -> ModItems.HERB_BOX.get();
        ModRegistries.HERB_CABINET_ITEM = () -> ModItems.HERB_CABINET.get();
        ModRegistries.HERB_BASKET_ITEM = () -> ModItems.HERB_BASKET.get();
        ModRegistries.RED_CHERRY_SHELF_ITEM = () -> ModItems.RED_CHERRY_SHELF.get();
        ModRegistries.WORKBENCH_ITEM = () -> ModItems.WORKBENCH.get();
        
        ModRegistries.CUTTING_KNIFE = () -> ModItems.CUTTING_KNIFE.get();
        ModRegistries.FEATHER_QUILL = () -> ModItems.FEATHER_QUILL.get();
        ModRegistries.WOVEN_ROPE = () -> ModItems.WOVEN_ROPE.get();
        ModRegistries.FORGE_HAMMER = () -> ModItems.FORGE_HAMMER.get();
        ModRegistries.POT = () -> ModItems.POT.get();
        ModRegistries.CAULDRON_ITEM = () -> ModItems.CAULDRON.get();
        
        // Armor Materials
        ModRegistries.LEAFWEAVE_ARMOR_MATERIAL = () -> ModArmorMaterials.LEAFWEAVE;
        
        // Block Entities
        ModRegistries.HERB_CABINET_BE = () -> ModBlockEntities.HERB_CABINET.get();
        ModRegistries.HERB_BASKET_BE = () -> ModBlockEntities.HERB_BASKET.get();
        ModRegistries.RED_CHERRY_SHELF_BE = () -> ModBlockEntities.RED_CHERRY_SHELF.get();
        ModRegistries.WORKBENCH_BE = () -> ModBlockEntities.WORKBENCH.get();
        ModRegistries.CAULDRON_BE = () -> ModBlockEntities.CAULDRON.get();
        
        // Recipe Types and Serializers
        ModRecipeTypes.initCommonReferences();
        ModRecipeSerializers.initCommonReferences();
    }
}

