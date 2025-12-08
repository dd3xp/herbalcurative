package com.cahcap.herbalcurative.block;

import com.cahcap.herbalcurative.registry.ModRegistries;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RedCherryBushBlock extends BushBlock implements BonemealableBlock {
    
    public static final MapCodec<RedCherryBushBlock> CODEC = simpleCodec(RedCherryBushBlock::new);
    
    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }
    
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 2);
    
    protected static final VoxelShape BUSH_AABB = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);
    protected static final VoxelShape GROWING_AABB = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    protected static final VoxelShape MATURE_AABB = Block.box(1.2D, 0.0D, 1.2D, 14.8D, 16.0D, 14.8D);
    
    public RedCherryBushBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int age = state.getValue(AGE);
        return switch (age) {
            case 0 -> BUSH_AABB;
            case 1 -> GROWING_AABB;
            default -> MATURE_AABB;
        };
    }
    
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
    
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos upPos = pos.above();
        BlockState upState = level.getBlockState(upPos);
        return upState.is(ModRegistries.RED_CHERRY_LEAVES.get());
    }
    
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        
        BlockPos upPos = pos.above();
        BlockState upState = level.getBlockState(upPos);
        if (!upState.is(ModRegistries.RED_CHERRY_LEAVES.get())) {
            if (ModRegistries.RED_CHERRY != null) {
                int dropCount = age >= 2 ? 1 + random.nextInt(2) : 1;
                Block.popResource(level, pos, new ItemStack(ModRegistries.RED_CHERRY.get(), dropCount));
            }
            level.removeBlock(pos, false);
            return;
        }
        
        if (age < 2 && level.getRawBrightness(pos.above(), 0) >= 6) {
            if (random.nextInt(25) == 0) {
                level.setBlock(pos, state.setValue(AGE, age + 1), 2);
            }
        }
    }
    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        int age = state.getValue(AGE);
        
        if (age >= 2) {
            if (!level.isClientSide && ModRegistries.RED_CHERRY != null) {
                int dropCount = 1 + level.random.nextInt(2);
                Block.popResource(level, pos, new ItemStack(ModRegistries.RED_CHERRY.get(), dropCount));
                level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
                level.setBlock(pos, state.setValue(AGE, 0), 2);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        
        return InteractionResult.PASS;
    }
    
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < 2 && canSurvive(state, level, pos);
    }
    
    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }
    
    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        if (age < 2) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
        }
    }
}

