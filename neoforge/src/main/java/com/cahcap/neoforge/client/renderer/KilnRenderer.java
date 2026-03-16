package com.cahcap.neoforge.client.renderer;

import com.cahcap.common.blockentity.KilnBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/**
 * NeoForge-specific wrapper for KilnRenderer.
 */
public class KilnRenderer extends com.cahcap.client.renderer.KilnRenderer {

    public KilnRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public AABB getRenderBoundingBox(KilnBlockEntity blockEntity) {
        if (blockEntity.renderAABB != null) return blockEntity.renderAABB;
        AABB aabb = blockEntity.computeRenderAABB();
        if (aabb != null) return aabb;
        return AABB.ofSize(blockEntity.getBlockPos().getCenter(), 1, 1, 1);
    }
}
