package com.cahcap.neoforge.common.registry;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.block.WorkbenchBlock;
import com.cahcap.common.blockentity.cauldron.CauldronBlockEntity;
import com.cahcap.common.blockentity.HerbBasketBlockEntity;
import com.cahcap.common.blockentity.HerbCabinetBlockEntity;
import com.cahcap.common.blockentity.ShelfBlockEntity;
import com.cahcap.common.blockentity.HerbPotBlockEntity;
import com.cahcap.common.blockentity.IncenseBurnerBlockEntity;
import com.cahcap.common.blockentity.HerbVaultBlockEntity;
import com.cahcap.common.blockentity.KilnBlockEntity;
import com.cahcap.common.blockentity.WorkbenchBlockEntity;
import com.cahcap.common.blockentity.ObeliskBlockEntity;
import com.cahcap.neoforge.common.handler.CauldronItemHandler;
import com.cahcap.neoforge.common.handler.IncenseBurnerItemHandler;
import com.cahcap.neoforge.common.handler.ObeliskItemHandler;
import com.cahcap.neoforge.common.handler.HerbVaultItemHandler;
import com.cahcap.neoforge.common.handler.KilnItemHandler;
import com.cahcap.neoforge.common.handler.HerbBasketItemHandler;
import com.cahcap.neoforge.common.handler.HerbCabinetItemHandler;
import com.cahcap.neoforge.common.handler.HerbPotItemHandler;
import com.cahcap.neoforge.common.handler.ShelfItemHandler;
import com.cahcap.neoforge.common.handler.WorkbenchItemHandler;
import net.minecraft.core.Direction;
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
        
        // Register IItemHandler capability for ShelfBlockEntity
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.SHELF.get(),
            (blockEntity, context) -> {
                if (blockEntity instanceof ShelfBlockEntity shelf) {
                    return new ShelfItemHandler(shelf);
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
                    int position = state.getValue(WorkbenchBlock.POSITION);
                    WorkbenchBlockEntity workbench = WorkbenchItemHandler.getWorkbenchBlockEntity(level, pos, state);
                    if (workbench != null) {
                        return new WorkbenchItemHandler(workbench, position);
                    }
                }
                return null;
            },
            ModBlocks.WORKBENCH.get()
        );
        
        // Register IItemHandler capability for HerbPotBlockEntity
        // Don't provide capability from below (DOWN) so hoppers can collect item entities
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.HERB_POT.get(),
            (blockEntity, context) -> {
                if (blockEntity instanceof HerbPotBlockEntity pot) {
                    // When accessed from below, return null so hopper collects item entities instead
                    if (context == Direction.DOWN) {
                        return null;
                    }
                    return new HerbPotItemHandler(pot);
                }
                return null;
            }
        );
        
        // Register IItemHandler capability for IncenseBurnerBlockEntity
        // Input only (no output) - hoppers/pipes can insert powder and herbs
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.INCENSE_BURNER.get(),
            (blockEntity, context) -> {
                if (blockEntity instanceof IncenseBurnerBlockEntity burner) {
                    return new IncenseBurnerItemHandler(burner);
                }
                return null;
            }
        );

        // Register IItemHandler capability for HerbVaultBlockEntity
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.HERB_VAULT.get(),
            (blockEntity, context) -> {
                if (blockEntity instanceof HerbVaultBlockEntity vault) {
                    if (vault.isFormed()) {
                        return new HerbVaultItemHandler(vault);
                    }
                }
                return null;
            }
        );

        // Register IItemHandler capability for ObeliskBlockEntity
        // Input only (no output) — hoppers/pipes can insert offering items
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.OBELISK.get(),
            (blockEntity, context) -> {
                if (blockEntity instanceof ObeliskBlockEntity obelisk && obelisk.isFormed()) {
                    // Delegate to master for capability
                    ObeliskBlockEntity master = obelisk.getMaster();
                    if (master != null) {
                        return new ObeliskItemHandler(master);
                    }
                }
                return null;
            }
        );

        // Register IItemHandler capability for KilnBlockEntity
        // Direction-aware: right=input, left=output, back=catalyst
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.KILN.get(),
            (blockEntity, context) -> {
                if (blockEntity instanceof KilnBlockEntity kiln && kiln.isFormed()) {
                    return new KilnItemHandler(kiln, context);
                }
                return null;
            }
        );
    }
}
