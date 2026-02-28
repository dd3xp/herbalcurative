package com.cahcap.neoforge.client.renderer;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.client.model.HerbBoxModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class HerbBoxItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HerbalCurativeCommon.MOD_ID, "textures/models/herb_box.png");

    private HerbBoxModel<?> model;

    public HerbBoxItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (model == null) {
            model = new HerbBoxModel<>(Minecraft.getInstance().getEntityModels()
                    .bakeLayer(HerbBoxModel.LAYER_LOCATION));
        }

        poseStack.pushPose();

        // Entity model space → item block model space:
        // Flip Y (entity Y-down → world Y-up) and Z (entity +Z is player back → item front)
        poseStack.translate(0.5, 0.72, 1.16);
        poseStack.scale(1.0F, -1.0F, -1.0F);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);

        poseStack.popPose();
    }
}
