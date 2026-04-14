package com.cahcap.client.renderer;

import com.cahcap.common.block.ShelfBlock;
import com.cahcap.common.blockentity.ShelfBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renderer for Red Cherry Shelf to display the stored item inside the frame.
 * <p>
 * Shelf model (base = facing north, wall at +Z):
 * <pre>
 *   Frame:  left [3,3,11]-[4,13,14]   right [12,3,11]-[13,13,14]
 *           top  [4,12,11]-[12,13,14]  bottom [4,3,11]-[12,4,14]
 *   Back:   [3,3,14]-[13,13,16]
 *   Shelf:  [4,4,10]-[12,5,13]
 *   Interior: x=[4,12] y=[5,12] z=[11,14]
 * </pre>
 */
public class ShelfRenderer implements BlockEntityRenderer<ShelfBlockEntity> {

    // Frame interior center in base model space (pixels / 16)
    private static final float CENTER_X = 8f / 16f;        // (4+12)/2
    private static final float CENTER_Y = 8.5f / 16f;      // (5+12)/2
    private static final float CENTER_Z = 12.5f / 16f;     // (11+14)/2

    // Item scale: fit the 7-pixel tall frame opening with a small margin
    private static final float ITEM_SCALE = 6f / 16f;

    public ShelfRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ShelfBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        if (!blockEntity.hasItem()) {
            return;
        }

        ItemStack stack = blockEntity.getItem();
        Direction facing = blockEntity.getBlockState().getValue(ShelfBlock.FACING);

        poseStack.pushPose();

        // Rotate to match FACING direction
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

        // Position at frame interior center (in base/north model space)
        poseStack.translate(CENTER_X, CENTER_Y, CENTER_Z);

        // Scale to fit the frame
        float scale = 7.5f / 16f;
        poseStack.scale(scale, scale, scale);

        // Enhance lighting slightly so the item is visible in the recessed shelf
        int blockLight = LightTexture.block(packedLight);
        int skyLight = LightTexture.sky(packedLight);
        int enhancedLight = LightTexture.pack(Math.min(blockLight + 2, 15), Math.min(skyLight + 2, 15));

        // Render the item
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, enhancedLight,
                OverlayTexture.NO_OVERLAY, poseStack, bufferSource,
                blockEntity.getLevel(), 0);

        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
