package com.cahcap.neoforge.client.renderer;

import com.cahcap.common.block.WorkbenchBlock;
import com.cahcap.common.blockentity.WorkbenchBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

/**
 * NeoForge-specific wrapper for WorkbenchRenderer
 * Adds platform-specific rendering features like custom render bounding box
 */
public class WorkbenchRenderer extends com.cahcap.client.renderer.WorkbenchRenderer {
    
    public WorkbenchRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }
    
    @Override
    public void render(WorkbenchBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        super.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }
    
    /**
     * Override render bounding box to include entire 3-block workbench structure
     * This ensures tools/materials render even when the center block is off-screen
     * Caches the AABB to avoid recalculation every frame
     */
    @Override
    public AABB getRenderBoundingBox(WorkbenchBlockEntity blockEntity) {
        // Return cached AABB if available
        if (blockEntity.renderAABB != null) {
            return blockEntity.renderAABB;
        }
        
        // Calculate and cache the bounding box for the entire 3-block workbench
        BlockPos centerPos = blockEntity.getBlockPos();
        Direction facing = blockEntity.getBlockState().getValue(WorkbenchBlock.FACING);
        
        // Workbench layout: LEFT - CENTER - RIGHT (perpendicular to facing)
        // Left is to the left when looking at the front, Right is to the right
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();
        
        BlockPos leftPos = centerPos.relative(left);
        BlockPos rightPos = centerPos.relative(right);
        
        // Create AABB that encompasses all 3 blocks plus some height for rendered items
        int minX = Math.min(Math.min(leftPos.getX(), centerPos.getX()), rightPos.getX());
        int minZ = Math.min(Math.min(leftPos.getZ(), centerPos.getZ()), rightPos.getZ());
        int maxX = Math.max(Math.max(leftPos.getX(), centerPos.getX()), rightPos.getX());
        int maxZ = Math.max(Math.max(leftPos.getZ(), centerPos.getZ()), rightPos.getZ());
        
        blockEntity.renderAABB = new AABB(
            minX,
            centerPos.getY(),
            minZ,
            maxX + 1,
            centerPos.getY() + 2, // Extra height for tool models
            maxZ + 1
        );
        
        return blockEntity.renderAABB;
    }
}
