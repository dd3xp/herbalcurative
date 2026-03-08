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

import java.util.Map;

/**
 * Client-side handler for rendering Herb Pot HUD tooltip.
 * Shows herb icons and counts below crosshair when looking at a herb pot.
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

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        int baseX = screenWidth / 2 - 8;
        int baseY = screenHeight / 2 + 12;
        int currentY = baseY;
        
        Map<Item, Integer> herbs = pot.getHerbs();
        
        if (herbs.isEmpty() && !pot.hasSoil() && !pot.hasSeedling()) {
            return;
        }
        
        if (pot.isGrowing()) {
            int progress = (int) (pot.getGrowthProgress() * 100);
            String growthText = "Growing: " + progress + "%";
            int textWidth = mc.font.width(growthText);
            guiGraphics.drawString(mc.font, growthText, screenWidth / 2 - textWidth / 2, currentY, 0x55FF55, true);
            currentY += 12;
        }
        
        for (Map.Entry<Item, Integer> entry : herbs.entrySet()) {
            ItemStack stack = new ItemStack(entry.getKey());
            int amount = entry.getValue();
            
            guiGraphics.renderItem(stack, baseX, currentY);
            
            String amountText = String.valueOf(amount);
            int textX = baseX + 16 + 2;
            int textY = currentY + 4;
            guiGraphics.drawString(mc.font, amountText, textX, textY, 0xFFFFFF, true);
            
            currentY += 18;
        }
    }
}
