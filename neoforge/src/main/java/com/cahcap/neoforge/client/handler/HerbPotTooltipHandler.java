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
import net.minecraft.world.phys.HitResult;
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
 * - Top row: [Seedling] → [Output]  (with progress bar below arrow)
 * - Bottom row: herbs horizontally
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID, value = Dist.CLIENT)
public class HerbPotTooltipHandler {

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null) {
            return;
        }

        HitResult hitResult = mc.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        BlockPos pos = blockHitResult.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);

        if (!(state.getBlock() instanceof HerbPotBlock)) {
            return;
        }

        BlockEntity blockEntity = mc.level.getBlockEntity(pos);
        if (!(blockEntity instanceof HerbPotBlockEntity pot)) {
            return;
        }

        ItemStack seedling = pot.getSeedling();
        Map<Item, Integer> herbs = pot.getHerbs();
        ItemStack pendingOutput = pot.getPendingOutput();
        boolean isGrowing = pot.isGrowing();

        // Don't render if nothing to show
        if (seedling.isEmpty() && herbs.isEmpty()) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        int baseY = screenHeight / 2 + 8;

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
