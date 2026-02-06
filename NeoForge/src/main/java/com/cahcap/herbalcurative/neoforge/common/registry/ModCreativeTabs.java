package com.cahcap.herbalcurative.neoforge.common.registry;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    
    public static final DeferredRegister<CreativeModeTab> TABS = 
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HerbalCurativeCommon.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> HERBAL_CURATIVE_TAB = TABS.register("herbalcurative_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + HerbalCurativeCommon.MOD_ID))
                    .icon(() -> new ItemStack(ModItems.FLOWWEAVE_RING.get()))
                    .displayItems((parameters, output) -> {
                        // Herb Products
                        output.accept(ModItems.SCALEPLATE.get());
                        output.accept(ModItems.DEWPETAL_SHARD.get());
                        output.accept(ModItems.GOLDEN_LILYBELL.get());
                        output.accept(ModItems.CRYST_SPINE.get());
                        output.accept(ModItems.BURNT_NODE.get());
                        output.accept(ModItems.HEART_OF_STARDREAM.get());
                        
                        // Herb Seeds
                        output.accept(ModItems.VERDSCALE_FERN_SEED.get());
                        output.accept(ModItems.DEWPETAL_SEED.get());
                        output.accept(ModItems.ZEPHYR_LILY_SEED.get());
                        output.accept(ModItems.CRYSTBUD_SEED.get());
                        output.accept(ModItems.PYRISAGE_SEED.get());
                        output.accept(ModItems.ROSYNIA_SEED.get());
                        
                        // Crafting Materials
                        output.accept(ModItems.LEATHER_ARMOR.get());
                        output.accept(ModItems.VELVET_ARMOR.get());
                        output.accept(ModItems.SILK_ARMOR.get());
                        output.accept(ModItems.MAGIC_ALLOY_DUST.get());
                        output.accept(ModItems.MAGIC_ALLOY_INGOT.get());
                        output.accept(ModItems.BRILLIANT_GEM_DUST.get());
                        output.accept(ModItems.BRILLIANT_GEM.get());
                        
                        // Red Cherry Items
                        output.accept(ModItems.RED_CHERRY_STICK.get());
                        output.accept(ModItems.RED_CHERRY.get());
                        
                        // Herb Flowers
                        output.accept(ModBlocks.VERDSCALE_FERN.get());
                        output.accept(ModBlocks.DEWPETAL.get());
                        output.accept(ModBlocks.ZEPHYR_LILY.get());
                        output.accept(ModBlocks.CRYSTBUD.get());
                        output.accept(ModBlocks.PYRISAGE.get());
                        output.accept(ModBlocks.ROSYNIA.get());
                        
                        // Red Cherry Blocks
                        output.accept(ModBlocks.RED_CHERRY_LOG.get());
                        output.accept(ModBlocks.STRIPPED_RED_CHERRY_LOG.get());
                        output.accept(ModBlocks.RED_CHERRY_WOOD.get());
                        output.accept(ModBlocks.STRIPPED_RED_CHERRY_WOOD.get());
                        output.accept(ModBlocks.RED_CHERRY_PLANKS.get());
                        output.accept(ModBlocks.RED_CHERRY_STAIRS.get());
                        output.accept(ModBlocks.RED_CHERRY_SLAB.get());
                        output.accept(ModBlocks.RED_CHERRY_LEAVES.get());
                        output.accept(ModBlocks.RED_CHERRY_SAPLING.get());
                        
                        // Lumistone Blocks
                        output.accept(ModBlocks.LUMISTONE.get());
                        output.accept(ModBlocks.LUMISTONE_BRICKS.get());
                        output.accept(ModBlocks.RUNE_STONE_BRICKS.get());
                        output.accept(ModBlocks.LUMISTONE_BRICK_SLAB.get());
                        output.accept(ModBlocks.LUMISTONE_BRICK_STAIRS.get());
                        
                        // Leafweave Armor
                        output.accept(ModItems.LEAFWEAVE_HELMET.get());
                        output.accept(ModItems.LEAFWEAVE_CHESTPLATE.get());
                        output.accept(ModItems.LEAFWEAVE_LEGGINGS.get());
                        output.accept(ModItems.LEAFWEAVE_BOOTS.get());
                        
                        // Lumistone Tools
                        output.accept(ModItems.LUMISTONE_SWORD.get());
                        output.accept(ModItems.LUMISTONE_PICKAXE.get());
                        output.accept(ModItems.LUMISTONE_AXE.get());
                        output.accept(ModItems.LUMISTONE_SHOVEL.get());
                        output.accept(ModItems.LUMISTONE_HOE.get());
                        
                        // Red Cherry Crossbow
                        output.accept(ModItems.RED_CHERRY_CROSSBOW.get());
                        output.accept(ModItems.RED_CHERRY_BOLT_MAGAZINE.get());
                        
                        // Special Items
                        output.accept(ModItems.FLOWWEAVE_RING.get());
                        output.accept(ModItems.HERB_BOX.get());
                        output.accept(ModItems.POT.get());
                        output.accept(ModItems.CAULDRON.get());
                        output.accept(ModItems.HERB_BASKET.get());
                        output.accept(ModItems.RED_CHERRY_SHELF.get());
                        output.accept(ModItems.WORKBENCH.get());
                        
                        // Workbench Tools
                        output.accept(ModItems.CUTTING_KNIFE.get());
                        output.accept(ModItems.FEATHER_QUILL.get());
                        output.accept(ModItems.ROPE.get());
                        output.accept(ModItems.FORGE_HAMMER.get());
                    })
                    .build());
}

