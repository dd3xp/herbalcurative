package com.cahcap.common.item.flowweavering;

import com.cahcap.common.block.WorkbenchBlock;
import com.cahcap.common.blockentity.WorkbenchBlockEntity;
import com.cahcap.common.recipe.WorkbenchRecipe;
import com.cahcap.common.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class WorkbenchCraftInteraction implements RingBlockInteraction {

    @Override
    public boolean canInteract(UseOnContext context, BlockState state, Level level, BlockPos pos, Player player, ItemStack ringStack) {
        return state.is(ModRegistries.WORKBENCH.get())
                && state.getValue(WorkbenchBlock.POSITION) == WorkbenchBlock.POS_CENTER;
    }

    @Override
    public InteractionResult interact(UseOnContext context, BlockState state, Level level, BlockPos pos, Player player, ItemStack ringStack) {
        boolean isShift = player != null && player.isShiftKeyDown();
        if (tryWorkbenchCraft(level, pos, player, isShift)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /**
     * Try to craft using the workbench.
     * @param level The world level
     * @param centerPos The position of the center workbench block
     * @param player The player performing the craft (can be null)
     * @param craftAll If true, craft as many as possible; if false, craft one
     * @return true if crafting was successful
     */
    private boolean tryWorkbenchCraft(Level level, BlockPos centerPos, Player player, boolean craftAll) {
        BlockEntity be = level.getBlockEntity(centerPos);
        if (!(be instanceof WorkbenchBlockEntity workbench)) {
            return false;
        }

        // Create recipe input from workbench state
        WorkbenchRecipe.WorkbenchInput input = new WorkbenchRecipe.WorkbenchInput(workbench);

        // Find matching recipe
        Optional<RecipeHolder<WorkbenchRecipe>> recipeHolder = level.getRecipeManager()
                .getRecipeFor(ModRegistries.WORKBENCH_RECIPE_TYPE.get(), input, level);

        if (recipeHolder.isEmpty()) {
            return false;
        }

        return workbench.executeCraft(recipeHolder.get().value(), player, craftAll);
    }
}
