package com.cahcap.neoforge.common.handler;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.block.HerbCabinetBlock;
import com.cahcap.common.block.HerbVaultBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Intercepts left-clicks on HerbCabinet and HerbVault front faces.
 * Performs herb extraction first, then cancels the event to prevent block breaking.
 * <p>
 * Event order: LeftClickBlock fires BEFORE Block.attack(), so we must handle
 * extraction here instead of relying on attack().
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID)
public class HerbStorageLeftClickHandler {

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Level level = event.getLevel();
        BlockState state = level.getBlockState(event.getPos());
        Block block = state.getBlock();

        if (block instanceof HerbCabinetBlock cabinet) {
            if (cabinet.shouldInterceptLeftClick(state, level, event.getPos(), event.getEntity())) {
                cabinet.handleLeftClickExtraction(level, event.getPos(), event.getEntity(), state);
                event.setCanceled(true);
            }
        } else if (block instanceof HerbVaultBlock vault) {
            if (vault.shouldInterceptLeftClick(state, level, event.getPos(), event.getEntity())) {
                vault.handleLeftClickExtraction(level, event.getPos(), event.getEntity(), state);
                event.setCanceled(true);
            }
        }
    }
}
