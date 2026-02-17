package com.cahcap.neoforge.common.registry;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.block.*;
import com.cahcap.neoforge.common.block.HerbCabinetBlock;
import com.cahcap.common.block.HerbBasketBlock;
import com.cahcap.common.block.RedCherryShelfBlock;
import com.cahcap.common.block.WorkbenchBlock;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
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
    
    // Overworld herbs (no light emission)
    public static final DeferredBlock<Block> VERDSCALE_FERN = registerFlower("verdscale_fern");
    public static final DeferredBlock<Block> DEWPETAL = registerFlower("dewpetal");
    public static final DeferredBlock<Block> ZEPHYR_LILY = registerFlower("zephyr_lily");
    
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
    
    // Logs
    public static final DeferredBlock<RotatedPillarBlock> RED_CHERRY_LOG = registerBlock("red_cherry_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)));
    
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_RED_CHERRY_LOG = registerBlock("stripped_red_cherry_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)));
    
    // Wood (6-sided bark/stripped texture)
    public static final DeferredBlock<RotatedPillarBlock> RED_CHERRY_WOOD = registerBlock("red_cherry_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)));
    
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_RED_CHERRY_WOOD = registerBlock("stripped_red_cherry_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)));
    
    // Planks
    public static final DeferredBlock<Block> RED_CHERRY_PLANKS = registerBlock("red_cherry_planks",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)));
    
    // Stairs and Slabs
    public static final DeferredBlock<StairBlock> RED_CHERRY_STAIRS = registerBlock("red_cherry_stairs",
            () -> new StairBlock(RED_CHERRY_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)));
    
    public static final DeferredBlock<SlabBlock> RED_CHERRY_SLAB = registerBlock("red_cherry_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)));
    
    public static final DeferredBlock<RedCherryLeavesBlock> RED_CHERRY_LEAVES = registerBlock("red_cherry_leaves",
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
    
    public static final DeferredBlock<RedCherrySaplingBlock> RED_CHERRY_SAPLING = registerBlock("red_cherry_sapling",
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

    // ==================== Potted Plants ====================
    
    public static final DeferredBlock<FlowerPotBlock> POTTED_VERDSCALE_FERN = registerPottedPlant("potted_verdscale_fern", VERDSCALE_FERN);
    public static final DeferredBlock<FlowerPotBlock> POTTED_DEWPETAL = registerPottedPlant("potted_dewpetal", DEWPETAL);
    public static final DeferredBlock<FlowerPotBlock> POTTED_ZEPHYR_LILY = registerPottedPlant("potted_zephyr_lily", ZEPHYR_LILY);
    public static final DeferredBlock<FlowerPotBlock> POTTED_CRYSTBUD = registerPottedPlant("potted_crystbud", CRYSTBUD);
    public static final DeferredBlock<FlowerPotBlock> POTTED_PYRISAGE = registerPottedPlant("potted_pyrisage", PYRISAGE);
    public static final DeferredBlock<FlowerPotBlock> POTTED_ROSYNIA = registerPottedPlant("potted_rosynia", ROSYNIA);
    public static final DeferredBlock<FlowerPotBlock> POTTED_RED_CHERRY_SAPLING = registerPottedPlant("potted_red_cherry_sapling", RED_CHERRY_SAPLING);

    // ==================== Lumistone Blocks ====================
    // Light levels: Lumistone series = 7 (weak glow), Rune Stone Bricks = 15 (glowstone level)
    
    public static final DeferredBlock<Block> LUMISTONE = registerBlock("lumistone",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(1.5F, 6.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 7)));
    
    public static final DeferredBlock<Block> LUMISTONE_BRICKS = registerBlock("lumistone_bricks",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(1.5F, 6.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 7)));
    
    public static final DeferredBlock<Block> RUNE_STONE_BRICKS = registerBlock("rune_stone_bricks",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 15)));
    
    public static final DeferredBlock<SlabBlock> LUMISTONE_BRICK_SLAB = registerBlock("lumistone_brick_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(1.5F, 6.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 7)));
    
    public static final DeferredBlock<StairBlock> LUMISTONE_BRICK_STAIRS = registerBlock("lumistone_brick_stairs",
            () -> new StairBlock(LUMISTONE_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(1.5F, 6.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 7)));

    // ==================== Multiblock Structures ====================
    
    public static final DeferredBlock<HerbCabinetBlock> HERB_CABINET = BLOCKS.register("herb_cabinet",
            () -> new HerbCabinetBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));
    
    public static final DeferredBlock<HerbBasketBlock> HERB_BASKET = BLOCKS.register("herb_basket",
            () -> new HerbBasketBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));
    
    public static final DeferredBlock<RedCherryShelfBlock> RED_CHERRY_SHELF = BLOCKS.register("red_cherry_shelf",
            () -> new RedCherryShelfBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));
    
    public static final DeferredBlock<WorkbenchBlock> WORKBENCH = BLOCKS.register("workbench",
            () -> new WorkbenchBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));
    
    public static final DeferredBlock<com.cahcap.neoforge.common.block.CauldronBlock> CAULDRON = BLOCKS.register("cauldron",
            () -> new com.cahcap.neoforge.common.block.CauldronBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(CauldronBlock.FORMED) ? 7 : 0)));

    // ==================== Helper Methods ====================
    
    private static DeferredBlock<Block> registerFlower(String name) {
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
    
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> blockSupplier) {
        return BLOCKS.register(name, blockSupplier);
    }
    
    private static DeferredBlock<FlowerPotBlock> registerPottedPlant(String name, Supplier<? extends Block> plant) {
        return BLOCKS.register(name, () -> new FlowerPotBlock(
                () -> (FlowerPotBlock) Blocks.FLOWER_POT,
                plant,
                BlockBehaviour.Properties.of()
                        .instabreak()
                        .noOcclusion()
                        .pushReaction(PushReaction.DESTROY)
        ));
    }
    
    /**
     * Register all potted plants to the FlowerPotBlock content map.
     * Must be called during mod setup (FMLCommonSetupEvent).
     */
    public static void registerFlowerPots() {
        ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(VERDSCALE_FERN.getId(), POTTED_VERDSCALE_FERN);
        ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(DEWPETAL.getId(), POTTED_DEWPETAL);
        ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ZEPHYR_LILY.getId(), POTTED_ZEPHYR_LILY);
        ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(CRYSTBUD.getId(), POTTED_CRYSTBUD);
        ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(PYRISAGE.getId(), POTTED_PYRISAGE);
        ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ROSYNIA.getId(), POTTED_ROSYNIA);
        ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(RED_CHERRY_SAPLING.getId(), POTTED_RED_CHERRY_SAPLING);
    }
}

