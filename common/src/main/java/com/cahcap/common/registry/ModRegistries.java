package com.cahcap.common.registry;

import com.cahcap.common.entity.FlowweaveProjectile;
import com.cahcap.common.recipe.CauldronBrewingRecipe;
import com.cahcap.common.recipe.CauldronInfusingRecipe;
import com.cahcap.common.recipe.HerbalBlendingRecipe;
import com.cahcap.common.recipe.HerbPotGrowingRecipe;
import com.cahcap.common.recipe.IncenseBurningRecipe;
import com.cahcap.common.recipe.KilnCatalystRecipe;
import com.cahcap.common.recipe.KilnSmeltingRecipe;
import com.cahcap.common.recipe.WorkbenchRecipe;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
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
    public static Supplier<Block> HERB_BASKET;
    public static Supplier<Block> RED_CHERRY_SHELF;
    public static Supplier<Block> WORKBENCH;
    public static Supplier<Block> CAULDRON;
    public static Supplier<Block> HERB_POT;
    public static Supplier<Block> INCENSE_BURNER;
    public static Supplier<Block> KILN;
    public static Supplier<Block> LUMISTONE;
    public static Supplier<Block> LUMISTONE_BRICKS;
    public static Supplier<Block> LUMISTONE_BRICK_SLAB;
    public static Supplier<Block> RUNE_STONE_BRICKS;
    
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
    
    public static Supplier<Item> LEAFWEAVE_HELMET;
    public static Supplier<Item> LEAFWEAVE_CHESTPLATE;
    public static Supplier<Item> LEAFWEAVE_LEGGINGS;
    public static Supplier<Item> LEAFWEAVE_BOOTS;
    
    public static Supplier<Item> LUMISTONE_SWORD;
    public static Supplier<Item> LUMISTONE_PICKAXE;
    public static Supplier<Item> LUMISTONE_AXE;
    public static Supplier<Item> LUMISTONE_SHOVEL;
    public static Supplier<Item> LUMISTONE_HOE;
    public static Supplier<Item> RED_CHERRY_CROSSBOW;
    public static Supplier<Item> RED_CHERRY_BOLT_MAGAZINE;
    
    public static Supplier<Item> FLOWWEAVE_RING;
    public static Supplier<Item> HERB_BOX;
    public static Supplier<Item> HERB_CABINET_ITEM;
    public static Supplier<Item> HERB_BASKET_ITEM;
    public static Supplier<Item> RED_CHERRY_SHELF_ITEM;
    public static Supplier<Item> WORKBENCH_ITEM;
    
    public static Supplier<Item> CUTTING_KNIFE;
    public static Supplier<Item> FEATHER_QUILL;
    public static Supplier<Item> WOVEN_ROPE;
    public static Supplier<Item> FORGE_HAMMER;
    public static Supplier<Item> POT;
    public static Supplier<Item> CAULDRON_ITEM;
    public static Supplier<Item> KILN_ITEM;
    public static Supplier<Item> INCENSE_BURNER_ITEM;
    public static Supplier<Item> WITHER_SKELETON_POWDER;
    
    // ==================== Armor Materials ====================
    public static Supplier<Holder<ArmorMaterial>> LEAFWEAVE_ARMOR_MATERIAL;
    
    // ==================== Block Entities ====================
    public static Supplier<BlockEntityType<?>> HERB_CABINET_BE;
    public static Supplier<BlockEntityType<?>> HERB_BASKET_BE;
    public static Supplier<BlockEntityType<?>> RED_CHERRY_SHELF_BE;
    public static Supplier<BlockEntityType<?>> WORKBENCH_BE;
    public static Supplier<BlockEntityType<?>> CAULDRON_BE;
    public static Supplier<BlockEntityType<?>> HERB_POT_BE;
    public static Supplier<BlockEntityType<?>> INCENSE_BURNER_BE;
    public static Supplier<BlockEntityType<?>> KILN_BE;

    // ==================== Recipe Types ====================
    public static Supplier<RecipeType<HerbalBlendingRecipe>> HERBAL_BLENDING_RECIPE_TYPE;
    public static Supplier<RecipeType<WorkbenchRecipe>> WORKBENCH_RECIPE_TYPE;
    public static Supplier<RecipeSerializer<WorkbenchRecipe>> WORKBENCH_RECIPE_SERIALIZER;
    public static Supplier<RecipeType<CauldronInfusingRecipe>> CAULDRON_INFUSING_RECIPE_TYPE;
    public static Supplier<RecipeType<CauldronBrewingRecipe>> CAULDRON_BREWING_RECIPE_TYPE;
    public static Supplier<RecipeType<HerbPotGrowingRecipe>> HERB_POT_GROWING_RECIPE_TYPE;
    public static Supplier<RecipeType<IncenseBurningRecipe>> INCENSE_BURNING_RECIPE_TYPE;
    public static Supplier<RecipeSerializer<IncenseBurningRecipe>> INCENSE_BURNING_SERIALIZER;
    public static Supplier<RecipeType<KilnSmeltingRecipe>> KILN_SMELTING_RECIPE_TYPE;
    public static Supplier<RecipeType<KilnCatalystRecipe>> KILN_CATALYST_RECIPE_TYPE;

    // ==================== Entity Types ====================
    public static Supplier<EntityType<FlowweaveProjectile>> FLOWWEAVE_PROJECTILE_TYPE;
}

