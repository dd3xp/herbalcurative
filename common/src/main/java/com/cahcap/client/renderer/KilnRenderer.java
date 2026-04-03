package com.cahcap.client.renderer;

import com.cahcap.common.blockentity.KilnBlockEntity;
import com.cahcap.common.registry.ModRegistries;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Renderer for the Kiln multiblock structure.
 * Renders the Pyraze plant model on the soulsand element after assembly.
 */
public class KilnRenderer implements BlockEntityRenderer<KilnBlockEntity> {

    private final BlockRenderDispatcher blockRenderer;

    public KilnRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(KilnBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.isFormed() || !blockEntity.isMaster()) {
            return;
        }

        if (blockEntity.getLevel() == null) {
            return;
        }

        // Render Pyraze plant on the soulsand element
        // Soulsand is at dy=-1 (one block below master), center
        // In the model, soulsand element: from [0, -16, 0] to [16, -12, 16]
        // So soulsand top surface is at y = -12/16 relative to master block origin
        // The pyraze plant should sit on top of that

        Block pyrazeBlock = (Block) ModRegistries.PYRAZE.get();
        BlockState pyrazeState = pyrazeBlock.defaultBlockState();
        BakedModel model = blockRenderer.getBlockModel(pyrazeState);

        // Get light level at the soulsand position (one below master)
        BlockPos soulsandPos = blockEntity.getBlockPos().below();
        int blockLight = blockEntity.getLevel().getBrightness(LightLayer.BLOCK, soulsandPos);
        int skyLight = blockEntity.getLevel().getBrightness(LightLayer.SKY, soulsandPos);
        int light = LightTexture.pack(blockLight, skyLight);

        poseStack.pushPose();

        // Translate to soulsand top: master origin is (0,0,0), soulsand top is at y=-12/16
        poseStack.translate(0, -12.0 / 16.0, 0);

        // Render the pyraze block model at full size (same as placed on ground)
        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(),
                bufferSource.getBuffer(RenderType.cutout()),
                pyrazeState,
                model,
                1.0f, 1.0f, 1.0f,
                light,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
    }

}
