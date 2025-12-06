package com.cahcap.herbalcurative.registry;

import com.cahcap.herbalcurative.HerbalCurative;
import com.cahcap.herbalcurative.blockentity.HerbCabinetBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Registers capabilities for block entities
 */
@EventBusSubscriber(modid = HerbalCurative.MODID)
public class ModCapabilities {
    
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Register IItemHandler capability for HerbCabinetBlockEntity
        // This allows hoppers and other automation to interact with the herb cabinet
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.HERB_CABINET.get(),
            (blockEntity, context) -> {
                if (blockEntity instanceof HerbCabinetBlockEntity cabinet) {
                    // Only provide capability if multiblock is formed
                    if (cabinet.isFormed()) {
                        return cabinet.getItemHandler();
                    }
                }
                return null;
            }
        );
    }
}
