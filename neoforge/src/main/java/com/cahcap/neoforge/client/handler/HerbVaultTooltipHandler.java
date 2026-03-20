package com.cahcap.neoforge.client.handler;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.blockentity.HerbCabinetBlockEntity;
import com.cahcap.common.blockentity.HerbVaultBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Client-side handler for rendering Herb Vault HUD tooltip.
 * Shows herb icon and count below crosshair when looking at a herb slot on the front face.
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID, value = Dist.CLIENT)
public class HerbVaultTooltipHandler {

    private static final TooltipAnimator animator = new TooltipAnimator();

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null) {
            animator.reset();
            return;
        }

        // Find herb vault and determine content
        BlockPos targetPos = null;
        Item herb = null;
        int amount = 0;

        HitResult hitResult = mc.hitResult;
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            targetPos = blockHitResult.getBlockPos();
            BlockEntity blockEntity = mc.level.getBlockEntity(targetPos);
            if (blockEntity instanceof HerbVaultBlockEntity vault && vault.isFormed()
                    && blockHitResult.getDirection() == vault.getFacing()) {
                int[] offset = vault.offset;
                net.minecraft.core.Direction facing = vault.getFacing();
                int forwardOffset = facing.getStepX() * offset[0] + facing.getStepZ() * offset[2];
                if (forwardOffset == 1 && offset[1] <= 0) {
                    int herbIndex = vault.getHerbIndexForBlock();
                    if (herbIndex >= 0 && herbIndex < 6) {
                        herb = HerbCabinetBlockEntity.getAllHerbItems()[herbIndex];
                        amount = vault.getHerbAmount(herb);
                    }
                }
            }
        }

        if (herb == null || amount <= 0) {
            animator.reset();
            return;
        }

        float anim = animator.update(targetPos);

        ItemStack stack = new ItemStack(herb);

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, centerY, 0);
        guiGraphics.pose().scale(anim, anim, 1.0f);
        guiGraphics.pose().translate(-centerX, -centerY, 0);

        int x = centerX - 8;
        int y = centerY + 10;

        guiGraphics.renderItem(stack, x, y);

        String amountText = String.valueOf(amount);
        int textX = x + 16 + 2;
        int textY = y + 4;
        guiGraphics.drawString(mc.font, amountText, textX, textY, 0xFFFFFF, true);

        guiGraphics.pose().popPose();
    }
}
