package com.cahcap.neoforge.client.renderer;

import com.cahcap.common.blockentity.HerbPotBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

/**
 * NeoForge-specific wrapper for HerbPotRenderer
 * Adds custom render bounding box so the plant model renders when block is off-screen
 */
public class HerbPotRenderer extends com.cahcap.client.renderer.HerbPotRenderer {
    
    public HerbPotRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }
    
    @Override
    public void render(HerbPotBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        super.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }
    
    /**
     * Override render bounding box to include the plant model above the pot.
     * Without this, the seedling/plant model disappears when the block is off-screen
     * because the default AABB only covers the 1x1 block.
     */
    @Override
    public AABB getRenderBoundingBox(HerbPotBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        // Extend upward to include seedling/plant model (rendered above pot)
        return new AABB(
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1
        );
    }
}
