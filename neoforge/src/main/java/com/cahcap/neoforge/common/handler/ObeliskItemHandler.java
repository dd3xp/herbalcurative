package com.cahcap.neoforge.common.handler;

import com.cahcap.common.blockentity.ObeliskBlockEntity;
import com.cahcap.common.recipe.ObeliskOfferingRecipe;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * IItemHandler for the Obelisk multiblock.
 * Single slot, input only — accepts any item.
 * If the item matches a recipe, starts the offering process.
 * If not, just holds the item on the pedestal.
 * No output — offering spawns mobs.
 */
public class ObeliskItemHandler implements IItemHandler {

    private final ObeliskBlockEntity obelisk;

    public ObeliskItemHandler(ObeliskBlockEntity obelisk) {
        this.obelisk = obelisk;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        if (slot == 0) {
            return obelisk.getOfferingItem();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || slot != 0) {
            return stack;
        }

        // Already has an item on the pedestal
        if (obelisk.hasItem()) {
            return stack;
        }

        if (!simulate) {
            ItemStack toOffer = stack.copyWithCount(1);
            ObeliskOfferingRecipe recipe = obelisk.findRecipe(stack);
            if (recipe != null) {
                obelisk.startOfferingFromAutomation(toOffer, recipe);
            } else {
                obelisk.placeOfferingItem(toOffer);
            }
        }

        ItemStack remainder = stack.copy();
        remainder.shrink(1);
        return remainder;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return slot == 0;
    }
}
