package com.cahcap.herbalcurative.client.handler;

import com.cahcap.herbalcurative.HerbalCurative;
import com.cahcap.herbalcurative.registry.ModBlocks;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.item.BlockItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/**
 * Block and Item color handlers for biome-based coloring
 */
@EventBusSubscriber(modid = HerbalCurative.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class BlockColorHandler {
    
    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        // Forest Heartwood Leaves - use biome foliage color
        BlockColor leavesColor = (state, level, pos, tintIndex) -> {
            if (tintIndex == 0 && level != null && pos != null) {
                return BiomeColors.getAverageFoliageColor(level, pos);
            }
            return 0xFFFFFF; // White (no tint) for other tint indices
        };
        
        event.register(leavesColor, ModBlocks.FOREST_HEARTWOOD_LEAVES.get());
        
        // Forest Berry Bush - also use biome foliage color
        event.register(leavesColor, ModBlocks.FOREST_BERRY_BUSH.get());
    }
    
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        // Forest Heartwood Leaves item - green color in inventory
        ItemColor leavesItemColor = (stack, tintIndex) -> {
            if (tintIndex == 0) {
                return 0x48B518; // Green color (similar to vanilla foliage)
            }
            return 0xFFFFFF;
        };
        
        event.register(leavesItemColor, ModBlocks.FOREST_HEARTWOOD_LEAVES.get());
        
        // Forest Berry Bush item - also green in inventory
        event.register(leavesItemColor, ModBlocks.FOREST_BERRY_BUSH.get());
    }
}
