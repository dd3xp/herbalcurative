package com.cahcap.herbalcurative.common.blockentity;

import com.cahcap.herbalcurative.common.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for Red Cherry Shelf.
 * Stores a single item for display.
 */
public class RedCherryShelfBlockEntity extends BlockEntity {
    
    private ItemStack storedItem = ItemStack.EMPTY;
    
    public RedCherryShelfBlockEntity(BlockPos pos, BlockState state) {
        super(getBlockEntityType(), pos, state);
    }
    
    @SuppressWarnings("unchecked")
    private static BlockEntityType<RedCherryShelfBlockEntity> getBlockEntityType() {
        return (BlockEntityType<RedCherryShelfBlockEntity>) ModRegistries.RED_CHERRY_SHELF_BE.get();
    }
    
    /**
     * Check if the shelf has an item.
     */
    public boolean hasItem() {
        return !storedItem.isEmpty();
    }
    
    /**
     * Get the stored item (copy).
     */
    public ItemStack getItem() {
        return storedItem.copy();
    }
    
    /**
     * Set the item on the shelf.
     */
    public void setItem(ItemStack stack) {
        this.storedItem = stack.copyWithCount(1);
        setChanged();
        syncToClient();
    }
    
    /**
     * Remove and return the item from the shelf.
     */
    public ItemStack removeItem() {
        ItemStack removed = storedItem.copy();
        this.storedItem = ItemStack.EMPTY;
        setChanged();
        syncToClient();
        return removed;
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        
        if (!storedItem.isEmpty()) {
            tag.put("StoredItem", storedItem.save(registries));
        }
        // Mark whether we have an item (for sync purposes)
        tag.putBoolean("HasItem", !storedItem.isEmpty());
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
        // Check explicit HasItem flag first for reliable sync
        if (tag.contains("HasItem") && !tag.getBoolean("HasItem")) {
            storedItem = ItemStack.EMPTY;
        } else if (tag.contains("StoredItem")) {
            storedItem = ItemStack.parseOptional(registries, tag.getCompound("StoredItem"));
        } else {
            storedItem = ItemStack.EMPTY;
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
