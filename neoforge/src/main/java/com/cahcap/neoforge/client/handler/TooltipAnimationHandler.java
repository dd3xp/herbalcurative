package com.cahcap.neoforge.client.handler;

/**
 * Tracks scale-up animation state for HUD tooltips.
 * Tooltip scales from 0 at the crosshair to full size at its final position.
 * Apply the returned scale via PoseStack before rendering, and use the returned Y offset.
 */
public class TooltipAnimationHandler {

    private static final long ANIMATION_DURATION_MS = 200; // 0.2 seconds

    private long animationStartTime = 0;
    private net.minecraft.core.BlockPos lastTarget = null;
    private boolean wasVisible = false;

    /**
     * Call when the tooltip IS visible. Pass the target block position.
     * Animation resets when looking at a different block.
     *
     * @param targetPos the block being looked at
     * @return eased progress: 0 = tiny at crosshair, 1 = full size
     */
    public float update(net.minecraft.core.BlockPos targetPos) {
        boolean targetChanged = lastTarget == null || !lastTarget.equals(targetPos);

        if (targetChanged || !wasVisible) {
            animationStartTime = System.currentTimeMillis();
            lastTarget = targetPos.immutable();
        }

        wasVisible = true;

        long elapsed = System.currentTimeMillis() - animationStartTime;
        float progress = Math.min(1.0f, (float) elapsed / ANIMATION_DURATION_MS);

        // Ease-out: fast start, slow end
        return 1.0f - (1.0f - progress) * (1.0f - progress);
    }

    /**
     * Call when the tooltip is NOT visible (looked away).
     * Resets state so next update() triggers animation.
     */
    public void reset() {
        wasVisible = false;
    }
}
