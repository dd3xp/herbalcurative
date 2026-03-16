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

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null) return;

        HitResult hitResult = mc.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        BlockPos pos = blockHitResult.getBlockPos();
        BlockEntity blockEntity = mc.level.getBlockEntity(pos);

        if (!(blockEntity instanceof HerbVaultBlockEntity vault)) return;
        if (!vault.isFormed()) return;

        // Only show tooltip when looking at the front face
        if (blockHitResult.getDirection() != vault.getFacing()) return;

        // Only show tooltip for front-row blocks (facing direction offset == +1, dy == 0)
        int[] offset = vault.offset;
        net.minecraft.core.Direction facing = vault.getFacing();
        int forwardOffset = facing.getStepX() * offset[0] + facing.getStepZ() * offset[2];
        if (forwardOffset != 1 || offset[1] != 0) return;

        int herbIndex = vault.getHerbIndexForBlock();
        if (herbIndex < 0 || herbIndex >= 6) return;

        Item herb = HerbCabinetBlockEntity.getAllHerbItems()[herbIndex];
        int amount = vault.getHerbAmount(herb);

        if (amount <= 0) return;

        ItemStack stack = new ItemStack(herb);

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        int x = screenWidth / 2 - 8;
        int y = screenHeight / 2 + 12;

        guiGraphics.renderItem(stack, x, y);

        String amountText = String.valueOf(amount);
        int textX = x + 16 + 2;
        int textY = y + 4;
        guiGraphics.drawString(mc.font, amountText, textX, textY, 0xFFFFFF, true);
    }
}
