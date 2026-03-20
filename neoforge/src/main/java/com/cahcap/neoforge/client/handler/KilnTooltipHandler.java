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

    private static final TooltipAnimator animator = new TooltipAnimator();

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null) {
            return;
        }

        // Determine if we're looking at a valid formed kiln with content
        KilnBlockEntity master = null;
        BlockPos targetPos = null;
        HitResult hitResult = mc.hitResult;
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            targetPos = blockHitResult.getBlockPos();
            BlockState state = mc.level.getBlockState(targetPos);
            if (state.getBlock() instanceof KilnBlock && state.getValue(KilnBlock.FORMED)) {
                BlockEntity blockEntity = mc.level.getBlockEntity(targetPos);
                if (blockEntity instanceof KilnBlockEntity kiln) {
                    master = kiln.getMaster();
                }
            }
        }

        ItemStack input = master != null ? master.getInputSlot() : ItemStack.EMPTY;
        ItemStack catalyst = master != null ? master.getCatalystSlot() : ItemStack.EMPTY;
        ItemStack output = master != null ? master.getOutputSlot() : ItemStack.EMPTY;
        ItemStack recipePreview = master != null ? master.getRecipePreview() : ItemStack.EMPTY;

        boolean hasContent = !input.isEmpty() || !catalyst.isEmpty() || !output.isEmpty() || !recipePreview.isEmpty();
        if (!hasContent) { animator.reset(); return; }
        float anim = animator.update(master.getBlockPos());

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        // Apply scale animation from crosshair center
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, centerY, 0);
        guiGraphics.pose().scale(anim, anim, 1.0f);
        guiGraphics.pose().translate(-centerX, -centerY, 0);

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

        guiGraphics.pose().popPose();
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
