package com.cahcap.herbalcurative;

import org.slf4j.Logger;

import com.cahcap.herbalcurative.item.ModArmorMaterials;
import com.cahcap.herbalcurative.registry.*;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(HerbalCurative.MODID)
public class HerbalCurative {
    
    public static final String MODID = "herbalcurative";
    public static final Logger LOGGER = LogUtils.getLogger();

    public HerbalCurative(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Herbal Curative is loading...");
        
        modEventBus.addListener(this::commonSetup);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        
        // Register armor materials BEFORE items (items depend on armor materials)
        ModArmorMaterials.register(modEventBus);
        
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        
        LOGGER.info("Herbal Curative registration complete");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Herbal Curative common setup complete");
    }
}
