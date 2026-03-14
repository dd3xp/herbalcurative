package com.cahcap.neoforge.common.handler;

import com.cahcap.common.util.ItemTransferHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.function.Predicate;

/**
 * NeoForge implementation of ItemTransferHelper.Provider.
 * Uses IItemHandler capability to support any mod container
 * (vanilla chests, barrels, drawers, herb cabinets, pipes, etc).
 */
public class NeoForgeItemTransferHelper implements ItemTransferHelper.Provider {

    @Override
    public ItemStack extractItem(Level level, BlockPos pos, Direction accessSide, int maxCount,
                                  Predicate<ItemStack> filter) {
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, accessSide);
        if (handler == null) return ItemStack.EMPTY;

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.isEmpty() || !filter.test(stack)) continue;

            int toExtract = Math.min(stack.getCount(), maxCount);
            ItemStack extracted = handler.extractItem(i, toExtract, false);
            if (!extracted.isEmpty()) {
                return extracted;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(Level level, BlockPos pos, Direction accessSide, ItemStack stack) {
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, accessSide);
        if (handler == null) return stack;

        ItemStack remaining = stack.copy();
        for (int i = 0; i < handler.getSlots(); i++) {
            remaining = handler.insertItem(i, remaining, false);
            if (remaining.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        return remaining;
    }
}
