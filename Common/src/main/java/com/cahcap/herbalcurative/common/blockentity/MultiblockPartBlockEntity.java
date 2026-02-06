package com.cahcap.herbalcurative.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class MultiblockPartBlockEntity extends BlockEntity {
    
    public boolean formed = false;
    public int posInMultiblock = -1;
    public int[] offset = {0, 0, 0};
    public Direction facing = Direction.NORTH;
    
    protected final int[] structureDimensions;
    
    public MultiblockPartBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int[] structureDimensions) {
        super(type, pos, state);
        this.structureDimensions = structureDimensions;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends MultiblockPartBlockEntity> T getMaster() {
        if (level == null || !formed) {
            return null;
        }
        
        BlockPos masterPos = this.getBlockPos().offset(-offset[0], -offset[1], -offset[2]);
        BlockEntity be = level.getBlockEntity(masterPos);
        
        if (be != null && be.getClass() == this.getClass()) {
            return (T) be;
        }
        
        return null;
    }
    
    public boolean isMaster() {
        return formed && offset[0] == 0 && offset[1] == 0 && offset[2] == 0;
    }
    
    public BlockPos getMasterPos() {
        if (!formed) {
            return null;
        }
        return this.getBlockPos().offset(-offset[0], -offset[1], -offset[2]);
    }
    
    public boolean isFormed() {
        return formed;
    }
    
    public Direction getFacing() {
        return facing;
    }
    
    public abstract void disassemble();
    
    public abstract ItemStack getOriginalBlock();
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        
        tag.putBoolean("formed", formed);
        tag.putInt("posInMultiblock", posInMultiblock);
        tag.putIntArray("offset", offset);
        tag.putInt("facing", facing.get3DDataValue());
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
        formed = tag.getBoolean("formed");
        posInMultiblock = tag.getInt("posInMultiblock");
        int[] loadedOffset = tag.getIntArray("offset");
        if (loadedOffset.length == 3) {
            offset = loadedOffset;
        } else {
            offset = new int[]{0, 0, 0};
        }
        facing = Direction.from3DDataValue(tag.getInt("facing"));
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

