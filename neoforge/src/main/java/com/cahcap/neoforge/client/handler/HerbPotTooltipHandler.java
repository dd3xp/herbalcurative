package com.cahcap.neoforge.client.handler;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.block.HerbPotBlock;
import com.cahcap.common.blockentity.HerbPotBlockEntity;
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
 * Client-side handler for rendering Herb Pot HUD tooltip.
 * Layout:
 * - Top row: [Seedling] >>> [Output]  (with progress bar below arrow)
 * - Bottom row: herbs horizontally
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID, value = Dist.CLIENT)
public class HerbPotTooltipHandler extends TooltipHandler {

    private static final HerbPotTooltipHandler INSTANCE = new HerbPotTooltipHandler();

    // Per-frame state
    private HerbPotBlockEntity pot;
    private ItemStack seedling;
    private Map<Item, Integer> herbs;
    private ItemStack pendingOutput;
    private boolean isGrowing;

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        INSTANCE.handleEvent(event);
    }

    @Override
    protected boolean isTargetBlock(BlockState state) {
        return state.getBlock() instanceof HerbPotBlock;
    }

    @Override
    protected boolean isValidEntity(BlockEntity entity) {
        if (entity instanceof HerbPotBlockEntity p) {
            pot = p;
            return true;
        }
        return false;
    }

    @Override
    protected boolean hasContent(BlockEntity entity) {
        seedling = pot.getSeedling();
        herbs = pot.getHerbs();
        pendingOutput = pot.getPendingOutput();
        isGrowing = pot.isGrowing();

        return !seedling.isEmpty() || !herbs.isEmpty();
    }

    @Override
    protected void renderContent(GuiGraphics guiGraphics, Minecraft mc,
                                 BlockEntity entity, int screenWidth, int screenHeight) {
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        int baseY = centerY + 10;

        // === Top row: [Seedling] [arrow] [Output] ===
        if (!seedling.isEmpty()) {
            // Layout: [seedling 16px] [gap 4px] [arrow 16px] [gap 4px] [output 16px]
            int totalWidth = 56;
            int startX = screenWidth / 2 - totalWidth / 2;
            int seedlingX = startX;
            int arrowX = startX + 20;
            int outputX = startX + 40;

            // Render seedling
            guiGraphics.renderItem(seedling, seedlingX, baseY);

            // Render arrow (always >>>, green when growing, gray when idle)
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 200);
            String arrow = ">>>";
            int arrowColor = isGrowing ? 0x55FF55 : 0xAAAAAA;
            int arrowTextX = arrowX + 8 - mc.font.width(arrow) / 2;
            guiGraphics.drawString(mc.font, arrow, arrowTextX, baseY + 4, arrowColor, true);
            guiGraphics.pose().popPose();

            // Render output (always show: pending output or recipe preview)
            if (pendingOutput != null && !pendingOutput.isEmpty()) {
                guiGraphics.renderItem(pendingOutput, outputX, baseY);
            } else {
                ItemStack preview = pot.getRecipePreview();
                if (!preview.isEmpty()) {
                    guiGraphics.renderItem(preview, outputX, baseY);
                }
            }

            // Render progress bar (always visible, filled when growing)
            int barWidth = 0;
            if (isGrowing) {
                barWidth = (int) (16 * pot.getGrowthProgress());
            }
            guiGraphics.fill(arrowX, baseY + 16, arrowX + 16, baseY + 18, 0xFF333333);
            guiGraphics.fill(arrowX, baseY + 16, arrowX + Math.min(barWidth, 16), baseY + 18, 0xFF55FF55);
        }

        // === Bottom row: herbs ===
        if (!herbs.isEmpty()) {
            int herbY = baseY + (seedling.isEmpty() ? 0 : 24);

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
