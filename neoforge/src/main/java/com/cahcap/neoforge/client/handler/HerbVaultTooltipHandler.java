package com.cahcap.neoforge.client.handler;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.block.HerbVaultBlock;
import com.cahcap.common.blockentity.HerbVaultBlockEntity;
import com.cahcap.common.util.HerbRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Client-side handler for rendering Herb Vault HUD tooltip.
 * Shows herb icon and count below crosshair when looking at a herb slot on the front face.
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID, value = Dist.CLIENT)
public class HerbVaultTooltipHandler extends TooltipHandler {

    private static final HerbVaultTooltipHandler INSTANCE = new HerbVaultTooltipHandler();

    // Per-frame state
    private Item herb;
    private int amount;

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        INSTANCE.handleEvent(event);
    }

    @Override
    protected void handleNoTarget() {
        resetAnimator();
    }

    @Override
    protected boolean isTargetBlock(BlockState state) {
        // Block type check is deferred to isValidEntity/additionalValidation
        return true;
    }

    @Override
    protected boolean isValidEntity(BlockEntity entity) {
        return entity instanceof HerbVaultBlockEntity;
    }

    @Override
    protected boolean additionalValidation(BlockEntity entity, BlockState state,
                                           BlockHitResult hitResult, BlockPos pos) {
        HerbVaultBlockEntity vault = (HerbVaultBlockEntity) entity;
        if (!vault.isFormed() || hitResult.getDirection() != vault.getFacing()) {
            return false;
        }

        int[] offset = vault.getOffset();
        Direction facing = vault.getFacing();
        int forwardOffset = facing.getStepX() * offset[0] + facing.getStepZ() * offset[2];
        if (forwardOffset != 1 || offset[1] > 0) {
            return false;
        }

        int herbIndex = vault.getHerbIndexForBlock();
        if (herbIndex < 0 || herbIndex >= 6) {
            return false;
        }

        if (!HerbVaultBlock.isHitInGridCell(hitResult, pos, facing, herbIndex)) {
            return false;
        }

        herb = HerbRegistry.getAllHerbItems()[herbIndex];
        amount = vault.getHerbAmount(herb);
        return herb != null && amount > 0;
    }

    @Override
    protected void renderContent(GuiGraphics guiGraphics, Minecraft mc,
                                 BlockEntity entity, int screenWidth, int screenHeight) {
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        ItemStack stack = new ItemStack(herb);

        int x = centerX - 8;
        int y = centerY + 10;

        guiGraphics.renderItem(stack, x, y);

        String amountText = String.valueOf(amount);
        int textX = x + 16 + 2;
        int textY = y + 4;
        guiGraphics.drawString(mc.font, amountText, textX, textY, 0xFFFFFF, true);
    }
}
