package com.cahcap.neoforge.common.handler;

import com.cahcap.common.blockentity.HerbCabinetBlockEntity;
import com.cahcap.common.blockentity.IncenseBurnerBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * IItemHandler implementation for IncenseBurnerBlockEntity.
 * Allows hoppers and mod pipes to insert items into the incense burner.
 *
 * Slot layout (input only, no output):
 * - Slot 0: Powder slot (1 incense powder item, insert only)
 * - Slots 1-6: Herb slots (up to 64 each, 6 types max, insert only)
 *
 * No output slots - burning spawns mobs, no item output.
 */
public class IncenseBurnerItemHandler implements IItemHandler {

    private static final int POWDER_SLOT = 0;
    private static final int HERB_SLOT_START = 1;
    private static final int MAX_HERB_SLOTS = 6;
    private static final int TOTAL_SLOTS = 1 + MAX_HERB_SLOTS;

    private final IncenseBurnerBlockEntity burner;

    public IncenseBurnerItemHandler(IncenseBurnerBlockEntity burner) {
        this.burner = burner;
    }

    @Override
    public int getSlots() {
        return TOTAL_SLOTS;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        if (slot == POWDER_SLOT) {
            return burner.getPowder();
        }

        int herbIndex = slot - HERB_SLOT_START;
        if (herbIndex >= 0 && herbIndex < MAX_HERB_SLOTS) {
            List<Map.Entry<Item, Integer>> herbEntries = new ArrayList<>(burner.getHerbs().entrySet());
            if (herbIndex < herbEntries.size()) {
                Map.Entry<Item, Integer> entry = herbEntries.get(herbIndex);
                return new ItemStack(entry.getKey(), entry.getValue());
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (slot == POWDER_SLOT) {
            return insertPowder(stack, simulate);
        }

        int herbIndex = slot - HERB_SLOT_START;
        if (herbIndex >= 0 && herbIndex < MAX_HERB_SLOTS) {
            return insertHerb(stack, simulate);
        }

        return stack;
    }

    private ItemStack insertPowder(ItemStack stack, boolean simulate) {
        if (!burner.canAddPowder(stack)) {
            return stack;
        }

        if (!simulate) {
            ItemStack toAdd = stack.copy();
            burner.addPowder(toAdd, false);
            return toAdd;
        } else {
            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            return remainder;
        }
    }

    private ItemStack insertHerb(ItemStack stack, boolean simulate) {
        if (!HerbCabinetBlockEntity.isHerb(stack.getItem())) {
            return stack;
        }

        Map<Item, Integer> herbs = burner.getHerbs();
        Item herbType = stack.getItem();
        int current = herbs.getOrDefault(herbType, 0);

        if (current >= IncenseBurnerBlockEntity.MAX_HERB_PER_TYPE) {
            return stack;
        }

        if (herbs.size() >= IncenseBurnerBlockEntity.MAX_HERB_TYPES && !herbs.containsKey(herbType)) {
            return stack;
        }

        int canAdd = Math.min(stack.getCount(), IncenseBurnerBlockEntity.MAX_HERB_PER_TYPE - current);

        if (!simulate) {
            ItemStack toAdd = stack.copy();
            burner.addHerb(toAdd, false);
            return toAdd;
        } else {
            if (canAdd >= stack.getCount()) {
                return ItemStack.EMPTY;
            }
            ItemStack remainder = stack.copy();
            remainder.setCount(stack.getCount() - canAdd);
            return remainder;
        }
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        // No extraction - burning spawns mobs, no item output
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot == POWDER_SLOT) {
            return 1;
        }
        return IncenseBurnerBlockEntity.MAX_HERB_PER_TYPE;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (slot == POWDER_SLOT) {
            return IncenseBurnerBlockEntity.isValidPowder(stack);
        }

        int herbIndex = slot - HERB_SLOT_START;
        if (herbIndex >= 0 && herbIndex < MAX_HERB_SLOTS) {
            return HerbCabinetBlockEntity.isHerb(stack.getItem());
        }

        return false;
    }
}
