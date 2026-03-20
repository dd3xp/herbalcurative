package com.cahcap.neoforge.client.handler;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.blockentity.CauldronBlockEntity;
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
import net.minecraft.world.phys.HitResult;
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
public class CauldronTooltipHandler {

    private static final TooltipAnimator animator = new TooltipAnimator();

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        
        if (mc.level == null || mc.player == null) {
            return;
        }

        // Find cauldron master
        CauldronBlockEntity master = null;
        HitResult hitResult = mc.hitResult;
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            BlockPos pos = blockHitResult.getBlockPos();
            BlockState state = mc.level.getBlockState(pos);
            if (state.is(ModRegistries.CAULDRON.get())) {
                BlockEntity blockEntity = mc.level.getBlockEntity(pos);
                if (blockEntity instanceof CauldronBlockEntity cauldron) {
                    master = cauldron.getMaster();
                }
            }
        }

        if (master == null) { animator.reset(); return; }

        // Get materials/herbs to display
        List<ItemCountPair> items = new ArrayList<>();

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

        ItemStack outputSlot = master.getOutputSlot();
        boolean hasOutput = !outputSlot.isEmpty();
        boolean hasPotionUnits = master.getFluid().isPotion();
        if (items.isEmpty() && !hasOutput && !hasPotionUnits) {
            animator.reset();
            return;
        }

        float anim = animator.update(master.getBlockPos());

        // Potion units info
        boolean hasPotion = master.getFluid().isPotion();
        int potionUnits = hasPotion ? master.getFluid().getPotionUnits() : 0;
        int potionColor = hasPotion ? master.getFluid().getColor() : 0;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        int totalItems = items.size();
        int materialsWidth = totalItems * 20;
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        int currentY = centerY + 10;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, centerY, 0);
        guiGraphics.pose().scale(anim, anim, 1.0f);
        guiGraphics.pose().translate(-centerX, -centerY, 0);

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

        // Row 2: Potion units (middle) — water texture tinted with potion color + "X/32"
        if (hasPotion) {
            String unitsText = potionUnits + "/" + CauldronBlockEntity.CauldronFluid.MAX_POTION_UNITS;
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

        guiGraphics.pose().popPose();
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
