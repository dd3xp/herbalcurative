package com.cahcap.herbalcurative.common.compat.wthit;

import com.cahcap.herbalcurative.common.blockentity.HerbBasketBlockEntity;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ITooltipComponent;
import mcp.mobius.waila.api.component.ItemComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * WTHIT component provider for Herb Basket.
 * Shows the bound herb type and current count.
 */
public enum HerbBasketComponentProvider implements IBlockComponentProvider {

    INSTANCE;

    @Nullable
    @Override
    public ITooltipComponent getIcon(IBlockAccessor accessor, IPluginConfig config) {
        // Use default block icon
        return null;
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof HerbBasketBlockEntity basket) {
            Item boundHerb = basket.getBoundHerb();
            int count = basket.getHerbCount();
            
            if (boundHerb != null) {
                // Show herb type and count
                ItemStack herbStack = new ItemStack(boundHerb);
                String herbName = herbStack.getHoverName().getString();
                tooltip.addLine(Component.translatable("tooltip.herbalcurative.herb_basket.contents", 
                        herbName, count, basket.getMaxCapacity()));
            }
            // Don't show anything if not bound
        }
    }
}
