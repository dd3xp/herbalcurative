package com.cahcap.herbalcurative.item;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Thornmark Axe
 * 80 durability, same stats as iron axe
 * Can quickly break leaves and vines (same speed as shears)
 * Makes leaves and vines drop themselves (handled by ThornmarkToolHandler event)
 * 
 * Any subclass of ThornmarkAxeItem will automatically inherit these behaviors
 */
public class ThornmarkAxeItem extends AxeItem {

    public ThornmarkAxeItem(Properties properties) {
        super(Tiers.IRON, properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        // Fast break speed for leaves and vines (same as shears - 15.0)
        if (state.getBlock() instanceof LeavesBlock || state.getBlock() instanceof VineBlock) {
            return 15.0F;
        }

        return super.getDestroySpeed(stack, state);
    }
}
