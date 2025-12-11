package com.cahcap.herbalcurative.client.renderer;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.common.blockentity.HerbCabinetBlockEntity;
import com.cahcap.herbalcurative.client.model.HerbCabinetModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class HerbCabinetRenderer implements BlockEntityRenderer<HerbCabinetBlockEntity> {
    
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HerbalCurativeCommon.MOD_ID, "textures/models/herb_cabinet.png");
    
    private final HerbCabinetModel model;
    
    public HerbCabinetRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new HerbCabinetModel(context.bakeLayer(HerbCabinetModel.LAYER_LOCATION));
    }
    
    @Override
    public void render(HerbCabinetBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // Only render if this is the master block and multiblock is formed
        if (!blockEntity.formed || !blockEntity.isMaster()) {
            return;
        }
        
        poseStack.pushPose();
        
        // Translate to block center and flip
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        
        // Rotate based on facing direction
        Direction facing = blockEntity.facing;
        float rotation = switch (facing) {
            case NORTH -> 180;
            case SOUTH -> 0;
            case WEST -> 90;
            case EAST -> 270;
            default -> 0;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        
        // Render the model
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
        
        poseStack.popPose();
        
        // Render herb item icons
        renderHerbIcons(blockEntity, poseStack, bufferSource, packedLight, facing);
    }
    
    private void renderHerbIcons(HerbCabinetBlockEntity be, PoseStack poseStack, MultiBufferSource bufferSource,
                                  int packedLight, Direction facing) {
        Item[] herbs = HerbCabinetBlockEntity.getAllHerbItems();
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
                float offsetZ = 1.9f / 16.0f;  // Fine-tuned: 1.9f gives a slight outward offset to avoid z-fighting
                
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
                
                // Each slot is 12 pixels wide x 11 pixels tall
                // Slots are offset from block centers due to frames and dividers
                // Translate to slot center, then offset by half item size to center the item
                poseStack.translate(slotCenterX - (8 * itemSize), slotCenterY - (8 * itemSize), 0);
                poseStack.scale(itemSize, itemSize, 1);
                
                // renderStatic renders items centered at (0,0) with size 16x16 in the current space
                // Offset by 8 to center the item (since it renders from -8 to +8)
                poseStack.translate(8, 8, 0);
                
                // Scale up to make the item visible (fix Y-axis to prevent upside-down)
                poseStack.scale(16, -16, 1);
                
                // Enhance environmental lighting to make icons brighter
                int blockLight = LightTexture.block(packedLight);
                int skyLight = LightTexture.sky(packedLight);
                // Boost both light values by 3 (capped at 15)
                int enhancedLight = LightTexture.pack(Math.min(blockLight + 3, 15), Math.min(skyLight + 3, 15));
                
                itemRenderer.renderStatic(stack, ItemDisplayContext.GUI, enhancedLight, 
                        OverlayTexture.NO_OVERLAY, poseStack, bufferSource, 
                        be.getLevel(), 0);
                
                poseStack.popPose();
            }
        }
    }
    
    private float getRotationYForSide(Direction side) {
        return switch (side) {
            case NORTH -> 180;
            case SOUTH -> 0;
            case WEST -> 270;  // Flipped: was 90, now 270 to render on front face
            case EAST -> 90;   // Flipped: was 270, now 90 to render on front face
            default -> 0;
        };
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