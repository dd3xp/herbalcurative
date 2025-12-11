package com.cahcap.herbalcurative.neoforge.client.handler;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.neoforge.common.registry.ModBlocks;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.BiomeColors;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/**
 * Block and Item color handlers for biome-based coloring
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class BlockColorHandler {
    
    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        // Red Cherry Leaves - use biome foliage color
        BlockColor leavesColor = (state, level, pos, tintIndex) -> {
            if (tintIndex == 0 && level != null && pos != null) {
                return BiomeColors.getAverageFoliageColor(level, pos);
            }
            return 0xFFFFFF; // White (no tint) for other tint indices
        };
        
        event.register(leavesColor, ModBlocks.RED_CHERRY_LEAVES.get());
        
        // Red Cherry Bush - also use biome foliage color
        event.register(leavesColor, ModBlocks.RED_CHERRY_BUSH.get());
    }
    
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        // Red Cherry Leaves item - green color in inventory
        ItemColor leavesItemColor = (stack, tintIndex) -> {
            if (tintIndex == 0) {
                return 0x48B518; // Green color (similar to vanilla foliage)
            }
            return 0xFFFFFF;
        };
        
        event.register(leavesItemColor, ModBlocks.RED_CHERRY_LEAVES.get());
        
        // Red Cherry Bush item - also green in inventory
        event.register(leavesItemColor, ModBlocks.RED_CHERRY_BUSH.get());
    }
}

