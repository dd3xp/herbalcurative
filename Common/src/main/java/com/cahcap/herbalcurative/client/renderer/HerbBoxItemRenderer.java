package com.cahcap.herbalcurative.client.renderer;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.client.model.ItemHerbBoxModel;
import com.cahcap.herbalcurative.common.registry.ModRegistries;
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

public class HerbBoxItemRenderer extends BlockEntityWithoutLevelRenderer {
    
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HerbalCurativeCommon.MOD_ID, "textures/models/herb_box.png");
    
    private ItemHerbBoxModel<?> model;
    
    public HerbBoxItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), 
              Minecraft.getInstance().getEntityModels());
    }
    
    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, 
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        // In inventory/GUI context, render 2D texture instead of 3D model
        if (displayContext == ItemDisplayContext.GUI || 
            displayContext == ItemDisplayContext.FIXED ||
            displayContext == ItemDisplayContext.GROUND) {
            // Render 2D texture with correct display context
            render2DTexture(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
            return;
        }
        
        // For hand-held contexts, render 3D model
        if (model == null) {
            model = new ItemHerbBoxModel<>(
                    Minecraft.getInstance().getEntityModels().bakeLayer(ItemHerbBoxModel.LAYER_LOCATION));
        }
        
        poseStack.pushPose();
        
        // Center the model in hand
        poseStack.translate(0.5F, 0.5F, 0.5F);
        
        // Flip the model (X-axis 180°) because it's upside down from Blockbench
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        
        // Rotate around Y-axis (180°) to face the player (fix front-back orientation)
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        
        // Compensate for the Y=24 offset in the model (move up by 24/16 = 1.5 blocks)
        poseStack.translate(0.0F, -1.125F, 0.2F);
        
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
        
        poseStack.popPose();
    }
    
    /**
     * Render 2D texture in GUI/inventory/ground context
     * Uses a technical item (herb_box_icon) that has a normal generated model (like flowweave_ring)
     * This avoids infinite recursion because the technical item doesn't have a custom BEWLR
     */
    private void render2DTexture(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, 
                                 MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        
        // Correct the offset - move right and up to center the item
        poseStack.translate(0.5, 0.5, 0.5);
        
        // Get the technical item from the cross-platform registry
        net.minecraft.world.item.Item iconItem = ModRegistries.HERB_BOX_ICON.get();
        
        // Create an ItemStack of the technical item
        ItemStack iconStack = new ItemStack(iconItem);
        
        // Render the technical item using the standard renderer with CORRECT display context
        // Using the actual displayContext (not hardcoded GUI) ensures proper rotation behavior
        // This works because herb_box_icon uses minecraft:item/generated (no custom BEWLR)
        Minecraft.getInstance().getItemRenderer().renderStatic(
                iconStack, displayContext, packedLight, packedOverlay, 
                poseStack, buffer, null, 0);
        
        poseStack.popPose();
    }
}

