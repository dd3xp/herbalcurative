package com.cahcap.neoforge.client.layer;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.client.model.HerbBoxModel;
import com.cahcap.neoforge.common.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.WeakHashMap;

/**
 * Renders herb box on player's back when they have one in inventory
 * Uses caching to avoid expensive inventory scans every frame
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID, value = Dist.CLIENT)
public class HerbBoxPlayerLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "textures/models/herb_box.png");
    private HerbBoxModel<AbstractClientPlayer> model;
    
    // Cache to avoid checking inventory every frame (60 times per second)
    // WeakHashMap automatically removes entries when player is garbage collected
    private static final WeakHashMap<Player, Boolean> herbBoxCache = new WeakHashMap<>();
    private static final int CACHE_UPDATE_INTERVAL = 10; // Update every 10 ticks (0.5 seconds)
    
    public HerbBoxPlayerLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }
    
    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, 
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount, 
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        
        if (!shouldRenderHerbBox(player)) {
            return;
        }
        
        if (model == null) {
            model = new HerbBoxModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(HerbBoxModel.LAYER_LOCATION));
        }
        
        // Copy model attributes from player model (includes sneak state, etc.)
        PlayerModel<AbstractClientPlayer> playerModel = this.getParentModel();
        model.young = playerModel.young;
        model.crouching = playerModel.crouching;
        model.riding = playerModel.riding;
        model.body.copyFrom(playerModel.body);
        
        poseStack.pushPose();
        
        // If wearing chestplate, move outward by 1 pixel (0.0625 units)
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!chestplate.isEmpty()) {
            poseStack.translate(0.0F, 0.0F, 0.0625F);
        }
        
        // Render - model automatically follows body rotation because Chest is child of body
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
        
        poseStack.popPose();
    }
    
    /**
     * Update cache periodically instead of every frame
     * Called from PlayerTickEvent (20 times per second)
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        
        // Only update cache every CACHE_UPDATE_INTERVAL ticks
        if (player.tickCount % CACHE_UPDATE_INTERVAL != 0) {
            return;
        }
        
        // Check if player has herb box in inventory
        boolean hasHerbBox = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.HERB_BOX.get())) {
                hasHerbBox = true;
                break;
            }
        }
        
        herbBoxCache.put(player, hasHerbBox);
    }
    
    private boolean shouldRenderHerbBox(Player player) {
        // Don't render if holding herb box
        if (player.getMainHandItem().is(ModItems.HERB_BOX.get()) || 
            player.getOffhandItem().is(ModItems.HERB_BOX.get())) {
            return false;
        }
        
        // Use cached value (updated every 10 ticks)
        // If not in cache yet, default to false (will be updated on next tick)
        return herbBoxCache.getOrDefault(player, false);
    }
}

