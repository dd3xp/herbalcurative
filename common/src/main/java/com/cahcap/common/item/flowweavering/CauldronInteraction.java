package com.cahcap.common.item.flowweavering;

import com.cahcap.common.block.CauldronBlock;
import com.cahcap.common.blockentity.cauldron.CauldronBlockEntity;
import com.cahcap.common.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CauldronInteraction implements RingBlockInteraction {

    @Override
    public boolean canInteract(UseOnContext context, BlockState state, Level level, BlockPos pos, Player player, ItemStack ringStack) {
        return state.is(ModRegistries.CAULDRON.get()) && state.getValue(CauldronBlock.FORMED);
    }

    @Override
    public InteractionResult interact(UseOnContext context, BlockState state, Level level, BlockPos pos, Player player, ItemStack ringStack) {
        if (level.getBlockEntity(pos) instanceof CauldronBlockEntity be) {
            CauldronBlockEntity master = be.getMaster();
            if (master != null) {
                if (player != null && player.isShiftKeyDown()) {
                    master.onFlowweaveRingShiftUse(player);
                    level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1.0F);
                } else {
                    master.onFlowweaveRingUse(player);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
