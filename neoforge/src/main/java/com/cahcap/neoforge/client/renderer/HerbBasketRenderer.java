package com.cahcap.neoforge.client.renderer;

import com.cahcap.common.block.HerbBasketBlock;
import com.cahcap.common.blockentity.HerbBasketBlockEntity;
import com.cahcap.common.registry.ModRegistries;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Renders a tinted red cherry leaf block inside the herb basket
 * when it contains herbs. The leaf fills the basket interior,
 * touching all 4 walls.
 */
public class HerbBasketRenderer implements BlockEntityRenderer<HerbBasketBlockEntity> {

    private static final int[] HERB_COLORS = {
            0xFFFFFF, // 0: unused (empty)
            0x497540, // 1: scaleplate
            0x7ABBD0, // 2: dewpetal
            0xF7D462, // 3: zephyr_blossom
            0xAE82F6, // 4: cryst_spine
            0x9B120D, // 5: pyro_node
            0x001C70, // 6: stellar_mote
    };

    // Basket interior dimensions in pixel space (from Blockbench model).
    // Floor: interior x=[4,12], y=[1,5], z=[4,12]
    // Wall (base model = facing north): interior x=[4,12], y=[1,5], z=[6,14]
    // Leaf cube is a square (8×8×8 pixels) that fills the interior wall-to-wall
    // and sits on the base plate. It will poke out above the basket walls slightly.
    private static final float INTERIOR_X_MIN = 4f / 16f;
    private static final float INTERIOR_SIZE = 8f / 16f;  // 8 pixels — same for all 3 axes
    private static final float INTERIOR_Y_MIN = 1f / 16f;  // sits on the base plate (y=1)

    private static final float FLOOR_Z_MIN = 4f / 16f;
    private static final float WALL_Z_MIN = 6f / 16f;

    private static final RandomSource RANDOM = RandomSource.create();

    public HerbBasketRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(HerbBasketBlockEntity basket, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (basket.getLevel() == null) return;

        Item boundHerb = basket.getBoundHerb();
        if (boundHerb == null || basket.getHerbCount() <= 0) return;

        int herbIndex = HerbBasketBlock.getHerbTypeIndex(boundHerb);
        if (herbIndex <= 0 || herbIndex >= HERB_COLORS.length) return;

        BlockState leafState = ModRegistries.RED_CHERRY_LEAVES.get().defaultBlockState();
        BlockState basketState = basket.getBlockState();
        boolean onWall = basketState.getValue(HerbBasketBlock.ON_WALL);
        Direction facing = basketState.getValue(HerbBasketBlock.FACING);

        int color = HERB_COLORS[herbIndex];
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        poseStack.pushPose();

        if (onWall) {
            // Rotate to match FACING direction (same convention as WorkbenchRenderer)
            poseStack.translate(0.5, 0, 0.5);
            float yRot = switch (facing) {
                case NORTH -> 0;
                case SOUTH -> 180;
                case WEST -> 90;
                case EAST -> -90;
                default -> 0;
            };
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
            poseStack.translate(-0.5, 0, -0.5);

            // Position and scale to fill wall basket interior (in base/north model space)
            poseStack.translate(INTERIOR_X_MIN, INTERIOR_Y_MIN, WALL_Z_MIN);
            poseStack.scale(INTERIOR_SIZE, INTERIOR_SIZE, INTERIOR_SIZE);
        } else {
            // Floor basket: symmetric interior, FACING doesn't matter for the leaf fill
            poseStack.translate(INTERIOR_X_MIN, INTERIOR_Y_MIN, FLOOR_Z_MIN);
            poseStack.scale(INTERIOR_SIZE, INTERIOR_SIZE, INTERIOR_SIZE);
        }

        // Render leaf model — it occupies 0-1 in all axes, our translate+scale
        // maps it to fill the basket interior
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(leafState);
        for (RenderType renderType : model.getRenderTypes(leafState, RANDOM, ModelData.EMPTY)) {
            VertexConsumer consumer = bufferSource.getBuffer(renderType);
            Minecraft.getInstance().getBlockRenderer().getModelRenderer()
                    .renderModel(poseStack.last(), consumer, leafState, model,
                            r, g, b, packedLight, OverlayTexture.NO_OVERLAY,
                            ModelData.EMPTY, renderType);
        }

        poseStack.popPose();
    }
}
