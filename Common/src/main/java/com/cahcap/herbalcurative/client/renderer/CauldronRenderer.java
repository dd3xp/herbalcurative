package com.cahcap.herbalcurative.client.renderer;

import com.cahcap.herbalcurative.common.blockentity.CauldronBlockEntity;
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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;

/**
 * Renderer for the Cauldron multiblock structure.
 * 
 * Renders:
 * - Water/potion liquid surface inside the cauldron
 * - Bubbling particle effects during brewing
 * - Materials and herbs floating in the liquid
 */
public class CauldronRenderer implements BlockEntityRenderer<CauldronBlockEntity> {
    
    // Water texture location
    private static final ResourceLocation WATER_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    
    // Liquid rendering dimensions (relative to master block at center of Layer 1)
    // The cauldron is 3x3, master is at center bottom, liquid fills the interior
    // Model: bottom Y=8 to 12, walls Y=12 to 32
    // Wall inner edges: all sides from -12 to 28 (now a perfect square, 40x40 pixels)
    private static final float LIQUID_MIN_X = -12.0f / 16.0f;   // West wall inner edge
    private static final float LIQUID_MAX_X = 28.0f / 16.0f;    // East wall inner edge
    private static final float LIQUID_MIN_Z = -12.0f / 16.0f;   // North wall inner edge
    private static final float LIQUID_MAX_Z = 28.0f / 16.0f;    // South wall inner edge
    private static final float LIQUID_BASE_Y = 12.0f / 16.0f;   // Top of bottom plate (Y=12)
    private static final float LIQUID_FULL_Y = 27.0f / 16.0f;   // ~75% height (bottom=12, top=32, 75%=27)
    
    // Item rendering scale
    private static final float ITEM_SCALE = 0.25f;
    
    public CauldronRenderer(BlockEntityRendererProvider.Context context) {
        // No special initialization needed
    }
    
    @Override
    public void render(CauldronBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        
        if (blockEntity.getLevel() == null || !blockEntity.isMaster()) {
            return;
        }
        
        // Only render liquid if cauldron has water
        if (!blockEntity.hasWater()) {
            return;
        }
        
        int phase = blockEntity.getPhase();
        int potionColor = blockEntity.getPotionColor();
        
        // Determine liquid color based on phase
        int liquidColor;
        if (phase == CauldronBlockEntity.PHASE_WATER) {
            // Blue water color
            liquidColor = 0x3F76E4;
        } else {
            // Potion color
            liquidColor = potionColor;
        }
        
        // Calculate light
        BlockPos pos = blockEntity.getBlockPos();
        int blockLight = blockEntity.getLevel().getBrightness(LightLayer.BLOCK, pos);
        int skyLight = blockEntity.getLevel().getBrightness(LightLayer.SKY, pos);
        int light = LightTexture.pack(Math.max(blockLight, 7), skyLight); // Minimum light for liquid visibility
        
        // No rotation needed - liquid area is now a perfect square
        
        // Render liquid surface
        renderLiquidSurface(poseStack, bufferSource, light, liquidColor);
        
        // Render materials floating in the liquid (phase 1)
        if (phase == CauldronBlockEntity.PHASE_WATER) {
            renderMaterials(blockEntity, poseStack, bufferSource, light, partialTick);
        }
        
        // Render herbs floating in the liquid (phase 2)
        if (phase == CauldronBlockEntity.PHASE_BREWING) {
            renderHerbs(blockEntity, poseStack, bufferSource, light, partialTick);
        }
    }
    
    private void renderLiquidSurface(PoseStack poseStack, MultiBufferSource bufferSource, 
                                      int light, int color) {
        poseStack.pushPose();
        
        // Get water texture
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(WATER_STILL);
        
        // Extract color components
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = 0.8f; // Slight transparency
        
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        Matrix4f matrix = poseStack.last().pose();
        
        // UV coordinates from sprite
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        
        // Render liquid as a 3x3 grid of tiles, each tile is 1 block size
        // This creates proper tiling without stretching the texture
        float tileSize = 1.0f; // 1 block per tile
        
        for (int tx = 0; tx < 3; tx++) {
            for (int tz = 0; tz < 3; tz++) {
                // Calculate tile bounds, clamped to liquid area
                float tileMinX = LIQUID_MIN_X + tx * tileSize;
                float tileMaxX = Math.min(tileMinX + tileSize, LIQUID_MAX_X);
                float tileMinZ = LIQUID_MIN_Z + tz * tileSize;
                float tileMaxZ = Math.min(tileMinZ + tileSize, LIQUID_MAX_Z);
                
                // Skip if tile is outside liquid area
                if (tileMinX >= LIQUID_MAX_X || tileMinZ >= LIQUID_MAX_Z) {
                    continue;
                }
                
                // Clamp to liquid bounds
                tileMinX = Math.max(tileMinX, LIQUID_MIN_X);
                tileMinZ = Math.max(tileMinZ, LIQUID_MIN_Z);
                
                // Calculate UV based on how much of the tile is visible
                float visibleWidth = tileMaxX - tileMinX;
                float visibleDepth = tileMaxZ - tileMinZ;
                float uvMaxU = u0 + (u1 - u0) * visibleWidth;
                float uvMaxV = v0 + (v1 - v0) * visibleDepth;
                
                // Render this tile
                consumer.addVertex(matrix, tileMinX, LIQUID_FULL_Y, tileMinZ)
                        .setColor(r, g, b, a)
                        .setUv(u0, v0)
                        .setOverlay(0)
                        .setLight(light)
                        .setNormal(0, 1, 0);
                
                consumer.addVertex(matrix, tileMinX, LIQUID_FULL_Y, tileMaxZ)
                        .setColor(r, g, b, a)
                        .setUv(u0, uvMaxV)
                        .setOverlay(0)
                        .setLight(light)
                        .setNormal(0, 1, 0);
                
                consumer.addVertex(matrix, tileMaxX, LIQUID_FULL_Y, tileMaxZ)
                        .setColor(r, g, b, a)
                        .setUv(uvMaxU, uvMaxV)
                        .setOverlay(0)
                        .setLight(light)
                        .setNormal(0, 1, 0);
                
                consumer.addVertex(matrix, tileMaxX, LIQUID_FULL_Y, tileMinZ)
                        .setColor(r, g, b, a)
                        .setUv(uvMaxU, v0)
                        .setOverlay(0)
                        .setLight(light)
                        .setNormal(0, 1, 0);
            }
        }
        
        poseStack.popPose();
    }
    
