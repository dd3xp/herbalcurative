package com.cahcap.herbalcurative.common.item;

import com.cahcap.herbalcurative.common.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Red Cherry item
 * Can be eaten to restore hunger points
 * Can be right-clicked on Red Cherry Leaves to plant berry bush below
 */
public class RedCherryItem extends Item {
    
    public RedCherryItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        
        // Check if clicking on Red Cherry Leaves
        if (state.is(ModRegistries.RED_CHERRY_LEAVES.get())) {
            // Try to plant bush below the leaves
            BlockPos belowPos = pos.below();
            BlockState belowState = level.getBlockState(belowPos);
            
            // Check if the position below is air or replaceable
            if (belowState.isAir() || belowState.canBeReplaced()) {
                // Check if berry bush can survive here (needs leaves above)
                if (ModRegistries.RED_CHERRY_BUSH.get().defaultBlockState()
                        .canSurvive(level, belowPos)) {
                    // Plant the bush
                    if (!level.isClientSide()) {
                        level.setBlock(belowPos, ModRegistries.RED_CHERRY_BUSH.get().defaultBlockState(), 3);
                        
                        // Consume berry if not in creative mode
                        if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
                            context.getItemInHand().shrink(1);
                        }
                    }
                    
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
            }
        }
        
        return InteractionResult.PASS;
    }
}

