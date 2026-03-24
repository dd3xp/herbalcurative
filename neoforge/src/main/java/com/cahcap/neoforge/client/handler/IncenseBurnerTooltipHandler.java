package com.cahcap.neoforge.client.handler;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.block.IncenseBurnerBlock;
import com.cahcap.common.blockentity.IncenseBurnerBlockEntity;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Client-side handler for rendering Incense Burner HUD tooltip.
 * Layout:
 * - When burning: [Powder icon] [vertical progress bar]
 * - Below: herbs horizontally (always shown if present)
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID, value = Dist.CLIENT)
public class IncenseBurnerTooltipHandler extends TooltipHandler {

    private static final IncenseBurnerTooltipHandler INSTANCE = new IncenseBurnerTooltipHandler();

    // Per-frame state
    private IncenseBurnerBlockEntity burner;
    private ItemStack powder;
    private Map<Item, Integer> herbs;
    private boolean isBurning;

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        INSTANCE.handleEvent(event);
    }

    @Override
    protected boolean isTargetBlock(BlockState state) {
        return state.getBlock() instanceof IncenseBurnerBlock;
    }

    @Override
    protected boolean isValidEntity(BlockEntity entity) {
        if (entity instanceof IncenseBurnerBlockEntity b) {
            burner = b;
            return true;
        }
        return false;
    }

    @Override
    protected boolean hasContent(BlockEntity entity) {
        powder = burner.getPowder();
        herbs = burner.getHerbs();
        isBurning = burner.isBurning();

        return !powder.isEmpty() || !herbs.isEmpty();
    }

    @Override
    protected void renderContent(GuiGraphics guiGraphics, Minecraft mc,
                                 BlockEntity entity, int screenWidth, int screenHeight) {
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        int baseY = centerY + 10;

        // === Powder + vertical progress bar ===
        int powderRowHeight = 0;
        if (!powder.isEmpty()) {
            // Powder centered under crosshair, progress bar to the right
            int powderX = screenWidth / 2 - 8;
            int barX = powderX + 18;

            // Render powder icon
            guiGraphics.renderItem(powder, powderX, baseY);

            // Render vertical progress bar next to powder (always visible)
            int barHeight = 16;
            int filledHeight = 0;
            if (isBurning) {
                filledHeight = (int) (barHeight * burner.getBurnProgress());
            }
            guiGraphics.fill(barX, baseY, barX + 3, baseY + barHeight, 0xFF333333);
            guiGraphics.fill(barX, baseY + barHeight - filledHeight, barX + 3, baseY + barHeight, 0xFF2F6099);

            powderRowHeight = 18;
        }

        // === Bottom row: herbs ===
        if (!herbs.isEmpty()) {
            int herbY = baseY + powderRowHeight;

            List<Map.Entry<Item, Integer>> itemList = new ArrayList<>(herbs.entrySet());
            int totalItems = itemList.size();
            int totalWidth = totalItems * 20;
            int startX = screenWidth / 2 - totalWidth / 2;

            // Render herb items horizontally
            int currentX = startX;
            for (Map.Entry<Item, Integer> entry : itemList) {
                ItemStack stack = new ItemStack(entry.getKey());
                guiGraphics.renderItem(stack, currentX, herbY);
                currentX += 20;
            }

            // Render counts
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 200);

            currentX = startX;
            for (Map.Entry<Item, Integer> entry : itemList) {
                String countText = String.valueOf(entry.getValue());
                int textX = currentX + 17 - mc.font.width(countText);
                int textY = herbY + 9;
                guiGraphics.drawString(mc.font, countText, textX, textY, 0xFFFFFF, true);
                currentX += 20;
            }

            guiGraphics.pose().popPose();
        }
    }
}
