package com.cahcap.herbalcurative.neoforge.client.handler;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.common.blockentity.CauldronBlockEntity;
import com.cahcap.herbalcurative.common.registry.ModRegistries;
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

        // Get items to display
        List<ItemCountPair> items = new ArrayList<>();
        
        if (master.isBrewing()) {
            // Show herbs during brewing
            Map<Item, Integer> herbs = master.getHerbs();
            for (Map.Entry<Item, Integer> entry : herbs.entrySet()) {
                items.add(new ItemCountPair(entry.getKey(), entry.getValue()));
            }
        } else {
            // Show materials when not brewing
            List<ItemStack> materials = master.getMaterials();
            for (ItemStack stack : materials) {
                if (!stack.isEmpty()) {
                    // Merge same items
                    boolean found = false;
                    for (ItemCountPair pair : items) {
                        if (pair.item == stack.getItem()) {
                            pair.count += stack.getCount();
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        items.add(new ItemCountPair(stack.getItem(), stack.getCount()));
                    }
                }
            }
        }

        // Don't render if nothing to show
        if (items.isEmpty()) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        // Position below crosshair
        int startX = screenWidth / 2 - (items.size() * 20) / 2;
        int y = screenHeight / 2 + 12;

        // Render each item (items first, then text on top)
        for (int i = 0; i < items.size(); i++) {
            ItemCountPair pair = items.get(i);
            ItemStack stack = new ItemStack(pair.item);
            int x = startX + i * 20;
            
            // Render the item icon
            guiGraphics.renderItem(stack, x, y);
        }
        
        // Render text on top of all items (higher z-layer)
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 200);  // Move text to higher z-layer
        for (int i = 0; i < items.size(); i++) {
            ItemCountPair pair = items.get(i);
            int x = startX + i * 20;
            
            // Render the amount as overlay
            String countText = String.valueOf(pair.count);
            int textX = x + 17 - mc.font.width(countText);
            int textY = y + 9;
            guiGraphics.drawString(mc.font, countText, textX, textY, 0xFFFFFF, true);
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
