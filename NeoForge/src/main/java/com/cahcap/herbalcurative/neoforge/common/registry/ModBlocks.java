package com.cahcap.herbalcurative.neoforge.common.registry;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.common.block.*;
import com.cahcap.herbalcurative.neoforge.common.block.HerbCabinetBlock;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HerbalCurativeCommon.MOD_ID);

    // ==================== Herb Flowers ====================
    
    // Overworld herbs
    public static final DeferredBlock<Block> VERDSCALE_FERN = registerFlower("verdscale_fern", 10);
    public static final DeferredBlock<Block> DEWPETAL = registerFlower("dewpetal", 10);
    public static final DeferredBlock<Block> ZEPHYR_LILY = registerFlower("zephyr_lily", 10);
    
    // Nether herbs - only grow on specific Nether blocks
    public static final DeferredBlock<Block> CRYSTBUD = registerCrystbudFlower("crystbud", 10);
    public static final DeferredBlock<Block> PYRISAGE = registerPyrisageFlower("pyrisage", 10);
    
    // End herb - only grows on End Stone
    public static final DeferredBlock<Block> ROSYNIA = registerRosyniaFlower("rosynia", 10);

    // ==================== Herb Crops ====================
    
    public static final DeferredBlock<HerbCropBlock> VERDSCALE_FERN_CROP = registerCrop("verdscale_fern_crop", 
            () -> ModItems.VERDSCALE_FERN_SEED);
    public static final DeferredBlock<HerbCropBlock> DEWPETAL_CROP = registerCrop("dewpetal_crop", 
            () -> ModItems.DEWPETAL_SEED);
    public static final DeferredBlock<HerbCropBlock> ZEPHYR_LILY_CROP = registerCrop("zephyr_lily_crop", 
            () -> ModItems.ZEPHYR_LILY_SEED);
    public static final DeferredBlock<HerbCropBlock> CRYSTBUD_CROP = registerCrop("crystbud_crop", 
            () -> ModItems.CRYSTBUD_SEED);
    public static final DeferredBlock<HerbCropBlock> PYRISAGE_CROP = registerCrop("pyrisage_crop", 
            () -> ModItems.PYRISAGE_SEED);
    public static final DeferredBlock<HerbCropBlock> ROSYNIA_CROP = registerCrop("rosynia_crop", 
            () -> ModItems.ROSYNIA_SEED);

    // ==================== Red Cherry Blocks ====================
    
    public static final DeferredBlock<Block> RED_CHERRY_LOG = registerBlockWithItem("red_cherry_log",
            () -> new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)));
    
    public static final DeferredBlock<Block> RED_CHERRY_PLANKS = registerBlockWithItem("red_cherry_planks",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)));
    
    public static final DeferredBlock<RedCherryLeavesBlock> RED_CHERRY_LEAVES = registerBlockWithItem("red_cherry_leaves",
            () -> new RedCherryLeavesBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .strength(0.2F)
                    .randomTicks()
                    .sound(SoundType.GRASS)
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
                    .ignitedByLava()
                    .pushReaction(PushReaction.DESTROY)));
    
    public static final DeferredBlock<RedCherrySaplingBlock> RED_CHERRY_SAPLING = registerBlockWithItem("red_cherry_sapling",
            () -> new RedCherrySaplingBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)));
    
    public static final DeferredBlock<RedCherryBushBlock> RED_CHERRY_BUSH = BLOCKS.register("red_cherry_bush",
            () -> new RedCherryBushBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.SWEET_BERRY_BUSH)
                    .pushReaction(PushReaction.DESTROY)));

    // ==================== Multiblock Structures ====================
    
    public static final DeferredBlock<HerbCabinetBlock> HERB_CABINET = BLOCKS.register("herb_cabinet",
            () -> new HerbCabinetBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    // ==================== Helper Methods ====================
    
    private static DeferredBlock<Block> registerFlower(String name, int lightLevel) {
        DeferredBlock<Block> block = BLOCKS.register(name, () -> new HerbFlowerBlock(
                MobEffects.REGENERATION,
                5.0F,
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.PLANT)
                        .noCollission()
                        .instabreak()
                        .sound(SoundType.GRASS)
                        .pushReaction(PushReaction.DESTROY)
        ));
        // Register BlockItem
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }
    
    private static DeferredBlock<Block> registerCrystbudFlower(String name, int lightLevel) {
        DeferredBlock<Block> block = BLOCKS.register(name, () -> new CrystbudFlowerBlock(
                MobEffects.REGENERATION,
                5.0F,
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.PLANT)
                        .noCollission()
                        .instabreak()
                        .sound(SoundType.GRASS)
                        .lightLevel(state -> lightLevel)
                        .pushReaction(PushReaction.DESTROY)
        ));
        // Register BlockItem
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }
    
    private static DeferredBlock<Block> registerPyrisageFlower(String name, int lightLevel) {
        DeferredBlock<Block> block = BLOCKS.register(name, () -> new PyrisageFlowerBlock(
                MobEffects.REGENERATION,
                5.0F,
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.PLANT)
                        .noCollission()
                        .instabreak()
                        .sound(SoundType.GRASS)
                        .lightLevel(state -> lightLevel)
                        .pushReaction(PushReaction.DESTROY)
        ));
        // Register BlockItem
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }
    
    private static DeferredBlock<Block> registerRosyniaFlower(String name, int lightLevel) {
        DeferredBlock<Block> block = BLOCKS.register(name, () -> new RosyniaFlowerBlock(
                MobEffects.REGENERATION,
                5.0F,
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.PLANT)
                        .noCollission()
                        .instabreak()
                        .sound(SoundType.GRASS)
                        .lightLevel(state -> lightLevel)
                        .pushReaction(PushReaction.DESTROY)
        ));
        // Register BlockItem
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }
    
    private static DeferredBlock<HerbCropBlock> registerCrop(String name, Supplier<DeferredItem<Item>> seedSupplier) {
        return BLOCKS.register(name, () -> new HerbCropBlock(
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.PLANT)
                        .noCollission()
                        .randomTicks()
                        .instabreak()
                        .sound(SoundType.CROP)
                        .pushReaction(PushReaction.DESTROY),
                () -> seedSupplier.get().get()
        ));
    }
    
    private static <T extends Block> DeferredBlock<T> registerBlockWithItem(String name, Supplier<T> blockSupplier) {
        DeferredBlock<T> block = BLOCKS.register(name, blockSupplier);
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }
}

