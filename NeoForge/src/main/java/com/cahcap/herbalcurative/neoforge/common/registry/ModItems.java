package com.cahcap.herbalcurative.neoforge.common.registry;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.common.item.*;
import com.cahcap.herbalcurative.common.item.WorkbenchToolItem;
import com.cahcap.herbalcurative.neoforge.common.item.LeafweaveArmorItem;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
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

    // ==================== Crafting Materials ====================
    
    // Armor materials
    public static final DeferredItem<Item> LEATHER_ARMOR = ITEMS.registerSimpleItem("leather_armor");
    public static final DeferredItem<Item> VELVET_ARMOR = ITEMS.registerSimpleItem("velvet_armor");
    public static final DeferredItem<Item> SILK_ARMOR = ITEMS.registerSimpleItem("silk_armor");
    
    // Magic Alloy materials
    public static final DeferredItem<Item> MAGIC_ALLOY_DUST = ITEMS.registerSimpleItem("magic_alloy_dust");
    public static final DeferredItem<Item> MAGIC_ALLOY_INGOT = ITEMS.registerSimpleItem("magic_alloy_ingot");
    
    // Brilliant Gem materials
    public static final DeferredItem<Item> BRILLIANT_GEM_DUST = ITEMS.registerSimpleItem("brilliant_gem_dust");
    public static final DeferredItem<Item> BRILLIANT_GEM = ITEMS.registerSimpleItem("brilliant_gem");

    // ==================== Red Cherry Items ====================
    
    public static final DeferredItem<Item> RED_CHERRY_STICK = ITEMS.registerSimpleItem("red_cherry_stick");
    
    public static final DeferredItem<Item> RED_CHERRY = ITEMS.register("red_cherry",
            () -> new RedCherryItem(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(4)
                            .saturationModifier(1.0F)
                            .fast()
                            .build())));

    // ==================== Leafweave Armor ====================
    
    public static final DeferredItem<ArmorItem> LEAFWEAVE_HELMET = ITEMS.register("leafweave_helmet",
            () -> new LeafweaveArmorItem(ModArmorMaterials.LEAFWEAVE, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(92)));
    
    public static final DeferredItem<ArmorItem> LEAFWEAVE_CHESTPLATE = ITEMS.register("leafweave_chestplate",
            () -> new LeafweaveArmorItem(ModArmorMaterials.LEAFWEAVE, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(95)));
    
    public static final DeferredItem<ArmorItem> LEAFWEAVE_LEGGINGS = ITEMS.register("leafweave_leggings",
            () -> new LeafweaveArmorItem(ModArmorMaterials.LEAFWEAVE, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(98)));
    
    public static final DeferredItem<ArmorItem> LEAFWEAVE_BOOTS = ITEMS.register("leafweave_boots",
            () -> new LeafweaveArmorItem(ModArmorMaterials.LEAFWEAVE, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(90)));

    // ==================== Lumistone Tools ====================
    
    public static final DeferredItem<SwordItem> LUMISTONE_SWORD = ITEMS.register("lumistone_sword",
            () -> new LumistoneSwordItem(new Item.Properties()
                    .durability(80)
                    .attributes(SwordItem.createAttributes(Tiers.IRON, 3, -2.4F))));
    
    public static final DeferredItem<PickaxeItem> LUMISTONE_PICKAXE = ITEMS.register("lumistone_pickaxe",
            () -> new LumistonePickaxeItem(new Item.Properties()
                    .durability(80)
                    .attributes(PickaxeItem.createAttributes(Tiers.IRON, 1.0F, -2.8F))));
    
    public static final DeferredItem<AxeItem> LUMISTONE_AXE = ITEMS.register("lumistone_axe",
            () -> new LumistoneAxeItem(new Item.Properties()
                    .durability(80)
                    .attributes(AxeItem.createAttributes(Tiers.IRON, 6.0F, -3.1F))));
    
    public static final DeferredItem<ShovelItem> LUMISTONE_SHOVEL = ITEMS.register("lumistone_shovel",
            () -> new LumistoneShovelItem(new Item.Properties()
                    .durability(80)
                    .attributes(ShovelItem.createAttributes(Tiers.IRON, 1.5F, -3.0F))));
    
    public static final DeferredItem<HoeItem> LUMISTONE_HOE = ITEMS.register("lumistone_hoe",
            () -> new LumistoneHoeItem(new Item.Properties()
                    .durability(80)
                    .attributes(HoeItem.createAttributes(Tiers.IRON, -2.0F, -1.0F))));

    // ==================== Red Cherry Crossbow ====================
    
    public static final DeferredItem<RedCherryCrossbowItem> RED_CHERRY_CROSSBOW = ITEMS.register("red_cherry_crossbow",
            () -> new RedCherryCrossbowItem(new Item.Properties().durability(80)));
    
    public static final DeferredItem<RedCherryBoltMagazineItem> RED_CHERRY_BOLT_MAGAZINE = ITEMS.register("red_cherry_bolt_magazine",
            () -> new RedCherryBoltMagazineItem(new Item.Properties().durability(10).stacksTo(1)));

    // ==================== Special Items ====================
    
    public static final DeferredItem<FlowweaveRingItem> FLOWWEAVE_RING = ITEMS.register("flowweave_ring",
            () -> new FlowweaveRingItem(new Item.Properties()
                    .stacksTo(1)
                    .setNoRepair() // Cannot be repaired, like a magical tool
                    .attributes(ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_DAMAGE,
                                    new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 5.0, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ATTACK_SPEED,
                                    new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.MAINHAND)
                            .build())));
    
    public static final DeferredItem<HerbBoxItem> HERB_BOX = ITEMS.register("herb_box",
            () -> new HerbBoxItem(new Item.Properties().stacksTo(1)));
    
    public static final DeferredItem<PotItem> POT = ITEMS.register("pot",
            () -> new PotItem(new Item.Properties().stacksTo(1)));

    // ==================== Multiblock Structures ====================
    
    public static final DeferredItem<BlockItem> HERB_CABINET = ITEMS.register("herb_cabinet",
            () -> new BlockItem(ModBlocks.HERB_CABINET.get(), new Item.Properties()));
    
    public static final DeferredItem<BlockItem> HERB_BASKET = ITEMS.register("herb_basket",
            () -> new BlockItem(ModBlocks.HERB_BASKET.get(), new Item.Properties()));
    
    public static final DeferredItem<BlockItem> RED_CHERRY_SHELF = ITEMS.register("red_cherry_shelf",
            () -> new BlockItem(ModBlocks.RED_CHERRY_SHELF.get(), new Item.Properties()));
    
    public static final DeferredItem<BlockItem> WORKBENCH = ITEMS.register("workbench",
            () -> new BlockItem(ModBlocks.WORKBENCH.get(), new Item.Properties()));
    
    // Cauldron display item (for JADE/WTHIT, not placeable)
    public static final DeferredItem<Item> CAULDRON = ITEMS.registerSimpleItem("cauldron");
    
    // ==================== Workbench Tools ====================
    // Repair materials: Cutting Knife + Forge Hammer = Iron Ingot, Feather Quill = Ink Sac, Rope = String
    
    public static final DeferredItem<WorkbenchToolItem> CUTTING_KNIFE = ITEMS.register("cutting_knife",
            () -> new WorkbenchToolItem(new Item.Properties().durability(256).stacksTo(1), () -> Items.IRON_INGOT));
    
    public static final DeferredItem<WorkbenchToolItem> FEATHER_QUILL = ITEMS.register("feather_quill",
            () -> new WorkbenchToolItem(new Item.Properties().durability(256).stacksTo(1), () -> Items.INK_SAC));
    
    public static final DeferredItem<WorkbenchToolItem> ROPE = ITEMS.register("rope",
            () -> new WorkbenchToolItem(new Item.Properties().durability(256).stacksTo(1), () -> Items.STRING));
    
    public static final DeferredItem<WorkbenchToolItem> FORGE_HAMMER = ITEMS.register("forge_hammer",
            () -> new WorkbenchToolItem(new Item.Properties().durability(256).stacksTo(1), () -> Items.IRON_INGOT));
}

