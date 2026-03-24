package com.cahcap.common.blockentity;

import com.cahcap.common.util.BlockEntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public abstract class MultiblockPartBlockEntity extends BlockEntity {
    
    private boolean formed = false;
    private int posInMultiblock = -1;
    private int[] offset = {0, 0, 0};
    private Direction facing = Direction.NORTH;
    private boolean mirrored = false;
    private BlockState originalBlockState = null;
    private boolean suppressDrops = false;

    public AABB renderAABB = null;

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

    public int getPosInMultiblock() { return posInMultiblock; }
    public void setPosInMultiblock(int pos) { this.posInMultiblock = pos; }
    public int[] getOffset() { return offset; }
    public void setOffset(int[] offset) { this.offset = offset; }
    public void setFormed(boolean formed) { this.formed = formed; }
    public void setFacing(Direction facing) { this.facing = facing; }
    public boolean isMirrored() { return mirrored; }
    public void setMirrored(boolean mirrored) { this.mirrored = mirrored; }
    public boolean isSuppressDrops() { return suppressDrops; }
    public void setSuppressDrops(boolean suppressDrops) { this.suppressDrops = suppressDrops; }
    public void setOriginalBlockState(BlockState state) { this.originalBlockState = state; }
    protected BlockState getRawOriginalBlockState() { return originalBlockState; }

    /** Return the Block registered for this multiblock type (e.g., ModRegistries.KILN.get()) */
    protected abstract Block getMultiblockBlock();

    /** Return all block positions that belong to this structure */
    protected abstract List<BlockPos> getStructurePositions(BlockPos masterPos);

    /** Drop all stored items before disassembly. Called on the entity that initiated disassembly (not necessarily master). */
    protected abstract void dropStoredItems(BlockPos masterPos);

    /** Hook to post-process restored block states (e.g., update fence/wall connections). Default: no-op. */
    protected BlockState postProcessRestoredBlock(BlockState state, BlockPos pos) {
        return state;
    }

    public final void disassemble() {
        if (level == null || level.isClientSide || !isFormed()) {
            return;
        }

        MultiblockPartBlockEntity master = getMaster();
        if (master == null) {
            return;
        }

        BlockPos masterPos = master.getBlockPos();
        BlockPos breakPos = getBlockPos();

        // Subclass drops its stored items
        dropStoredItems(masterPos);

        // Get all structure positions
        List<BlockPos> positions = getStructurePositions(masterPos);
        Block multiblockBlock = getMultiblockBlock();

        // First pass: Mark all blocks as not formed
        for (BlockPos targetPos : positions) {
            if (level.getBlockState(targetPos).is(multiblockBlock)) {
                if (level.getBlockEntity(targetPos) instanceof MultiblockPartBlockEntity part) {
                    part.setFormed(false);
                    part.renderAABB = null;

                    if (!targetPos.equals(breakPos)) {
                        part.setSuppressDrops(true);
                    }

                    part.setChanged();
                    BlockState state = level.getBlockState(targetPos);
                    level.sendBlockUpdated(targetPos, state, state, 3);
                }
            }
        }

        // Second pass: Replace non-broken blocks with original blocks
        for (BlockPos targetPos : positions) {
            if (level.getBlockState(targetPos).is(multiblockBlock)) {
                if (!targetPos.equals(breakPos)) {
                    BlockState original = null;
                    if (level.getBlockEntity(targetPos) instanceof MultiblockPartBlockEntity part) {
                        original = part.getOriginalBlockState();
                    }
                    if (original == null) {
                        ItemStack fallback = getOriginalBlock();
                        original = Block.byItem(fallback.getItem()).defaultBlockState();
                    }
                    original = postProcessRestoredBlock(original, targetPos);
                    level.setBlock(targetPos, original, 3);
                }
            }
        }

        setChanged();
    }

    public abstract ItemStack getOriginalBlock();
    
    /**
     * Get the original block state for this position in the multiblock.
     * Used for destroy particle effects and block restoration on disassembly.
     * Prefers the stored originalBlockState (captured at assembly time).
     * Falls back to getOriginalBlock() for legacy worlds.
     */
    public BlockState getOriginalBlockState() {
        if (originalBlockState != null) {
            return originalBlockState;
        }
        ItemStack originalBlock = getOriginalBlock();
        if (originalBlock.isEmpty()) {
            return null;
        }
        return Block.byItem(originalBlock.getItem()).defaultBlockState();
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        
        tag.putBoolean("formed", formed);
        tag.putInt("posInMultiblock", posInMultiblock);
        tag.putIntArray("offset", offset);
        tag.putInt("facing", facing.get3DDataValue());
        tag.putBoolean("mirrored", mirrored);
        tag.putBoolean("SuppressDrops", suppressDrops);
        if (originalBlockState != null) {
            tag.put("originalBlockState", NbtUtils.writeBlockState(originalBlockState));
        }
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
        mirrored = tag.getBoolean("mirrored");
        suppressDrops = tag.getBoolean("SuppressDrops");
        if (tag.contains("originalBlockState")) {
            originalBlockState = NbtUtils.readBlockState(
                    registries.lookupOrThrow(Registries.BLOCK),
                    tag.getCompound("originalBlockState"));
        } else {
            originalBlockState = null;
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
        BlockEntityHelper.syncToClient(this);
    }
}

