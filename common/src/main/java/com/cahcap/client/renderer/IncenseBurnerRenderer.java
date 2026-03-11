package com.cahcap.client.renderer;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.blockentity.IncenseBurnerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LightLayer;

/**
 * Renderer for Incense Burner.
 * Renders the powder layer inside the burner when powder is present.
 * The powder layer color is determined by the type of powder.
 */
public class IncenseBurnerRenderer implements BlockEntityRenderer<IncenseBurnerBlockEntity> {
    
    private static final ResourceLocation POWDER_LAYER_TEXTURE = 
            ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "textures/block/incense_powder_layer.png");
    
    private static final float LAYER_Y = 3.01f / 16f;
    
    public IncenseBurnerRenderer(BlockEntityRendererProvider.Context context) {
    }
    
    @Override
    public void render(IncenseBurnerBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.hasPowder()) {
            return;
        }
        
        if (blockEntity.getLevel() == null) {
            return;
        }
        
        int color = blockEntity.getPowderColor();
        
        BlockPos pos = blockEntity.getBlockPos();
        int blockLight = blockEntity.getLevel().getBrightness(LightLayer.BLOCK, pos.above());
        int skyLight = blockEntity.getLevel().getBrightness(LightLayer.SKY, pos.above());
        int light = LightTexture.pack(blockLight, skyLight);
        
        poseStack.pushPose();
        
        renderPowderLayer(poseStack, bufferSource, light, color);
        
        poseStack.popPose();
    }
    
    private void renderPowderLayer(PoseStack poseStack, MultiBufferSource bufferSource, 
                                    int packedLight, int color) {
        // Use entity cutout render type with direct texture binding
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutout(POWDER_LAYER_TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        
        // UV coordinates for the full texture (0-1)
        float u0 = 0f;
        float u1 = 1f;
        float v0 = 0f;
        float v1 = 1f;
        
        // Top face of the powder layer - render full block face (0-1)
        buffer.addVertex(pose, 0f, LAYER_Y, 1f)
                .setColor(r, g, b, 1.0f)
                .setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0, 1, 0);
        buffer.addVertex(pose, 1f, LAYER_Y, 1f)
                .setColor(r, g, b, 1.0f)
                .setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0, 1, 0);
        buffer.addVertex(pose, 1f, LAYER_Y, 0f)
                .setColor(r, g, b, 1.0f)
                .setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0, 1, 0);
        buffer.addVertex(pose, 0f, LAYER_Y, 0f)
                .setColor(r, g, b, 1.0f)
                .setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0, 1, 0);
    }
}
