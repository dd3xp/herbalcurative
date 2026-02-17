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
        
        // Check if looking at a herb basket
        if (!(state.getBlock() instanceof HerbBasketBlock)) {
            return;
        }
        
        BlockEntity blockEntity = mc.level.getBlockEntity(pos);
        if (!(blockEntity instanceof HerbBasketBlockEntity basket)) {
            return;
        }

        // Get bound herb info
        Item boundHerb = basket.getBoundHerb();
        int amount = basket.getHerbCount();

        // Don't render if not bound
        if (boundHerb == null) {
            return;
        }

        // Prepare item stack for rendering
        ItemStack stack = new ItemStack(boundHerb);

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        // Position below crosshair
        int x = screenWidth / 2 - 8; // Item icon is 16x16, so center it
        int y = screenHeight / 2 + 12;

        // Render the item icon
        guiGraphics.renderItem(stack, x, y);

        // Render the amount as text (format: "128/256")
        String amountText = amount + "/" + basket.getMaxCapacity();
        int textX = x + 16 + 2; // Right of the item icon
        int textY = y + 4; // Vertically centered with icon
        guiGraphics.drawString(mc.font, amountText, textX, textY, 0xFFFFFF, true);
    }
}
