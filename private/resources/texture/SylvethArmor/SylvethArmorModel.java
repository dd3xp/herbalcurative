// Made with Blockbench 5.0.7
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class SylvethArmorModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "sylvetharmormodel"), "main");
	private final ModelPart Head;
	private final ModelPart Body;
	private final ModelPart RightArm;
	private final ModelPart LeftArm;
	private final ModelPart RightLegLeggings;
	private final ModelPart RightRobeSide;
	private final ModelPart RightRobeBack;
	private final ModelPart RightRobeRope;
	private final ModelPart LeftLegLeggings;
	private final ModelPart LeftRobeSide;
	private final ModelPart LeftRobeBack;
	private final ModelPart LeftRobeRope;
	private final ModelPart RightLegBoots;
	private final ModelPart LeftLegBoots;

	public SylvethArmorModel(ModelPart root) {
		this.Head = root.getChild("Head");
		this.Body = root.getChild("Body");
		this.RightArm = root.getChild("RightArm");
		this.LeftArm = root.getChild("LeftArm");
		this.RightLegLeggings = root.getChild("RightLegLeggings");
		this.RightRobeSide = this.RightLegLeggings.getChild("RightRobeSide");
		this.RightRobeBack = this.RightLegLeggings.getChild("RightRobeBack");
		this.RightRobeRope = this.RightLegLeggings.getChild("RightRobeRope");
		this.LeftLegLeggings = root.getChild("LeftLegLeggings");
		this.LeftRobeSide = this.LeftLegLeggings.getChild("LeftRobeSide");
		this.LeftRobeBack = this.LeftLegLeggings.getChild("LeftRobeBack");
		this.LeftRobeRope = this.LeftLegLeggings.getChild("LeftRobeRope");
		this.RightLegBoots = root.getChild("RightLegBoots");
		this.LeftLegBoots = root.getChild("LeftLegBoots");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Head = partdefinition.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F))
		.texOffs(32, 0).mirror().addBox(-4.0F, -8.0F, 5.0F, 8.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 11.0F, 4.0F, new CubeDeformation(1.01F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition RightArm = partdefinition.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(24, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(1.0F))
		.texOffs(40, 16).addBox(-3.0F, 3.0F, 2.7F, 4.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(40, 23).addBox(-2.5F, 6.0F, 3.4F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition LeftArm = partdefinition.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, 3.0F, 2.7F, 4.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(40, 23).mirror().addBox(-0.5F, 6.0F, 3.3F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(24, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(1.0F)).mirror(false), PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition RightLegLeggings = partdefinition.addOrReplaceChild("RightLegLeggings", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition RightRobeSide = RightLegLeggings.addOrReplaceChild("RightRobeSide", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.3945F, 2.3493F, 0.0F, 0.0F, 0.0F, 0.0436F));

		PartDefinition RightRobeSideCenter_r1 = RightRobeSide.addOrReplaceChild("RightRobeSideCenter_r1", CubeListBuilder.create().texOffs(0, 31).addBox(-4.5F, -4.5F, -0.5F, 5.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0109F, 0.0826F, 2.5F, 0.0F, -1.5708F, 0.1309F));

		PartDefinition RightRobeSideFront_r1 = RightRobeSide.addOrReplaceChild("RightRobeSideFront_r1", CubeListBuilder.create().texOffs(36, 31).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0544F, -0.4131F, -2.5F, 0.0F, -1.5708F, 0.1309F));

		PartDefinition RightRobeBack = RightLegLeggings.addOrReplaceChild("RightRobeBack", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.35F, 2.6716F, 3.3657F, 0.0436F, 0.0F, 0.0F));

		PartDefinition RightRobeBackSide_r1 = RightRobeBack.addOrReplaceChild("RightRobeBackSide_r1", CubeListBuilder.create().texOffs(22, 31).mirror().addBox(-2.0F, -5.0F, -0.5F, 4.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.25F, 0.2479F, 0.0326F, 0.1309F, 0.0F, 0.0F));

		PartDefinition RightRobeBackSide_r2 = RightRobeBack.addOrReplaceChild("RightRobeBackSide_r2", CubeListBuilder.create().texOffs(32, 31).addBox(-0.5F, -4.5F, -0.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.25F, -0.2479F, -0.0326F, 0.1309F, 0.0F, 0.0F));

		PartDefinition RightRobeRope = RightLegLeggings.addOrReplaceChild("RightRobeRope", CubeListBuilder.create().texOffs(40, 31).addBox(-1.0F, -0.5F, -2.64F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.04F, -1.4211F, 1.6F, 0.0F, 1.5708F, 0.0F));

		PartDefinition LeftLegLeggings = partdefinition.addOrReplaceChild("LeftLegLeggings", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition LeftRobeSide = LeftLegLeggings.addOrReplaceChild("LeftRobeSide", CubeListBuilder.create(), PartPose.offsetAndRotation(3.3945F, 2.3493F, 0.0F, 0.0F, 0.0F, -0.0436F));

		PartDefinition LeftRobeSideCenter_r1 = LeftRobeSide.addOrReplaceChild("LeftRobeSideCenter_r1", CubeListBuilder.create().texOffs(0, 31).mirror().addBox(-3.5F, -4.5F, -0.5F, 5.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0109F, 0.0826F, -0.5F, 0.0F, 1.5708F, -0.1309F));

		PartDefinition LeftRobeSideFront_r1 = LeftRobeSide.addOrReplaceChild("LeftRobeSideFront_r1", CubeListBuilder.create().texOffs(36, 31).mirror().addBox(-0.5F, -4.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.0544F, -0.4131F, -2.5F, 0.0F, 1.5708F, -0.1309F));

		PartDefinition LeftRobeBack = LeftLegLeggings.addOrReplaceChild("LeftRobeBack", CubeListBuilder.create(), PartPose.offsetAndRotation(1.35F, 2.6716F, 3.3657F, 0.0436F, 0.0F, 0.0F));

		PartDefinition LeftRobeBackSide_r1 = LeftRobeBack.addOrReplaceChild("LeftRobeBackSide_r1", CubeListBuilder.create().texOffs(12, 31).mirror().addBox(-2.0F, -5.0F, -0.5F, 4.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.25F, 0.2479F, 0.0326F, 0.1309F, 0.0F, 0.0F));

		PartDefinition LeftRobeBackSide_r2 = LeftRobeBack.addOrReplaceChild("LeftRobeBackSide_r2", CubeListBuilder.create().texOffs(32, 31).mirror().addBox(-0.5F, -4.5F, -0.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.25F, -0.2479F, -0.0326F, 0.1309F, 0.0F, 0.0F));

		PartDefinition LeftRobeRope = LeftLegLeggings.addOrReplaceChild("LeftRobeRope", CubeListBuilder.create().texOffs(40, 31).addBox(-1.0F, -0.5F, -2.64F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.04F, -1.4211F, 1.6F, 0.0F, 1.5708F, 0.0F));

		PartDefinition RightLegBoots = partdefinition.addOrReplaceChild("RightLegBoots", CubeListBuilder.create().texOffs(0, 42).addBox(-2.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(1.0F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition LeftLegBoots = partdefinition.addOrReplaceChild("LeftLegBoots", CubeListBuilder.create().texOffs(0, 42).mirror().addBox(-2.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(1.0F)).mirror(false), PartPose.offset(1.9F, 12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		Head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		RightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		LeftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		RightLegLeggings.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		LeftLegLeggings.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		RightLegBoots.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		LeftLegBoots.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}