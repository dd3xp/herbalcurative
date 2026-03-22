package com.cahcap.common.compat.jade;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.registry.ModRegistries;
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
 * Jade icon provider for Obelisk multiblock.
 */
public class ObeliskComponentProvider implements IBlockComponentProvider {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "obelisk_provider");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
    }

    @Nullable
    @Override
    public IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        return IElementHelper.get().item(new ItemStack(ModRegistries.OBELISK_ITEM.get()));
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}
