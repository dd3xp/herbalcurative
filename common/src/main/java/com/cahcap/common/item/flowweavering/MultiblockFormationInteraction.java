package com.cahcap.common.item.flowweavering;

import com.cahcap.common.multiblock.Multiblock;
import com.cahcap.common.multiblock.MultiblockCauldron;
import com.cahcap.common.multiblock.MultiblockHerbCabinet;
import com.cahcap.common.multiblock.MultiblockHerbVault;
import com.cahcap.common.multiblock.MultiblockKiln;
import com.cahcap.common.multiblock.MultiblockObelisk;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MultiblockFormationInteraction implements RingBlockInteraction {

    private static final Multiblock[] BLUEPRINTS = new Multiblock[]{
            MultiblockHerbCabinet.BLUEPRINT,
            MultiblockCauldron.BLUEPRINT,
            MultiblockKiln.BLUEPRINT,
            MultiblockHerbVault.BLUEPRINT,
            MultiblockObelisk.BLUEPRINT
    };

    @Override
    public boolean canInteract(UseOnContext context, BlockState state, Level level, BlockPos pos, Player player, ItemStack ringStack) {
        for (Multiblock blueprint : BLUEPRINTS) {
            if (blueprint.isBlockTrigger(state)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public InteractionResult interact(UseOnContext context, BlockState state, Level level, BlockPos pos, Player player, ItemStack ringStack) {
        for (Multiblock blueprint : BLUEPRINTS) {
            if (blueprint.isBlockTrigger(state)) {
                if (blueprint.tryAssemble(level, pos, context.getClickedFace(), player)) {
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }
}
