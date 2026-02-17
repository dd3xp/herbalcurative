package com.cahcap.neoforge.common.handler;

import com.cahcap.common.block.WorkbenchBlock;
import com.cahcap.common.blockentity.WorkbenchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * IItemHandler implementation for WorkbenchBlockEntity.
 * Allows hoppers and mod pipes to interact with the workbench.
 * 
 * Each part only accepts input to its own slots:
 * - LEFT: 4 tool slots (slots 0-3)
 * - CENTER: 1 input slot (slot 0)
 * - RIGHT: 9 material slots (slots 0-8)
 * 
 * Output is disabled (products fly out directly when crafting).
 */
public class WorkbenchItemHandler implements IItemHandler {
    
    private final WorkbenchBlockEntity workbench;
    private final WorkbenchBlock.WorkbenchPart part;
    
    public WorkbenchItemHandler(WorkbenchBlockEntity workbench, WorkbenchBlock.WorkbenchPart part) {
        this.workbench = workbench;
        this.part = part;
    }
    
    @Override
    public int getSlots() {
        return switch (part) {
            case LEFT -> WorkbenchBlockEntity.TOOL_SLOTS;    // 4 tool slots
            case CENTER -> 1;                                  // 1 input slot
            case RIGHT -> WorkbenchBlockEntity.MATERIAL_SLOTS; // 9 material slots
        };
    }
    
    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return switch (part) {
            case LEFT -> {
                if (slot >= 0 && slot < WorkbenchBlockEntity.TOOL_SLOTS) {
                    yield workbench.getToolAt(slot);
                }
                yield ItemStack.EMPTY;
            }
            case CENTER -> slot == 0 ? workbench.getInputItem() : ItemStack.EMPTY;
            case RIGHT -> {
                if (slot >= 0 && slot < workbench.getMaterialCount()) {
                    yield workbench.getMaterialAt(slot);
                }
                yield ItemStack.EMPTY;
            }
        };
    }
    
    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        
        return switch (part) {
            case LEFT -> insertTool(slot, stack, simulate);
            case CENTER -> insertInput(stack, simulate);
            case RIGHT -> insertMaterial(stack, simulate);
        };
    }
    
    private ItemStack insertTool(int slot, ItemStack stack, boolean simulate) {
        if (slot < 0 || slot >= WorkbenchBlockEntity.TOOL_SLOTS) {
            return stack;
        }
        
        ItemStack existing = workbench.getToolAt(slot);
        
        if (existing.isEmpty()) {
            // Empty slot - insert full stack (up to max)
            int toInsert = Math.min(stack.getCount(), stack.getMaxStackSize());
            if (!simulate) {
                workbench.setToolAt(slot, stack.copyWithCount(toInsert));
            }
            if (toInsert >= stack.getCount()) {
                return ItemStack.EMPTY;
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(toInsert);
            return remainder;
        } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
            // Try to stack
            int space = existing.getMaxStackSize() - existing.getCount();
            if (space <= 0) {
                return stack;
            }
            int toInsert = Math.min(space, stack.getCount());
            if (!simulate) {
                ItemStack newStack = existing.copy();
                newStack.grow(toInsert);
                workbench.setToolAt(slot, newStack);
            }
            if (toInsert >= stack.getCount()) {
                return ItemStack.EMPTY;
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(toInsert);
            return remainder;
        }
        
        return stack;
    }
    
    private ItemStack insertInput(ItemStack stack, boolean simulate) {
        ItemStack existing = workbench.getInputItem();
        
        if (existing.isEmpty()) {
            int toInsert = Math.min(stack.getCount(), stack.getMaxStackSize());
            if (!simulate) {
                workbench.setInputItemDirect(stack.copyWithCount(toInsert));
            }
            if (toInsert >= stack.getCount()) {
                return ItemStack.EMPTY;
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(toInsert);
            return remainder;
        } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
            int space = existing.getMaxStackSize() - existing.getCount();
            if (space <= 0) {
                return stack;
            }
            int toInsert = Math.min(space, stack.getCount());
            if (!simulate) {
                ItemStack newStack = existing.copy();
                newStack.grow(toInsert);
                workbench.setInputItemDirect(newStack);
            }
            if (toInsert >= stack.getCount()) {
                return ItemStack.EMPTY;
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(toInsert);
            return remainder;
        }
        
        return stack;
    }
    
    private ItemStack insertMaterial(ItemStack stack, boolean simulate) {
        // Materials use a stack (LIFO) structure
        // Try to stack with top item first, then add new slot
        
        java.util.List<ItemStack> materials = workbench.getMaterials();
        int materialCount = materials.size();
        int remaining = stack.getCount();
        
        // Check space in top stack (if compatible)
        int canStackWithTop = 0;
        if (materialCount > 0) {
            ItemStack top = materials.get(materialCount - 1);
            if (ItemStack.isSameItemSameComponents(top, stack)) {
                canStackWithTop = Math.min(remaining, top.getMaxStackSize() - top.getCount());
                remaining -= canStackWithTop;
            }
        }
        
        // Check if we can add as new stack
        int canAddNew = 0;
        if (remaining > 0 && materialCount < WorkbenchBlockEntity.MATERIAL_SLOTS) {
            canAddNew = Math.min(remaining, stack.getMaxStackSize());
            remaining -= canAddNew;
        }
        
        int totalInserted = canStackWithTop + canAddNew;
        
        if (totalInserted == 0) {
            return stack;
        }
        
        if (!simulate) {
            // Insert using pushMaterial for proper synchronization
            ItemStack toInsert = stack.copyWithCount(totalInserted);
            workbench.pushMaterial(toInsert, true); // creativeMode=true means don't shrink source
        }
        
        if (totalInserted >= stack.getCount()) {
            return ItemStack.EMPTY;
        }
        
        ItemStack remainder = stack.copy();
        remainder.shrink(totalInserted);
        return remainder;
    }
    
    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        // Output is disabled - products fly out directly when crafting
        return ItemStack.EMPTY;
    }
    
    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }
    
    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        // All items are valid for insertion
        return true;
    }
    
    /**
     * Get the WorkbenchBlockEntity for a workbench block position.
     * Handles finding the center block entity for any part.
     */
    public static WorkbenchBlockEntity getWorkbenchBlockEntity(Level level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof WorkbenchBlock)) {
            return null;
        }
        
        WorkbenchBlock.WorkbenchPart part = state.getValue(WorkbenchBlock.PART);
        Direction facing = state.getValue(WorkbenchBlock.FACING);
        
        BlockPos centerPos;
        if (part == WorkbenchBlock.WorkbenchPart.CENTER) {
            centerPos = pos;
        } else if (part == WorkbenchBlock.WorkbenchPart.LEFT) {
            centerPos = pos.relative(facing.getClockWise());
        } else { // RIGHT
            centerPos = pos.relative(facing.getCounterClockWise());
        }
        
        if (level.getBlockEntity(centerPos) instanceof WorkbenchBlockEntity workbench) {
            return workbench;
        }
        return null;
    }
}
