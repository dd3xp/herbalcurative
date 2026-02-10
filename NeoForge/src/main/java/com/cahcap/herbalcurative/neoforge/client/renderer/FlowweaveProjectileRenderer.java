package com.cahcap.herbalcurative.neoforge.client.renderer;

import com.cahcap.herbalcurative.common.entity.FlowweaveProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for the Flowweave Projectile.
 * Does not render anything visible - the projectile is purely represented by particles.
 * The particle trail is spawned in FlowweaveProjectile.tick() method.
 */
public class FlowweaveProjectileRenderer extends EntityRenderer<FlowweaveProjectile> {

    // Empty texture - we don't render anything
    private static final ResourceLocation TEXTURE = 
            ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public FlowweaveProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FlowweaveProjectile entity, float entityYaw, float partialTick, 
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Don't render anything - the projectile is invisible
        // Only the particle trail (spawned in tick()) is visible
    }

    @Override
    public ResourceLocation getTextureLocation(FlowweaveProjectile entity) {
        return TEXTURE;
    }
}
