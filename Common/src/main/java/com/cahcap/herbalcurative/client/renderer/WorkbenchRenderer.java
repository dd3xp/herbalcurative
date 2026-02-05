package com.cahcap.herbalcurative.client.renderer;

import com.cahcap.herbalcurative.common.block.WorkbenchBlock;
import com.cahcap.herbalcurative.common.blockentity.WorkbenchBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;

import java.util.List;

/**
 * Renderer for Workbench to display stored items on the table surface.
 * 
 * Layout on table top (when facing north):
 * - Left block: 4 tool slots in a 2x2 grid
 * - Center block: 1 input slot in the middle
 * - Right block: 9 material slots in a 3x3 grid
 */
public class WorkbenchRenderer implements BlockEntityRenderer<WorkbenchBlockEntity> {
    
    // Item rendering scale
    private static final float ITEM_SCALE = 0.35f;
    
    public WorkbenchRenderer(BlockEntityRendererProvider.Context context) {
        // No special initialization needed
    }
    
    @Override
    public void render(WorkbenchBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        
        if (blockEntity.getLevel() == null) {
            return;
        }
        
        Direction facing = blockEntity.getBlockState().getValue(WorkbenchBlock.FACING);
        
        poseStack.pushPose();
        
        // Rotate based on facing direction
        poseStack.translate(0.5, 0, 0.5);
        float yRot = switch (facing) {
            case NORTH -> 0;
            case SOUTH -> 180;
            case WEST -> 90;
            case EAST -> -90;
            default -> 0;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.translate(-0.5, 0, -0.5);
        
        // Calculate light at this position
        BlockPos pos = blockEntity.getBlockPos();
        int blockLight = blockEntity.getLevel().getBrightness(LightLayer.BLOCK, pos.above());
        int skyLight = blockEntity.getLevel().getBrightness(LightLayer.SKY, pos.above());
        int light = LightTexture.pack(blockLight, skyLight);
        
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        
        // Render tools on left block (relative positions, looking at front of workbench)
        // Left block is at x-1 relative to center in local coordinates
        // Tool layout on top surface: 
        // [0: front-left] [1: front-right]
        // [2: back-left]  [3: back-right]
        renderToolSlots(blockEntity, poseStack, bufferSource, itemRenderer, light);
        
        // Render input item on center block
        renderInputSlot(blockEntity, poseStack, bufferSource, itemRenderer, light);
        
        // Render materials on right block (stacked appearance)
        renderMaterialSlots(blockEntity, poseStack, bufferSource, itemRenderer, light);
        
        poseStack.popPose();
    }
    
    private void renderToolSlots(WorkbenchBlockEntity workbench, PoseStack poseStack,
                                  MultiBufferSource bufferSource, ItemRenderer itemRenderer, int light) {
        // Tool positions on the left block (x offset -1 from center in local space after rotation)
        // After rotation, "left" is at local x-1
        Direction facing = workbench.getBlockState().getValue(WorkbenchBlock.FACING);
        Direction left = facing.getCounterClockWise();
        
        // Calculate offsets in rotated space
        // Tool slot positions on the left block's top surface
        float[][] toolPositions = {
            {0.25f, 0.25f},  // Slot 0: front-left (local x, z after rotation)
            {0.75f, 0.25f},  // Slot 1: front-right
            {0.25f, 0.75f},  // Slot 2: back-left
            {0.75f, 0.75f}   // Slot 3: back-right
        };
        
        for (int i = 0; i < WorkbenchBlockEntity.TOOL_SLOTS; i++) {
            if (workbench.hasToolAt(i)) {
                ItemStack tool = workbench.getToolAt(i);
                
                poseStack.pushPose();
                
                // Position on left block
                // In rotated local coords, left block is at x-1
                float localX = toolPositions[i][0] - 1.0f;
                float localZ = toolPositions[i][1];
                
                poseStack.translate(localX, 1.02, localZ);
                
                // Lay flat on the table
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                
                poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
                
                itemRenderer.renderStatic(tool, ItemDisplayContext.FIXED, light,
                        OverlayTexture.NO_OVERLAY, poseStack, bufferSource,
                        workbench.getLevel(), 0);
                
                poseStack.popPose();
            }
        }
    }
    
    private void renderInputSlot(WorkbenchBlockEntity workbench, PoseStack poseStack,
                                  MultiBufferSource bufferSource, ItemRenderer itemRenderer, int light) {
        if (!workbench.hasInputItem()) {
            return;
        }
        
        ItemStack input = workbench.getInputItem();
        
        poseStack.pushPose();
        
        // Center of the center block
        poseStack.translate(0.5, 1.02, 0.5);
        
        // Lay flat on the table
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        
        poseStack.scale(ITEM_SCALE * 1.2f, ITEM_SCALE * 1.2f, ITEM_SCALE * 1.2f);
        
        itemRenderer.renderStatic(input, ItemDisplayContext.FIXED, light,
                OverlayTexture.NO_OVERLAY, poseStack, bufferSource,
                workbench.getLevel(), 0);
        
        poseStack.popPose();
    }
    
    private void renderMaterialSlots(WorkbenchBlockEntity workbench, PoseStack poseStack,
                                      MultiBufferSource bufferSource, ItemRenderer itemRenderer, int light) {
        List<ItemStack> materials = workbench.getMaterials();
        if (materials.isEmpty()) {
            return;
        }
        
        // Material positions on the right block (x offset +1 from center in local space)
        // Render as a 3x3 grid layout
        // Grid positions (row, col) -> slot index:
        // [0][1][2]   front row
        // [3][4][5]   middle row
        // [6][7][8]   back row
        
        float baseY = 1.02f;
        float cellSize = 0.28f;  // Size of each grid cell
        float gridStartX = 0.18f;  // Start position (left edge of grid on right block)
        float gridStartZ = 0.18f;  // Start position (front edge of grid)
        
        for (int i = 0; i < materials.size(); i++) {
            ItemStack mat = materials.get(i);
            if (mat.isEmpty()) continue;
            
            poseStack.pushPose();
            
            // Calculate grid position (3x3)
            int col = i % 3;  // 0, 1, 2
            int row = i / 3;  // 0, 1, 2
            
            // Position on right block in 3x3 grid
            float xOffset = gridStartX + col * cellSize;
            float zOffset = gridStartZ + row * cellSize;
            
            poseStack.translate(1.0f + xOffset, baseY, zOffset);
            
            // Lay flat on the table
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
            
            // Slightly smaller scale for 3x3 grid to fit
            float gridScale = ITEM_SCALE * 0.8f;
            poseStack.scale(gridScale, gridScale, gridScale);
            
            itemRenderer.renderStatic(mat, ItemDisplayContext.FIXED, light,
                    OverlayTexture.NO_OVERLAY, poseStack, bufferSource,
                    workbench.getLevel(), 0);
            
            poseStack.popPose();
        }
    }
    
    @Override
    public int getViewDistance() {
        return 64;
    }
}
