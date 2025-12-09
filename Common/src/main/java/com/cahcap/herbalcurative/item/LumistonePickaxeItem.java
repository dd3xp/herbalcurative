package com.cahcap.herbalcurative.item;

import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;

/**
 * Lumistone Pickaxe
 * - 80 durability, same stats as iron pickaxe
 * - Regenerates 1 durability per second
 * - Auto-pickup: Broken block drops go directly to inventory
 * 
 * Auto-pickup is handled by LumistoneToolHandler event
 */
public class LumistonePickaxeItem extends PickaxeItem {

    public LumistonePickaxeItem(Properties properties) {
        super(Tiers.IRON, properties);
    }
}

