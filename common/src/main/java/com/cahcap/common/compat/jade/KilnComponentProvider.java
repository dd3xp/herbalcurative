package com.cahcap.common.compat.jade;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.blockentity.KilnBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

import org.jetbrains.annotations.Nullable;

/**
 * Jade component provider for Kiln multiblock.
 * Shows stone bricks icon and displays internal contents.
 */
public class KilnComponentProvider implements IBlockComponentProvider {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "kiln_provider");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof KilnBlockEntity be) {
            KilnBlockEntity master = be.getMaster();
            if (master == null) return;

            ItemStack input = master.getInputSlot();
            ItemStack catalyst = master.getCatalystSlot();
            ItemStack output = master.getOutputSlot();

            if (!input.isEmpty()) {
                tooltip.add(Component.translatable("tooltip.herbalcurative.kiln.input")
                        .append(": ")
                        .append(input.getHoverName())
                        .append(" x" + input.getCount()));
            }
            if (!catalyst.isEmpty()) {
                tooltip.add(Component.translatable("tooltip.herbalcurative.kiln.catalyst")
                        .append(": ")
                        .append(catalyst.getHoverName())
                        .append(" x" + catalyst.getCount()));
            }
            if (!output.isEmpty()) {
                tooltip.add(Component.translatable("tooltip.herbalcurative.kiln.output")
                        .append(": ")
                        .append(output.getHoverName())
                        .append(" x" + output.getCount()));
            }

            if (master.isSmelting()) {
                int progress = master.getSmeltProgress() * 100 / KilnBlockEntity.SMELT_TIME;
                tooltip.add(Component.translatable("tooltip.herbalcurative.kiln.smelting")
                        .append(": " + progress + "%"));
            }
        }
    }

    @Nullable
    @Override
    public IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        return IElementHelper.get().item(new ItemStack(Blocks.STONE_BRICKS));
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}
