package com.cahcap.neoforge.client.handler;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/**
 * Shared rendering utilities for HUD tooltip handlers.
 */
public final class TooltipRenderHelper {

    private TooltipRenderHelper() {}

    /**
     * Render an item icon at ({@code x}, {@code y}) with a count string
     * in the bottom-right corner, matching vanilla item-count style.
     *
     * @param color text color (e.g. 0xFFFFFF for white)
     */
    public static void renderItemWithCount(GuiGraphics guiGraphics, Font font,
                                           ItemStack stack, int count, int x, int y, int color) {
        guiGraphics.renderItem(stack, x, y);
        if (count <= 1) return;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 200);
        String countText = String.valueOf(count);
        int textX = x + 17 - font.width(countText);
        int textY = y + 9;
        guiGraphics.drawString(font, countText, textX, textY, color, true);
        guiGraphics.pose().popPose();
    }
}
