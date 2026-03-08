package com.cahcap.client.renderer;

import com.cahcap.common.blockentity.HerbPotBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Renderer for Herb Pot to display:
 * - Soil inside the pot
 * - Seedling/plant on top with growth stages
 */
public class HerbPotRenderer implements BlockEntityRenderer<HerbPotBlockEntity> {
    
    private static final float SOIL_Y = 10f / 16f;
    private static final float SOIL_SCALE = 10f / 16f;
    private static final float SOIL_HEIGHT_SCALE = 0.15f;
    
    private static final float SEEDLING_Y = 11f / 16f;
    
    private static final float DEFAULT_SCALE = 0.70f;
    private static final float[] GROWTH_STAGE_THRESHOLDS = {0.25f, 0.50f, 0.75f, 1.00f};
    private static final float[] GROWTH_STAGE_SCALES = {0.70f, 0.80f, 0.90f, 1.00f};
    
    private final BlockRenderDispatcher blockRenderer;
    
    public HerbPotRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }
    
    @Override
    public void render(HerbPotBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        
        if (blockEntity.getLevel() == null) {
            return;
        }
        
        BlockPos pos = blockEntity.getBlockPos();
        int blockLight = blockEntity.getLevel().getBrightness(LightLayer.BLOCK, pos.above());
        int skyLight = blockEntity.getLevel().getBrightness(LightLayer.SKY, pos.above());
        int light = LightTexture.pack(blockLight, skyLight);
        
        if (blockEntity.hasSoil()) {
            renderSoil(blockEntity, poseStack, bufferSource, light);
        }
        
        if (blockEntity.hasSeedling()) {
            renderSeedling(blockEntity, poseStack, bufferSource, light, partialTick);
        }
    }
    
    private void renderSoil(HerbPotBlockEntity blockEntity, PoseStack poseStack,
                            MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        
        poseStack.translate(0.5, SOIL_Y, 0.5);
        poseStack.scale(SOIL_SCALE, SOIL_HEIGHT_SCALE, SOIL_SCALE);
        poseStack.translate(-0.5, 0, -0.5);
        
        ItemStack soilItem = blockEntity.getSoil();
        Block soilBlock = Block.byItem(soilItem.getItem());
        BlockState soilState = soilBlock != Blocks.AIR ? soilBlock.defaultBlockState() : Blocks.DIRT.defaultBlockState();
        
        BakedModel model = blockRenderer.getBlockModel(soilState);
        
        float r = 1.0f, g = 1.0f, b = 1.0f;
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        int color = blockColors.getColor(soilState, blockEntity.getLevel(), blockEntity.getBlockPos(), 0);
        if (color != -1) {
            r = ((color >> 16) & 0xFF) / 255.0f;
            g = ((color >> 8) & 0xFF) / 255.0f;
            b = (color & 0xFF) / 255.0f;
        }
        
        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(),
                bufferSource.getBuffer(RenderType.solid()),
                soilState,
                model,
                r, g, b,
                light,
                OverlayTexture.NO_OVERLAY
        );
        
        poseStack.popPose();
    }
    
    private void renderSeedling(HerbPotBlockEntity blockEntity, PoseStack poseStack,
                                 MultiBufferSource bufferSource, int light, float partialTick) {
        poseStack.pushPose();
        
        ItemStack seedling = blockEntity.getSeedling();
        
        float scale;
        
        if (blockEntity.isGrowing()) {
            float progress = blockEntity.getGrowthProgress();
            scale = getGrowthStageScale(progress);
        } else {
            scale = DEFAULT_SCALE;
        }
        
        Block seedlingBlock = Block.byItem(seedling.getItem());
        
        if (seedlingBlock != Blocks.AIR) {
            BlockState seedlingState = seedlingBlock.defaultBlockState();
            BakedModel model = blockRenderer.getBlockModel(seedlingState);
            
            poseStack.translate(0.5, SEEDLING_Y, 0.5);
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-0.5, 0, -0.5);
            
            BlockColors blockColors = Minecraft.getInstance().getBlockColors();
            int color = blockColors.getColor(seedlingState, blockEntity.getLevel(), blockEntity.getBlockPos(), 0);
            float r = 1.0f, g = 1.0f, b = 1.0f;
            if (color != -1) {
                r = ((color >> 16) & 0xFF) / 255.0f;
                g = ((color >> 8) & 0xFF) / 255.0f;
                b = (color & 0xFF) / 255.0f;
            }
            
            blockRenderer.getModelRenderer().renderModel(
                    poseStack.last(),
                    bufferSource.getBuffer(RenderType.cutout()),
                    seedlingState,
                    model,
                    r, g, b,
                    light,
                    OverlayTexture.NO_OVERLAY
            );
        } else {
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            poseStack.translate(0.5, SEEDLING_Y + scale * 0.5, 0.5);
            poseStack.scale(scale, scale, scale);
            
            itemRenderer.renderStatic(seedling, ItemDisplayContext.FIXED, light,
                    OverlayTexture.NO_OVERLAY, poseStack, bufferSource,
                    blockEntity.getLevel(), 0);
        }
        
        poseStack.popPose();
    }
    
    private float getGrowthStageScale(float progress) {
        for (int i = 0; i < GROWTH_STAGE_THRESHOLDS.length; i++) {
            if (progress <= GROWTH_STAGE_THRESHOLDS[i]) {
                return GROWTH_STAGE_SCALES[i];
            }
        }
        return GROWTH_STAGE_SCALES[GROWTH_STAGE_SCALES.length - 1];
    }
    
    @Override
    public int getViewDistance() {
        return 64;
    }
}
