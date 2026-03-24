package com.cahcap.neoforge.client.handler;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.block.HerbBasketBlock;
import com.cahcap.common.blockentity.HerbBasketBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Client-side handler for rendering Herb Basket HUD tooltip.
 * Shows herb icon and count below crosshair when looking at a herb basket.
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID, value = Dist.CLIENT)
public class HerbBasketTooltipHandler extends TooltipHandler {

    private static final HerbBasketTooltipHandler INSTANCE = new HerbBasketTooltipHandler();

    // Per-frame state
    private HerbBasketBlockEntity basket;

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        INSTANCE.handleEvent(event);
    }

    @Override
    protected boolean isTargetBlock(BlockState state) {
        return state.getBlock() instanceof HerbBasketBlock;
    }

    @Override
    protected boolean isValidEntity(BlockEntity entity) {
        if (entity instanceof HerbBasketBlockEntity b) {
            basket = b;
            return true;
        }
        return false;
    }

    @Override
    protected boolean hasContent(BlockEntity entity) {
        return basket.getBoundHerb() != null;
    }

    @Override
    protected void renderContent(GuiGraphics guiGraphics, Minecraft mc,
                                 BlockEntity entity, int screenWidth, int screenHeight) {
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        Item boundHerb = basket.getBoundHerb();
        int amount = basket.getHerbCount();
        ItemStack stack = new ItemStack(boundHerb);

        // Position below crosshair
        int x = centerX - 8; // Item icon is 16x16, so center it
        int y = centerY + 10;

        // Render the item icon
        guiGraphics.renderItem(stack, x, y);

        // Render the amount as text (format: "128/256")
        String amountText = amount + "/" + basket.getMaxCapacity();
        int textX = x + 16 + 2; // Right of the item icon
        int textY = y + 4; // Vertically centered with icon
        guiGraphics.drawString(mc.font, amountText, textX, textY, 0xFFFFFF, true);
    }
}
