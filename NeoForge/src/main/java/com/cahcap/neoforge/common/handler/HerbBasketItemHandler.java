package com.cahcap.neoforge.common.handler;

import com.cahcap.common.blockentity.HerbBasketBlockEntity;
import com.cahcap.common.blockentity.HerbCabinetBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * IItemHandler implementation for HerbBasketBlockEntity.
 * Allows hoppers and mod pipes to interact with the herb basket.
 */
public class HerbBasketItemHandler implements IItemHandler {
    
    private static final int MAX_CAPACITY = 256;
    private final HerbBasketBlockEntity basket;
    
    public HerbBasketItemHandler(HerbBasketBlockEntity basket) {
        this.basket = basket;
    }
    
    @Override
    public int getSlots() {
        return 1;
    }
    
    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        if (slot != 0) {
            return ItemStack.EMPTY;
        }
        
        Item boundHerb = basket.getBoundHerb();
        int count = basket.getHerbCount();
        
        if (boundHerb == null || count <= 0) {
            return ItemStack.EMPTY;
        }
        
        return new ItemStack(boundHerb, count);
    }
    
    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (slot != 0 || stack.isEmpty()) {
            return stack;
        }
        
        Item item = stack.getItem();
        if (!HerbCabinetBlockEntity.isHerb(item)) {
            return stack;
        }
        
        Item boundHerb = basket.getBoundHerb();
        
        // If bound to a different herb, reject
        if (boundHerb != null && boundHerb != item) {
            return stack;
        }
        
        int toInsert = stack.getCount();
        int currentCount = basket.getHerbCount();
        int space = MAX_CAPACITY - currentCount;
        int canInsert = Math.min(toInsert, space);
        
        if (canInsert <= 0) {
            return stack;
        }
        
        if (!simulate) {
            // Bind if not already bound
            if (boundHerb == null) {
                basket.bindHerb(item);
            }
            basket.addHerb(canInsert);
        }
        
        if (canInsert >= toInsert) {
            return ItemStack.EMPTY;
        }
        
        ItemStack remainder = stack.copy();
        remainder.setCount(toInsert - canInsert);
        return remainder;
    }
    
    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot != 0 || amount <= 0) {
            return ItemStack.EMPTY;
        }
        
        Item boundHerb = basket.getBoundHerb();
        int stored = basket.getHerbCount();
        
        if (boundHerb == null || stored <= 0) {
            return ItemStack.EMPTY;
        }
        
        int toExtract = Math.min(amount, stored);
        
        if (!simulate) {
            basket.removeHerb(toExtract);
        }
        
        return new ItemStack(boundHerb, toExtract);
    }
    
    @Override
    public int getSlotLimit(int slot) {
        return MAX_CAPACITY;
    }
    
    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (!HerbCabinetBlockEntity.isHerb(stack.getItem())) {
            return false;
        }
        
        Item boundHerb = basket.getBoundHerb();
        // Accept if not bound, or if bound to the same herb
        return boundHerb == null || boundHerb == stack.getItem();
    }
}
