package com.cahcap.herbalcurative.common.blockentity;

import com.cahcap.herbalcurative.common.block.HerbBasketBlock;
import com.cahcap.herbalcurative.common.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

/**
 * Block entity for Herb Basket.
 * Stores up to 256 of a single herb type.
 * Automation support is provided via IItemHandler capability in NeoForge.
 */
public class HerbBasketBlockEntity extends BlockEntity {
    
    private static final int MAX_CAPACITY = 256;
    
    @Nullable
    private Item boundHerb = null;
    private int herbCount = 0;
    
    // Double-click detection
    private long lastClickTime = -100; // Initialize to negative to prevent false double-click on first click
    private java.util.UUID lastClickUUID = null;
    
    public HerbBasketBlockEntity(BlockPos pos, BlockState state) {
        super(getBlockEntityType(), pos, state);
    }
    
    @SuppressWarnings("unchecked")
    private static BlockEntityType<HerbBasketBlockEntity> getBlockEntityType() {
        return (BlockEntityType<HerbBasketBlockEntity>) ModRegistries.HERB_BASKET_BE.get();
    }
    
    /**
     * Get the herb this basket is bound to.
     */
    @Nullable
    public Item getBoundHerb() {
        return boundHerb;
    }
    
    /**
     * Get the current herb count in this basket.
     */
    public int getHerbCount() {
        return herbCount;
    }
    
    /**
     * Get the maximum capacity of this basket.
     */
    public int getMaxCapacity() {
        return MAX_CAPACITY;
    }
    
    /**
     * Bind this basket to a specific herb type.
     * Can only be done once when the basket is empty.
     */
    public void bindHerb(Item herb) {
        if (this.boundHerb == null && HerbCabinetBlockEntity.isHerb(herb)) {
            this.boundHerb = herb;
            updateBlockState();
            setChanged();
            syncToClient();
        }
    }
    
    /**
     * Unbind this basket and reset the herb count.
     * Used when clearing with Flowweave Ring.
     */
    public void unbindHerb() {
        this.boundHerb = null;
        this.herbCount = 0;
        updateBlockState();
        setChanged();
        syncToClient();
    }
    
    /**
     * Add herbs to this basket.
     * @param amount Amount to add
     * @return Amount actually added
     */
    public int addHerb(int amount) {
        if (boundHerb == null) {
            return 0;
        }
        
        int canAdd = Math.min(amount, MAX_CAPACITY - herbCount);
        
        if (canAdd > 0) {
            boolean wasEmpty = herbCount == 0;
            herbCount += canAdd;
            if (wasEmpty) {
                updateBlockState(); // Update texture when going from 0 to having herbs
            }
            setChanged();
            syncToClient();
        }
        
        return canAdd;
    }
    
    /**
     * Remove herbs from this basket.
     * @param amount Amount to remove
     * @return Amount actually removed
     */
    public int removeHerb(int amount) {
        int canRemove = Math.min(amount, herbCount);
        
        if (canRemove > 0) {
            herbCount -= canRemove;
            if (herbCount == 0) {
                updateBlockState(); // Update texture when becoming empty
            }
            setChanged();
            syncToClient();
        }
        
        return canRemove;
    }
    
    /**
     * Check if this basket is empty (no herbs stored, but may still be bound).
     */
    public boolean isEmpty() {
        return herbCount <= 0;
    }
    
    /**
     * Check if this basket has any bound herb.
     */
    public boolean isBound() {
        return boundHerb != null;
    }
    
    /**
     * Check if this is a double-click from the same player.
     */
    public boolean isDoubleClick(java.util.UUID playerUUID) {
        if (level == null) {
            return false;
        }
        
        long currentTime = level.getGameTime();
        boolean isDouble = (currentTime - lastClickTime < 10 && playerUUID.equals(lastClickUUID));
        
        lastClickTime = currentTime;
        lastClickUUID = playerUUID;
        
        return isDouble;
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        
        if (boundHerb != null) {
            tag.putString("BoundHerb", BuiltInRegistries.ITEM.getKey(boundHerb).toString());
        }
        tag.putInt("HerbCount", herbCount);
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
        if (tag.contains("BoundHerb")) {
            String herbId = tag.getString("BoundHerb");
            Item loadedHerb = BuiltInRegistries.ITEM.get(ResourceLocation.parse(herbId));
            if (loadedHerb != Items.AIR && HerbCabinetBlockEntity.isHerb(loadedHerb)) {
                boundHerb = loadedHerb;
            } else {
                boundHerb = null;
            }
        } else {
            boundHerb = null;
        }
        
        herbCount = tag.getInt("HerbCount");
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
    
    /**
     * Update the block state to reflect the current bound herb type.
     * Only shows herb texture when both bound AND has herbs (count > 0).
     */
    private void updateBlockState() {
        if (level != null && !level.isClientSide) {
            BlockState currentState = level.getBlockState(worldPosition);
            // Show herb texture only when bound AND has herbs
            int herbType = (boundHerb != null && herbCount > 0) ? HerbBasketBlock.getHerbTypeIndex(boundHerb) : 0;
            BlockState newState = currentState.setValue(HerbBasketBlock.HERB_TYPE, herbType);
            if (currentState != newState) {
                level.setBlock(worldPosition, newState, 3);
            }
        }
    }
    
    @Override
    public void setLevel(net.minecraft.world.level.Level level) {
        super.setLevel(level);
        // Update block state when level is set (e.g., after loading)
        if (level != null && !level.isClientSide) {
            updateBlockState();
        }
    }
}
