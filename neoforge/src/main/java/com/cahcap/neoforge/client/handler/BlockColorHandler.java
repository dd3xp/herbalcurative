package com.cahcap.neoforge.client.handler;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.block.CrystalPlantBlock;
import com.cahcap.common.item.IncensePowderItem;
import com.cahcap.neoforge.common.registry.ModBlocks;
import com.cahcap.neoforge.common.registry.ModItems;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
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
            return 0xFFFFFF;
        };
        
        event.register(leavesColor, ModBlocks.RED_CHERRY_LEAVES.get());
        event.register(leavesColor, ModBlocks.RED_CHERRY_BUSH.get());
        
        // Crystal Plants - colored based on ore type (block placed in world)
        BlockColor crystPlantBlockColor = (state, level, pos, tintIndex) -> {
            if (tintIndex == 0 && state.getBlock() instanceof CrystalPlantBlock plant) {
                return plant.getColor();
            }
            return 0xFFFFFF;
        };
        event.register(crystPlantBlockColor, ModBlocks.IRON_CRYST_PLANT.get());
        
        // Crystal Plants in flower pots - need to get color from the plant block
        BlockColor pottedCrystPlantColor = (state, level, pos, tintIndex) -> {
            if (tintIndex == 0) {
                return ModBlocks.IRON_CRYST_PLANT.get().getColor();
            }
            return 0xFFFFFF;
        };
        event.register(pottedCrystPlantColor, ModBlocks.POTTED_IRON_CRYST_PLANT.get());
    }
    
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        // Red Cherry Leaves item - green color in inventory
        ItemColor leavesItemColor = (stack, tintIndex) -> {
            if (tintIndex == 0) {
                return 0x48B518;
            }
            return 0xFFFFFF;
        };
        
        event.register(leavesItemColor, ModBlocks.RED_CHERRY_LEAVES.get());
        event.register(leavesItemColor, ModBlocks.RED_CHERRY_BUSH.get());
        
        // Incense Powder - colored based on mob type
        ItemColor powderColor = (stack, tintIndex) -> {
            if (tintIndex == 0 && stack.getItem() instanceof IncensePowderItem powder) {
                return powder.getColor(stack);
            }
            return 0xFFFFFF;
        };
        event.register(powderColor, ModItems.WITHER_SKELETON_POWDER.get());
        
        // Crystal Plants - colored based on ore type (item in inventory/hand)
        ItemColor crystPlantItemColor = (stack, tintIndex) -> {
            if (tintIndex == 0 && stack.getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                if (block instanceof CrystalPlantBlock plant) {
                    return plant.getColor();
                }
            }
            return 0xFFFFFF;
        };
        event.register(crystPlantItemColor, ModItems.IRON_CRYST_PLANT.get());
    }
}

