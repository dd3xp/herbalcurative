package com.cahcap.herbalcurative.block;

import com.cahcap.herbalcurative.registry.ModBlocks;
import com.cahcap.herbalcurative.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ForestHeartwoodLeavesBlock extends LeavesBlock {
    
    public ForestHeartwoodLeavesBlock(Properties properties) {
        super(properties);
    }
    
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Check for berry bush below before decay
        if (this.decaying(state)) {
            BlockPos downPos = pos.below();
            BlockState downState = level.getBlockState(downPos);
            
            if (downState.getBlock() instanceof ForestBerryBushBlock) {
                // Drop berries from the bush
                int age = downState.getValue(ForestBerryBushBlock.AGE);
                if (ModItems.FOREST_BERRY != null) {
                    int dropCount = age >= 2 ? 1 + random.nextInt(2) : 1;
                    Block.popResource(level, downPos, new ItemStack(ModItems.FOREST_BERRY.get(), dropCount));
                }
                // Remove the bush
                level.removeBlock(downPos, false);
            }
        }
        super.randomTick(state, level, pos, random);
    }
    
    @Override
    public void onRemove(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            // Check for berry bush below
            BlockPos downPos = pos.below();
            BlockState downState = level.getBlockState(downPos);
            
            if (downState.getBlock() instanceof ForestBerryBushBlock) {
                int age = downState.getValue(ForestBerryBushBlock.AGE);
                if (ModItems.FOREST_BERRY != null) {
                    int dropCount = age >= 2 ? 1 + level.random.nextInt(2) : 1;
                    Block.popResource(level, downPos, new ItemStack(ModItems.FOREST_BERRY.get(), dropCount));
                }
                level.removeBlock(downPos, false);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
