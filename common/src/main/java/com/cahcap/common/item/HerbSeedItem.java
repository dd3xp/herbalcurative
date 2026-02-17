package com.cahcap.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base class for herb seeds
 * Can be planted on farmland to grow herb crops
 */
public class HerbSeedItem extends Item {
    
    private final Block cropBlock;
    
    public HerbSeedItem(Block cropBlock, Properties properties) {
        super(properties);
        this.cropBlock = cropBlock;
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        
        // Check if clicking on farmland from the top
        if (state.is(Blocks.FARMLAND) && context.getClickedFace().getStepY() > 0) {
            BlockPos cropPos = pos.above();
            
            // Check if the position above farmland is air
            if (level.getBlockState(cropPos).isAir()) {
                // Plant the crop
                if (!level.isClientSide()) {
                    level.setBlock(cropPos, cropBlock.defaultBlockState(), 3);
                    
                    // Consume seed if not in creative mode
                    if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
                        context.getItemInHand().shrink(1);
                    }
                }
                
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        
        return InteractionResult.PASS;
    }
}

