package com.cahcap.herbalcurative.neoforge;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.neoforge.common.handler.StrippingHandler;
import com.cahcap.herbalcurative.neoforge.common.registry.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(HerbalCurativeCommon.MOD_ID)
public class HerbalCurativeNeoForge {
    
    public static final Logger LOGGER = LoggerFactory.getLogger(HerbalCurativeCommon.MOD_ID);

    public HerbalCurativeNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Herbal Curative NeoForge is loading...");
        
        modEventBus.addListener(this::commonSetup);
        
        // Register armor materials BEFORE items (items depend on armor materials)
        ModArmorMaterials.register(modEventBus);
        
        // Register all deferred registries
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        
        // Register recipe types and serializers
        ModRecipeTypes.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        
        // Initialize common registries
        RegistryInit.init();
        
        // Initialize common module
        HerbalCurativeCommon.init();
        
        LOGGER.info("Herbal Curative NeoForge registration complete");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Initialize stripping handler (Red Cherry log -> Stripped Red Cherry log, etc.)
            StrippingHandler.init();
            
            // Register flower pot contents (must be done in enqueueWork for thread safety)
            ModBlocks.registerFlowerPots();
        });
        
        HerbalCurativeCommon.commonSetup();
        LOGGER.info("Herbal Curative NeoForge common setup complete");
    }
}

