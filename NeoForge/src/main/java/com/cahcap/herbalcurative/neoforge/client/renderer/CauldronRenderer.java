package com.cahcap.herbalcurative.neoforge.client.renderer;

import com.cahcap.herbalcurative.common.blockentity.CauldronBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

/**
 * NeoForge-specific wrapper for CauldronRenderer
 * Adds platform-specific rendering features like custom render bounding box
 */
public class CauldronRenderer extends com.cahcap.herbalcurative.client.renderer.CauldronRenderer {
    
    public CauldronRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }
    
    @Override
    public void render(CauldronBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        super.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }
    
    /**
     * Override render bounding box to include entire multiblock structure
     * This ensures the liquid renders even when the master block is off-screen
     * Caches the AABB to avoid recalculation every frame
     */
    @Override
    public AABB getRenderBoundingBox(CauldronBlockEntity blockEntity) {
        // Return cached AABB if available
        if (blockEntity.renderAABB != null) {
            return blockEntity.renderAABB;
        }
        
        // If not formed, use small box around this block
        if (!blockEntity.formed) {
            blockEntity.renderAABB = AABB.ofSize(blockEntity.getBlockPos().getCenter(), 1, 1, 1);
            return blockEntity.renderAABB;
        }
        
        // Calculate and cache the bounding box for the entire 3x3x2 multiblock
        BlockPos masterPos = blockEntity.getMasterPos();
        if (masterPos == null) {
            masterPos = blockEntity.getBlockPos();
        }
        
        // Multiblock is 3x3x2:
        // Layer 1 (y=0): masterPos ± 1 in X and Z
        // Layer 2 (y=1): masterPos ± 1 in X and Z, +1 in Y
        BlockPos minPos = masterPos.offset(-1, 0, -1);
        BlockPos maxPos = masterPos.offset(1, 1, 1);
        
        // Create and cache AABB that encompasses all blocks in the structure
        blockEntity.renderAABB = new AABB(
            minPos.getX(),
            minPos.getY(),
            minPos.getZ(),
            maxPos.getX() + 1,
            maxPos.getY() + 1,
            maxPos.getZ() + 1
        );
        
        return blockEntity.renderAABB;
    }
}
