package com.cahcap.neoforge.common.handler;

import com.cahcap.common.blockentity.HerbCabinetBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * IItemHandler implementation for HerbCabinetBlockEntity.
 * Allows hoppers and mod pipes to interact with the herb cabinet.
 */
public class HerbCabinetItemHandler implements IItemHandler {
    
    private static final int MAX_CAPACITY = 4096;
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
