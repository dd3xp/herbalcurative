package com.cahcap.neoforge.client.handler;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.block.KilnBlock;
import com.cahcap.common.blockentity.KilnBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Client-side handler for rendering Kiln HUD tooltip.
 * Layout: [Input] → [Output], catalyst above arrow.
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID, value = Dist.CLIENT)
public class KilnTooltipHandler {

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

        if (!(state.getBlock() instanceof KilnBlock)) {
            return;
        }

        if (!state.getValue(KilnBlock.FORMED)) {
            return;
        }

        BlockEntity blockEntity = mc.level.getBlockEntity(pos);
        if (!(blockEntity instanceof KilnBlockEntity kiln)) {
            return;
        }

        KilnBlockEntity master = kiln.getMaster();
        if (master == null) {
            return;
        }

        ItemStack input = master.getInputSlot();
        ItemStack catalyst = master.getCatalystSlot();
        ItemStack output = master.getOutputSlot();
        ItemStack recipePreview = master.getRecipePreview();

        // Don't render if nothing to show
        if (input.isEmpty() && catalyst.isEmpty() && output.isEmpty() && recipePreview.isEmpty()) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        // Layout: [input 16px] [gap 4px] [arrow 16px] [gap 4px] [output 16px]
        int totalWidth = 56;
        int startX = screenWidth / 2 - totalWidth / 2;
        int baseY = screenHeight / 2 + 24; // Below crosshair with enough clearance

        int inputX = startX;
        int arrowX = startX + 20;
        int outputX = startX + 40;

        // Render input item
        if (!input.isEmpty()) {
            guiGraphics.renderItem(input, inputX, baseY);
            renderCount(guiGraphics, mc, input.getCount(), inputX, baseY, 0xFFFFFF);
        }

        // Render arrow
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 200);
        String arrow = master.isSmelting() ? ">>>" : "\u2192";
        int arrowColor = master.isSmelting() ? 0xFFAA00 : 0xAAAAAA;
        int arrowTextX = arrowX + 8 - mc.font.width(arrow) / 2;
        guiGraphics.drawString(mc.font, arrow, arrowTextX, baseY + 4, arrowColor, true);
        guiGraphics.pose().popPose();

        // Render catalyst above arrow
        if (!catalyst.isEmpty()) {
            guiGraphics.renderItem(catalyst, arrowX, baseY - 18);
            renderCount(guiGraphics, mc, catalyst.getCount(), arrowX, baseY - 18, 0xFF6600);
        }

        // Render output item: show cached output with count, or recipe preview without count
        if (!output.isEmpty()) {
            guiGraphics.renderItem(output, outputX, baseY);
            renderCount(guiGraphics, mc, output.getCount(), outputX, baseY, 0xFFFF00);
        } else if (!recipePreview.isEmpty()) {
            // Show preview of what will be produced (no count)
            guiGraphics.renderItem(recipePreview, outputX, baseY);
        }

        // Render smelting progress bar below arrow
        if (master.isSmelting()) {
            int targetTime = master.getCurrentSmeltTime();
            int progress = master.getSmeltProgress() * 16 / targetTime;
            guiGraphics.fill(arrowX, baseY + 16, arrowX + 16, baseY + 18, 0xFF333333);
            guiGraphics.fill(arrowX, baseY + 16, arrowX + Math.min(progress, 16), baseY + 18, 0xFFFF8800);
        }
    }

    private static void renderCount(GuiGraphics guiGraphics, Minecraft mc, int count, int x, int y, int color) {
        if (count <= 1) return;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 200);
        String countText = String.valueOf(count);
        int textX = x + 17 - mc.font.width(countText);
        int textY = y + 9;
        guiGraphics.drawString(mc.font, countText, textX, textY, color, true);
        guiGraphics.pose().popPose();
    }
}
