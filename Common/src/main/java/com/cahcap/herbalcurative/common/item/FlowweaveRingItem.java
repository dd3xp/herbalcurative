package com.cahcap.herbalcurative.common.item;

import com.cahcap.herbalcurative.common.multiblock.MultiblockHerbCabinet;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/**
 * Flowweave Ring
 * Magical tool with no durability (permanent use, like a wand)
 * Can be held in offhand
 * Has same attack attributes as iron sword (6 attack damage, -2.4 attack speed)
 * Can be used to form Herb Cabinet multiblock structure
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
        
        // Try to form Herb Cabinet multiblock
        if (MultiblockHerbCabinet.INSTANCE.isBlockTrigger(context.getLevel().getBlockState(context.getClickedPos()))) {
            if (MultiblockHerbCabinet.INSTANCE.createStructure(
                    context.getLevel(),
                    context.getClickedPos(),
                    context.getClickedFace(),
                    context.getPlayer())) {
                return InteractionResult.SUCCESS;
            }
        }
        
        return InteractionResult.PASS;
    }
    
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
