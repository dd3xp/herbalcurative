package com.cahcap.herbalcurative.neoforge;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.client.model.HerbBoxModel;
import com.cahcap.herbalcurative.client.model.LeafweaveArmorModel;
import com.cahcap.herbalcurative.neoforge.client.renderer.CauldronRenderer;
import com.cahcap.herbalcurative.client.renderer.RedCherryShelfRenderer;
import com.cahcap.herbalcurative.client.renderer.WorkbenchRenderer;
import com.cahcap.herbalcurative.neoforge.client.layer.HerbBoxPlayerLayer;
import com.cahcap.herbalcurative.neoforge.client.renderer.HerbCabinetRenderer;
import com.cahcap.herbalcurative.neoforge.common.registry.ModBlockEntities;
import com.cahcap.herbalcurative.neoforge.common.registry.ModBlocks;
import com.cahcap.herbalcurative.neoforge.common.registry.ModItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;

@Mod(value = HerbalCurativeCommon.MOD_ID, dist = Dist.CLIENT)
public class HerbalCurativeNeoForgeClient {
    
    public HerbalCurativeNeoForgeClient() {
        HerbalCurativeCommon.LOGGER.info("Herbal Curative NeoForge client initializing");
    }

    @EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        
        private static LeafweaveArmorModel<?> armorModel;
        
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                // Set render layers for transparent blocks (crops, saplings, flowers)
                // Cutout: for blocks with fully transparent or fully opaque pixels (no translucency)
                
                // Herb Crops (all 6 types)
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.VERDSCALE_FERN_CROP.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.DEWPETAL_CROP.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.ZEPHYR_LILY_CROP.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.CRYSTBUD_CROP.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.PYRISAGE_CROP.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.ROSYNIA_CROP.get(), RenderType.cutout());
                
                // Herb Flowers (all 6 types)
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.VERDSCALE_FERN.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.DEWPETAL.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.ZEPHYR_LILY.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.CRYSTBUD.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.PYRISAGE.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.ROSYNIA.get(), RenderType.cutout());
                
                // Red Cherry Sapling
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.RED_CHERRY_SAPLING.get(), RenderType.cutout());
                
                // Red Cherry Bush
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.RED_CHERRY_BUSH.get(), RenderType.cutout());
                
                // Red Cherry Leaves
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.RED_CHERRY_LEAVES.get(), RenderType.cutout());
                
                // Register Red Cherry Crossbow item properties for animations
                registerCrossbowItemProperties();
            });
            
            HerbalCurativeCommon.LOGGER.info("Herbal Curative NeoForge client setup complete");
        }
        
        /**
         * Register item properties for Red Cherry Crossbow to enable pulling and charged animations
         */
        private static void registerCrossbowItemProperties() {
            // Register "pulling" predicate - returns 1 when crossbow is being pulled (charged)
            ItemProperties.register(ModItems.RED_CHERRY_CROSSBOW.get(),
                    ResourceLocation.withDefaultNamespace("pulling"),
                    (stack, level, entity, seed) -> {
                        if (entity == null) {
                            return 0.0F;
                        }
                        return entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F;
                    });
            
            // Register "pull" predicate - returns progress of pulling (0.0 to 1.0)
            ItemProperties.register(ModItems.RED_CHERRY_CROSSBOW.get(),
                    ResourceLocation.withDefaultNamespace("pull"),
                    (stack, level, entity, seed) -> {
                        if (entity == null) {
                            return 0.0F;
                        }
                        return entity.getUseItem() != stack ? 0.0F : 
                                (float)(stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / 
                                (float)CrossbowItem.getChargeDuration(stack, entity);
                    });
            
            // Register "charged" predicate - returns 1 when crossbow is fully charged
            ItemProperties.register(ModItems.RED_CHERRY_CROSSBOW.get(),
                    ResourceLocation.withDefaultNamespace("charged"),
                    (stack, level, entity, seed) -> CrossbowItem.isCharged(stack) ? 1.0F : 0.0F);
            
            // Register "firework" predicate - returns 1 when loaded with firework rocket
            ItemProperties.register(ModItems.RED_CHERRY_CROSSBOW.get(),
                    ResourceLocation.withDefaultNamespace("firework"),
                    (stack, level, entity, seed) -> {
                        if (!CrossbowItem.isCharged(stack)) {
                            return 0.0F;
                        }
                        // Check if any charged projectile is a firework rocket
                        var chargedProjectiles = stack.get(net.minecraft.core.component.DataComponents.CHARGED_PROJECTILES);
                        if (chargedProjectiles != null && !chargedProjectiles.isEmpty()) {
                            // Iterate through the list to check for firework rockets
                            var list = chargedProjectiles.getItems();
                            for (int i = 0; i < list.size(); i++) {
                                if (list.get(i).is(net.minecraft.world.item.Items.FIREWORK_ROCKET)) {
                                    return 1.0F;
                                }
                            }
                        }
                        return 0.0F;
            });
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(LeafweaveArmorModel.LAYER_LOCATION, LeafweaveArmorModel::createBodyLayer);
            event.registerLayerDefinition(HerbBoxModel.LAYER_LOCATION, HerbBoxModel::createBodyLayer);
        }
        
        @SubscribeEvent
        public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.HERB_CABINET.get(), HerbCabinetRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.RED_CHERRY_SHELF.get(), RedCherryShelfRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.WORKBENCH.get(), WorkbenchRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.CAULDRON.get(), CauldronRenderer::new);
        }
        
        @SubscribeEvent
        public static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
            for (var skin : event.getSkins()) {
                PlayerRenderer renderer = event.getSkin(skin);
                if (renderer != null) {
                    renderer.addLayer(new HerbBoxPlayerLayer(renderer));
                }
            }
        }

        @SubscribeEvent
        public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
            // Leafweave Armor
            IClientItemExtensions leafweaveArmorExtensions = new IClientItemExtensions() {
                @Override
                public @NotNull Model getGenericArmorModel(LivingEntity livingEntity, ItemStack itemStack, 
                        EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                    if (armorModel == null) {
                        armorModel = new LeafweaveArmorModel<>(
                                net.minecraft.client.Minecraft.getInstance().getEntityModels()
                                        .bakeLayer(LeafweaveArmorModel.LAYER_LOCATION));
                    }
                    
                    armorModel.copyPoseFrom(original);
                    armorModel.setCurrentSlot(equipmentSlot);
                    
                    return armorModel;
                }
            };

            event.registerItem(leafweaveArmorExtensions,
                    ModItems.LEAFWEAVE_HELMET.get(),
                    ModItems.LEAFWEAVE_CHESTPLATE.get(),
                    ModItems.LEAFWEAVE_LEGGINGS.get(),
                    ModItems.LEAFWEAVE_BOOTS.get());
        }
    }
}
