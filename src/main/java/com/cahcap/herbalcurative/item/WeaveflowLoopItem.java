package com.cahcap.herbalcurative.item;

import com.cahcap.herbalcurative.multiblock.MultiblockHerbCabinet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class WeaveflowLoopItem extends Item {
    
    public WeaveflowLoopItem(Properties properties) {
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
