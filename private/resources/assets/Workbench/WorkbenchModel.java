// Made with Blockbench 5.0.7
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class WorkbenchModel extends EntityModel<Entity> {
	private final ModelPart TableLegs;
	private final ModelPart TableTop;
	public WorkbenchModel(ModelPart root) {
		this.TableLegs = root.getChild("TableLegs");
		this.TableTop = root.getChild("TableTop");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData TableLegs = modelPartData.addChild("TableLegs", ModelPartBuilder.create().uv(0, 19).cuboid(20.0F, -5.0F, -7.0F, 3.0F, 13.0F, 3.0F, new Dilation(0.0F))
		.uv(12, 19).cuboid(20.0F, -5.0F, 4.0F, 3.0F, 13.0F, 3.0F, new Dilation(0.0F))
		.uv(24, 19).cuboid(-23.0F, -5.0F, -7.0F, 3.0F, 13.0F, 3.0F, new Dilation(0.0F))
		.uv(0, 35).cuboid(-23.0F, -5.0F, 4.0F, 3.0F, 13.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 16.0F, 0.0F));

		ModelPartData TableTop = modelPartData.addChild("TableTop", ModelPartBuilder.create().uv(0, 0).cuboid(-24.0F, -8.0F, -8.0F, 48.0F, 3.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 16.0F, 0.0F));
		return TexturedModelData.of(modelData, 128, 128);
	}
	@Override
	public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		TableLegs.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		TableTop.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}