package com.cahcap.client.renderer;

import com.cahcap.common.block.RedCherryShelfBlock;
import com.cahcap.common.blockentity.RedCherryShelfBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renderer for Red Cherry Shelf to display the stored item.
 */
public class RedCherryShelfRenderer implements BlockEntityRenderer<RedCherryShelfBlockEntity> {
    
    public RedCherryShelfRenderer(BlockEntityRendererProvider.Context context) {
        // No special initialization needed
    }
    
    @Override
    public void render(RedCherryShelfBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        
        if (!blockEntity.hasItem()) {
            return;
        }
        
        ItemStack stack = blockEntity.getItem();
        Direction facing = blockEntity.getBlockState().getValue(RedCherryShelfBlock.FACING);
        
        poseStack.pushPose();
        
        // Position at center of block
        poseStack.translate(0.5, 0.5, 0.5);
        
        // Rotate based on facing direction
        float yRot = switch (facing) {
            case NORTH -> 0;
            case SOUTH -> 180;
            case WEST -> 90;
            case EAST -> -90;
            default -> 0;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        
        // Move forward towards the face of the block
        // The shelf is 6 pixels thick (from z=10 to z=16), front face at z=10 (0.625)
        // Position the item slightly in front of the shelf
        poseStack.translate(0, 0, 0.12);
        
        // Scale the item down to fit on the shelf (12x12 pixel face)
        float scale = 0.45f;
        poseStack.scale(scale, scale, scale);
        
        // Enhance lighting
        int blockLight = LightTexture.block(packedLight);
        int skyLight = LightTexture.sky(packedLight);
        int enhancedLight = LightTexture.pack(Math.min(blockLight + 2, 15), Math.min(skyLight + 2, 15));
        
        // Render the item
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, enhancedLight, 
                OverlayTexture.NO_OVERLAY, poseStack, bufferSource, 
                blockEntity.getLevel(), 0);
        
        poseStack.popPose();
    }
    
    @Override
    public int getViewDistance() {
        return 64;
    }
}
