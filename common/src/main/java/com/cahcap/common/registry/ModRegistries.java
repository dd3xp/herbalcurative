package com.cahcap.common.registry;

import com.cahcap.common.entity.FlowweaveProjectile;
import com.cahcap.common.recipe.CauldronBrewingRecipe;
import com.cahcap.common.recipe.CauldronInfusingRecipe;
import com.cahcap.common.recipe.HerbalBlendingRecipe;
import com.cahcap.common.recipe.HerbPotGrowingRecipe;
import com.cahcap.common.recipe.IncenseBurningRecipe;
import com.cahcap.common.recipe.KilnCatalystRecipe;
import com.cahcap.common.recipe.KilnSmeltingRecipe;
import com.cahcap.common.recipe.ObeliskOfferingRecipe;
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
 * Mod loader modules should populate these suppliers during initialization.
 */
public class ModRegistries {
    
    // ==================== Blocks ====================
    public static Supplier<Block> SCLERIS;
    public static Supplier<Block> DORELLA;
    public static Supplier<Block> SEPHREL;
    public static Supplier<Block> CRYSEL;
    public static Supplier<Block> PYRAZE;
    public static Supplier<Block> STELLIA;
    
    public static Supplier<Block> SCLERIS_CROP;
    public static Supplier<Block> DORELLA_CROP;
    public static Supplier<Block> SEPHREL_CROP;
    public static Supplier<Block> CRYSEL_CROP;
    public static Supplier<Block> PYRAZE_CROP;
    public static Supplier<Block> STELLIA_CROP;
    
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
    public static Supplier<Block> HERB_VAULT;
    public static Supplier<Block> RED_CHERRY_FENCE;
    public static Supplier<Block> LUMISTONE;
    public static Supplier<Block> LUMISTONE_BRICKS;
    public static Supplier<Block> LUMISTONE_SLAB;
    public static Supplier<Block> LUMISTONE_BRICK_SLAB;
    public static Supplier<Block> LUMISTONE_BRICK_WALL;
    public static Supplier<Block> OBELISK;
    public static Supplier<Block> RUNE_STONE_BRICKS;
    public static Supplier<Block> MAGIC_ALLOY_BLOCK;
    
    // ==================== Items ====================
    public static Supplier<Item> SCALEPLATE;
    public static Supplier<Item> DEWPETAL;
    public static Supplier<Item> ZEPHYR_BLOSSOM;
    public static Supplier<Item> CRYST_SPINE;
    public static Supplier<Item> PYRO_NODE;
    public static Supplier<Item> STELLAR_MOTE;
    
    public static Supplier<Item> SCLERIS_SEED;
    public static Supplier<Item> DORELLA_SEED;
    public static Supplier<Item> SEPHREL_SEED;
    public static Supplier<Item> CRYSEL_SEED;
    public static Supplier<Item> PYRAZE_SEED;
    public static Supplier<Item> STELLIA_SEED;
    
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
    public static Supplier<Item> HERB_VAULT_ITEM;
    public static Supplier<Item> INCENSE_BURNER_ITEM;
    public static Supplier<Item> OBELISK_ITEM;
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
    public static Supplier<BlockEntityType<?>> OBELISK_BE;
    public static Supplier<BlockEntityType<?>> HERB_VAULT_BE;

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
    public static Supplier<RecipeType<ObeliskOfferingRecipe>> OBELISK_OFFERING_RECIPE_TYPE;
    public static Supplier<RecipeSerializer<ObeliskOfferingRecipe>> OBELISK_OFFERING_SERIALIZER;

    // ==================== Entity Types ====================
    public static Supplier<EntityType<FlowweaveProjectile>> FLOWWEAVE_PROJECTILE_TYPE;
}

