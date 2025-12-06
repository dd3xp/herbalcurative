package com.cahcap.herbalcurative.client.model;

import com.cahcap.herbalcurative.HerbalCurative;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class HerbCabinetModel extends Model {
    
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(HerbalCurative.MODID, "herb_cabinet"), "main");
    
    private final ModelPart bottom;
    private final ModelPart top;
    private final ModelPart backside;
    private final ModelPart rightside;
    private final ModelPart leftside;
    private final ModelPart frontside;

    public HerbCabinetModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.bottom = root.getChild("bottom");
        this.top = root.getChild("top");
        this.backside = root.getChild("backside");
        this.rightside = root.getChild("rightside");
        this.leftside = root.getChild("leftside");
        this.frontside = root.getChild("frontside");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bottom = partdefinition.addOrReplaceChild("bottom", 
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-5.0F, -2.0F, -8.0F, 48.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), 
                PartPose.offset(-19.0F, 24.0F, 0.0F));

        PartDefinition top = partdefinition.addOrReplaceChild("top", 
                CubeListBuilder.create().texOffs(0, 18)
                        .addBox(-5.0F, -32.0F, -8.0F, 48.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), 
                PartPose.offset(-19.0F, 24.0F, 0.0F));

        PartDefinition backside = partdefinition.addOrReplaceChild("backside", 
                CubeListBuilder.create().texOffs(0, 36)
                        .addBox(-22.0F, -28.0F, -1.0F, 44.0F, 28.0F, 1.0F, new CubeDeformation(0.0F)), 
                PartPose.offset(0.0F, 22.0F, 7.0F));

        PartDefinition rightside = partdefinition.addOrReplaceChild("rightside", 
                CubeListBuilder.create()
                        .texOffs(90, 45).addBox(22.0F, -28.0F, -13.0F, 1.0F, 28.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 94).addBox(22.0F, -28.0F, -15.0F, 2.0F, 28.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 94).addBox(22.0F, -28.0F, -1.0F, 2.0F, 28.0F, 2.0F, new CubeDeformation(0.0F)), 
                PartPose.offset(0.0F, 22.0F, 7.0F));

        PartDefinition leftside = partdefinition.addOrReplaceChild("leftside", 
                CubeListBuilder.create()
                        .texOffs(8, 94).addBox(-1.0F, -28.0F, -3.0F, 2.0F, 28.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(90, 85).addBox(0.0F, -28.0F, -1.0F, 1.0F, 28.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 94).addBox(-1.0F, -28.0F, 11.0F, 2.0F, 28.0F, 2.0F, new CubeDeformation(0.0F)), 
                PartPose.offset(-23.0F, 22.0F, -5.0F));

        PartDefinition frontside = partdefinition.addOrReplaceChild("frontside", 
                CubeListBuilder.create()
                        .texOffs(38, 94).addBox(-22.0F, -28.0F, -1.0F, 2.0F, 28.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(32, 94).addBox(-64.0F, -28.0F, -1.0F, 2.0F, 28.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(90, 36).addBox(-62.0F, -2.0F, -1.0F, 40.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(90, 39).addBox(-62.0F, -28.0F, -1.0F, 40.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(90, 42).addBox(-62.0F, -15.0F, -1.0F, 40.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(44, 94).addBox(-50.0F, -26.0F, -1.0F, 2.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(50, 94).addBox(-36.0F, -26.0F, -1.0F, 2.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 65).addBox(-64.0F, -28.0F, 0.0F, 44.0F, 28.0F, 1.0F, new CubeDeformation(0.0F)), 
                PartPose.offset(42.0F, 22.0F, -6.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        bottom.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        top.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        backside.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rightside.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leftside.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        frontside.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
