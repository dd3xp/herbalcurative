package com.cahcap.common.item.flowweavering;

import com.cahcap.common.blockentity.HerbBasketBlockEntity;
import com.cahcap.common.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class HerbBasketInteraction implements RingBlockInteraction {

    @Override
    public boolean canInteract(UseOnContext context, BlockState state, Level level, BlockPos pos, Player player, ItemStack ringStack) {
        if (player != null && player.isShiftKeyDown() && state.is(ModRegistries.HERB_BASKET.get())) {
            if (level.getBlockEntity(pos) instanceof HerbBasketBlockEntity basket
                    && basket.getBoundHerb() != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public InteractionResult interact(UseOnContext context, BlockState state, Level level, BlockPos pos, Player player, ItemStack ringStack) {
        if (level.getBlockEntity(pos) instanceof HerbBasketBlockEntity basket) {
            Item boundHerbToClear = basket.getBoundHerb();
            if (boundHerbToClear != null) {
                int count = basket.getHerbCount();

                // Eject all herbs
                if (count > 0) {
                    while (count > 0) {
                        int stackSize = Math.min(count, 64);
                        ItemStack herbStack = new ItemStack(boundHerbToClear, stackSize);
                        ItemEntity entityItem = new ItemEntity(
                                level,
                                pos.getX() + 0.5,
                                pos.getY() + 0.5,
                                pos.getZ() + 0.5,
                                herbStack
                        );
                        entityItem.setDeltaMovement(
                                (level.random.nextDouble() - 0.5) * 0.2,
                                level.random.nextDouble() * 0.2 + 0.1,
                                (level.random.nextDouble() - 0.5) * 0.2
                        );
                        level.addFreshEntity(entityItem);
                        count -= stackSize;
                    }
                }

                // Clear binding
                basket.unbindHerb();

                // Play leaf break particles
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.COMPOSTER,
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            15,
                            0.3, 0.3, 0.3,
                            0.05
                    );
                }

                level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
