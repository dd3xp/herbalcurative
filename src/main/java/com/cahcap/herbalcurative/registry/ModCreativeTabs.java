package com.cahcap.herbalcurative.registry;

import com.cahcap.herbalcurative.HerbalCurative;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    
    public static final DeferredRegister<CreativeModeTab> TABS = 
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HerbalCurative.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> HERBAL_CURATIVE_TAB = TABS.register("herbalcurative_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + HerbalCurative.MODID))
                    .icon(() -> new ItemStack(ModItems.SCALEPLATE.get()))
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
                        
                        // Forest Heartwood Items
                        output.accept(ModItems.FOREST_HEARTWOOD_STICK.get());
                        output.accept(ModItems.FOREST_BERRY.get());
                        
                        // Herb Flowers
                        output.accept(ModBlocks.VERDSCALE_FERN.get());
                        output.accept(ModBlocks.DEWPETAL.get());
                        output.accept(ModBlocks.ZEPHYR_LILY.get());
                        output.accept(ModBlocks.CRYSTBUD.get());
                        output.accept(ModBlocks.PYRISAGE.get());
                        output.accept(ModBlocks.ROSYNIA.get());
                        
                        // Forest Heartwood Blocks
                        output.accept(ModBlocks.FOREST_HEARTWOOD_LOG.get());
                        output.accept(ModBlocks.FOREST_HEARTWOOD_PLANKS.get());
                        output.accept(ModBlocks.FOREST_HEARTWOOD_LEAVES.get());
                        output.accept(ModBlocks.FOREST_HEARTWOOD_SAPLING.get());
                        
                        // Weaveleaf Armor
                        output.accept(ModItems.WEAVELEAF_HELMET.get());
                        output.accept(ModItems.WEAVELEAF_CHESTPLATE.get());
                        output.accept(ModItems.WEAVELEAF_LEGGINGS.get());
                        output.accept(ModItems.WEAVELEAF_BOOTS.get());
                        
                        // Thornmark Tools
                        output.accept(ModItems.THORNMARK_SWORD.get());
                        output.accept(ModItems.THORNMARK_PICKAXE.get());
                        output.accept(ModItems.THORNMARK_AXE.get());
                        output.accept(ModItems.THORNMARK_SHOVEL.get());
                        output.accept(ModItems.THORNMARK_HOE.get());
                        
                        // Thornmark Crossbow
                        output.accept(ModItems.THORNMARK_CROSSBOW.get());
                        output.accept(ModItems.THORNMARK_BOLT_MAGAZINE.get());
                        
                        // Special Items
                        output.accept(ModItems.WEAVEFLOW_LOOP.get());
                        output.accept(ModItems.HERB_BOX.get());
                    })
                    .build());

}
