package com.cahcap.herbalcurative.registry;

import com.cahcap.herbalcurative.HerbalCurative;
import com.cahcap.herbalcurative.item.ForestBerryItem;
import com.cahcap.herbalcurative.item.HerbBoxItem;
import com.cahcap.herbalcurative.item.HerbSeedItem;
import com.cahcap.herbalcurative.item.ModArmorMaterials;
import com.cahcap.herbalcurative.item.ThornmarkBoltMagazineItem;
import com.cahcap.herbalcurative.item.ThornmarkCrossbowItem;
import com.cahcap.herbalcurative.item.ThornmarkPickaxeItem;
import com.cahcap.herbalcurative.item.ThornmarkAxeItem;
import com.cahcap.herbalcurative.item.ThornmarkShovelItem;
import com.cahcap.herbalcurative.item.ThornmarkSwordItem;
import com.cahcap.herbalcurative.item.ThornmarkHoeItem;
import com.cahcap.herbalcurative.item.WeaveleafArmorItem;
import com.cahcap.herbalcurative.item.WeaveflowLoopItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HerbalCurative.MODID);

    // ==================== Herb Products ====================
    
    // Verdscale Fern product - used for crafting armor and tools with durability regen
    public static final DeferredItem<Item> SCALEPLATE = ITEMS.registerSimpleItem("scaleplate");
    
    // Dewpetal product - used for potion-related items
    public static final DeferredItem<Item> DEWPETAL_SHARD = ITEMS.registerSimpleItem("dewpetal_shard");
    
    // Zephyr Lily product - used for logistics-related items
    public static final DeferredItem<Item> GOLDEN_LILYBELL = ITEMS.registerSimpleItem("golden_lilybell");
    
    // Crystbud product - used for production equipment
    public static final DeferredItem<Item> CRYST_SPINE = ITEMS.registerSimpleItem("cryst_spine");
    
    // Pyrisage product - used for upgrading vanilla equipment
    public static final DeferredItem<Item> BURNT_NODE = ITEMS.registerSimpleItem("burnt_node");
    
    // Rosynia product - used for advanced equipment
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
    
    // Forest Berry - food item that can be planted under leaves
    // Nutrition: 4 hunger (2 shanks), 1.0 saturation, fast eating (16 ticks like apple)
    public static final DeferredItem<Item> FOREST_BERRY = ITEMS.register("forest_berry",
            () -> new ForestBerryItem(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(4)
                            .saturationModifier(1.0F)
                            .fast() // 16 ticks eating time (same as apple)
                            .build())));

    // ==================== Weaveleaf Armor ====================
    // Early game armor with durability regeneration (1 per second)
    // Uses custom model and single texture file
    
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
    // Early game tools with durability regeneration (1 per second)
    // Same stats as iron tools, 80 durability
    
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
    // Crossbow with faster charge time (half of vanilla)
    // 80 durability, prioritizes bolt magazine over arrows
    
    public static final DeferredItem<ThornmarkCrossbowItem> THORNMARK_CROSSBOW = ITEMS.register("thornmark_crossbow",
            () -> new ThornmarkCrossbowItem(new Item.Properties().durability(80)));
    
    public static final DeferredItem<ThornmarkBoltMagazineItem> THORNMARK_BOLT_MAGAZINE = ITEMS.register("thornmark_bolt_magazine",
            () -> new ThornmarkBoltMagazineItem(new Item.Properties().durability(80).stacksTo(1)));

    // ==================== Special Items ====================
    
    public static final DeferredItem<WeaveflowLoopItem> WEAVEFLOW_LOOP = ITEMS.register("weaveflow_loop",
            () -> new WeaveflowLoopItem(new Item.Properties()
                    .stacksTo(1)
                    .attributes(net.minecraft.world.item.SwordItem.createAttributes(
                            Tiers.IRON, 5, -2.4F))));
    
    public static final DeferredItem<HerbBoxItem> HERB_BOX = ITEMS.register("herb_box",
            () -> new HerbBoxItem(new Item.Properties().stacksTo(1)));

    // ==================== Multiblock Structures ====================
    
    // Herb Cabinet BlockItem - hidden from creative (no creative tab set)
    // Only used for multiblock structure icon display in WAILA/Jade/WTHIT
    public static final DeferredItem<net.minecraft.world.item.BlockItem> HERB_CABINET = ITEMS.register("herb_cabinet",
            () -> new net.minecraft.world.item.BlockItem(ModBlocks.HERB_CABINET.get(), new Item.Properties()));

}
