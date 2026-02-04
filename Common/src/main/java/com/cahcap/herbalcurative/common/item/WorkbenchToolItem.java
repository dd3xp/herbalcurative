package com.cahcap.herbalcurative.common.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Base class for workbench tools.
 * These are simple tools with durability that are used on the workbench.
 * - Wood Chisel (木工凿)
 * - Quill Pen (羽毛笔)
 * - Rope (绳子)
 * - Forge Hammer (打造锤)
 */
public class WorkbenchToolItem extends Item {
    
    public WorkbenchToolItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public boolean isEnchantable(ItemStack stack) {
        // Only enchantable with Unbreaking
        return true;
    }
    
    @Override
    public int getEnchantmentValue() {
        return 5;
    }
}
