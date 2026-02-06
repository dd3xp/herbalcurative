package com.cahcap.herbalcurative.common.compat.jade;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.common.registry.ModRegistries;
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
 * Jade icon provider for Cauldron multiblock.
 * Shows the cauldron item model.
 */
public class CauldronIconProvider implements IBlockComponentProvider {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "cauldron_icon_provider");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
    }

    @Nullable
    @Override
    public IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        // Return the cauldron item to show cauldron model
        return IElementHelper.get().item(new ItemStack(ModRegistries.CAULDRON_ITEM.get()));
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}