    /**
     * Render materials floating on the liquid surface (phase 1)
     */
    private void renderMaterials(CauldronBlockEntity blockEntity, PoseStack poseStack, 
                                  MultiBufferSource bufferSource, int light, float partialTick) {
        List<ItemStack> materials = blockEntity.getMaterials();
        if (materials.isEmpty()) {
            return;
        }
        
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        
        // Calculate animation offset for bobbing effect
        long gameTime = blockEntity.getLevel().getGameTime();
        float bobOffset = (float) Math.sin((gameTime + partialTick) * 0.1) * 0.02f;
        
        // Render materials in a circular pattern on the liquid surface
        int count = Math.min(materials.size(), 9);
        float radius = 0.6f;
        
        for (int i = 0; i < count; i++) {
            ItemStack stack = materials.get(i);
            if (stack.isEmpty()) continue;
            
            poseStack.pushPose();
            
            // Calculate position in circle
            float angle = (float) (i * 2 * Math.PI / count);
            float offsetX = (float) Math.cos(angle) * radius;
            float offsetZ = (float) Math.sin(angle) * radius;
            
            // Position on liquid surface (center of cauldron)
            poseStack.translate(0.5f + offsetX, LIQUID_FULL_Y + 0.05f + bobOffset, 0.5f + offsetZ);
            
            // Lay flat and rotate slowly
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
            poseStack.mulPose(Axis.ZP.rotationDegrees((gameTime + partialTick) * 2 + i * 40));
            
            poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
            
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light,
                    OverlayTexture.NO_OVERLAY, poseStack, bufferSource,
                    blockEntity.getLevel(), 0);
            
            poseStack.popPose();
        }
    }
    
    /**
     * Render herbs floating in the brewing liquid (phase 2)
     */
    private void renderHerbs(CauldronBlockEntity blockEntity, PoseStack poseStack,
                              MultiBufferSource bufferSource, int light, float partialTick) {
        Map<Item, Integer> herbs = blockEntity.getHerbs();
        if (herbs.isEmpty()) {
            return;
        }
        
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        
        // Calculate animation offset for bobbing effect
        long gameTime = blockEntity.getLevel().getGameTime();
        float bobOffset = (float) Math.sin((gameTime + partialTick) * 0.15) * 0.03f;
        
        // Render herbs in a circular pattern, slightly submerged
        int index = 0;
        int totalTypes = herbs.size();
        float radius = 0.5f;
        
        for (Map.Entry<Item, Integer> entry : herbs.entrySet()) {
            if (index >= 6) break; // Max 6 different herbs shown
            
            Item herb = entry.getKey();
            int count = entry.getValue();
            
            poseStack.pushPose();
            
            // Calculate position in circle
            float angle = (float) (index * 2 * Math.PI / Math.min(totalTypes, 6));
            float offsetX = (float) Math.cos(angle) * radius;
            float offsetZ = (float) Math.sin(angle) * radius;
            
            // Slightly different bob phase for each herb
            float herbBob = (float) Math.sin((gameTime + partialTick) * 0.15 + index * 0.5) * 0.03f;
            
            // Position slightly below liquid surface (being cooked)
            poseStack.translate(0.5f + offsetX, LIQUID_FULL_Y - 0.05f + herbBob, 0.5f + offsetZ);
            
            // Lay flat and rotate
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
            poseStack.mulPose(Axis.ZP.rotationDegrees((gameTime + partialTick) * 3 + index * 60));
            
            poseStack.scale(ITEM_SCALE * 0.8f, ITEM_SCALE * 0.8f, ITEM_SCALE * 0.8f);
            
            // Create a stack with count for display (just 1 for visual)
            ItemStack displayStack = new ItemStack(herb, 1);
            
            itemRenderer.renderStatic(displayStack, ItemDisplayContext.FIXED, light,
                    OverlayTexture.NO_OVERLAY, poseStack, bufferSource,
                    blockEntity.getLevel(), 0);
            
            poseStack.popPose();
            index++;
        }
    }
    
    @Override
    public boolean shouldRenderOffScreen(CauldronBlockEntity blockEntity) {
        // Cauldron is 3x3x2, need to render when master is off-screen
        return true;
    }
}
