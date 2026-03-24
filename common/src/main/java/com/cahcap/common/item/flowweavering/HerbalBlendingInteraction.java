package com.cahcap.common.item.flowweavering;

import com.cahcap.common.recipe.MultiblockHerbalBlending;
import com.cahcap.common.recipe.MultiblockHerbalBlending.BlendingStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class HerbalBlendingInteraction implements RingBlockInteraction {

    @Override
    public boolean canInteract(UseOnContext context, BlockState state, Level level, BlockPos pos, Player player, ItemStack ringStack) {
        return player != null && player.isShiftKeyDown()
                && MultiblockHerbalBlending.INSTANCE.isBlockTrigger(state);
    }

    @Override
    public InteractionResult interact(UseOnContext context, BlockState state, Level level, BlockPos pos, Player player, ItemStack ringStack) {
        BlendingStructure structure = MultiblockHerbalBlending.INSTANCE.findStructure(
                level,
                pos,
                context.getClickedFace(),
                player);

        if (structure != null) {
            if (MultiblockHerbalBlending.INSTANCE.tryCraft(level, structure, player)) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
