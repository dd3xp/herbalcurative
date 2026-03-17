package com.cahcap.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Predicate;

/**
 * Platform-agnostic item transfer abstraction.
 * Mod loader module sets the INSTANCE to use platform-specific item transfer,
 * supporting any mod container (vanilla chests, drawers, pipes, herb cabinets, etc).
 *
 * Usage: ItemTransferHelper.INSTANCE.extractItem(...)
 */
public final class ItemTransferHelper {

    /** Platform implementation, set by mod loader module on init. */
    public static Provider INSTANCE = null;

    public interface Provider {
        /**
         * Extract items matching the filter from a container at the given position.
         * @param level      The world
         * @param pos        Position of the container
         * @param accessSide The side to access the container from
         * @param maxCount   Maximum number of items to extract
         * @param filter     Predicate to filter which items to extract
         * @return Extracted items, or EMPTY if nothing was extracted
         */
        ItemStack extractItem(Level level, BlockPos pos, Direction accessSide, int maxCount,
                              Predicate<ItemStack> filter);

        /**
         * Insert an item into a container at the given position.
         * @param level      The world
         * @param pos        Position of the container
         * @param accessSide The side to access the container from
         * @param stack      The item stack to insert
         * @return Remainder that could not be inserted, or EMPTY if fully inserted
         */
        ItemStack insertItem(Level level, BlockPos pos, Direction accessSide, ItemStack stack);
    }

    private ItemTransferHelper() {}
}
