package com.cahcap.herbalcurative.compat.jade;

import com.cahcap.herbalcurative.HerbalCurative;
import com.cahcap.herbalcurative.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

import javax.annotation.Nullable;

/**
 * Jade icon provider for the Herb Cabinet multiblock structure.
 * Directly returns the Herb Cabinet item instead of the block's getCloneItemStack result.
 */
public class HerbCabinetIconProvider implements IBlockComponentProvider {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HerbalCurative.MODID, "herb_cabinet_icon");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
    }

    @Nullable
    @Override
    public IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        // Return the herb cabinet item directly to avoid showing the forest heartwood log
        return IElementHelper.get().item(new ItemStack(ModItems.HERB_CABINET.get()));
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}
