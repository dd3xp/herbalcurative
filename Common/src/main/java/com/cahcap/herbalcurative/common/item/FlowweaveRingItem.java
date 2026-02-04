package com.cahcap.herbalcurative.common.item;

import com.cahcap.herbalcurative.common.block.WorkbenchBlock;
import com.cahcap.herbalcurative.common.blockentity.WorkbenchBlockEntity;
import com.cahcap.herbalcurative.common.multiblock.MultiblockHerbCabinet;
import com.cahcap.herbalcurative.common.multiblock.MultiblockHerbalBlending;
import com.cahcap.herbalcurative.common.multiblock.MultiblockHerbalBlending.BlendingStructure;
import com.cahcap.herbalcurative.common.recipe.WorkbenchRecipe;
import com.cahcap.herbalcurative.common.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

/**
 * Flowweave Ring
 * Magical tool with no durability (permanent use, like a wand)
 * Can be held in offhand
 * Has same attack attributes as iron sword (6 attack damage, -2.4 attack speed)
 * Can be used to:
 * - Form Herb Cabinet multiblock structure
 * - Trigger Herbal Blending Rack crafting
 * - Trigger Workbench crafting
 */
public class FlowweaveRingItem extends Item {
    
    public FlowweaveRingItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        
        BlockState clickedState = context.getLevel().getBlockState(context.getClickedPos());
        
        // Try to form Herb Cabinet multiblock
        if (MultiblockHerbCabinet.INSTANCE.isBlockTrigger(clickedState)) {
            if (MultiblockHerbCabinet.INSTANCE.createStructure(
                    context.getLevel(),
                    context.getClickedPos(),
                    context.getClickedFace(),
                    context.getPlayer())) {
                return InteractionResult.SUCCESS;
            }
        }
        
        // Try to trigger Herbal Blending Rack crafting (requires shift + right click)
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown() 
                && MultiblockHerbalBlending.INSTANCE.isBlockTrigger(clickedState)) {
            BlendingStructure structure = MultiblockHerbalBlending.INSTANCE.findStructure(
                    context.getLevel(),
                    context.getClickedPos(),
                    context.getClickedFace(),
                    context.getPlayer());
            
            if (structure != null) {
                if (MultiblockHerbalBlending.INSTANCE.tryCraft(context.getLevel(), structure, context.getPlayer())) {
                    return InteractionResult.SUCCESS;
                }
            }
        }
        
        // Try to trigger Workbench crafting (right-click center block)
        if (clickedState.is(ModRegistries.WORKBENCH.get())) {
            if (clickedState.getValue(WorkbenchBlock.PART) == WorkbenchBlock.WorkbenchPart.CENTER) {
                boolean isShift = context.getPlayer() != null && context.getPlayer().isShiftKeyDown();
                if (tryWorkbenchCraft(context.getLevel(), context.getClickedPos(), isShift)) {
                    return InteractionResult.SUCCESS;
                }
            }
        }
        
        return InteractionResult.PASS;
    }
    
    /**
     * Try to craft using the workbench.
     * @param level The world level
     * @param centerPos The position of the center workbench block
     * @param craftAll If true, craft as many as possible; if false, craft one
     * @return true if crafting was successful
     */
    private boolean tryWorkbenchCraft(Level level, BlockPos centerPos, boolean craftAll) {
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
        
        WorkbenchRecipe recipe = recipeHolder.get().value();
        
        // Calculate how many to craft
        int craftCount = craftAll ? recipe.getMaxCraftCount(input) : 1;
        if (craftCount <= 0) {
            return false;
        }
        
        // Perform crafting
        for (int i = 0; i < craftCount; i++) {
            // Damage tools
            for (WorkbenchRecipe.ToolRequirement tool : recipe.getTools()) {
                for (int d = 0; d < tool.damage(); d++) {
                    workbench.damageTool(tool.slot());
                }
            }
            
            // Consume materials by type
            for (WorkbenchRecipe.MaterialRequirement req : recipe.getMaterials()) {
                workbench.consumeMaterialByType(req.item(), req.count());
            }
            
            // Consume input
            workbench.consumeInput(1);
        }
        
        // Create result and drop it
        ItemStack result = recipe.getResult();
        result.setCount(result.getCount() * craftCount);
        
        // Drop the result (pop out from center)
        ItemEntity itemEntity = new ItemEntity(level, 
                centerPos.getX() + 0.5, centerPos.getY() + 1.0, centerPos.getZ() + 0.5, result);
        itemEntity.setDeltaMovement(0, 0.2, 0); // Small upward velocity
        level.addFreshEntity(itemEntity);
        
        // Play success sound
        level.playSound(null, centerPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        
        return true;
    }
    
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
