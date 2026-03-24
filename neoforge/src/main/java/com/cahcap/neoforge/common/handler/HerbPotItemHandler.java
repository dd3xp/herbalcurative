package com.cahcap.neoforge.common.handler;

import com.cahcap.common.util.HerbRegistry;
import com.cahcap.common.blockentity.HerbPotBlockEntity;
import com.cahcap.common.util.HerbRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * IItemHandler implementation for HerbPotBlockEntity.
 * Allows hoppers and mod pipes to insert items into the herb pot.
 * 
 * Slot layout (input only, no output):
 * - Slot 0: Soil slot (1 item, insert only)
 * - Slot 1: Seedling slot (1 item, requires soil, insert only)
 * - Slots 2-7: Herb slots (up to 64 each, 6 types max, insert only)
 * 
 * Input behavior:
 * - Slot 0 accepts valid soil items (dirt, grass_block, etc.)
 * - Slot 1 accepts valid seedlings (seeds, saplings, herb flowers, etc.) if soil is present
 * - Slots 2-7 accept herbs (herb products like scaleplate, dewpetal_shard, etc.)
 * 
 * No output slots - products drop as item entities when growth completes.
 */
public class HerbPotItemHandler implements IItemHandler {
    
    private static final int SOIL_SLOT = 0;
    private static final int SEEDLING_SLOT = 1;
    private static final int HERB_SLOT_START = 2;
    private static final int MAX_HERB_SLOTS = 6;
    private static final int TOTAL_SLOTS = 2 + MAX_HERB_SLOTS; // soil + seedling + 6 herbs
    
    private final HerbPotBlockEntity pot;
    
    public HerbPotItemHandler(HerbPotBlockEntity pot) {
        this.pot = pot;
    }
    
    @Override
    public int getSlots() {
        return TOTAL_SLOTS;
    }
    
    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        if (slot == SOIL_SLOT) {
            return pot.getSoil();
        }
        
        if (slot == SEEDLING_SLOT) {
            return pot.getSeedling();
        }
        
        int herbIndex = slot - HERB_SLOT_START;
        if (herbIndex >= 0 && herbIndex < MAX_HERB_SLOTS) {
            List<Map.Entry<Item, Integer>> herbEntries = new ArrayList<>(pot.getHerbs().entrySet());
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
        
        if (slot == SOIL_SLOT) {
            return insertSoil(stack, simulate);
        }
        
        if (slot == SEEDLING_SLOT) {
            return insertSeedling(stack, simulate);
        }
        
        int herbIndex = slot - HERB_SLOT_START;
        if (herbIndex >= 0 && herbIndex < MAX_HERB_SLOTS) {
            return insertHerb(stack, simulate);
        }
        
        return stack;
    }
    
    private ItemStack insertSoil(ItemStack stack, boolean simulate) {
        if (!pot.canAddSoil(stack)) {
            return stack;
        }
        
        if (!simulate) {
            ItemStack toAdd = stack.copy();
            pot.addSoil(toAdd, false);
            return toAdd;
        } else {
            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            return remainder;
        }
    }
    
    private ItemStack insertSeedling(ItemStack stack, boolean simulate) {
        if (!pot.canAddSeedling(stack)) {
            return stack;
        }
        
        if (!simulate) {
            ItemStack toAdd = stack.copy();
            pot.addSeedling(toAdd, false);
            return toAdd;
        } else {
            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            return remainder;
        }
    }
    
    private ItemStack insertHerb(ItemStack stack, boolean simulate) {
        if (!HerbRegistry.isHerb(stack.getItem())) {
            return stack;
        }
        if (!pot.getAcceptedHerbs().contains(stack.getItem())) {
            return stack;
        }
        
        Map<Item, Integer> herbs = pot.getHerbs();
        Item herbType = stack.getItem();
        int current = herbs.getOrDefault(herbType, 0);
        
        if (current >= HerbPotBlockEntity.MAX_HERB_PER_TYPE) {
            return stack;
        }
        
        if (herbs.size() >= HerbPotBlockEntity.MAX_HERB_TYPES && !herbs.containsKey(herbType)) {
            return stack;
        }
        
        int canAdd = Math.min(stack.getCount(), HerbPotBlockEntity.MAX_HERB_PER_TYPE - current);
        
        if (!simulate) {
            ItemStack toAdd = stack.copy();
            pot.addHerb(toAdd, false);
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
        // No extraction - products drop as item entities
        return ItemStack.EMPTY;
    }
    
    @Override
    public int getSlotLimit(int slot) {
        if (slot == SOIL_SLOT || slot == SEEDLING_SLOT) {
            return 1;
        }
        return HerbPotBlockEntity.MAX_HERB_PER_TYPE;
    }
    
    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (slot == SOIL_SLOT) {
            return HerbPotBlockEntity.isValidSoil(stack);
        }
        
        if (slot == SEEDLING_SLOT) {
            return pot.hasSoil() && HerbPotBlockEntity.isValidSeedling(stack);
        }
        
        int herbIndex = slot - HERB_SLOT_START;
        if (herbIndex >= 0 && herbIndex < MAX_HERB_SLOTS) {
            return pot.getAcceptedHerbs().contains(stack.getItem());
        }

        return false;
    }
}
