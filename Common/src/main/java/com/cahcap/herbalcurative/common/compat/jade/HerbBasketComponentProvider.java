package com.cahcap.herbalcurative.common.compat.jade;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.common.blockentity.HerbBasketBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade component provider for Herb Basket.
 * Shows the bound herb type and current count.
 */
public class HerbBasketComponentProvider implements IBlockComponentProvider {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "herb_basket");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof HerbBasketBlockEntity basket) {
            Item boundHerb = basket.getBoundHerb();
            int count = basket.getHerbCount();
            
            if (boundHerb != null) {
                // Show herb type and count
                ItemStack herbStack = new ItemStack(boundHerb);
                String herbName = herbStack.getHoverName().getString();
                tooltip.add(Component.translatable("tooltip.herbalcurative.herb_basket.contents", 
                        herbName, count, basket.getMaxCapacity()));
            }
            // Don't show anything if not bound
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}
