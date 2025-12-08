package com.cahcap.herbalcurative.neoforge.client;

import com.cahcap.herbalcurative.blockentity.HerbCabinetBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

/**
 * NeoForge-specific wrapper for HerbCabinetRenderer
 * Adds platform-specific rendering features like custom render bounding box
 */
public class HerbCabinetRenderer extends com.cahcap.herbalcurative.client.renderer.HerbCabinetRenderer {
    
    public HerbCabinetRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }
    
    @Override
    public void render(HerbCabinetBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        super.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }
    
    /**
     * Override render bounding box to include entire multiblock structure
     * This ensures the structure renders even when only part of it is visible
     */
    @Override
    public AABB getRenderBoundingBox(HerbCabinetBlockEntity blockEntity) {
        if (!blockEntity.formed) {
            return AABB.ofSize(blockEntity.getBlockPos().getCenter(), 1, 1, 1);
        }
        
        // Get master position
        BlockPos pos = blockEntity.getBlockPos();
        int[] offset = blockEntity.offset;
        BlockPos masterPos = pos.offset(-offset[0], -offset[1], -offset[2]);
        
        // Calculate the bounding box for the entire 3x2 multiblock
        Direction facing = blockEntity.facing;
        Direction right = facing.getClockWise();
        BlockPos bottomLeft = masterPos.relative(right.getOpposite());
        BlockPos topRight = bottomLeft.relative(Direction.UP, 1).relative(right, 2);
        
        // Create AABB that encompasses all blocks in the structure
        return new AABB(
            Math.min(bottomLeft.getX(), topRight.getX()),
            Math.min(bottomLeft.getY(), topRight.getY()),
            Math.min(bottomLeft.getZ(), topRight.getZ()),
            Math.max(bottomLeft.getX(), topRight.getX()) + 1,
            Math.max(bottomLeft.getY(), topRight.getY()) + 1,
            Math.max(bottomLeft.getZ(), topRight.getZ()) + 1
        );
    }
}
