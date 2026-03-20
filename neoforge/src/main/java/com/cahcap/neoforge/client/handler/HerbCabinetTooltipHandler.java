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

    private static final TooltipAnimator animator = new TooltipAnimator();

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
            animator.reset();
            return;
        }

        // Find herb cabinet and determine content
        HerbCabinetBlockEntity cabinet = null;
        BlockPos targetPos = null;
        Item herb = null;
        int amount = 0;

        HitResult hitResult = mc.hitResult;
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            targetPos = blockHitResult.getBlockPos();
            BlockEntity blockEntity = mc.level.getBlockEntity(targetPos);
            if (blockEntity instanceof HerbCabinetBlockEntity cab && cab.isFormed()
                    && blockHitResult.getDirection() == cab.getFacing()) {
                int herbIndex = cab.getHerbIndexForBlock();
                if (herbIndex >= 0 && herbIndex < 6
                        && HerbCabinetBlock.isHitInGridCell(blockHitResult, targetPos,
                                cab.getFacing(), herbIndex)) {
                    herb = getHerbItem(herbIndex);
                    if (herb != null) {
                        String herbKey = getHerbKey(herbIndex);
                        amount = cab.getHerbAmount(herbKey);
                        if (amount > 0) {
                            cabinet = cab;
                        }
                    }
                }
            }
        }

        if (cabinet == null) {
            animator.reset();
            return;
        }

        float anim = animator.update(targetPos);

        // Prepare item stack for rendering
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

        guiGraphics.pose().popPose();
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

