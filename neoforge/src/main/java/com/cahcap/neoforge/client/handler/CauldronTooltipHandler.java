package com.cahcap.neoforge.client.handler;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.blockentity.CauldronBlockEntity;
import com.cahcap.common.registry.ModRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
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

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        
        if (mc.level == null || mc.player == null) {
            return;
        }

        HitResult hitResult = mc.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        BlockPos pos = blockHitResult.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        
        // Check if looking at cauldron
        if (!state.is(ModRegistries.CAULDRON.get())) {
            return;
        }
        
        BlockEntity blockEntity = mc.level.getBlockEntity(pos);
        if (!(blockEntity instanceof CauldronBlockEntity cauldron)) {
            return;
        }

        // Get the master block entity
        CauldronBlockEntity master = cauldron.getMaster();
        if (master == null) {
            return;
        }

        // Get materials/herbs to display
        List<ItemCountPair> items = new ArrayList<>();
        
        if (master.isBrewing()) {
            // Show herbs during brewing
            Map<Item, Integer> herbs = master.getHerbs();
            for (Map.Entry<Item, Integer> entry : herbs.entrySet()) {
                items.add(new ItemCountPair(entry.getKey(), entry.getValue()));
            }
        } else {
            // Show materials when not brewing - one icon per slot (no merging)
            // Items with low stack limit (e.g. flowweave ring max 2) may occupy multiple slots
            List<ItemStack> materials = master.getMaterials();
            for (ItemStack stack : materials) {
                if (!stack.isEmpty()) {
                    items.add(new ItemCountPair(stack.getItem(), stack.getCount()));
                }
            }
        }
        
        // Get output slot contents
        ItemStack outputSlot = master.getOutputSlot();
        boolean hasOutput = !outputSlot.isEmpty();

        // Don't render if nothing to show
        if (items.isEmpty() && !hasOutput) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        // Calculate widths for each row
        int totalItems = items.size();
        int materialsWidth = totalItems * 20;
        int outputWidth = hasOutput ? 20 : 0;
        
        // Position below crosshair
        int materialsY = screenHeight / 2 + 12;
        int outputY = materialsY + 20; // Output on second row
        
        // Render materials/herbs first row
        if (totalItems > 0) {
            int materialsStartX = screenWidth / 2 - materialsWidth / 2;
            int currentX = materialsStartX;
            
            for (int i = 0; i < items.size(); i++) {
                ItemCountPair pair = items.get(i);
                ItemStack stack = new ItemStack(pair.item);
                
                // Render the item icon
                guiGraphics.renderItem(stack, currentX, materialsY);
                currentX += 20;
            }
            
            // Render text on top of materials (higher z-layer)
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 200);
            
            currentX = materialsStartX;
            for (int i = 0; i < items.size(); i++) {
                ItemCountPair pair = items.get(i);
                
                // Render the amount as overlay
                String countText = String.valueOf(pair.count);
                int textX = currentX + 17 - mc.font.width(countText);
                int textY = materialsY + 9;
                guiGraphics.drawString(mc.font, countText, textX, textY, 0xFFFFFF, true);
                currentX += 20;
            }
            
            guiGraphics.pose().popPose();
        }
        
        // Render output on second row (centered)
        if (hasOutput) {
            int outputStartX = screenWidth / 2 - outputWidth / 2;
            
            // If no materials, render output on first row instead
            int outputRenderY = totalItems > 0 ? outputY : materialsY;
            
            // Render output item
            guiGraphics.renderItem(outputSlot, outputStartX, outputRenderY);
            
            // Render output count with yellow color
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 200);
            
            String countText = String.valueOf(outputSlot.getCount());
            int textX = outputStartX + 17 - mc.font.width(countText);
            int textY = outputRenderY + 9;
            guiGraphics.drawString(mc.font, countText, textX, textY, 0xFFFF00, true); // Yellow for output
            
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
