package com.cahcap.neoforge.common.registry;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.block.*;
import com.cahcap.common.multiblock.Multiblock;
import com.cahcap.neoforge.common.block.HerbCabinetBlock;
import com.cahcap.common.block.HerbBasketBlock;
import com.cahcap.common.block.ShelfBlock;
import com.cahcap.common.block.WorkbenchBlock;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
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
    public static final DeferredBlock<Block> SCLERIS = registerFlower("scleris");
    public static final DeferredBlock<Block> DORELLA = registerFlower("dorella");
    public static final DeferredBlock<Block> SEPHREL = registerFlower("sephrel");
    
    // Nether herbs - only grow on specific Nether blocks
    public static final DeferredBlock<Block> CRYSEL = registerCryselFlower("crysel", 10);
    public static final DeferredBlock<Block> PYRAZE = registerPyrazeFlower("pyraze", 10);
    
    // End herb - only grows on End Stone
    public static final DeferredBlock<Block> STELLIA = registerStelliaFlower("stellia", 10);
    
    // ==================== Crystal Plants ====================
    // Crystal plant seedlings that can be placed on ground or in flower pots
    // Uses grayscale texture tinted to ore color
    
    public static final DeferredBlock<CrystalPlantBlock> IRON_CRYST_PLANT = registerCrystalPlant(
            "iron_cryst_plant", "iron", 0xFFD8D8D8);

    // ==================== Herb Crops ====================
    
    public static final DeferredBlock<HerbCropBlock> SCLERIS_CROP = registerCrop("scleris_crop", 
            () -> ModItems.SCLERIS_SEED);
    public static final DeferredBlock<HerbCropBlock> DORELLA_CROP = registerCrop("dorella_crop", 
            () -> ModItems.DORELLA_SEED);
    public static final DeferredBlock<HerbCropBlock> SEPHREL_CROP = registerCrop("sephrel_crop", 
            () -> ModItems.SEPHREL_SEED);
    public static final DeferredBlock<HerbCropBlock> CRYSEL_CROP = registerCrop("crysel_crop", 
            () -> ModItems.CRYSEL_SEED);
    public static final DeferredBlock<HerbCropBlock> PYRAZE_CROP = registerCrop("pyraze_crop", 
            () -> ModItems.PYRAZE_SEED);
    public static final DeferredBlock<HerbCropBlock> STELLIA_CROP = registerCrop("stellia_crop", 
            () -> ModItems.STELLIA_SEED);

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
    
    // Fence and Fence Gate
    public static final DeferredBlock<FenceBlock> RED_CHERRY_FENCE = registerBlock("red_cherry_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)));
    
    public static final DeferredBlock<FenceGateBlock> RED_CHERRY_FENCE_GATE = registerBlock("red_cherry_fence_gate",
            () -> new FenceGateBlock(WoodType.CHERRY, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)));
    
    // Button and Pressure Plate
    public static final DeferredBlock<ButtonBlock> RED_CHERRY_BUTTON = registerBlock("red_cherry_button",
            () -> new ButtonBlock(BlockSetType.CHERRY, 30, BlockBehaviour.Properties.of()
                    .noCollission()
                    .strength(0.5F)
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.DESTROY)));
    
    public static final DeferredBlock<PressurePlateBlock> RED_CHERRY_PRESSURE_PLATE = registerBlock("red_cherry_pressure_plate",
            () -> new PressurePlateBlock(BlockSetType.CHERRY, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .noCollission()
                    .strength(0.5F)
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.DESTROY)));
    
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
    
    public static final DeferredBlock<FlowerPotBlock> POTTED_SCLERIS = registerPottedPlant("potted_scleris", SCLERIS);
    public static final DeferredBlock<FlowerPotBlock> POTTED_DORELLA = registerPottedPlant("potted_dorella", DORELLA);
    public static final DeferredBlock<FlowerPotBlock> POTTED_SEPHREL = registerPottedPlant("potted_sephrel", SEPHREL);
    public static final DeferredBlock<FlowerPotBlock> POTTED_CRYSEL = registerPottedPlant("potted_crysel", CRYSEL);
    public static final DeferredBlock<FlowerPotBlock> POTTED_PYRAZE = registerPottedPlant("potted_pyraze", PYRAZE);
    public static final DeferredBlock<FlowerPotBlock> POTTED_STELLIA = registerPottedPlant("potted_stellia", STELLIA);
    public static final DeferredBlock<FlowerPotBlock> POTTED_RED_CHERRY_SAPLING = registerPottedPlant("potted_red_cherry_sapling", RED_CHERRY_SAPLING);
    
    // Crystal Plant potted versions
    public static final DeferredBlock<FlowerPotBlock> POTTED_IRON_CRYST_PLANT = registerPottedPlant("potted_iron_cryst_plant", IRON_CRYST_PLANT);

    // ==================== Lumistone Blocks ====================
    // Light levels: Rune Stone Bricks = 15 (glowstone level)
    
    public static final DeferredBlock<Block> LUMISTONE = registerBlock("lumistone",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(1.5F, 6.0F)
                    .sound(SoundType.STONE)
));

    public static final DeferredBlock<SlabBlock> LUMISTONE_SLAB = registerBlock("lumistone_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(1.5F, 6.0F)
                    .sound(SoundType.STONE)
));

    public static final DeferredBlock<StairBlock> LUMISTONE_STAIRS = registerBlock("lumistone_stairs",
            () -> new StairBlock(LUMISTONE.get().defaultBlockState(), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(1.5F, 6.0F)
                    .sound(SoundType.STONE)
));

    public static final DeferredBlock<WallBlock> LUMISTONE_WALL = registerBlock("lumistone_wall",
            () -> new WallBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(1.5F, 6.0F)
                    .sound(SoundType.STONE)
                    
                    .forceSolidOn()));

    public static final DeferredBlock<PressurePlateBlock> LUMISTONE_PRESSURE_PLATE = registerBlock("lumistone_pressure_plate",
            () -> new PressurePlateBlock(BlockSetType.STONE, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(0.5F)
                    .sound(SoundType.STONE)
                    
                    .noCollission()));

    public static final DeferredBlock<ButtonBlock> LUMISTONE_BUTTON = registerBlock("lumistone_button",
            () -> new ButtonBlock(BlockSetType.STONE, 20, BlockBehaviour.Properties.of()
                    .noCollission()
                    .strength(0.5F)
                    .sound(SoundType.STONE)
));

    public static final DeferredBlock<Block> LUMISTONE_BRICKS = registerBlock("lumistone_bricks",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(1.5F, 6.0F)
                    .sound(SoundType.STONE)
));
    
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
));
    
    public static final DeferredBlock<StairBlock> LUMISTONE_BRICK_STAIRS = registerBlock("lumistone_brick_stairs",
            () -> new StairBlock(LUMISTONE_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(1.5F, 6.0F)
                    .sound(SoundType.STONE)
));

    public static final DeferredBlock<WallBlock> LUMISTONE_BRICK_WALL = registerBlock("lumistone_brick_wall",
            () -> new WallBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(1.5F, 6.0F)
                    .sound(SoundType.STONE)
                    
                    .forceSolidOn()));

    // ==================== Magic Alloy ====================

    public static final DeferredBlock<Block> MAGIC_ALLOY_BLOCK = registerBlock("magic_alloy_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.METAL)
));

    // ==================== Multiblock Structures ====================
    
    public static final DeferredBlock<HerbCabinetBlock> HERB_CABINET = BLOCKS.register("herb_cabinet",
            () -> new HerbCabinetBlock(Multiblock.addInteriorSpaceProperties(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD))));
    
    public static final DeferredBlock<HerbBasketBlock> HERB_BASKET = BLOCKS.register("herb_basket",
            () -> new HerbBasketBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));
    
    public static final DeferredBlock<ShelfBlock> SHELF = BLOCKS.register("shelf",
            () -> new ShelfBlock(BlockBehaviour.Properties.of()
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
            () -> new com.cahcap.neoforge.common.block.CauldronBlock(Multiblock.addInteriorSpaceProperties(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops())
));
    
    public static final DeferredBlock<com.cahcap.common.block.HerbPotBlock> HERB_POT = BLOCKS.register("herb_pot",
            () -> new com.cahcap.common.block.HerbPotBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_BROWN)
                    .strength(1.25F, 4.2F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<com.cahcap.common.block.IncenseBurnerBlock> INCENSE_BURNER = BLOCKS.register("incense_burner",
            () -> new com.cahcap.common.block.IncenseBurnerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(1.25F, 4.2F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<com.cahcap.neoforge.common.block.KilnBlock> KILN = BLOCKS.register("kiln",
            () -> new com.cahcap.neoforge.common.block.KilnBlock(Multiblock.addInteriorSpaceProperties(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops())
                    .lightLevel(state -> state.getValue(com.cahcap.common.block.KilnBlock.LIT) ? 15 : 0)));

    public static final DeferredBlock<com.cahcap.neoforge.common.block.HerbVaultBlock> HERB_VAULT = BLOCKS.register("herb_vault",
            () -> new com.cahcap.neoforge.common.block.HerbVaultBlock(Multiblock.addInteriorSpaceProperties(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops())
));

    public static final DeferredBlock<ObeliskBlock> OBELISK = BLOCKS.register("obelisk",
            () -> new ObeliskBlock(Multiblock.addInteriorSpaceProperties(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops())));

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
    
    private static DeferredBlock<Block> registerCryselFlower(String name, int lightLevel) {
        DeferredBlock<Block> block = BLOCKS.register(name, () -> new CryselFlowerBlock(
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
    
    private static DeferredBlock<Block> registerPyrazeFlower(String name, int lightLevel) {
        DeferredBlock<Block> block = BLOCKS.register(name, () -> new PyrazeFlowerBlock(
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
    
    private static DeferredBlock<Block> registerStelliaFlower(String name, int lightLevel) {
        DeferredBlock<Block> block = BLOCKS.register(name, () -> new StelliaFlowerBlock(
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
    
    private static DeferredBlock<CrystalPlantBlock> registerCrystalPlant(String name, String oreType, int color) {
        return BLOCKS.register(name, () -> new CrystalPlantBlock(
                oreType,
                color,
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.PLANT)
                        .noCollission()
                        .instabreak()
                        .sound(SoundType.GRASS)
                        .pushReaction(PushReaction.DESTROY)
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
        ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(SCLERIS.getId(), POTTED_SCLERIS);
        ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(DORELLA.getId(), POTTED_DORELLA);
        ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(SEPHREL.getId(), POTTED_SEPHREL);
        ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(CRYSEL.getId(), POTTED_CRYSEL);
        ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(PYRAZE.getId(), POTTED_PYRAZE);
        ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(STELLIA.getId(), POTTED_STELLIA);
        ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(RED_CHERRY_SAPLING.getId(), POTTED_RED_CHERRY_SAPLING);
        
        // Crystal Plants
        ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(IRON_CRYST_PLANT.getId(), POTTED_IRON_CRYST_PLANT);
    }
}

