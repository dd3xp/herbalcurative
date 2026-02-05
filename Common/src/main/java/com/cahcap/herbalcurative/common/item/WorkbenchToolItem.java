package com.cahcap.herbalcurative.common.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * Base class for workbench tools.
 * These are simple tools with durability that are used on the workbench.
 * - Cutting Knife - repaired with Iron Ingot
 * - Feather Quill - repaired with Ink Sac
 * - Rope - repaired with String
 * - Forge Hammer - repaired with Iron Ingot
 */
public class WorkbenchToolItem extends Item {
    
    private final Supplier<Item> repairMaterial;
    
    public WorkbenchToolItem(Properties properties, Supplier<Item> repairMaterial) {
        super(properties);
        this.repairMaterial = repairMaterial;
    }
    
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(repairMaterial.get());
    }
    
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }
    
    @Override
    public int getEnchantmentValue() {
        return 5;
    }
}
