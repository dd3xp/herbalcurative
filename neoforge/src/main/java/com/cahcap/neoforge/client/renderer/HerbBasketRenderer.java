package com.cahcap.neoforge.client.renderer;

import com.cahcap.common.block.HerbBasketBlock;
import com.cahcap.common.blockentity.HerbBasketBlockEntity;
import com.cahcap.common.registry.ModRegistries;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
 * when it contains herbs.
 */
public class HerbBasketRenderer implements BlockEntityRenderer<HerbBasketBlockEntity> {

    private static final int[] HERB_COLORS = {
            0xFFFFFF, // 0: unused (empty)
            0x497540, // 1: scaleplate
            0x7ABBD0, // 2: dewpetal_shard
            0xF7D462, // 3: golden_lilybell
            0xAE82F6, // 4: cryst_spine
            0x9B120D, // 5: burnt_node
            0x001C70, // 6: heart_of_stardream
    };

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
            float depth = 5.0f / 16.0f;
            poseStack.translate(
                    0.5f + facing.getStepX() * (0.5f - depth),
                    0.5f,
                    0.5f + facing.getStepZ() * (0.5f - depth)
            );
        } else {
            poseStack.translate(0.5f, 3.0f / 16.0f, 0.5f);
        }

        float scale = 0.35f;
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5f, -0.5f, -0.5f);

        // Render leaf model with custom RGB tint
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
