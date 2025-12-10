package com.cahcap.herbalcurative.compat.wthit;

import com.cahcap.herbalcurative.registry.ModRegistries;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ITooltipComponent;
import mcp.mobius.waila.api.component.ItemComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * WTHIT icon provider for Herbal Curative blocks.
 * Provides custom icons for multiblock structures and special blocks.
 */
enum HerbalCurativeIconProvider implements IBlockComponentProvider {

    INSTANCE;

    @Nullable
    @Override
    public ITooltipComponent getIcon(IBlockAccessor accessor, IPluginConfig config) {
        // Return the herb cabinet item directly to avoid showing the red cherry log
        return new ItemComponent(new ItemStack(ModRegistries.HERB_CABINET_ITEM.get()));
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        // No additional body content needed
    }
}
