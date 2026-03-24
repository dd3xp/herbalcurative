package com.cahcap.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Shared utility for checking whether a block hit falls within a grid cell
 * on the front face of a directional block (e.g. Herb Cabinet, Herb Vault).
 */
public final class GridHitHelper {

    private GridHitHelper() {}

    /**
     * Check if a hit location on the front face is within the grid cell for the given index.
     * Converts world hit coordinates to model-space face coordinates, then checks bounds.
     *
     * @param hitResult the block hit result from the player's interaction
     * @param pos       the block position
     * @param facing    the direction the block is facing
     * @param index     the grid cell index to test
     * @param gridCells per-slot bounds in local block coordinates (0-16), NORTH facing.
     *                  [slot][0=minX, 1=maxX, 2=minY, 3=maxY]
     * @return true if the hit is inside the specified grid cell
     */
    public static boolean isHitInGridCell(BlockHitResult hitResult, BlockPos pos, Direction facing, int index, double[][] gridCells) {
        if (index < 0 || index >= gridCells.length) return false;

        // Get local coordinates (0-16) within the block
        double localX = (hitResult.getLocation().x - pos.getX()) * 16;
        double localY = (hitResult.getLocation().y - pos.getY()) * 16;
        double localZ = (hitResult.getLocation().z - pos.getZ()) * 16;

        // Convert to model-space "across" coordinate on the front face.
        // In model space (NORTH), across = X, up = Y.
        double faceX, faceY;
        switch (facing) {
            case SOUTH -> { faceX = 16 - localX; faceY = localY; }
            case EAST  -> { faceX = 16 - localZ; faceY = localY; }
            case WEST  -> { faceX = localZ; faceY = localY; }
            default    -> { faceX = localX; faceY = localY; } // NORTH
        }

        double[] cell = gridCells[index];
        return faceX >= cell[0] && faceX <= cell[1] && faceY >= cell[2] && faceY <= cell[3];
    }
}
