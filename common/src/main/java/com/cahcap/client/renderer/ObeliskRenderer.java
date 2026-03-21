package com.cahcap.client.renderer;

import com.cahcap.common.blockentity.ObeliskBlockEntity;
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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;

/**
 * Renderer for the Obelisk multiblock structure.
 * Renders the offering item floating and rotating above the offering pedestal.
 */
public class ObeliskRenderer implements BlockEntityRenderer<ObeliskBlockEntity> {

    private static final float FLOAT_HEIGHT = 0.4f;
    private static final float BOB_AMPLITUDE = 0.1f;
    private static final float BOB_SPEED = 0.08f;
    private static final float ROTATION_SPEED = 2.0f;

    public ObeliskRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ObeliskBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (blockEntity.getLevel() == null || !blockEntity.isMaster()) {
            return;
        }

        ItemStack offeringItem = blockEntity.getOfferingItem();
        if (offeringItem.isEmpty()) {
            return;
        }

        double[] center = blockEntity.getPedestalCenter();
        if (center == null) {
            return;
        }

        BlockPos masterPos = blockEntity.getBlockPos();

        // Translate relative to master pos (poseStack origin)
        float offsetX = (float) (center[0] - masterPos.getX());
        float offsetY = (float) (center[1] - masterPos.getY());
        float offsetZ = (float) (center[2] - masterPos.getZ());

        // Light at the pedestal position
        BlockPos lightPos = BlockPos.containing(center[0], center[1] + 1, center[2]);
        int blockLight = blockEntity.getLevel().getBrightness(LightLayer.BLOCK, lightPos);
        int skyLight = blockEntity.getLevel().getBrightness(LightLayer.SKY, lightPos);
        int light = LightTexture.pack(blockLight, skyLight);

        // Animation
        long gameTime = blockEntity.getLevel().getGameTime();
        float time = gameTime + partialTick;
        float bobOffset = (float) Math.sin(time * BOB_SPEED) * BOB_AMPLITUDE;
        float rotationAngle = time * ROTATION_SPEED;

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        poseStack.pushPose();
        poseStack.translate(offsetX, offsetY + FLOAT_HEIGHT + bobOffset, offsetZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationAngle));

        itemRenderer.renderStatic(offeringItem, ItemDisplayContext.GROUND, light,
                OverlayTexture.NO_OVERLAY, poseStack, bufferSource,
                blockEntity.getLevel(), 0);

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(ObeliskBlockEntity blockEntity) {
        return true;
    }
}
