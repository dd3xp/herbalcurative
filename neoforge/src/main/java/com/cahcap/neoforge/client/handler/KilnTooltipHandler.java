package com.cahcap.neoforge.client.handler;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.block.KilnBlock;
import com.cahcap.common.blockentity.KilnBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Client-side handler for rendering Kiln HUD tooltip.
 * Layout: [Input] >>> [Output], catalyst above arrow.
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID, value = Dist.CLIENT)
public class KilnTooltipHandler extends TooltipHandler {

    private static final KilnTooltipHandler INSTANCE = new KilnTooltipHandler();

    // Per-frame state
    private KilnBlockEntity master;
    private ItemStack input;
    private ItemStack catalyst;
    private ItemStack output;
    private ItemStack recipePreview;

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        INSTANCE.handleEvent(event);
    }

    @Override
    protected boolean isTargetBlock(BlockState state) {
        return state.getBlock() instanceof KilnBlock && state.getValue(KilnBlock.FORMED);
    }

    @Override
    protected boolean isValidEntity(BlockEntity entity) {
        return entity instanceof KilnBlockEntity;
    }

    @Override
    protected boolean additionalValidation(BlockEntity entity, BlockState state,
                                           BlockHitResult hitResult, BlockPos pos) {
        KilnBlockEntity kiln = (KilnBlockEntity) entity;
        master = kiln.getMaster();
        return master != null;
    }

    @Override
    protected boolean hasContent(BlockEntity entity) {
        input = master.getInputSlot();
        catalyst = master.getCatalystSlot();
        output = master.getOutputSlot();
        recipePreview = master.getRecipePreview();

        return !input.isEmpty() || !catalyst.isEmpty() || !output.isEmpty() || !recipePreview.isEmpty();
    }

    @Override
    protected BlockPos getAnimationPos(BlockEntity entity, BlockPos targetPos) {
        return master.getBlockPos();
    }

    @Override
    protected void renderContent(GuiGraphics guiGraphics, Minecraft mc,
                                 BlockEntity entity, int screenWidth, int screenHeight) {
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        // Layout: [input 16px] [gap 4px] [arrow 16px] [gap 4px] [output 16px]
        int totalWidth = 56;
        int startX = centerX - totalWidth / 2;
        int baseY = centerY + 10;

        int inputX = startX;
        int arrowX = startX + 20;
        int outputX = startX + 40;

        // Render input item
        if (!input.isEmpty()) {
            guiGraphics.renderItem(input, inputX, baseY);
            renderCount(guiGraphics, mc, input.getCount(), inputX, baseY, 0xFFFFFF);
        }

        // Render arrow (only when smelting)
        if (master.isSmelting()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 200);
            String arrow = ">>>";
            int arrowTextX = arrowX + 8 - mc.font.width(arrow) / 2;
            guiGraphics.drawString(mc.font, arrow, arrowTextX, baseY + 4, 0xFFAA00, true);
            guiGraphics.pose().popPose();
        }

        // Render output item: cached output with count, or recipe preview without count
        if (!output.isEmpty()) {
            guiGraphics.renderItem(output, outputX, baseY);
            renderCount(guiGraphics, mc, output.getCount(), outputX, baseY, 0xFFFF00);
        } else if (!recipePreview.isEmpty()) {
            guiGraphics.renderItem(recipePreview, outputX, baseY);
        }

        // Render progress bar below arrow (only when smelting)
        if (master.isSmelting()) {
            int targetTime = master.getCurrentSmeltTime();
            int progressFill = master.getSmeltProgress() * 16 / targetTime;
            guiGraphics.fill(arrowX, baseY + 16, arrowX + 16, baseY + 18, 0xFF333333);
            guiGraphics.fill(arrowX, baseY + 16, arrowX + Math.min(progressFill, 16), baseY + 18, 0xFFFF8800);
        }

        // Render catalyst below progress bar (or directly below items if not smelting)
        if (!catalyst.isEmpty()) {
            boolean hasTopRow = !input.isEmpty() || !output.isEmpty() || !recipePreview.isEmpty();
            int catalystY = baseY + (hasTopRow && master.isSmelting() ? 24 : hasTopRow ? 20 : 0);
            guiGraphics.renderItem(catalyst, arrowX, catalystY);
            renderCount(guiGraphics, mc, catalyst.getCount(), arrowX, catalystY, 0xFF6600);
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
