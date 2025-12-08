package com.cahcap.herbalcurative.client.model;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class HerbBoxModel<T extends LivingEntity> extends HumanoidModel<T> {
    
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "herb_box"), "main");

    public HerbBoxModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Required empty parts for HumanoidModel
        partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

        // Body with herb box attached
        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition Chest = body.addOrReplaceChild("Chest", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Box = Chest.addOrReplaceChild("Box", CubeListBuilder.create().texOffs(0, 0).addBox(-7.5F, -3.0F, 0.0F, 14.0F, 11.0F, 11.0F, new CubeDeformation(-2.0F)), PartPose.offset(0.5F, 2.0F, 1.0F));

        PartDefinition Fabric = Chest.addOrReplaceChild("Fabric", CubeListBuilder.create().texOffs(0, 22).addBox(-7.5F, 4.0F, 0.5F, 13.0F, 7.0F, 7.0F, new CubeDeformation(-2.0F)), PartPose.offset(1.0F, 2.0F, 1.5F));

        PartDefinition RightRope = Fabric.addOrReplaceChild("RightRope", CubeListBuilder.create(), PartPose.offsetAndRotation(-6.1409F, 7.5F, 4.0F, 0.0F, 0.0F, -3.1416F));

        RightRope.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(24, 36).addBox(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(0.3591F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        RightRope.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(16, 36).addBox(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(-0.7499F)), PartPose.offsetAndRotation(-0.1802F, -0.6931F, 0.0F, 0.0F, 0.0F, -0.3927F));

        RightRope.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 36).addBox(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(-0.7499F)), PartPose.offsetAndRotation(-0.1788F, 0.6932F, 0.0F, 0.0F, 0.0F, 0.3927F));

        PartDefinition LeftRope = Fabric.addOrReplaceChild("LeftRope", CubeListBuilder.create(), PartPose.offset(4.1409F, 7.5F, 4.0F));

        LeftRope.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(32, 36).addBox(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(0.3591F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        LeftRope.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(40, 36).addBox(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(-0.7499F)), PartPose.offsetAndRotation(-0.1802F, -0.6931F, 0.0F, 0.0F, 0.0F, -0.3927F));

        LeftRope.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(8, 36).addBox(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(-0.7499F)), PartPose.offsetAndRotation(-0.1788F, 0.6932F, 0.0F, 0.0F, 0.0F, 0.3927F));

        Chest.addOrReplaceChild("Pad", CubeListBuilder.create().texOffs(0, 38).addBox(-5.5F, 0.5F, 0.899F, 11.0F, 8.0F, 3.0F, new CubeDeformation(-0.999F)), PartPose.offset(0.0F, 0.0F, 0.1F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        // Only render body (which contains the herb box)
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}

