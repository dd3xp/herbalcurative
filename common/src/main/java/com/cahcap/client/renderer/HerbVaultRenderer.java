package com.cahcap.client.renderer;

import com.cahcap.common.util.HerbRegistry;
import com.cahcap.common.blockentity.HerbVaultBlockEntity;
import com.cahcap.common.util.HerbRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;

/**
 * Renderer for Herb Vault - renders herb item icons in the 6 front grid slots.
 * Exact same rendering approach as HerbCabinetRenderer.
 *
 * Front face has 3 columns (along right axis) x 2 rows (dy=0 top, dy=-1 bottom).
 * Each column is one block wide, each row is one block tall.
 * Items are centered in each block on the front face.
 */
public class HerbVaultRenderer implements BlockEntityRenderer<HerbVaultBlockEntity> {

    private static final Matrix3f ITEM_LIGHT_ROTATION_3D = new Matrix3f().rotationYXZ(0.36f, -0.36f, -0.014f);
    private static final Matrix3f ITEM_LIGHT_ROTATION_FLAT = new Matrix3f().rotationYXZ(0, -0.785398f, 0);

    public HerbVaultRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(HerbVaultBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.isFormed() || !blockEntity.isMaster()) return;
        renderHerbIcons(blockEntity, poseStack, bufferSource, packedLight, blockEntity.getFacing());
    }

    private void renderHerbIcons(HerbVaultBlockEntity be, PoseStack poseStack, MultiBufferSource bufferSource,
                                  int packedLight, Direction facing) {
        Item[] herbs = HerbRegistry.getAllHerbItems();
        Direction right = facing.getClockWise();
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        // 3 columns x 2 rows on the front face
        // Front face blocks are at master + facing direction
        // Columns go along the right axis: col-1 = -1, 0, +1
        // Rows: row 0 = dy=0 (master layer), row 1 = dy=-1 (below)

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                int index = row * 3 + (2 - col);
                Item herb = herbs[index];
                int amount = be.getHerbAmount(herb);

                if (amount <= 0) continue;

                ItemStack stack = new ItemStack(herb);

                poseStack.pushPose();

                // === Identical to HerbCabinetRenderer from here ===
                // Start from master block center
                poseStack.translate(0.5, 0.5, 0.5);

                // Offset to the target block on the front face:
                // - right direction * (col - 1) for horizontal position
                // - facing direction * 1 to move to front face
                // - vertical: row 0 = same level, row 1 = one below
                double blockOffsetX = right.getStepX() * (col - 1) + facing.getStepX();
                double blockOffsetY = -row;
                double blockOffsetZ = right.getStepZ() * (col - 1) + facing.getStepZ();

                poseStack.translate(blockOffsetX, blockOffsetY, blockOffsetZ);

                // Rotate to face the correct direction
                poseStack.mulPose(Axis.YP.rotationDegrees(getRotationYForSide(facing)));
                poseStack.translate(-0.5f, -0.5f, -0.5f);

                // Position on the front face of this block
                float itemSize = 0.40F;
                // The front wall surface is at about Z=2/16 into the block from the facing side
                // offsetZ controls how close to the face the item renders
                // In the rotated local space, Z=1 is the facing-direction face
                float offsetZ = 2.9f / 16.0f;

                poseStack.translate(0, 1, 1 - offsetZ);
                poseStack.scale(1 / 16f, -1 / 16f, 0.00001f);

                // Center the item in the grid cell within this block
                // Grid cell Y centers: top row at Y=7px from master origin (=7 in block),
                //                      bottom row at Y=-5px (=11 in block below)
                // Grid cell X centers within each block:
                // Left block: cell center at X=11, Center block: X=8, Right block: X=5
                float slotCenterX = (col == 0) ? 5.0f : (col == 1) ? 8.0f : 11.0f;
                float slotCenterY = (row == 0) ? 9.0f : 5.0f;

                poseStack.translate(slotCenterX - (8 * itemSize), slotCenterY - (8 * itemSize), 0);
                poseStack.scale(itemSize, itemSize, 1);
                poseStack.translate(8, 8, 0);
                poseStack.scale(16, -16, 1);

                // === Identical to HerbCabinetRenderer from here ===
                poseStack.last().trustedNormals = true;

                BakedModel itemModel = itemRenderer.getModel(stack, be.getLevel(), null, 0);

                if (itemModel.isGui3d()) {
                    poseStack.last().normal().rotateYXZ(-getRotationYForSideRadians(facing), 0, 0).mul(ITEM_LIGHT_ROTATION_3D);
                } else {
                    poseStack.last().normal().mul(ITEM_LIGHT_ROTATION_FLAT);
                }

                ItemDisplayContext context = itemModel.isCustomRenderer() ? ItemDisplayContext.FIXED : ItemDisplayContext.GUI;

                itemRenderer.render(stack, context, false, poseStack, bufferSource,
                        packedLight, OverlayTexture.NO_OVERLAY, itemModel);

                poseStack.popPose();
            }
        }
    }

    private float getRotationYForSide(Direction side) {
        return switch (side) {
            case NORTH -> 180;
            case SOUTH -> 0;
            case WEST -> 270;
            case EAST -> 90;
            default -> 0;
        };
    }

    private float getRotationYForSideRadians(Direction side) {
        return getRotationYForSide(side) * (float) Math.PI / 180f;
    }

    @Override
    public boolean shouldRenderOffScreen(HerbVaultBlockEntity blockEntity) { return true; }

    @Override
    public int getViewDistance() { return 256; }
}
