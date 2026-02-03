package com.cahcap.herbalcurative.neoforge.common.handler;

import com.cahcap.herbalcurative.common.blockentity.RedCherryShelfBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * IItemHandler implementation for RedCherryShelfBlockEntity.
 * Allows hoppers and mod pipes to interact with the shelf.
 * The shelf can hold exactly 1 item of any type.
 */
public class RedCherryShelfItemHandler implements IItemHandler {
    
    private final RedCherryShelfBlockEntity shelf;
    
    public RedCherryShelfItemHandler(RedCherryShelfBlockEntity shelf) {
        this.shelf = shelf;
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
        return shelf.getItem();
    }
    
    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (slot != 0 || stack.isEmpty()) {
            return stack;
        }
        
        // If shelf already has an item, can't insert
        if (shelf.hasItem()) {
            return stack;
        }
        
        if (!simulate) {
            shelf.setItem(stack.copyWithCount(1));
        }
        
        // Return the remainder (all but 1)
        if (stack.getCount() > 1) {
            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            return remainder;
        }
        
        return ItemStack.EMPTY;
    }
    
    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot != 0 || amount <= 0) {
            return ItemStack.EMPTY;
        }
        
        if (!shelf.hasItem()) {
            return ItemStack.EMPTY;
        }
        
        ItemStack stored = shelf.getItem();
        
        if (!simulate) {
            shelf.removeItem();
        }
        
        return stored;
    }
    
    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }
    
    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        // Accept any item, but only if the shelf is empty
        return slot == 0 && !shelf.hasItem();
    }
}
