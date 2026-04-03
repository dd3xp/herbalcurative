package com.cahcap.neoforge.common.registry;

import com.cahcap.common.registry.ModRegistries;

/**
 * Initializes the common registry references with NeoForge implementations.
 * This must be called during mod initialization before any common code tries to access registries.
 */
public class RegistryInit {
    
    public static void init() {
        // Blocks
        ModRegistries.SCLERIS = () -> ModBlocks.SCLERIS.get();
        ModRegistries.DORELLA = () -> ModBlocks.DORELLA.get();
        ModRegistries.SEPHREL = () -> ModBlocks.SEPHREL.get();
        ModRegistries.CRYSEL = () -> ModBlocks.CRYSEL.get();
        ModRegistries.PYRAZE = () -> ModBlocks.PYRAZE.get();
        ModRegistries.STELLIA = () -> ModBlocks.STELLIA.get();
        
        ModRegistries.SCLERIS_CROP = () -> ModBlocks.SCLERIS_CROP.get();
        ModRegistries.DORELLA_CROP = () -> ModBlocks.DORELLA_CROP.get();
        ModRegistries.SEPHREL_CROP = () -> ModBlocks.SEPHREL_CROP.get();
        ModRegistries.CRYSEL_CROP = () -> ModBlocks.CRYSEL_CROP.get();
        ModRegistries.PYRAZE_CROP = () -> ModBlocks.PYRAZE_CROP.get();
        ModRegistries.STELLIA_CROP = () -> ModBlocks.STELLIA_CROP.get();
        
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
        ModRegistries.HERB_POT = () -> ModBlocks.HERB_POT.get();
        ModRegistries.INCENSE_BURNER = () -> ModBlocks.INCENSE_BURNER.get();
        ModRegistries.KILN = () -> ModBlocks.KILN.get();
        ModRegistries.HERB_VAULT = () -> ModBlocks.HERB_VAULT.get();
        ModRegistries.OBELISK = () -> ModBlocks.OBELISK.get();
        ModRegistries.RED_CHERRY_FENCE = () -> ModBlocks.RED_CHERRY_FENCE.get();
        ModRegistries.LUMISTONE = () -> ModBlocks.LUMISTONE.get();
        ModRegistries.LUMISTONE_BRICKS = () -> ModBlocks.LUMISTONE_BRICKS.get();
        ModRegistries.LUMISTONE_SLAB = () -> ModBlocks.LUMISTONE_SLAB.get();
        ModRegistries.LUMISTONE_BRICK_SLAB = () -> ModBlocks.LUMISTONE_BRICK_SLAB.get();
        ModRegistries.LUMISTONE_BRICK_WALL = () -> ModBlocks.LUMISTONE_BRICK_WALL.get();
        ModRegistries.RUNE_STONE_BRICKS = () -> ModBlocks.RUNE_STONE_BRICKS.get();
        ModRegistries.MAGIC_ALLOY_BLOCK = () -> ModBlocks.MAGIC_ALLOY_BLOCK.get();
        
        // Items
        ModRegistries.SCALEPLATE = () -> ModItems.SCALEPLATE.get();
        ModRegistries.DEWPETAL = () -> ModItems.DEWPETAL.get();
        ModRegistries.ZEPHYR_BLOSSOM = () -> ModItems.ZEPHYR_BLOSSOM.get();
        ModRegistries.CRYST_SPINE = () -> ModItems.CRYST_SPINE.get();
        ModRegistries.PYRO_NODE = () -> ModItems.PYRO_NODE.get();
        ModRegistries.STELLAR_MOTE = () -> ModItems.STELLAR_MOTE.get();
        
        ModRegistries.SCLERIS_SEED = () -> ModItems.SCLERIS_SEED.get();
        ModRegistries.DORELLA_SEED = () -> ModItems.DORELLA_SEED.get();
        ModRegistries.SEPHREL_SEED = () -> ModItems.SEPHREL_SEED.get();
        ModRegistries.CRYSEL_SEED = () -> ModItems.CRYSEL_SEED.get();
        ModRegistries.PYRAZE_SEED = () -> ModItems.PYRAZE_SEED.get();
        ModRegistries.STELLIA_SEED = () -> ModItems.STELLIA_SEED.get();
        
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
        ModRegistries.KILN_ITEM = () -> ModItems.KILN.get();
        ModRegistries.HERB_VAULT_ITEM = () -> ModItems.HERB_VAULT.get();
        ModRegistries.INCENSE_BURNER_ITEM = () -> ModItems.INCENSE_BURNER.get();
        ModRegistries.OBELISK_ITEM = () -> ModItems.OBELISK.get();
        ModRegistries.WITHER_SKELETON_POWDER = () -> ModItems.WITHER_SKELETON_POWDER.get();
        
        // Armor Materials
        ModRegistries.LEAFWEAVE_ARMOR_MATERIAL = () -> ModArmorMaterials.LEAFWEAVE;
        
        // Block Entities
        ModRegistries.HERB_CABINET_BE = () -> ModBlockEntities.HERB_CABINET.get();
        ModRegistries.HERB_BASKET_BE = () -> ModBlockEntities.HERB_BASKET.get();
        ModRegistries.RED_CHERRY_SHELF_BE = () -> ModBlockEntities.RED_CHERRY_SHELF.get();
        ModRegistries.WORKBENCH_BE = () -> ModBlockEntities.WORKBENCH.get();
        ModRegistries.CAULDRON_BE = () -> ModBlockEntities.CAULDRON.get();
        ModRegistries.HERB_POT_BE = () -> ModBlockEntities.HERB_POT.get();
        ModRegistries.INCENSE_BURNER_BE = () -> ModBlockEntities.INCENSE_BURNER.get();
        ModRegistries.KILN_BE = () -> ModBlockEntities.KILN.get();
        ModRegistries.OBELISK_BE = () -> ModBlockEntities.OBELISK.get();
        ModRegistries.HERB_VAULT_BE = () -> ModBlockEntities.HERB_VAULT.get();

        // Entity Types
        ModRegistries.FLOWWEAVE_PROJECTILE_TYPE = () -> ModEntityTypes.FLOWWEAVE_PROJECTILE.get();
        
        // Recipe Types and Serializers
        ModRecipeTypes.initCommonReferences();
        ModRecipeSerializers.initCommonReferences();
    }
}

