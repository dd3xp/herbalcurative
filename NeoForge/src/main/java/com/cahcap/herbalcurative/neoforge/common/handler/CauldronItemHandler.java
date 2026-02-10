package com.cahcap.herbalcurative.neoforge.common.handler;

import com.cahcap.herbalcurative.common.blockentity.CauldronBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * IItemHandler implementation for CauldronBlockEntity.
 * Allows hoppers and mod pipes to interact with the cauldron.
 * 
 * Slot layout:
 * - Slot 0: Output slot (extract only) - infusing results
 * - Slots 1-10: Input slots (insert only) - materials/herbs
 * 
 * Input behavior (slots 1-10):
 * - If not brewing: accepts materials (added to materials list)
 * - If brewing: accepts herbs only (consumed for duration/level)
 * 
 * Output behavior (slot 0):
 * - Can only extract from output slot
 * - Materials cannot be extracted via automation
 */
public class CauldronItemHandler implements IItemHandler {
    
    private static final int OUTPUT_SLOT = 0;
    private static final int INPUT_SLOT_START = 1;
    private static final int MAX_INPUT_SLOTS = 10;
    private static final int TOTAL_SLOTS = 1 + MAX_INPUT_SLOTS; // 1 output + 10 input
    
    private final CauldronBlockEntity cauldron;
    
    public CauldronItemHandler(CauldronBlockEntity cauldron) {
        this.cauldron = cauldron;
    }
    
    @Override
    public int getSlots() {
        return TOTAL_SLOTS;
    }
    
    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        CauldronBlockEntity master = cauldron.getMaster();
        if (master == null) {
            return ItemStack.EMPTY;
        }
        
        // Slot 0 is the output slot
        if (slot == OUTPUT_SLOT) {
            return master.getOutputSlot();
        }
        
        // Slots 1-10 are input slots (materials)
        int materialIndex = slot - INPUT_SLOT_START;
        if (materialIndex >= 0 && materialIndex < MAX_INPUT_SLOTS) {
            java.util.List<ItemStack> materials = master.getMaterials();
            if (materialIndex < materials.size()) {
                return materials.get(materialIndex);
            }
        }
        
        return ItemStack.EMPTY;
    }
    
    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        
        // Cannot insert into output slot
        if (slot == OUTPUT_SLOT) {
            return stack;
        }
        
        CauldronBlockEntity master = cauldron.getMaster();
        if (master == null) {
            return stack;
        }
        
        // Cannot insert if cauldron has no fluid
        if (!master.hasFluid()) {
            return stack;
        }
        
        // Allow inserting during infusing - may cause infusing to stop if recipe no longer matches
        
        // If brewing, only accept herbs
        if (master.isBrewing()) {
            if (!CauldronBlockEntity.isHerb(stack.getItem())) {
                return stack;
            }
            
            if (!simulate) {
                // Add herbs during brewing - herbs have no limit
                ItemStack toAdd = stack.copy();
                master.addItem(toAdd, null);
                // toAdd is modified by addItem (shrink), return remaining
                return toAdd;
            } else {
                // Simulate: herbs are always fully accepted during brewing (no limit)
                return ItemStack.EMPTY;
            }
        }
        
        // Not brewing: add as material
        java.util.List<ItemStack> materials = master.getMaterials();
        
        // Calculate how much can be inserted
        int remaining = stack.getCount();
        int maxStack = stack.getMaxStackSize();
        
        // First check existing stacks for space
        for (ItemStack existing : materials) {
            if (ItemStack.isSameItemSameComponents(existing, stack)) {
                int space = maxStack - existing.getCount();
                if (space > 0) {
                    int toInsert = Math.min(remaining, space);
                    remaining -= toInsert;
                    if (remaining <= 0) break;
                }
            }
        }
        
        // If still have remaining and room for new slots
        if (remaining > 0 && materials.size() < MAX_INPUT_SLOTS) {
            // Calculate how many new slots we can use
            int emptySlots = MAX_INPUT_SLOTS - materials.size();
            int canAddToNewSlots = Math.min(remaining, emptySlots * maxStack);
            remaining -= canAddToNewSlots;
        }
        
        if (!simulate) {
            // Actually insert
            ItemStack toAdd = stack.copy();
            master.addItem(toAdd, null);
            // toAdd is modified by addItem (shrink), return remaining
            return toAdd;
        } else {
            // Return simulated remainder
            if (remaining <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack remainder = stack.copy();
            remainder.setCount(remaining);
            return remainder;
        }
    }
    
    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }
        
        CauldronBlockEntity master = cauldron.getMaster();
        if (master == null) {
            return ItemStack.EMPTY;
        }
        
        // Can only extract from output slot (slot 0)
        if (slot != OUTPUT_SLOT) {
            return ItemStack.EMPTY;
        }
        
        // Allow extracting from output slot even during infusing
        
        ItemStack outputSlot = master.getOutputSlot();
        if (outputSlot.isEmpty()) {
            return ItemStack.EMPTY;
        }
        
        int toExtract = Math.min(amount, outputSlot.getCount());
        
        if (!simulate) {
            // Extract from output slot
            ItemStack extracted = master.extractFromOutputSlot();
            if (toExtract < extracted.getCount()) {
                // Need to put back the remainder
                // This shouldn't happen with normal hopper behavior, but handle it
                ItemStack remainder = extracted.copy();
                remainder.shrink(toExtract);
                // We can't easily put back, so just return what was extracted
                // The extractFromOutputSlot extracts all, so we return all
            }
            return extracted;
        } else {
            ItemStack result = outputSlot.copy();
            result.setCount(toExtract);
            return result;
        }
    }
    
    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }
    
    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        // Cannot insert into output slot
        if (slot == OUTPUT_SLOT) {
            return false;
        }
        
        CauldronBlockEntity master = cauldron.getMaster();
        if (master == null) {
            return false;
        }
        
        // Must have fluid to accept items
        if (!master.hasFluid()) {
            return false;
        }
        
        // During brewing, only herbs are valid
        if (master.isBrewing()) {
            return CauldronBlockEntity.isHerb(stack.getItem());
        }
        
        // Not brewing: any item is valid as material
        return true;
    }
}
