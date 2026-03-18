package com.cahcap.neoforge.common.handler;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.block.HerbCabinetBlock;
import com.cahcap.common.block.HerbVaultBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Creative-mode only handler for HerbCabinet and HerbVault left-clicks.
 * <p>
 * In survival mode, extraction is handled by Block.attack() which fires once
 * per click naturally. No event cancellation needed.
 * <p>
 * In creative mode, blocks break instantly so the event must be canceled to
 * prevent destruction. We call attack() manually since canceling the event
 * prevents Minecraft from calling it.
 */
@EventBusSubscriber(modid = HerbalCurativeCommon.MOD_ID)
public class HerbStorageLeftClickHandler {

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (!player.isCreative()) return;

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof HerbCabinetBlock cabinet && cabinet.isFrontFaceClick(state, level, pos, player)) {
            state.attack(level, pos, player);
            event.setCanceled(true);
        } else if (block instanceof HerbVaultBlock vault && vault.isFrontFaceClick(state, level, pos, player)) {
            state.attack(level, pos, player);
            event.setCanceled(true);
        }
    }
}
