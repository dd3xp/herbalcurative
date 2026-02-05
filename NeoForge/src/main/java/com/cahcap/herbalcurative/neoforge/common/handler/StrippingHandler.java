package com.cahcap.herbalcurative.neoforge.common.handler;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.neoforge.common.registry.ModBlocks;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles axe stripping for Red Cherry logs and wood.
 * When a player right-clicks with an axe on Red Cherry logs/wood,
 * they will be converted to their stripped variants.
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID)
public class StrippingHandler {
    
    private static final Map<BlockState, BlockState> STRIPPABLES = new HashMap<>();
    
    /**
     * Initialize the strippable block mappings.
     * Called during mod setup.
     */
    public static void init() {
        // Red Cherry Log -> Stripped Red Cherry Log
        STRIPPABLES.put(
            ModBlocks.RED_CHERRY_LOG.get().defaultBlockState(),
            ModBlocks.STRIPPED_RED_CHERRY_LOG.get().defaultBlockState()
        );
        
        // Red Cherry Wood -> Stripped Red Cherry Wood
        STRIPPABLES.put(
            ModBlocks.RED_CHERRY_WOOD.get().defaultBlockState(),
            ModBlocks.STRIPPED_RED_CHERRY_WOOD.get().defaultBlockState()
        );
    }
    
    @SubscribeEvent
    public static void onBlockToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.getItemAbility() == ItemAbilities.AXE_STRIP) {
            BlockState state = event.getState();
            
            // Check all axis values for rotated pillar blocks
            for (Map.Entry<BlockState, BlockState> entry : STRIPPABLES.entrySet()) {
                if (state.getBlock() == entry.getKey().getBlock()) {
                    // Preserve the axis property when stripping
                    BlockState strippedState = entry.getValue().getBlock().defaultBlockState();
                    if (state.hasProperty(net.minecraft.world.level.block.RotatedPillarBlock.AXIS)) {
                        strippedState = strippedState.setValue(
                            net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
                            state.getValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS)
                        );
                    }
                    event.setFinalState(strippedState);
                    return;
                }
            }
        }
    }
}
