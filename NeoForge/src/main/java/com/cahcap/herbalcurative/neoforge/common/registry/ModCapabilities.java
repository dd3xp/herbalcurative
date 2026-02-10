package com.cahcap.herbalcurative.neoforge.common.registry;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.common.blockentity.CauldronBlockEntity;
import com.cahcap.herbalcurative.common.blockentity.HerbBasketBlockEntity;
import com.cahcap.herbalcurative.common.blockentity.HerbCabinetBlockEntity;
import com.cahcap.herbalcurative.common.blockentity.RedCherryShelfBlockEntity;
import com.cahcap.herbalcurative.neoforge.common.handler.CauldronItemHandler;
import com.cahcap.herbalcurative.neoforge.common.handler.HerbBasketItemHandler;
import com.cahcap.herbalcurative.neoforge.common.handler.HerbCabinetItemHandler;
import com.cahcap.herbalcurative.neoforge.common.handler.RedCherryShelfItemHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Registers capabilities for block entities
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModCapabilities {
    
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Register IItemHandler capability for HerbCabinetBlockEntity
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.HERB_CABINET.get(),
            (blockEntity, context) -> {
                if (blockEntity instanceof HerbCabinetBlockEntity cabinet) {
                    // Only provide capability if multiblock is formed
                    if (cabinet.isFormed()) {
                        return new HerbCabinetItemHandler(cabinet);
                    }
                }
                return null;
            }
        );
        
        // Register IItemHandler capability for HerbBasketBlockEntity
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.HERB_BASKET.get(),
            (blockEntity, context) -> {
                if (blockEntity instanceof HerbBasketBlockEntity basket) {
                    return new HerbBasketItemHandler(basket);
                }
                return null;
            }
        );
        
        // Register IItemHandler capability for RedCherryShelfBlockEntity
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.RED_CHERRY_SHELF.get(),
            (blockEntity, context) -> {
                if (blockEntity instanceof RedCherryShelfBlockEntity shelf) {
                    return new RedCherryShelfItemHandler(shelf);
                }
                return null;
            }
        );
        
        // Register IItemHandler capability for CauldronBlockEntity
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.CAULDRON.get(),
            (blockEntity, context) -> {
                if (blockEntity instanceof CauldronBlockEntity cauldron) {
                    // Only provide capability if multiblock is formed and has fluid
                    if (cauldron.isFormed() && cauldron.hasFluid()) {
                        return new CauldronItemHandler(cauldron);
                    }
                }
                return null;
            }
        );
    }
}
