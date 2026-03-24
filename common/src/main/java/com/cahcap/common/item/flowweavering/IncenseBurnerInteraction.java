package com.cahcap.common.item.flowweavering;

import com.cahcap.common.blockentity.IncenseBurnerBlockEntity;
import com.cahcap.common.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class IncenseBurnerInteraction implements RingBlockInteraction {

    @Override
    public boolean canInteract(UseOnContext context, BlockState state, Level level, BlockPos pos, Player player, ItemStack ringStack) {
        if (player != null && player.isShiftKeyDown() && state.is(ModRegistries.INCENSE_BURNER.get())) {
            if (level.getBlockEntity(pos) instanceof IncenseBurnerBlockEntity burner
                    && burner.hasPowder()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public InteractionResult interact(UseOnContext context, BlockState state, Level level, BlockPos pos, Player player, ItemStack ringStack) {
        if (level.getBlockEntity(pos) instanceof IncenseBurnerBlockEntity burner) {
            ItemStack removed = burner.onFlowweaveRingShiftUse(player);
            if (!removed.isEmpty()) {
                if (!player.getInventory().add(removed)) {
                    ItemEntity itemEntity = new ItemEntity(level,
                            pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, removed);
                    level.addFreshEntity(itemEntity);
                }
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
