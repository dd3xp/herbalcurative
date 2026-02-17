package com.cahcap.neoforge.client.handler;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.blockentity.HerbCabinetBlockEntity;
import com.cahcap.neoforge.common.registry.ModItems;
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
 * Client-side handler for rendering Herb Cabinet HUD tooltip
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID, value = Dist.CLIENT)
public class HerbCabinetTooltipHandler {

    /**
     * Get the herb item at the given index
     */
    private static Item getHerbItem(int index) {
        return switch (index) {
            case 0 -> ModItems.SCALEPLATE.get();
            case 1 -> ModItems.DEWPETAL_SHARD.get();
            case 2 -> ModItems.GOLDEN_LILYBELL.get();
            case 3 -> ModItems.CRYST_SPINE.get();
            case 4 -> ModItems.BURNT_NODE.get();
            case 5 -> ModItems.HEART_OF_STARDREAM.get();
            default -> null;
        };
    }

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
        BlockEntity blockEntity = mc.level.getBlockEntity(pos);

        if (!(blockEntity instanceof HerbCabinetBlockEntity cabinet)) {
            return;
        }

        // Only show tooltip if multiblock is formed
        if (!cabinet.isFormed()) {
            return;
        }

        // Only show tooltip when looking at the front face
        if (blockHitResult.getDirection() != cabinet.getFacing()) {
            return;
        }

        // Get which herb this block corresponds to
        int herbIndex = cabinet.getHerbIndexForBlock();
        if (herbIndex < 0 || herbIndex >= 6) {
            return;
        }

        // Get the herb at this slot
        Item herb = getHerbItem(herbIndex);
        if (herb == null) {
            return;
        }
        
        String herbKey = getHerbKey(herbIndex);
        int amount = cabinet.getHerbAmount(herbKey);

        // Don't render if slot is empty
        if (amount <= 0) {
            return;
        }

        // Prepare item stack for rendering
        ItemStack stack = new ItemStack(herb);

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        // Position below crosshair
        int x = screenWidth / 2 - 8; // Item icon is 16x16, so center it
        int y = screenHeight / 2 + 12;

        // Render the item icon
        guiGraphics.renderItem(stack, x, y);

        // Render the amount as text
        String amountText = String.valueOf(amount);
        int textX = x + 16 + 2; // Right of the item icon
        int textY = y + 4; // Vertically centered with icon
        guiGraphics.drawString(mc.font, amountText, textX, textY, 0xFFFFFF, true);
    }

    private static String getHerbKey(int index) {
        return switch (index) {
            case 0 -> "scaleplate";
            case 1 -> "dewpetal_shard";
            case 2 -> "golden_lilybell";
            case 3 -> "cryst_spine";
            case 4 -> "burnt_node";
            case 5 -> "heart_of_stardream";
            default -> "";
        };
    }
}

