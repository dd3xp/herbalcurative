package com.cahcap.herbalcurative.client.model;

import com.cahcap.herbalcurative.HerbalCurative;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public class WeaveleafArmorModel<T extends LivingEntity> extends HumanoidModel<T> {
    
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(HerbalCurative.MODID, "weaveleaf_armor"), "main");

    private final ModelPart Head;
    private final ModelPart Body;
    private final ModelPart RightArm;
    private final ModelPart LeftArm;
    private final ModelPart RightLegLeggings;
    private final ModelPart LeftLegLeggings;
    private final ModelPart RightLegBoots;
    private final ModelPart LeftLegBoots;
    
    private EquipmentSlot currentSlot = EquipmentSlot.CHEST;

    public WeaveleafArmorModel(ModelPart root) {
        super(root);
        this.Head = root.getChild("Head");
        this.Body = root.getChild("Body");
        this.RightArm = root.getChild("RightArm");
        this.LeftArm = root.getChild("LeftArm");
        this.RightLegLeggings = root.getChild("RightLegLeggings");
        this.LeftLegLeggings = root.getChild("LeftLegLeggings");
        this.RightLegBoots = root.getChild("RightLegBoots");
        this.LeftLegBoots = root.getChild("LeftLegBoots");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Required by HumanoidModel but we use our own parts
        partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

        // ==================== Actual model parts from Blockbench ====================
        
        partdefinition.addOrReplaceChild("Head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F))
                .texOffs(32, 0).mirror().addBox(-4.0F, -8.0F, 5.0F, 8.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), 
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Body", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 11.0F, 4.0F, new CubeDeformation(1.01F)), 
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("RightArm", CubeListBuilder.create()
                .texOffs(24, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(1.0F))
                .texOffs(40, 16).addBox(-3.0F, 3.0F, 2.7F, 4.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(40, 23).addBox(-2.5F, 6.0F, 3.4F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), 
                PartPose.offset(-5.0F, 2.0F, 0.0F));

        partdefinition.addOrReplaceChild("LeftArm", CubeListBuilder.create()
                .texOffs(40, 16).mirror().addBox(-1.0F, 3.0F, 2.7F, 4.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(40, 23).mirror().addBox(-0.5F, 6.0F, 3.3F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(24, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(1.0F)).mirror(false), 
                PartPose.offset(5.0F, 2.0F, 0.0F));

        // Leggings
        PartDefinition RightLegLeggings = partdefinition.addOrReplaceChild("RightLegLeggings", 
                CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition RightRobeSide = RightLegLeggings.addOrReplaceChild("RightRobeSide", 
                CubeListBuilder.create(), PartPose.offsetAndRotation(-3.3945F, 2.3493F, 0.0F, 0.0F, 0.0F, 0.0436F));

        RightRobeSide.addOrReplaceChild("RightRobeSideCenter_r1", CubeListBuilder.create()
                .texOffs(0, 31).addBox(-4.5F, -4.5F, -0.5F, 5.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), 
                PartPose.offsetAndRotation(-0.0109F, 0.0826F, 2.5F, 0.0F, -1.5708F, 0.1309F));

        RightRobeSide.addOrReplaceChild("RightRobeSideFront_r1", CubeListBuilder.create()
                .texOffs(36, 31).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), 
                PartPose.offsetAndRotation(0.0544F, -0.4131F, -2.5F, 0.0F, -1.5708F, 0.1309F));

        PartDefinition RightRobeBack = RightLegLeggings.addOrReplaceChild("RightRobeBack", 
                CubeListBuilder.create(), PartPose.offsetAndRotation(-1.35F, 2.6716F, 3.3657F, 0.0436F, 0.0F, 0.0F));

        RightRobeBack.addOrReplaceChild("RightRobeBackSide_r1", CubeListBuilder.create()
                .texOffs(22, 31).mirror().addBox(-2.0F, -5.0F, -0.5F, 4.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), 
                PartPose.offsetAndRotation(1.25F, 0.2479F, 0.0326F, 0.1309F, 0.0F, 0.0F));

        RightRobeBack.addOrReplaceChild("RightRobeBackSide_r2", CubeListBuilder.create()
                .texOffs(32, 31).addBox(-0.5F, -4.5F, -0.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), 
                PartPose.offsetAndRotation(-1.25F, -0.2479F, -0.0326F, 0.1309F, 0.0F, 0.0F));

        RightLegLeggings.addOrReplaceChild("RightRobeRope", CubeListBuilder.create()
                .texOffs(40, 31).addBox(-1.0F, -0.5F, -2.64F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), 
                PartPose.offsetAndRotation(-0.04F, -1.4211F, 1.6F, 0.0F, 1.5708F, 0.0F));

        PartDefinition LeftLegLeggings = partdefinition.addOrReplaceChild("LeftLegLeggings", 
                CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition LeftRobeSide = LeftLegLeggings.addOrReplaceChild("LeftRobeSide", 
                CubeListBuilder.create(), PartPose.offsetAndRotation(3.3945F, 2.3493F, 0.0F, 0.0F, 0.0F, -0.0436F));

        LeftRobeSide.addOrReplaceChild("LeftRobeSideCenter_r1", CubeListBuilder.create()
                .texOffs(0, 31).mirror().addBox(-3.5F, -4.5F, -0.5F, 5.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), 
                PartPose.offsetAndRotation(0.0109F, 0.0826F, -0.5F, 0.0F, 1.5708F, -0.1309F));

        LeftRobeSide.addOrReplaceChild("LeftRobeSideFront_r1", CubeListBuilder.create()
                .texOffs(36, 31).mirror().addBox(-0.5F, -4.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), 
                PartPose.offsetAndRotation(-0.0544F, -0.4131F, -2.5F, 0.0F, 1.5708F, -0.1309F));

        PartDefinition LeftRobeBack = LeftLegLeggings.addOrReplaceChild("LeftRobeBack", 
                CubeListBuilder.create(), PartPose.offsetAndRotation(1.35F, 2.6716F, 3.3657F, 0.0436F, 0.0F, 0.0F));

        LeftRobeBack.addOrReplaceChild("LeftRobeBackSide_r1", CubeListBuilder.create()
                .texOffs(12, 31).mirror().addBox(-2.0F, -5.0F, -0.5F, 4.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), 
                PartPose.offsetAndRotation(-1.25F, 0.2479F, 0.0326F, 0.1309F, 0.0F, 0.0F));

        LeftRobeBack.addOrReplaceChild("LeftRobeBackSide_r2", CubeListBuilder.create()
                .texOffs(32, 31).mirror().addBox(-0.5F, -4.5F, -0.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), 
                PartPose.offsetAndRotation(1.25F, -0.2479F, -0.0326F, 0.1309F, 0.0F, 0.0F));

        LeftLegLeggings.addOrReplaceChild("LeftRobeRope", CubeListBuilder.create()
                .texOffs(40, 31).addBox(-1.0F, -0.5F, -2.64F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), 
                PartPose.offsetAndRotation(0.04F, -1.4211F, 1.6F, 0.0F, 1.5708F, 0.0F));

        // Boots
        partdefinition.addOrReplaceChild("RightLegBoots", CubeListBuilder.create()
                .texOffs(0, 42).addBox(-2.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(1.0F)), 
                PartPose.offset(-1.9F, 12.0F, 0.0F));

        partdefinition.addOrReplaceChild("LeftLegBoots", CubeListBuilder.create()
                .texOffs(0, 42).mirror().addBox(-2.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(1.0F)).mirror(false), 
                PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }
    
    public void setCurrentSlot(EquipmentSlot slot) {
        this.currentSlot = slot;
    }
    
    public void copyPoseFrom(HumanoidModel<?> original) {
        this.young = original.young;
        this.crouching = original.crouching;
        this.riding = original.riding;
        
        // Copy poses
        this.Head.copyFrom(original.head);
        this.Body.copyFrom(original.body);
        this.RightArm.copyFrom(original.rightArm);
        this.LeftArm.copyFrom(original.leftArm);
        this.RightLegLeggings.copyFrom(original.rightLeg);
        this.LeftLegLeggings.copyFrom(original.leftLeg);
        this.RightLegBoots.copyFrom(original.rightLeg);
        this.LeftLegBoots.copyFrom(original.leftLeg);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        // Render based on current slot
        switch (currentSlot) {
            case HEAD -> Head.render(poseStack, buffer, packedLight, packedOverlay, color);
            case CHEST -> {
                Body.render(poseStack, buffer, packedLight, packedOverlay, color);
                RightArm.render(poseStack, buffer, packedLight, packedOverlay, color);
                LeftArm.render(poseStack, buffer, packedLight, packedOverlay, color);
            }
            case LEGS -> {
                RightLegLeggings.render(poseStack, buffer, packedLight, packedOverlay, color);
                LeftLegLeggings.render(poseStack, buffer, packedLight, packedOverlay, color);
            }
            case FEET -> {
                RightLegBoots.render(poseStack, buffer, packedLight, packedOverlay, color);
                LeftLegBoots.render(poseStack, buffer, packedLight, packedOverlay, color);
            }
            default -> {}
        }
    }
}
