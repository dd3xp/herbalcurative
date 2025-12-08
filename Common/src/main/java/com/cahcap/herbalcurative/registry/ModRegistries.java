package com.cahcap.herbalcurative.registry;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

/**
 * Common registry references for cross-platform access.
 * Platform-specific modules (NeoForge, Fabric) should populate these suppliers during initialization.
 */
public class ModRegistries {
    
    // ==================== Blocks ====================
    public static Supplier<Block> VERDSCALE_FERN;
    public static Supplier<Block> DEWPETAL;
    public static Supplier<Block> ZEPHYR_LILY;
    public static Supplier<Block> CRYSTBUD;
    public static Supplier<Block> PYRISAGE;
    public static Supplier<Block> ROSYNIA;
    
    public static Supplier<Block> VERDSCALE_FERN_CROP;
    public static Supplier<Block> DEWPETAL_CROP;
    public static Supplier<Block> ZEPHYR_LILY_CROP;
    public static Supplier<Block> CRYSTBUD_CROP;
    public static Supplier<Block> PYRISAGE_CROP;
    public static Supplier<Block> ROSYNIA_CROP;
    
    public static Supplier<Block> RED_CHERRY_LOG;
    public static Supplier<Block> RED_CHERRY_PLANKS;
    public static Supplier<Block> RED_CHERRY_LEAVES;
    public static Supplier<Block> RED_CHERRY_SAPLING;
    public static Supplier<Block> RED_CHERRY_BUSH;
    public static Supplier<Block> HERB_CABINET;
    
    // ==================== Items ====================
    public static Supplier<Item> SCALEPLATE;
    public static Supplier<Item> DEWPETAL_SHARD;
    public static Supplier<Item> GOLDEN_LILYBELL;
    public static Supplier<Item> CRYST_SPINE;
    public static Supplier<Item> BURNT_NODE;
    public static Supplier<Item> HEART_OF_STARDREAM;
    
    public static Supplier<Item> VERDSCALE_FERN_SEED;
    public static Supplier<Item> DEWPETAL_SEED;
    public static Supplier<Item> ZEPHYR_LILY_SEED;
    public static Supplier<Item> CRYSTBUD_SEED;
    public static Supplier<Item> PYRISAGE_SEED;
    public static Supplier<Item> ROSYNIA_SEED;
    
    public static Supplier<Item> RED_CHERRY_STICK;
    public static Supplier<Item> RED_CHERRY;
    
    public static Supplier<Item> WEAVELEAF_HELMET;
    public static Supplier<Item> WEAVELEAF_CHESTPLATE;
    public static Supplier<Item> WEAVELEAF_LEGGINGS;
    public static Supplier<Item> WEAVELEAF_BOOTS;
    
    public static Supplier<Item> THORNMARK_SWORD;
    public static Supplier<Item> THORNMARK_PICKAXE;
    public static Supplier<Item> THORNMARK_AXE;
    public static Supplier<Item> THORNMARK_SHOVEL;
    public static Supplier<Item> THORNMARK_HOE;
    public static Supplier<Item> THORNMARK_CROSSBOW;
    public static Supplier<Item> THORNMARK_BOLT_MAGAZINE;
    
    public static Supplier<Item> WEAVEFLOW_LOOP;
    public static Supplier<Item> HERB_BOX;
    public static Supplier<Item> HERB_CABINET_ITEM;
    
    // ==================== Armor Materials ====================
    public static Supplier<Holder<ArmorMaterial>> WEAVELEAF_ARMOR_MATERIAL;
    
    // ==================== Block Entities ====================
    public static Supplier<BlockEntityType<?>> HERB_CABINET_BE;
}

