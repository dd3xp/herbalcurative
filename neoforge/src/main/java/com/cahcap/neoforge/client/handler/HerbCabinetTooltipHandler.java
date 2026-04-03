package com.cahcap.neoforge.client.handler;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.block.HerbCabinetBlock;
import com.cahcap.common.blockentity.HerbCabinetBlockEntity;
import com.cahcap.neoforge.common.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
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
 * Client-side handler for rendering Herb Cabinet HUD tooltip
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID, value = Dist.CLIENT)
public class HerbCabinetTooltipHandler extends TooltipHandler {

    private static final HerbCabinetTooltipHandler INSTANCE = new HerbCabinetTooltipHandler();

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
        // because we need the block entity to check formed state and facing.
        return true;
    }

    @Override
    protected boolean isValidEntity(BlockEntity entity) {
        return entity instanceof HerbCabinetBlockEntity;
    }

    @Override
    protected boolean additionalValidation(BlockEntity entity, BlockState state,
                                           BlockHitResult hitResult, BlockPos pos) {
        HerbCabinetBlockEntity cab = (HerbCabinetBlockEntity) entity;
        if (!cab.isFormed() || hitResult.getDirection() != cab.getFacing()) {
            return false;
        }

        int herbIndex = cab.getHerbIndexForBlock();
        if (herbIndex < 0 || herbIndex >= 6) {
            return false;
        }

        if (!HerbCabinetBlock.isHitInGridCell(hitResult, pos, cab.getFacing(), herbIndex)) {
            return false;
        }

        herb = getHerbItem(herbIndex);
        if (herb == null) {
            return false;
        }

        String herbKey = getHerbKey(herbIndex);
        amount = cab.getHerbAmount(herbKey);
        return amount > 0;
    }

    @Override
    protected void renderContent(GuiGraphics guiGraphics, Minecraft mc,
                                 BlockEntity entity, int screenWidth, int screenHeight) {
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        ItemStack stack = new ItemStack(herb);

        // Position below crosshair
        int x = centerX - 8; // Item icon is 16x16, so center it
        int y = centerY + 10;

        // Render the item icon
        guiGraphics.renderItem(stack, x, y);

        // Render the amount as text
        String amountText = String.valueOf(amount);
        int textX = x + 16 + 2; // Right of the item icon
        int textY = y + 4; // Vertically centered with icon
        guiGraphics.drawString(mc.font, amountText, textX, textY, 0xFFFFFF, true);
    }

    /**
     * Get the herb item at the given index
     */
    private static Item getHerbItem(int index) {
        return switch (index) {
            case 0 -> ModItems.SCALEPLATE.get();
            case 1 -> ModItems.DEWPETAL.get();
            case 2 -> ModItems.ZEPHYR_BLOSSOM.get();
            case 3 -> ModItems.CRYST_SPINE.get();
            case 4 -> ModItems.PYRO_NODE.get();
            case 5 -> ModItems.STELLAR_MOTE.get();
            default -> null;
        };
    }

    private static String getHerbKey(int index) {
        return switch (index) {
            case 0 -> "scaleplate";
            case 1 -> "dewpetal";
            case 2 -> "zephyr_blossom";
            case 3 -> "cryst_spine";
            case 4 -> "pyro_node";
            case 5 -> "stellar_mote";
            default -> "";
        };
    }
}
