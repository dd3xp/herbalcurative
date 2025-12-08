package com.cahcap.herbalcurative.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Thornmark Pickaxe
 * 80 durability, same stats as iron pickaxe
 * Can quickly break gravel and dirt (same speed as iron shovel - 6.0)
 * 
 * Subclasses can override getDestroySpeed() to customize mining speeds for different blocks
 */
public class ThornmarkPickaxeItem extends PickaxeItem {

    public ThornmarkPickaxeItem(Properties properties) {
        super(Tiers.IRON, properties);
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        Block block = state.getBlock();
        
        // Fast break speed for dirt and gravel (same as iron shovel - 6.0)
        if (block == Blocks.DIRT || block == Blocks.GRAVEL) {
            return 6.0F;
        }
        
        // Use default pickaxe speed for other blocks
        return super.getDestroySpeed(stack, state);
    }
}

