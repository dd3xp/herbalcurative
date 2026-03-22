package com.cahcap.common.compat.wthit;

import com.cahcap.common.registry.ModRegistries;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ITooltipComponent;
import mcp.mobius.waila.api.component.ItemComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * WTHIT icon provider for Obelisk multiblock.
 */
enum ObeliskComponentProvider implements IBlockComponentProvider {

    INSTANCE;

    @Nullable
    @Override
    public ITooltipComponent getIcon(IBlockAccessor accessor, IPluginConfig config) {
        return new ItemComponent(new ItemStack(ModRegistries.OBELISK_ITEM.get()));
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
    }
}
