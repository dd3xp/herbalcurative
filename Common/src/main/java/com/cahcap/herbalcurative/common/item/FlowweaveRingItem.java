package com.cahcap.herbalcurative.common.item;

import com.cahcap.herbalcurative.common.multiblock.MultiblockHerbCabinet;
import com.cahcap.herbalcurative.common.multiblock.MultiblockHerbalBlending;
import com.cahcap.herbalcurative.common.multiblock.MultiblockHerbalBlending.BlendingStructure;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Flowweave Ring
 * Magical tool with no durability (permanent use, like a wand)
 * Can be held in offhand
 * Has same attack attributes as iron sword (6 attack damage, -2.4 attack speed)
 * Can be used to:
 * - Form Herb Cabinet multiblock structure
 * - Trigger Herbal Blending Rack crafting
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
        
        return InteractionResult.PASS;
    }
    
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
