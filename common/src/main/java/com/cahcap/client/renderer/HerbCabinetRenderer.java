package com.cahcap.client.renderer;

import com.cahcap.common.blockentity.HerbCabinetBlockEntity;
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
import org.joml.Matrix4f;

/**
 * Renderer for Herb Cabinet - renders herb item icons on the cabinet front.
 * The cabinet structure itself uses JSON model rendering.
 */
public class HerbCabinetRenderer implements BlockEntityRenderer<HerbCabinetBlockEntity> {
    
    // Light rotation matrices - adjust normal vectors so items appear brighter
    private static final Matrix3f ITEM_LIGHT_ROTATION_3D = new Matrix3f().rotationYXZ(0.36f, -0.36f, -0.014f);
    private static final Matrix3f ITEM_LIGHT_ROTATION_FLAT = new Matrix3f().rotationYXZ(0, -0.785398f, 0);
    
    public HerbCabinetRenderer(BlockEntityRendererProvider.Context context) {
        // No model initialization needed - using JSON model for cabinet structure
    }
    
    @Override
    public void render(HerbCabinetBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // Only render if this is the master block and multiblock is formed
        if (!blockEntity.isFormed() || !blockEntity.isMaster()) {
            return;
        }

        // Render herb item icons on the cabinet front
        renderHerbIcons(blockEntity, poseStack, bufferSource, packedLight, blockEntity.getFacing());
    }
    
    private void renderHerbIcons(HerbCabinetBlockEntity be, PoseStack poseStack, MultiBufferSource bufferSource,
                                  int packedLight, Direction facing) {
        Item[] herbs = HerbRegistry.getAllHerbItems();
        Direction right = facing.getClockWise();
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                int index = row * 3 + (2 - col);
                Item herb = herbs[index];
                int amount = be.getHerbAmount(herb);
                
                if (amount <= 0) {
                    continue;
                }
                
                ItemStack stack = new ItemStack(herb);
                
                poseStack.pushPose();
                
                // Start from master block center
                poseStack.translate(0.5, 0.5, 0.5);
                
                // Offset to the correct block in the multiblock
                double blockOffsetX = right.getStepX() * (col - 1);
                double blockOffsetY = (1 - row);
                double blockOffsetZ = right.getStepZ() * (col - 1);
                
                poseStack.translate(blockOffsetX, blockOffsetY, blockOffsetZ);
                
                // Rotate to face the correct direction
                poseStack.mulPose(Axis.YP.rotationDegrees(getRotationYForSide(facing)));
                poseStack.translate(-0.5f, -0.5f, -0.5f);
                
                // Move to render position
                float itemSize = 0.45F;
                float offsetZ = 1.9f / 16.0f;
                
                poseStack.translate(0, 1, 1 - offsetZ);
                poseStack.scale(1 / 16f, -1 / 16f, 0.00001f);
                
                float[] xOffsets = {-2.0f, 0.0f, 2.0f};
                float[] yOffsets = {1.25f, -1.25f};
                
                float slotCenterX = 8.0f + xOffsets[col];
                float slotCenterY = 8.0f + yOffsets[row];
                
                // Special offset for Burnt Node (index 4) to align with slot
                if (index == 4) {
                    slotCenterX += 0.5f * itemSize;
                }
                
                poseStack.translate(slotCenterX - (8 * itemSize), slotCenterY - (8 * itemSize), 0);
                poseStack.scale(itemSize, itemSize, 1);
                poseStack.translate(8, 8, 0);
                poseStack.scale(16, -16, 1);
                
                // Skip normal vector normalization for proper lighting
                poseStack.last().trustedNormals = true;
                
                // Get the item model
                BakedModel itemModel = itemRenderer.getModel(stack, be.getLevel(), null, 0);
                
                // Apply light rotation to normals for better lighting
                if (itemModel.isGui3d()) {
                    poseStack.last().normal().rotateYXZ(-getRotationYForSideRadians(facing), 0, 0).mul(ITEM_LIGHT_ROTATION_3D);
                } else {
                    poseStack.last().normal().mul(ITEM_LIGHT_ROTATION_FLAT);
                }
                
                // Use GUI context for flat items, FIXED for custom renderers
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
    public boolean shouldRenderOffScreen(HerbCabinetBlockEntity blockEntity) {
        return true;
    }
    
    /**
     * Increase view distance to render like normal blocks
     * Returns 256 blocks (4x the default 64 blocks)
     */
    @Override
    public int getViewDistance() {
        return 256;
    }
}