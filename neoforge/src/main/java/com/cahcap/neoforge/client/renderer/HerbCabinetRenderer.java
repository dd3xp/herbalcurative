package com.cahcap.neoforge.client.renderer;

import com.cahcap.common.blockentity.HerbCabinetBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/**
 * NeoForge-specific wrapper for HerbCabinetRenderer.
 */
public class HerbCabinetRenderer extends com.cahcap.client.renderer.HerbCabinetRenderer {

    public HerbCabinetRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public AABB getRenderBoundingBox(HerbCabinetBlockEntity blockEntity) {
        if (blockEntity.renderAABB != null) return blockEntity.renderAABB;
        AABB aabb = blockEntity.computeRenderAABB();
        if (aabb != null) return aabb;
        return AABB.ofSize(blockEntity.getBlockPos().getCenter(), 1, 1, 1);
    }
}
