package com.cahcap.neoforge.common.handler;

import com.cahcap.common.blockentity.HerbCabinetBlockEntity;
import com.cahcap.common.blockentity.HerbVaultBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * IItemHandler implementation for HerbVaultBlockEntity.
 * Delegates to the vault's ItemHandlerCallback.
 */
public class HerbVaultItemHandler implements IItemHandler {

    private final HerbCabinetBlockEntity.ItemHandlerCallback callback;

    public HerbVaultItemHandler(HerbVaultBlockEntity vault) {
        this.callback = vault.getItemHandlerCallback();
    }

    @Override
    public int getSlots() { return 6; }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) { return callback.getStackInSlot(slot); }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return callback.insertItem(slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return callback.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) { return callback.getSlotLimit(slot); }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return callback.isItemValid(slot, stack);
    }
}
