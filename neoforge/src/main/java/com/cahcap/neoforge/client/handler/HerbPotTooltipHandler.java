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
 * Shows herb icons horizontally below crosshair when looking at a herb pot.
 * Style matches CauldronTooltipHandler with counts in bottom-right corner.
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

        Map<Item, Integer> herbs = pot.getHerbs();
        
        if (herbs.isEmpty() && !pot.isGrowing()) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        int herbsY = screenHeight / 2 + 12;
        int currentY = herbsY;
        
        // Show growth progress text first
        if (pot.isGrowing()) {
            int progress = (int) (pot.getGrowthProgress() * 100);
            String growthText = progress + "%";
            int textWidth = mc.font.width(growthText);
            guiGraphics.drawString(mc.font, growthText, screenWidth / 2 - textWidth / 2, currentY, 0x55FF55, true);
            currentY += 12;
        }
        
        // Collect herb items for horizontal display
        List<ItemCountPair> items = new ArrayList<>();
        for (Map.Entry<Item, Integer> entry : herbs.entrySet()) {
            items.add(new ItemCountPair(entry.getKey(), entry.getValue()));
        }
        
        if (items.isEmpty()) {
            return;
        }
        
        // Calculate total width for centering (20 pixels per item)
        int totalItems = items.size();
        int totalWidth = totalItems * 20;
        int startX = screenWidth / 2 - totalWidth / 2;
        
        // Render herb items horizontally
        int currentX = startX;
        for (ItemCountPair pair : items) {
            ItemStack stack = new ItemStack(pair.item);
            guiGraphics.renderItem(stack, currentX, currentY);
            currentX += 20;
        }
        
        // Render counts on top (higher z-layer) in bottom-right corner of each item
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 200);
        
        currentX = startX;
        for (ItemCountPair pair : items) {
            String countText = String.valueOf(pair.count);
            int textX = currentX + 17 - mc.font.width(countText);
            int textY = currentY + 9;
            guiGraphics.drawString(mc.font, countText, textX, textY, 0xFFFFFF, true);
            currentX += 20;
        }
        
        guiGraphics.pose().popPose();
    }
    
    private static class ItemCountPair {
        Item item;
        int count;
        
        ItemCountPair(Item item, int count) {
            this.item = item;
            this.count = count;
        }
    }
}
