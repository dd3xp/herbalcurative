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
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Client-side handler for rendering Herb Basket HUD tooltip.
 * Shows herb icon and count below crosshair when looking at a herb basket.
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID, value = Dist.CLIENT)
public class HerbBasketTooltipHandler {

    private static final TooltipAnimator animator = new TooltipAnimator();

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        
        if (mc.level == null || mc.player == null) {
            return;
        }

        HerbBasketBlockEntity basket = null;
        BlockPos targetPos = null;
        HitResult hitResult = mc.hitResult;
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            targetPos = blockHitResult.getBlockPos();
            BlockState state = mc.level.getBlockState(targetPos);
            if (state.getBlock() instanceof HerbBasketBlock) {
                BlockEntity blockEntity = mc.level.getBlockEntity(targetPos);
                if (blockEntity instanceof HerbBasketBlockEntity b) {
                    basket = b;
                }
            }
        }

        Item boundHerb = basket != null ? basket.getBoundHerb() : null;
        int amount = basket != null ? basket.getHerbCount() : 0;

        if (boundHerb == null) { animator.reset(); return; }

        float anim = animator.update(targetPos);

        // Prepare item stack for rendering
        ItemStack stack = new ItemStack(boundHerb);

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, centerY, 0);
        guiGraphics.pose().scale(anim, anim, 1.0f);
        guiGraphics.pose().translate(-centerX, -centerY, 0);

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

        guiGraphics.pose().popPose();
    }
}
