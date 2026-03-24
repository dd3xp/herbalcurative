package com.cahcap.neoforge.client.handler;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.blockentity.cauldron.CauldronBlockEntity;
import com.cahcap.common.blockentity.cauldron.CauldronFluid;
import com.cahcap.common.registry.ModRegistries;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Client-side handler for rendering Cauldron HUD tooltip.
 * Shows materials when not brewing, shows herbs when brewing.
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID, value = Dist.CLIENT)
public class CauldronTooltipHandler extends TooltipHandler {

    private static final CauldronTooltipHandler INSTANCE = new CauldronTooltipHandler();

    // Per-frame state, set during validation, consumed during render
    private CauldronBlockEntity master;
    private final List<ItemCountPair> items = new ArrayList<>();
    private ItemStack outputSlot;
    private boolean hasOutput;
    private boolean hasPotion;
    private int potionUnits;
    private int potionColor;

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        INSTANCE.handleEvent(event);
    }

    @Override
    protected boolean isTargetBlock(BlockState state) {
        return state.is(ModRegistries.CAULDRON.get());
    }

    @Override
    protected boolean isValidEntity(BlockEntity entity) {
        return entity instanceof CauldronBlockEntity;
    }

    @Override
    protected boolean additionalValidation(BlockEntity entity, BlockState state,
                                           BlockHitResult hitResult, BlockPos pos) {
        CauldronBlockEntity cauldron = (CauldronBlockEntity) entity;
        master = cauldron.getMaster();
        return master != null;
    }

    @Override
    protected boolean hasContent(BlockEntity entity) {
        items.clear();

        if (master.isBrewing()) {
            Map<Item, Integer> herbs = master.getHerbs();
            for (Map.Entry<Item, Integer> entry : herbs.entrySet()) {
                items.add(new ItemCountPair(entry.getKey(), entry.getValue()));
            }
        } else {
            List<ItemStack> materials = master.getMaterials();
            for (ItemStack stack : materials) {
                if (!stack.isEmpty()) {
                    items.add(new ItemCountPair(stack.getItem(), stack.getCount()));
                }
            }
        }

        outputSlot = master.getOutputSlot();
        hasOutput = !outputSlot.isEmpty();
        boolean hasPotionUnits = master.getFluid().isPotion();

        if (items.isEmpty() && !hasOutput && !hasPotionUnits) {
            return false;
        }

        hasPotion = master.getFluid().isPotion();
        potionUnits = hasPotion ? master.getFluid().getPotionUnits() : 0;
        potionColor = hasPotion ? master.getFluid().getColor() : 0;

        return true;
    }

    @Override
    protected BlockPos getAnimationPos(BlockEntity entity, BlockPos targetPos) {
        return master.getBlockPos();
    }

    @Override
    protected void renderContent(GuiGraphics guiGraphics, Minecraft mc,
                                 BlockEntity entity, int screenWidth, int screenHeight) {
        int totalItems = items.size();
        int materialsWidth = totalItems * 20;
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        int currentY = centerY + 10;

        // Row 1: Materials/herbs (top)
        if (totalItems > 0) {
            int startX = centerX - materialsWidth / 2;
            int currentX = startX;

            for (ItemCountPair pair : items) {
                guiGraphics.renderItem(new ItemStack(pair.item), currentX, currentY);
                currentX += 20;
            }

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 200);
            currentX = startX;
            for (ItemCountPair pair : items) {
                String countText = String.valueOf(pair.count);
                guiGraphics.drawString(mc.font, countText,
                        currentX + 17 - mc.font.width(countText), currentY + 9, 0xFFFFFF, true);
                currentX += 20;
            }
            guiGraphics.pose().popPose();
            currentY += 20;
        }

        // Row 2: Potion units (middle) -- water texture tinted with potion color + "X/32"
        if (hasPotion) {
            String unitsText = potionUnits + "/" + CauldronFluid.MAX_POTION_UNITS;
            int textWidth = mc.font.width(unitsText);
            int iconSize = 12;
            int gap = 3;
            int totalWidth = iconSize + gap + textWidth;
            int startX = centerX - totalWidth / 2;
            int sy = currentY + 2;

            // Render water_still texture tinted with potion color
            TextureAtlasSprite sprite = mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                    .apply(ResourceLocation.withDefaultNamespace("block/water_still"));
            float r = ((potionColor >> 16) & 0xFF) / 255.0f;
            float g = ((potionColor >> 8) & 0xFF) / 255.0f;
            float b = (potionColor & 0xFF) / 255.0f;
            RenderSystem.setShaderColor(r, g, b, 0.9f);
            guiGraphics.blit(startX, sy, 0, iconSize, iconSize, sprite);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

            // Draw text
            guiGraphics.drawString(mc.font, unitsText, startX + iconSize + gap, currentY + 4, 0xFFFFFF, true);
            currentY += 18;
        }

        // Row 3: Output item (bottom)
        if (hasOutput) {
            int startX = centerX - 10;
            guiGraphics.renderItem(outputSlot, startX, currentY);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 200);
            String countText = String.valueOf(outputSlot.getCount());
            guiGraphics.drawString(mc.font, countText,
                    startX + 17 - mc.font.width(countText), currentY + 9, 0xFFFF00, true);
            guiGraphics.pose().popPose();
        }
    }

    private static class ItemCountPair {
        Item item;
        int count;

        ItemCountPair(Item item, int count) {
            this.item = item;
            this.count = count;
        }
    }
}
