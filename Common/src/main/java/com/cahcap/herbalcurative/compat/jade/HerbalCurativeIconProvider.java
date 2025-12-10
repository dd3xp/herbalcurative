package com.cahcap.herbalcurative.compat.jade;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.registry.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

import org.jetbrains.annotations.Nullable;

/**
 * Jade icon provider for Herbal Curative blocks.
 * Provides custom icons for multiblock structures and special blocks.
 */
public class HerbalCurativeIconProvider implements IBlockComponentProvider {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "icon_provider");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
    }

    @Nullable
    @Override
    public IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        // Return the herb cabinet item directly to avoid showing the forest heartwood log
        return IElementHelper.get().item(new ItemStack(ModRegistries.HERB_CABINET_ITEM.get()));
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}