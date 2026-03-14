package com.cahcap.neoforge.client.renderer;

import com.cahcap.common.blockentity.HerbVaultBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

/**
 * NeoForge-specific HerbVaultRenderer with custom render bounding box.
 */
public class HerbVaultRenderer extends com.cahcap.client.renderer.HerbVaultRenderer {

    public HerbVaultRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public AABB getRenderBoundingBox(HerbVaultBlockEntity blockEntity) {
        if (blockEntity.renderAABB != null) return blockEntity.renderAABB;
        AABB aabb = blockEntity.computeRenderAABB();
        if (aabb != null) return aabb;
        return AABB.ofSize(blockEntity.getBlockPos().getCenter(), 1, 1, 1);
    }
}
