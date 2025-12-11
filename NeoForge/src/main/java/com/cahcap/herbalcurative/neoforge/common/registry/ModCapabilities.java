package com.cahcap.herbalcurative.neoforge.common.registry;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.common.blockentity.HerbCabinetBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Registers capabilities for block entities
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModCapabilities {
    
    private static final int MAX_CAPACITY = 4096;
    
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
                        return new HerbCabinetItemHandler(cabinet);
                    }
                }
                return null;
            }
        );
    }
    
    /**
     * IItemHandler implementation for HerbCabinetBlockEntity
     */
    private static class HerbCabinetItemHandler implements IItemHandler {
        
        private final HerbCabinetBlockEntity cabinet;
        
        public HerbCabinetItemHandler(HerbCabinetBlockEntity cabinet) {
            this.cabinet = cabinet;
        }
        
        @Override
        public int getSlots() {
            return 6;
        }
        
        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            if (slot < 0 || slot >= 6) {
                return ItemStack.EMPTY;
            }
            
            HerbCabinetBlockEntity master = cabinet.getMaster();
            if (master == null) {
                return ItemStack.EMPTY;
            }
            
            Item herb = HerbCabinetBlockEntity.getAllHerbItems()[slot];
            int amount = master.getHerbAmount(herb);
            
            if (amount <= 0) {
                return ItemStack.EMPTY;
            }
            
            return new ItemStack(herb, amount);
        }
        
        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            
            Item item = stack.getItem();
            if (!HerbCabinetBlockEntity.isHerb(item)) {
                return stack;
            }
            
            HerbCabinetBlockEntity master = cabinet.getMaster();
            if (master == null) {
                return stack;
            }
            
            int toInsert = stack.getCount();
            int inserted;
            
            if (simulate) {
                int current = master.getHerbAmount(item);
                int space = MAX_CAPACITY - current;
                inserted = Math.min(toInsert, space);
            } else {
                inserted = master.addHerb(item, toInsert);
            }
            
            if (inserted >= toInsert) {
                return ItemStack.EMPTY;
            }
            
            ItemStack remainder = stack.copy();
            remainder.setCount(toInsert - inserted);
            return remainder;
        }
        
        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot < 0 || slot >= 6 || amount <= 0) {
                return ItemStack.EMPTY;
            }
            
            HerbCabinetBlockEntity master = cabinet.getMaster();
            if (master == null) {
                return ItemStack.EMPTY;
            }
            
            Item herb = HerbCabinetBlockEntity.getAllHerbItems()[slot];
            int stored = master.getHerbAmount(herb);
            
            if (stored <= 0) {
                return ItemStack.EMPTY;
            }
            
            int toExtract = Math.min(amount, stored);
            
            if (!simulate) {
                master.removeHerb(herb, toExtract);
            }
            
            return new ItemStack(herb, toExtract);
        }
        
        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }
        
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return HerbCabinetBlockEntity.isHerb(stack.getItem());
        }
    }
}

