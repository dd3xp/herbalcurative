package com.cahcap.herbalcurative.common.compat.wthit;

import com.cahcap.herbalcurative.common.registry.ModRegistries;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ITooltipComponent;
import mcp.mobius.waila.api.component.ItemComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * WTHIT icon provider for Cauldron multiblock.
 * Shows the cauldron item model.
 */
enum CauldronIconProvider implements IBlockComponentProvider {

    INSTANCE;

    @Nullable
    @Override
    public ITooltipComponent getIcon(IBlockAccessor accessor, IPluginConfig config) {
        // Return the cauldron item to show cauldron model
        return new ItemComponent(new ItemStack(ModRegistries.CAULDRON_ITEM.get()));
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        // No additional body content needed
    }
}
