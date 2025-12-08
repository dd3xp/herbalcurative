package com.cahcap.herbalcurative.neoforge.registry;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.item.*;
import com.cahcap.herbalcurative.neoforge.item.WeaveleafArmorItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HerbalCurativeCommon.MOD_ID);

    // ==================== Herb Products ====================
    
    public static final DeferredItem<Item> SCALEPLATE = ITEMS.registerSimpleItem("scaleplate");
    public static final DeferredItem<Item> DEWPETAL_SHARD = ITEMS.registerSimpleItem("dewpetal_shard");
    public static final DeferredItem<Item> GOLDEN_LILYBELL = ITEMS.registerSimpleItem("golden_lilybell");
    public static final DeferredItem<Item> CRYST_SPINE = ITEMS.registerSimpleItem("cryst_spine");
    public static final DeferredItem<Item> BURNT_NODE = ITEMS.registerSimpleItem("burnt_node");
    public static final DeferredItem<Item> HEART_OF_STARDREAM = ITEMS.registerSimpleItem("heart_of_stardream");

    // ==================== Herb Seeds ====================
    
    public static final DeferredItem<Item> VERDSCALE_FERN_SEED = ITEMS.register("verdscale_fern_seed",
            () -> new HerbSeedItem(ModBlocks.VERDSCALE_FERN_CROP.get(), new Item.Properties()));
    public static final DeferredItem<Item> DEWPETAL_SEED = ITEMS.register("dewpetal_seed",
            () -> new HerbSeedItem(ModBlocks.DEWPETAL_CROP.get(), new Item.Properties()));
    public static final DeferredItem<Item> ZEPHYR_LILY_SEED = ITEMS.register("zephyr_lily_seed",
            () -> new HerbSeedItem(ModBlocks.ZEPHYR_LILY_CROP.get(), new Item.Properties()));
    public static final DeferredItem<Item> CRYSTBUD_SEED = ITEMS.register("crystbud_seed",
            () -> new HerbSeedItem(ModBlocks.CRYSTBUD_CROP.get(), new Item.Properties()));
    public static final DeferredItem<Item> PYRISAGE_SEED = ITEMS.register("pyrisage_seed",
            () -> new HerbSeedItem(ModBlocks.PYRISAGE_CROP.get(), new Item.Properties()));
    public static final DeferredItem<Item> ROSYNIA_SEED = ITEMS.register("rosynia_seed",
            () -> new HerbSeedItem(ModBlocks.ROSYNIA_CROP.get(), new Item.Properties()));

    // ==================== Forest Heartwood Items ====================
    
    public static final DeferredItem<Item> FOREST_HEARTWOOD_STICK = ITEMS.registerSimpleItem("forest_heartwood_stick");
    
    public static final DeferredItem<Item> FOREST_BERRY = ITEMS.register("forest_berry",
            () -> new ForestBerryItem(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(4)
                            .saturationModifier(1.0F)
                            .fast()
                            .build())));

    // ==================== Weaveleaf Armor ====================
    
    public static final DeferredItem<ArmorItem> WEAVELEAF_HELMET = ITEMS.register("weaveleaf_helmet",
            () -> new WeaveleafArmorItem(ModArmorMaterials.WEAVELEAF, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(92)));
    
    public static final DeferredItem<ArmorItem> WEAVELEAF_CHESTPLATE = ITEMS.register("weaveleaf_chestplate",
            () -> new WeaveleafArmorItem(ModArmorMaterials.WEAVELEAF, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(95)));
    
    public static final DeferredItem<ArmorItem> WEAVELEAF_LEGGINGS = ITEMS.register("weaveleaf_leggings",
            () -> new WeaveleafArmorItem(ModArmorMaterials.WEAVELEAF, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(98)));
    
    public static final DeferredItem<ArmorItem> WEAVELEAF_BOOTS = ITEMS.register("weaveleaf_boots",
            () -> new WeaveleafArmorItem(ModArmorMaterials.WEAVELEAF, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(90)));

    // ==================== Thornmark Tools ====================
    
    public static final DeferredItem<SwordItem> THORNMARK_SWORD = ITEMS.register("thornmark_sword",
            () -> new ThornmarkSwordItem(new Item.Properties()
                    .durability(80)
                    .attributes(SwordItem.createAttributes(Tiers.IRON, 3, -2.4F))));
    
    public static final DeferredItem<PickaxeItem> THORNMARK_PICKAXE = ITEMS.register("thornmark_pickaxe",
            () -> new ThornmarkPickaxeItem(new Item.Properties()
                    .durability(80)
                    .attributes(PickaxeItem.createAttributes(Tiers.IRON, 1.0F, -2.8F))));
    
    public static final DeferredItem<AxeItem> THORNMARK_AXE = ITEMS.register("thornmark_axe",
            () -> new ThornmarkAxeItem(new Item.Properties()
                    .durability(80)
                    .attributes(AxeItem.createAttributes(Tiers.IRON, 6.0F, -3.1F))));
    
    public static final DeferredItem<ShovelItem> THORNMARK_SHOVEL = ITEMS.register("thornmark_shovel",
            () -> new ThornmarkShovelItem(new Item.Properties()
                    .durability(80)
                    .attributes(ShovelItem.createAttributes(Tiers.IRON, 1.5F, -3.0F))));
    
    public static final DeferredItem<HoeItem> THORNMARK_HOE = ITEMS.register("thornmark_hoe",
            () -> new ThornmarkHoeItem(new Item.Properties()
                    .durability(80)
                    .attributes(HoeItem.createAttributes(Tiers.IRON, -2.0F, -1.0F))));

    // ==================== Thornmark Crossbow ====================
    
    public static final DeferredItem<ThornmarkCrossbowItem> THORNMARK_CROSSBOW = ITEMS.register("thornmark_crossbow",
            () -> new ThornmarkCrossbowItem(new Item.Properties().durability(80)));
    
    public static final DeferredItem<ThornmarkBoltMagazineItem> THORNMARK_BOLT_MAGAZINE = ITEMS.register("thornmark_bolt_magazine",
            () -> new ThornmarkBoltMagazineItem(new Item.Properties().durability(80).stacksTo(1)));

    // ==================== Special Items ====================
    
    public static final DeferredItem<WeaveflowLoopItem> WEAVEFLOW_LOOP = ITEMS.register("weaveflow_loop",
            () -> new WeaveflowLoopItem(new Item.Properties()
                    .stacksTo(1)
                    .attributes(SwordItem.createAttributes(Tiers.IRON, 5, -2.4F))));
    
    public static final DeferredItem<HerbBoxItem> HERB_BOX = ITEMS.register("herb_box",
            () -> new HerbBoxItem(new Item.Properties().stacksTo(1)));

    // ==================== Multiblock Structures ====================
    
    public static final DeferredItem<BlockItem> HERB_CABINET = ITEMS.register("herb_cabinet",
            () -> new BlockItem(ModBlocks.HERB_CABINET.get(), new Item.Properties()));
}

