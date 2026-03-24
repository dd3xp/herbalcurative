package com.cahcap.common.util;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/**
 * Shared utilities for BlockEntity operations.
 * Eliminates duplicated syncToClient() and isDoubleClick() across multiple BEs.
 */
public final class BlockEntityHelper {

    private BlockEntityHelper() {}

    /**
     * Sync block entity data to all tracking clients.
     * Replacement for duplicated syncToClient() in multiple BEs.
     */
    public static void syncToClient(BlockEntity be) {
        Level level = be.getLevel();
        if (level != null && !level.isClientSide) {
            BlockState state = level.getBlockState(be.getBlockPos());
            level.sendBlockUpdated(be.getBlockPos(), state, state, 3);
            be.setChanged();
        }
    }

    /**
     * Tracks double-click state for a block entity.
     * Eliminates duplicated lastClickTime/lastClickUUID fields and isDoubleClick() methods.
     */
    public static class DoubleClickTracker {
        private long lastClickTime;
        private UUID lastClickUUID;

        public boolean check(Level level, UUID playerUUID) {
            if (level == null) {
                return false;
            }

            long currentTime = level.getGameTime();
            boolean isDouble = (currentTime - lastClickTime < 10 && playerUUID.equals(lastClickUUID));

            lastClickTime = currentTime;
            lastClickUUID = playerUUID;

            return isDouble;
        }
    }
}
