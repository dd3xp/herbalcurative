package com.cahcap.herbalcurative.common.item;

import com.cahcap.herbalcurative.common.block.WorkbenchBlock;
import com.cahcap.herbalcurative.common.blockentity.WorkbenchBlockEntity;
import com.cahcap.herbalcurative.common.multiblock.MultiblockHerbCabinet;
import com.cahcap.herbalcurative.common.multiblock.MultiblockHerbalBlending;
import com.cahcap.herbalcurative.common.multiblock.MultiblockHerbalBlending.BlendingStructure;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

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
        
        // TODO: Implement actual recipe checking once WorkbenchRecipe system is implemented
        // For now, just play a sound to indicate the action was recognized
        level.playSound(null, centerPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        
        // Placeholder: In the future, this will:
        // 1. Check if there's a matching recipe
        // 2. Consume inputs (tools durability, center item, materials)
        // 3. Spawn output item
        // 4. Handle returns if any
        
        return true;
    }
    
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
