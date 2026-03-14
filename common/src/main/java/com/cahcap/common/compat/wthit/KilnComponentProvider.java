package com.cahcap.common.compat.wthit;

import com.cahcap.common.blockentity.KilnBlockEntity;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ITooltipComponent;
import mcp.mobius.waila.api.component.ItemComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

/**
 * WTHIT component provider for Kiln multiblock.
 */
enum KilnComponentProvider implements IBlockComponentProvider {

    INSTANCE;

    @Nullable
    @Override
    public ITooltipComponent getIcon(IBlockAccessor accessor, IPluginConfig config) {
        return new ItemComponent(new ItemStack(Blocks.STONE_BRICKS));
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof KilnBlockEntity be) {
            KilnBlockEntity master = be.getMaster();
            if (master == null) return;

            ItemStack input = master.getInputSlot();
            ItemStack catalyst = master.getCatalystSlot();
            ItemStack output = master.getOutputSlot();

            if (!input.isEmpty()) {
                tooltip.addLine(Component.translatable("tooltip.herbalcurative.kiln.input")
                        .append(": ")
                        .append(input.getHoverName())
                        .append(" x" + input.getCount()));
            }
            if (!catalyst.isEmpty()) {
                tooltip.addLine(Component.translatable("tooltip.herbalcurative.kiln.catalyst")
                        .append(": ")
                        .append(catalyst.getHoverName())
                        .append(" x" + catalyst.getCount()));
            }
            if (!output.isEmpty()) {
                tooltip.addLine(Component.translatable("tooltip.herbalcurative.kiln.output")
                        .append(": ")
                        .append(output.getHoverName())
                        .append(" x" + output.getCount()));
            }

            if (master.isSmelting()) {
                int progress = master.getSmeltProgress() * 100 / KilnBlockEntity.SMELT_TIME;
                tooltip.addLine(Component.translatable("tooltip.herbalcurative.kiln.smelting")
                        .append(": " + progress + "%"));
            }
        }
    }
}
