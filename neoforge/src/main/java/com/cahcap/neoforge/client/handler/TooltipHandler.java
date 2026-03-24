package com.cahcap.neoforge.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Base class for HUD tooltip handlers that display information when
 * the player looks at specific blocks.
 * <p>
 * Implements the template method pattern: common boilerplate (hit detection,
 * animation, pose stack management) lives here; subclasses provide block
 * identification and rendering logic.
 */
public abstract class TooltipHandler {

    private final TooltipAnimationHandler animator = new TooltipAnimationHandler();

    /**
     * Template method called from the static {@code @SubscribeEvent} method in each subclass.
     * Named {@code handleEvent} to avoid clash with the static {@code onRenderGuiPost}
     * required by the {@code @EventBusSubscriber} / {@code @SubscribeEvent} pattern.
     */
    public void handleEvent(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null) {
            handleNoTarget();
            return;
        }

        HitResult hitResult = mc.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            animator.reset();
            return;
        }

        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        BlockPos targetPos = blockHitResult.getBlockPos();
        BlockState state = mc.level.getBlockState(targetPos);

        if (!isTargetBlock(state)) {
            animator.reset();
            return;
        }

        BlockEntity blockEntity = mc.level.getBlockEntity(targetPos);
        if (!isValidEntity(blockEntity)) {
            animator.reset();
            return;
        }

        if (!additionalValidation(blockEntity, state, blockHitResult, targetPos)) {
            animator.reset();
            return;
        }

        if (!hasContent(blockEntity)) {
            animator.reset();
            return;
        }

        BlockPos animPos = getAnimationPos(blockEntity, targetPos);
        float anim = animator.update(animPos);

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, centerY, 0);
        guiGraphics.pose().scale(anim, anim, 1.0f);
        guiGraphics.pose().translate(-centerX, -centerY, 0);

        renderContent(guiGraphics, mc, blockEntity, screenWidth, screenHeight);

        guiGraphics.pose().popPose();
    }

    /**
     * Called when level or player is null.
     * Default does nothing (just returns).
     * Override to call {@link #resetAnimator()} if the handler should
     * reset animation state when the player/level is unavailable.
     */
    protected void handleNoTarget() {
        // Most handlers simply return without resetting
    }

    /** Reset the animation state. Exposed for subclass use. */
    protected void resetAnimator() {
        animator.reset();
    }

    /** Return {@code true} if the block state is the type this handler cares about. */
    protected abstract boolean isTargetBlock(BlockState state);

    /** Return {@code true} if the block entity is the expected type. */
    protected abstract boolean isValidEntity(BlockEntity entity);

    /**
     * Optional extra validation (e.g. formed check, grid cell hit test).
     * Default returns {@code true}.
     */
    protected boolean additionalValidation(BlockEntity entity, BlockState state,
                                           BlockHitResult hitResult, BlockPos pos) {
        return true;
    }

    /**
     * Return {@code true} if there is content worth displaying.
     * Called after all validation passes.  Default returns {@code true}.
     */
    protected boolean hasContent(BlockEntity entity) {
        return true;
    }

    /**
     * Return the {@link BlockPos} used for animation tracking.
     * Default returns {@code targetPos}.  Override when the handler
     * should track a different position (e.g. the master entity).
     */
    protected BlockPos getAnimationPos(BlockEntity entity, BlockPos targetPos) {
        return targetPos;
    }

    /** Render the tooltip content inside the already-scaled pose stack. */
    protected abstract void renderContent(GuiGraphics graphics, Minecraft mc,
                                          BlockEntity entity, int screenWidth, int screenHeight);
}
