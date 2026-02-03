// Made with Blockbench 5.0.5
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class item_herb_box<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "item_herb_box"), "main");
	private final ModelPart Chest;
	private final ModelPart Box;
	private final ModelPart Fabric;
	private final ModelPart RightRope;
	private final ModelPart LeftRope;
	private final ModelPart Pad;

	public item_herb_box(ModelPart root) {
		this.Chest = root.getChild("Chest");
		this.Box = this.Chest.getChild("Box");
		this.Fabric = this.Chest.getChild("Fabric");
		this.RightRope = this.Fabric.getChild("RightRope");
		this.LeftRope = this.Fabric.getChild("LeftRope");
		this.Pad = this.Chest.getChild("Pad");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Chest = partdefinition.addOrReplaceChild("Chest", CubeListBuilder.create(), PartPose.offset(0.0F, 18.0F, -1.0F));

		PartDefinition Box = Chest.addOrReplaceChild("Box", CubeListBuilder.create().texOffs(0, 0).addBox(-7.5F, -3.0F, -11.0F, 14.0F, 11.0F, 11.0F, new CubeDeformation(-2.0F)), PartPose.offset(0.5F, -3.0466F, 4.4838F));

		PartDefinition Fabric = Chest.addOrReplaceChild("Fabric", CubeListBuilder.create().texOffs(0, 22).addBox(-7.5F, 4.0F, -7.5F, 13.0F, 7.0F, 7.0F, new CubeDeformation(-2.0F)), PartPose.offset(1.0F, -3.0466F, 3.9838F));

		PartDefinition RightRope = Fabric.addOrReplaceChild("RightRope", CubeListBuilder.create(), PartPose.offsetAndRotation(-6.1409F, 7.5F, -4.0F, 0.0F, 0.0F, -3.1416F));

		PartDefinition cube_r1 = RightRope.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(24, 36).addBox(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(-0.7497F)), PartPose.offsetAndRotation(0.3591F, -0.0001F, 0.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition cube_r2 = RightRope.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(16, 36).addBox(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(-0.7499F)), PartPose.offsetAndRotation(-0.1802F, -0.6931F, 0.0F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r3 = RightRope.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 36).addBox(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(-0.7499F)), PartPose.offsetAndRotation(-0.1788F, 0.6932F, 0.0F, 0.0F, 0.0F, 0.3927F));

		PartDefinition LeftRope = Fabric.addOrReplaceChild("LeftRope", CubeListBuilder.create(), PartPose.offset(4.1409F, 7.5F, -4.0F));

		PartDefinition cube_r4 = LeftRope.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(32, 36).addBox(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(-0.7497F)), PartPose.offsetAndRotation(0.3591F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition cube_r5 = LeftRope.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(40, 36).addBox(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(-0.7499F)), PartPose.offsetAndRotation(-0.1802F, -0.6931F, 0.0F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r6 = LeftRope.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(8, 36).addBox(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(-0.7499F)), PartPose.offsetAndRotation(-0.1788F, 0.6932F, 0.0F, 0.0F, 0.0F, 0.3927F));

		PartDefinition Pad = Chest.addOrReplaceChild("Pad", CubeListBuilder.create().texOffs(0, 38).addBox(-5.5F, -11.0F, -9.701F, 11.0F, 8.0F, 3.0F, new CubeDeformation(-0.999F)), PartPose.offset(0.0F, 6.4799F, 11.2172F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		Chest.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}