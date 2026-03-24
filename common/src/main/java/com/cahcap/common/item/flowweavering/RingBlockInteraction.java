package com.cahcap.common.item.flowweavering;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface RingBlockInteraction {
    /** Check if this interaction applies to the given block */
    boolean canInteract(UseOnContext context, BlockState state, Level level, BlockPos pos, Player player, ItemStack ringStack);

    /** Perform the interaction. Return SUCCESS/CONSUME to stop further processing. */
    InteractionResult interact(UseOnContext context, BlockState state, Level level, BlockPos pos, Player player, ItemStack ringStack);
}
