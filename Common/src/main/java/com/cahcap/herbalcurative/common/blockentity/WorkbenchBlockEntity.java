package com.cahcap.herbalcurative.common.blockentity;

import com.cahcap.herbalcurative.common.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Block entity for the Workbench.
 * 
 * Stores:
 * - 4 tool slots (left block, each can hold full stack)
 * - 1 input slot (center block)
 * - 6 material slots as a stack (right block, LIFO order)
 */
public class WorkbenchBlockEntity extends BlockEntity {
    
    public static final int TOOL_SLOTS = 4;
    public static final int MATERIAL_SLOTS = 6;
    
    // Tool slots [0-3]: top-left, top-right, bottom-left, bottom-right
    private final ItemStack[] toolSlots = new ItemStack[TOOL_SLOTS];
    
    // Input slot
    private ItemStack inputSlot = ItemStack.EMPTY;
    
    // Material stack [0] is bottom (first in), [size-1] is top (last in, first out)
    private final List<ItemStack> materialStack = new ArrayList<>();
    
    public WorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(getBlockEntityType(), pos, state);
        for (int i = 0; i < TOOL_SLOTS; i++) {
            toolSlots[i] = ItemStack.EMPTY;
        }
    }
    
    @SuppressWarnings("unchecked")
    private static BlockEntityType<WorkbenchBlockEntity> getBlockEntityType() {
        return (BlockEntityType<WorkbenchBlockEntity>) ModRegistries.WORKBENCH_BE.get();
    }
    
    // ==================== Tool Slots (Left Block) ====================
    
    /**
     * Check if a specific tool slot has an item.
     */
    public boolean hasToolAt(int slot) {
        if (slot < 0 || slot >= TOOL_SLOTS) return false;
        return !toolSlots[slot].isEmpty();
    }
    
    /**
     * Get the tool at a specific slot (copy).
     */
    public ItemStack getToolAt(int slot) {
        if (slot < 0 || slot >= TOOL_SLOTS) return ItemStack.EMPTY;
        return toolSlots[slot].copy();
    }
    
    /**
     * Add a tool to the first available slot.
     * @param stack The item to add
     * @param creativeMode If true, don't consume from source stack (creative mode)
     * @return true if successful
     */
    public boolean addTool(ItemStack stack, boolean creativeMode) {
        for (int i = 0; i < TOOL_SLOTS; i++) {
            if (toolSlots[i].isEmpty()) {
                if (creativeMode) {
                    toolSlots[i] = stack.copyWithCount(Math.min(stack.getCount(), stack.getMaxStackSize()));
                } else {
                    toolSlots[i] = stack.split(stack.getMaxStackSize());
                }
                setChanged();
                syncToClient();
                return true;
            }
            // Check if we can stack with existing
            if (ItemStack.isSameItemSameComponents(toolSlots[i], stack)) {
                int canAdd = toolSlots[i].getMaxStackSize() - toolSlots[i].getCount();
                if (canAdd > 0) {
                    int toAdd = Math.min(canAdd, stack.getCount());
                    toolSlots[i].grow(toAdd);
                    if (!creativeMode) {
                        stack.shrink(toAdd);
                    }
                    setChanged();
                    syncToClient();
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Set a tool at a specific slot.
     */
    public void setToolAt(int slot, ItemStack stack) {
        if (slot < 0 || slot >= TOOL_SLOTS) return;
        toolSlots[slot] = stack.copy();
        setChanged();
        syncToClient();
    }
    
    /**
     * Remove and return the tool from a specific slot.
     */
    public ItemStack removeTool(int slot) {
        if (slot < 0 || slot >= TOOL_SLOTS) return ItemStack.EMPTY;
        ItemStack removed = toolSlots[slot].copy();
        toolSlots[slot] = ItemStack.EMPTY;
        setChanged();
        syncToClient();
        return removed;
    }
    
    /**
     * Damage a tool at a specific slot by 1 durability.
     * @return true if the tool was damaged (and possibly broken)
     */
    public boolean damageTool(int slot) {
        if (slot < 0 || slot >= TOOL_SLOTS) return false;
        ItemStack tool = toolSlots[slot];
        if (tool.isEmpty()) return false;
        
        if (tool.isDamageableItem()) {
            tool.setDamageValue(tool.getDamageValue() + 1);
            if (tool.getDamageValue() >= tool.getMaxDamage()) {
                toolSlots[slot] = ItemStack.EMPTY;
            }
            setChanged();
            syncToClient();
            return true;
        }
        return false;
    }
    
    /**
     * Damage a tool by item type (finds it in any slot).
     * @param item The item type to find and damage
     * @return true if a matching tool was damaged
     */
    public boolean damageToolByItem(net.minecraft.world.item.Item item) {
        for (int i = 0; i < TOOL_SLOTS; i++) {
            if (!toolSlots[i].isEmpty() && toolSlots[i].is(item)) {
                return damageTool(i);
            }
        }
        return false;
    }
    
    // ==================== Input Slot (Center Block) ====================
    
    /**
     * Check if the input slot has an item.
     */
    public boolean hasInputItem() {
        return !inputSlot.isEmpty();
    }
    
    /**
     * Get the input item (copy).
     */
    public ItemStack getInputItem() {
        return inputSlot.copy();
    }
    
    /**
     * Set the input item.
     * @param stack The item to set
     * @param creativeMode If true, don't consume from source stack (creative mode)
     * @return true if successful
     */
    public boolean setInputItem(ItemStack stack, boolean creativeMode) {
        if (inputSlot.isEmpty()) {
            if (creativeMode) {
                inputSlot = stack.copyWithCount(Math.min(stack.getCount(), stack.getMaxStackSize()));
            } else {
                inputSlot = stack.split(stack.getMaxStackSize());
            }
            setChanged();
            syncToClient();
            return true;
        }
        // Try to stack with existing
        if (ItemStack.isSameItemSameComponents(inputSlot, stack)) {
            int canAdd = inputSlot.getMaxStackSize() - inputSlot.getCount();
            if (canAdd > 0) {
                int toAdd = Math.min(canAdd, stack.getCount());
                inputSlot.grow(toAdd);
                if (!creativeMode) {
                    stack.shrink(toAdd);
                }
                setChanged();
                syncToClient();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Remove and return the input item.
     */
    public ItemStack removeInputItem() {
        ItemStack removed = inputSlot.copy();
        inputSlot = ItemStack.EMPTY;
        setChanged();
        syncToClient();
        return removed;
    }
    
    /**
     * Consume a specific amount from the input slot.
     * @return true if successful
     */
    public boolean consumeInput(int count) {
        if (inputSlot.getCount() >= count) {
            inputSlot.shrink(count);
            if (inputSlot.isEmpty()) {
                inputSlot = ItemStack.EMPTY;
            }
            setChanged();
            syncToClient();
            return true;
        }
        return false;
    }
    
    /**
     * Set the input item directly (for crafting results).
     */
    public void setInputItemDirect(ItemStack stack) {
        inputSlot = stack.copy();
        setChanged();
        syncToClient();
    }
    
    // ==================== Material Stack (Right Block) ====================
    
    /**
     * Get the number of material stacks.
     */
    public int getMaterialCount() {
        return materialStack.size();
    }
    
    /**
     * Check if material stack is full.
     */
    public boolean isMaterialFull() {
        return materialStack.size() >= MATERIAL_SLOTS;
    }
    
    /**
     * Get a material at a specific index (0 = bottom, size-1 = top).
     */
    public ItemStack getMaterialAt(int index) {
        if (index < 0 || index >= materialStack.size()) return ItemStack.EMPTY;
        return materialStack.get(index).copy();
    }
    
    /**
     * Get all materials as a list (copy).
     */
    public List<ItemStack> getMaterials() {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack stack : materialStack) {
            result.add(stack.copy());
        }
        return result;
    }
    
    /**
     * Push a material onto the stack.
     * @param stack The item to push
     * @param creativeMode If true, don't consume from source stack (creative mode)
     * @return true if successful
     */
    public boolean pushMaterial(ItemStack stack, boolean creativeMode) {
        // First try to stack with top item
        if (!materialStack.isEmpty()) {
            ItemStack top = materialStack.get(materialStack.size() - 1);
            if (ItemStack.isSameItemSameComponents(top, stack)) {
                int canAdd = top.getMaxStackSize() - top.getCount();
                if (canAdd > 0) {
                    int toAdd = Math.min(canAdd, stack.getCount());
                    top.grow(toAdd);
                    if (!creativeMode) {
                        stack.shrink(toAdd);
                    }
                    setChanged();
                    syncToClient();
                    return true;
                }
            }
        }
        
        // Add as new stack if there's room
        if (materialStack.size() < MATERIAL_SLOTS) {
            if (creativeMode) {
                materialStack.add(stack.copyWithCount(Math.min(stack.getCount(), stack.getMaxStackSize())));
            } else {
                materialStack.add(stack.split(stack.getMaxStackSize()));
            }
            setChanged();
            syncToClient();
            return true;
        }
        
        return false;
    }
    
    /**
     * Pop a material from the top of the stack.
     * @return The removed material, or EMPTY if stack is empty
     */
    public ItemStack popMaterial() {
        if (materialStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = materialStack.remove(materialStack.size() - 1);
        setChanged();
        syncToClient();
        return removed;
    }
    
    /**
     * Consume a specific amount from a material at a given index.
     * @param index The index of the material in the stack
     * @param count The amount to consume
     * @return true if successful
     */
    public boolean consumeMaterial(int index, int count) {
        if (index < 0 || index >= materialStack.size()) {
            return false;
        }
        ItemStack mat = materialStack.get(index);
        if (mat.getCount() < count) {
            return false;
        }
        mat.shrink(count);
        if (mat.isEmpty()) {
            materialStack.remove(index);
        }
        setChanged();
        syncToClient();
        return true;
    }
    
    /**
     * Consume materials that match the given requirements.
     * @param requirements List of (item, count) pairs to consume
     * @return true if all materials were consumed successfully
     */
    public boolean consumeMaterials(List<ItemStack> requirements) {
        // First verify we have enough of everything
        for (ItemStack required : requirements) {
            int needed = required.getCount();
            for (ItemStack mat : materialStack) {
                if (ItemStack.isSameItemSameComponents(mat, required)) {
                    needed -= mat.getCount();
                    if (needed <= 0) break;
                }
            }
            if (needed > 0) return false;
        }
        
        // Actually consume
        for (ItemStack required : requirements) {
            int toConsume = required.getCount();
            for (int i = materialStack.size() - 1; i >= 0 && toConsume > 0; i--) {
                ItemStack mat = materialStack.get(i);
                if (ItemStack.isSameItemSameComponents(mat, required)) {
                    int consume = Math.min(toConsume, mat.getCount());
                    mat.shrink(consume);
                    toConsume -= consume;
                    if (mat.isEmpty()) {
                        materialStack.remove(i);
                    }
                }
            }
        }
        
        setChanged();
        syncToClient();
        return true;
    }
    
    /**
     * Consume a specific item type from the material stack.
     * Will consume from multiple stacks if needed.
     * @param item The item type to consume
     * @param count The amount to consume
     * @return true if successful
     */
    public boolean consumeMaterialByType(net.minecraft.world.item.Item item, int count) {
        // First verify we have enough
        int available = 0;
        for (ItemStack mat : materialStack) {
            if (mat.is(item)) {
                available += mat.getCount();
            }
        }
        if (available < count) {
            return false;
        }
        
        // Actually consume (from top of stack first - LIFO)
        int toConsume = count;
        for (int i = materialStack.size() - 1; i >= 0 && toConsume > 0; i--) {
            ItemStack mat = materialStack.get(i);
            if (mat.is(item)) {
                int consume = Math.min(toConsume, mat.getCount());
                mat.shrink(consume);
                toConsume -= consume;
                if (mat.isEmpty()) {
                    materialStack.remove(i);
                }
            }
        }
        
        setChanged();
        syncToClient();
        return true;
    }
    
    // ==================== Utility Methods ====================
    
    /**
     * Get all items stored in the workbench for dropping.
     */
    public List<ItemStack> getAllItems() {
        List<ItemStack> items = new ArrayList<>();
        
        for (ItemStack tool : toolSlots) {
            if (!tool.isEmpty()) {
                items.add(tool.copy());
            }
        }
        
        if (!inputSlot.isEmpty()) {
            items.add(inputSlot.copy());
        }
        
        for (ItemStack mat : materialStack) {
            if (!mat.isEmpty()) {
                items.add(mat.copy());
            }
        }
        
        return items;
    }
    
    /**
     * Clear all slots.
     */
    public void clear() {
        for (int i = 0; i < TOOL_SLOTS; i++) {
            toolSlots[i] = ItemStack.EMPTY;
        }
        inputSlot = ItemStack.EMPTY;
        materialStack.clear();
        setChanged();
        syncToClient();
    }
    
    // ==================== NBT Serialization ====================
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        
        // Save tools
        ListTag toolsTag = new ListTag();
        for (int i = 0; i < TOOL_SLOTS; i++) {
            CompoundTag slotTag = new CompoundTag();
            slotTag.putInt("Slot", i);
            if (!toolSlots[i].isEmpty()) {
                slotTag.put("Item", toolSlots[i].save(registries));
            }
            toolsTag.add(slotTag);
        }
        tag.put("Tools", toolsTag);
        
        // Save input
        if (!inputSlot.isEmpty()) {
            tag.put("Input", inputSlot.save(registries));
        }
        
        // Save materials
        ListTag materialsTag = new ListTag();
        for (ItemStack mat : materialStack) {
            if (!mat.isEmpty()) {
                materialsTag.add(mat.save(registries));
            }
        }
        tag.put("Materials", materialsTag);
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
        // Load tools
        if (tag.contains("Tools", Tag.TAG_LIST)) {
            ListTag toolsTag = tag.getList("Tools", Tag.TAG_COMPOUND);
            for (int i = 0; i < TOOL_SLOTS; i++) {
                toolSlots[i] = ItemStack.EMPTY;
            }
            for (int i = 0; i < toolsTag.size(); i++) {
                CompoundTag slotTag = toolsTag.getCompound(i);
                int slot = slotTag.getInt("Slot");
                if (slot >= 0 && slot < TOOL_SLOTS && slotTag.contains("Item")) {
                    toolSlots[slot] = ItemStack.parseOptional(registries, slotTag.getCompound("Item"));
                }
            }
        }
        
        // Load input
        if (tag.contains("Input")) {
            inputSlot = ItemStack.parseOptional(registries, tag.getCompound("Input"));
        } else {
            inputSlot = ItemStack.EMPTY;
        }
        
        // Load materials
        materialStack.clear();
        if (tag.contains("Materials", Tag.TAG_LIST)) {
            ListTag materialsTag = tag.getList("Materials", Tag.TAG_COMPOUND);
            for (int i = 0; i < materialsTag.size() && i < MATERIAL_SLOTS; i++) {
                ItemStack mat = ItemStack.parseOptional(registries, materialsTag.getCompound(i));
                if (!mat.isEmpty()) {
                    materialStack.add(mat);
                }
            }
        }
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }
    
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    public void syncToClient() {
        if (level != null && !level.isClientSide) {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, 3);
            setChanged();
        }
    }
}
