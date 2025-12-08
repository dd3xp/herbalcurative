package com.cahcap.herbalcurative.client.renderer;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.client.model.HerbCabinetModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Custom item renderer for Herb Cabinet
 * Renders the full 3D multiblock model in WAILA/Jade/WTHIT tooltips
 */
public class HerbCabinetItemRenderer extends BlockEntityWithoutLevelRenderer {
    
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HerbalCurativeCommon.MOD_ID, "textures/models/herb_cabinet.png");
    
    private HerbCabinetModel model;
    
    public HerbCabinetItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), 
              Minecraft.getInstance().getEntityModels());
    }
    
    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, 
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (model == null) {
            model = new HerbCabinetModel(
                    Minecraft.getInstance().getEntityModels().bakeLayer(HerbCabinetModel.LAYER_LOCATION));
        }
        
        poseStack.pushPose();
        
        // Center the model
        poseStack.translate(0.5F, 0.5F, 0.5F);

        // Flip the model upside down first (models are usually built upside down)
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

        // Adjust positioning - move model down to sit properly
        poseStack.translate(0.0F, -1.1875F, 0.0F);
        
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
        
        poseStack.popPose();
    }
}

