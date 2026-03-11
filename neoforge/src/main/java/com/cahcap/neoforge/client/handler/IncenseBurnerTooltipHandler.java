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
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Client-side handler for rendering Incense Burner HUD tooltip.
 * Shows herb icons horizontally below crosshair when looking at an incense burner.
 * Also shows burning progress when active.
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID, value = Dist.CLIENT)
public class IncenseBurnerTooltipHandler {

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
        
        if (!(state.getBlock() instanceof IncenseBurnerBlock)) {
            return;
        }
        
        BlockEntity blockEntity = mc.level.getBlockEntity(pos);
        if (!(blockEntity instanceof IncenseBurnerBlockEntity burner)) {
            return;
        }

        Map<Item, Integer> herbs = burner.getHerbs();
        
        if (herbs.isEmpty()) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        int currentY = screenHeight / 2 + 12;
        
        // Convert to list for indexed access
        List<Map.Entry<Item, Integer>> itemList = new ArrayList<>(herbs.entrySet());
        
        // Calculate total width for centering (20 pixels per item)
        int totalItems = itemList.size();
        int totalWidth = totalItems * 20;
        int startX = screenWidth / 2 - totalWidth / 2;
        
        // Render herb items horizontally
        int currentX = startX;
        for (Map.Entry<Item, Integer> entry : itemList) {
            ItemStack stack = new ItemStack(entry.getKey());
            guiGraphics.renderItem(stack, currentX, currentY);
            currentX += 20;
        }
        
        // Render counts on top (higher z-layer) in bottom-right corner of each item
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 200);
        
        currentX = startX;
        for (Map.Entry<Item, Integer> entry : itemList) {
            String countText = String.valueOf(entry.getValue());
            int textX = currentX + 17 - mc.font.width(countText);
            int textY = currentY + 9;
            guiGraphics.drawString(mc.font, countText, textX, textY, 0xFFFFFF, true);
            currentX += 20;
        }
        
        guiGraphics.pose().popPose();
    }
}
