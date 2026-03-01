package com.cahcap.common.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Reusable multiblock utilities.
 * <p>
 * Assembly: setBlock with flags 0 first (no client notify), configure BlockEntities,
 * then sendBlockUpdated. This ensures the client receives block + BE data together,
 * reducing BE-not-ready timing issues.
 * <p>
 * Interior blocks: use {@link #addInteriorSpaceProperties} for multiblocks where the player
 * may stand inside. In getShape/getCollisionShape, return Shapes.empty() when BE not ready.
 */
public final class Multiblock {

    /**
     * Add properties for multiblock blocks with interior space (player can stand inside).
     * Avoids suffocation/view-blocking when BE data arrives after block state.
     */
    public static BlockBehaviour.Properties addInteriorSpaceProperties(BlockBehaviour.Properties props) {
        return props.dynamicShape()
                .isSuffocating((state, getter, pos) -> false)
                .isViewBlocking((state, getter, pos) -> false);
    }

    /**
     * Describes one block position in the multiblock structure.
     */
    public record BlockTransform(BlockPos offsetFromMaster, boolean isMaster, int posInMultiblock) {
        public BlockPos worldPos(BlockPos masterPos) {
            return masterPos.offset(offsetFromMaster.getX(), offsetFromMaster.getY(), offsetFromMaster.getZ());
        }
    }

    /**
     * Assemble a multiblock by transforming blocks in-place.
     * setBlock(0) → configure BE → sendBlockUpdated.
     *
     * @param level         The world
     * @param masterPos     Master block position
     * @param facing        Facing direction
     * @param transforms    List of block positions (offsets from master)
     * @param stateSupplier (worldPos, transform) → BlockState to set
     * @param beConfigurator (blockEntity, transform) → configure BE (offset, formed, etc.)
     */
    public static void assemble(
            Level level,
            BlockPos masterPos,
            Direction facing,
            List<BlockTransform> transforms,
            BiFunction<BlockPos, BlockTransform, BlockState> stateSupplier,
            BiConsumer<BlockEntity, BlockTransform> beConfigurator
    ) {
        for (BlockTransform t : transforms) {
            BlockPos pos = t.worldPos(masterPos);
            BlockState oldState = level.getBlockState(pos);
            BlockState newState = stateSupplier.apply(pos, t);

            level.setBlock(pos, newState, 0);

            BlockEntity be = level.getBlockEntity(pos);
            if (be != null) {
                beConfigurator.accept(be, t);
                be.setChanged();
            }

            level.sendBlockUpdated(pos, oldState, newState, Block.UPDATE_ALL);
        }
    }
}
