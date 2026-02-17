package com.cahcap.neoforge.common.registry;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.block.WorkbenchBlock;
import com.cahcap.common.blockentity.CauldronBlockEntity;
import com.cahcap.common.blockentity.HerbBasketBlockEntity;
import com.cahcap.common.blockentity.HerbCabinetBlockEntity;
import com.cahcap.common.blockentity.RedCherryShelfBlockEntity;
import com.cahcap.common.blockentity.WorkbenchBlockEntity;
import com.cahcap.neoforge.common.handler.CauldronItemHandler;
import com.cahcap.neoforge.common.handler.HerbBasketItemHandler;
import com.cahcap.neoforge.common.handler.HerbCabinetItemHandler;
import com.cahcap.neoforge.common.handler.RedCherryShelfItemHandler;
import com.cahcap.neoforge.common.handler.WorkbenchItemHandler;
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
        
        // Register IItemHandler capability for WorkbenchBlockEntity
        // Note: Workbench has 3 parts (LEFT, CENTER, RIGHT), but only CENTER has BlockEntity
        // We register on the block level to handle all parts
        event.registerBlock(
            Capabilities.ItemHandler.BLOCK,
            (level, pos, state, blockEntity, context) -> {
                if (state.getBlock() instanceof WorkbenchBlock) {
                    WorkbenchBlock.WorkbenchPart part = state.getValue(WorkbenchBlock.PART);
                    WorkbenchBlockEntity workbench = WorkbenchItemHandler.getWorkbenchBlockEntity(level, pos, state);
                    if (workbench != null) {
                        return new WorkbenchItemHandler(workbench, part);
                    }
                }
                return null;
            },
            ModBlocks.WORKBENCH.get()
        );
    }
}
