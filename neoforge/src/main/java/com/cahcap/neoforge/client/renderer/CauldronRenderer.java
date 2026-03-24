package com.cahcap.neoforge.client.renderer;

import com.cahcap.common.blockentity.cauldron.CauldronBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/**
 * NeoForge-specific wrapper for CauldronRenderer.
 */
public class CauldronRenderer extends com.cahcap.client.renderer.CauldronRenderer {

    public CauldronRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public AABB getRenderBoundingBox(CauldronBlockEntity blockEntity) {
        if (blockEntity.renderAABB != null) return blockEntity.renderAABB;
        AABB aabb = blockEntity.computeRenderAABB();
        if (aabb != null) return aabb;
        return AABB.ofSize(blockEntity.getBlockPos().getCenter(), 1, 1, 1);
    }
}
