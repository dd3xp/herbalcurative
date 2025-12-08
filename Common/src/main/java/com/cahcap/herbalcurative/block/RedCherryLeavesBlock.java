package com.cahcap.herbalcurative.block;

import com.cahcap.herbalcurative.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RedCherryLeavesBlock extends LeavesBlock {
    
    public RedCherryLeavesBlock(Properties properties) {
        super(properties);
    }
    
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (this.decaying(state)) {
            BlockPos downPos = pos.below();
            BlockState downState = level.getBlockState(downPos);
            
            if (downState.getBlock() instanceof RedCherryBushBlock) {
                int age = downState.getValue(RedCherryBushBlock.AGE);
                if (ModRegistries.RED_CHERRY != null) {
                    int dropCount = age >= 2 ? 1 + random.nextInt(2) : 1;
                    Block.popResource(level, downPos, new ItemStack(ModRegistries.RED_CHERRY.get(), dropCount));
                }
                level.removeBlock(downPos, false);
            }
        }
        super.randomTick(state, level, pos, random);
    }
    
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            BlockPos downPos = pos.below();
            BlockState downState = level.getBlockState(downPos);
            
            if (downState.getBlock() instanceof RedCherryBushBlock) {
                int age = downState.getValue(RedCherryBushBlock.AGE);
                if (ModRegistries.RED_CHERRY != null) {
                    int dropCount = age >= 2 ? 1 + level.random.nextInt(2) : 1;
                    Block.popResource(level, downPos, new ItemStack(ModRegistries.RED_CHERRY.get(), dropCount));
                }
                level.removeBlock(downPos, false);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}

