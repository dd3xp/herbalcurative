package com.cahcap.neoforge.common.datagen.loot;

import com.cahcap.common.block.RedCherryBushBlock;
import com.cahcap.common.block.HerbCropBlock;
import com.cahcap.neoforge.common.registry.ModBlocks;
import com.cahcap.neoforge.common.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Block loot table provider
 * Generates loot tables for 18 blocks
 */
public class ModBlockLootProvider extends LootTableProvider {
    
    public ModBlockLootProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Set.of(), List.of(
            new SubProviderEntry(ModBlockLoot::new, LootContextParamSets.BLOCK)
        ), lookupProvider);
    }
    
    private static class ModBlockLoot extends BlockLootSubProvider {
        
        protected ModBlockLoot(HolderLookup.Provider provider) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
        }
        
        @Override
        protected void generate() {
            // ==================== Blocks that drop themselves ====================
            
            // Herb flower blocks
            this.dropSelf(ModBlocks.CRYSTBUD.get());
            this.dropSelf(ModBlocks.DEWPETAL.get());
            this.dropSelf(ModBlocks.PYRISAGE.get());
            this.dropSelf(ModBlocks.ROSYNIA.get());
            this.dropSelf(ModBlocks.VERDSCALE_FERN.get());
            this.dropSelf(ModBlocks.ZEPHYR_LILY.get());
            
            // Crystal plant blocks
            this.dropSelf(ModBlocks.IRON_CRYST_PLANT.get());
            
            // Red Cherry blocks
            this.dropSelf(ModBlocks.RED_CHERRY_LOG.get());
            this.dropSelf(ModBlocks.STRIPPED_RED_CHERRY_LOG.get());
            this.dropSelf(ModBlocks.RED_CHERRY_WOOD.get());
            this.dropSelf(ModBlocks.STRIPPED_RED_CHERRY_WOOD.get());
            this.dropSelf(ModBlocks.RED_CHERRY_PLANKS.get());
            this.dropSelf(ModBlocks.RED_CHERRY_STAIRS.get());
            this.add(ModBlocks.RED_CHERRY_SLAB.get(), this::createSlabItemTable);
            this.dropSelf(ModBlocks.RED_CHERRY_FENCE.get());
            this.dropSelf(ModBlocks.RED_CHERRY_FENCE_GATE.get());
            this.dropSelf(ModBlocks.RED_CHERRY_BUTTON.get());
            this.dropSelf(ModBlocks.RED_CHERRY_PRESSURE_PLATE.get());
            this.dropSelf(ModBlocks.RED_CHERRY_SAPLING.get());
            
            // Herb Cabinet
            this.dropSelf(ModBlocks.HERB_CABINET.get());
            
            // Herb Basket
            this.dropSelf(ModBlocks.HERB_BASKET.get());
            
            // Red Cherry Shelf
            this.dropSelf(ModBlocks.RED_CHERRY_SHELF.get());
            
            // Workbench (center part only drops itself, left/right parts drop nothing - handled by block)
            this.dropSelf(ModBlocks.WORKBENCH.get());
            
            // Herb Pot - drops handled by block's getDrops method (contains items)
            this.add(ModBlocks.HERB_POT.get(), noDrop());
            
            // Incense Burner - drops handled by block's getDrops method (contains items)
            this.add(ModBlocks.INCENSE_BURNER.get(), noDrop());
            
            // ==================== Lumistone blocks ====================
            
            this.dropSelf(ModBlocks.LUMISTONE.get());
            this.dropSelf(ModBlocks.LUMISTONE_BRICKS.get());
            this.dropSelf(ModBlocks.RUNE_STONE_BRICKS.get());
            this.add(ModBlocks.LUMISTONE_BRICK_SLAB.get(), this::createSlabItemTable);
            this.dropSelf(ModBlocks.LUMISTONE_BRICK_STAIRS.get());
            
            // Cauldron - drops handled by block's getDrops method (position-based original blocks)
            this.add(ModBlocks.CAULDRON.get(), noDrop());

            // Kiln - drops handled by block's getDrops method (position-based original blocks)
            this.add(ModBlocks.KILN.get(), noDrop());
            
            // ==================== Potted plants (drop flower pot + plant) ====================
            
            this.add(ModBlocks.POTTED_VERDSCALE_FERN.get(), block -> 
                createPotFlowerItemTable(ModBlocks.VERDSCALE_FERN.get()));
            this.add(ModBlocks.POTTED_DEWPETAL.get(), block -> 
                createPotFlowerItemTable(ModBlocks.DEWPETAL.get()));
            this.add(ModBlocks.POTTED_ZEPHYR_LILY.get(), block -> 
                createPotFlowerItemTable(ModBlocks.ZEPHYR_LILY.get()));
            this.add(ModBlocks.POTTED_CRYSTBUD.get(), block -> 
                createPotFlowerItemTable(ModBlocks.CRYSTBUD.get()));
            this.add(ModBlocks.POTTED_PYRISAGE.get(), block -> 
                createPotFlowerItemTable(ModBlocks.PYRISAGE.get()));
            this.add(ModBlocks.POTTED_ROSYNIA.get(), block -> 
                createPotFlowerItemTable(ModBlocks.ROSYNIA.get()));
            this.add(ModBlocks.POTTED_RED_CHERRY_SAPLING.get(), block -> 
                createPotFlowerItemTable(ModBlocks.RED_CHERRY_SAPLING.get()));
            
            // Crystal plant potted versions
            this.add(ModBlocks.POTTED_IRON_CRYST_PLANT.get(), block -> 
                createPotFlowerItemTable(ModBlocks.IRON_CRYST_PLANT.get()));
            
            // ==================== Crop blocks (drop seeds and products when mature at age 9) ====================
            
            // Crystbud crop - drops Cryst Spine
            this.add(ModBlocks.CRYSTBUD_CROP.get(), block -> 
                createHerbCropDrops(block, ModItems.CRYST_SPINE.get(), ModItems.CRYSTBUD_SEED.get()));
            
            // Dewpetal crop - drops Dewpetal Shard
            this.add(ModBlocks.DEWPETAL_CROP.get(), block -> 
                createHerbCropDrops(block, ModItems.DEWPETAL_SHARD.get(), ModItems.DEWPETAL_SEED.get()));
            
            // Pyrisage crop - drops Burnt Node
            this.add(ModBlocks.PYRISAGE_CROP.get(), block -> 
                createHerbCropDrops(block, ModItems.BURNT_NODE.get(), ModItems.PYRISAGE_SEED.get()));
            
            // Rosynia crop - drops Heart of Stardream
            this.add(ModBlocks.ROSYNIA_CROP.get(), block -> 
                createHerbCropDrops(block, ModItems.HEART_OF_STARDREAM.get(), ModItems.ROSYNIA_SEED.get()));
            
            // Verdscale Fern crop - drops Scaleplate
            this.add(ModBlocks.VERDSCALE_FERN_CROP.get(), block -> 
                createHerbCropDrops(block, ModItems.SCALEPLATE.get(), ModItems.VERDSCALE_FERN_SEED.get()));
            
            // Zephyr Lily crop - drops Golden Lilybell
            this.add(ModBlocks.ZEPHYR_LILY_CROP.get(), block -> 
                createHerbCropDrops(block, ModItems.GOLDEN_LILYBELL.get(), ModItems.ZEPHYR_LILY_SEED.get()));
            
            // ==================== Special blocks ====================
            
            // Red Cherry Leaves (drops saplings and red cherry sticks)
            this.add(ModBlocks.RED_CHERRY_LEAVES.get(), block ->
                createRedCherryLeavesDrops(block, ModBlocks.RED_CHERRY_SAPLING.get(), ModItems.RED_CHERRY_STICK.get())
            );
            
            // Red Cherry Bush - drops berries at all stages
            // age 0-1: drops 1 berry, age 2: drops 1-2 berries (mature)
            this.add(ModBlocks.RED_CHERRY_BUSH.get(), block -> 
                applyExplosionDecay(block, LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                        // age 2 (mature): drops 1-2 berries
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                .hasProperty(RedCherryBushBlock.AGE, 2)))
                        .add(LootItem.lootTableItem(ModItems.RED_CHERRY.get()))
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
                    .withPool(LootPool.lootPool()
                        // age 1: drops 1 berry
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                .hasProperty(RedCherryBushBlock.AGE, 1)))
                        .add(LootItem.lootTableItem(ModItems.RED_CHERRY.get())))
                    .withPool(LootPool.lootPool()
                        // age 0: drops 1 berry
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                .hasProperty(RedCherryBushBlock.AGE, 0)))
                        .add(LootItem.lootTableItem(ModItems.RED_CHERRY.get())))
                )
            );
        }
        
        /**
         * Create Red Cherry leaves drops (saplings and red cherry sticks).
         * Fully custom implementation to use red cherry sticks instead of vanilla sticks.
         * 
         * Drop logic:
         * - Shears or Silk Touch: drops leaves block
         * - Normal break: chance to drop sapling (5% base) and red cherry sticks (2% base)
         */
        protected LootTable.Builder createRedCherryLeavesDrops(Block leavesBlock, Block saplingBlock, Item stickItem) {
            // Vanilla stick drop chances: 2%, 2.22%, 2.5%, 3.33%, 10% (Fortune 0-4)
            float[] stickChances = new float[]{0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F};
            
            var fortuneEnchantment = this.registries.lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.FORTUNE);
            
            return this.createSilkTouchOrShearsDispatchTable(leavesBlock,
                    // When NOT using shears/silk touch, drop sapling with chance
                    applyExplosionCondition(leavesBlock, LootItem.lootTableItem(saplingBlock))
                        .when(BonusLevelTableCondition.bonusLevelFlatChance(fortuneEnchantment, NORMAL_LEAVES_SAPLING_CHANCES)))
                // Red cherry sticks pool (only when not using shears)
                .withPool(LootPool.lootPool()
                    .setRolls(net.minecraft.world.level.storage.loot.providers.number.ConstantValue.exactly(1.0F))
                    .when(HAS_SHEARS.invert())
                    .add(applyExplosionCondition(leavesBlock, LootItem.lootTableItem(stickItem)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
                        .when(BonusLevelTableCondition.bonusLevelFlatChance(fortuneEnchantment, stickChances))));
        }
        
        /**
         * Create herb crop drops (mature at age 9 - HerbCropBlock uses 0-9)
         * When mature: drops 1 product + 1-2 seeds
         * When not mature: only drops 1 seed
         */
        protected LootTable.Builder createHerbCropDrops(Block cropBlock, Item productItem, Item seedItem) {
            // Use HerbCropBlock.AGE with max value 9 (not vanilla CropBlock.AGE which is 0-7)
            LootItemCondition.Builder matureCondition = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(cropBlock)
                .setProperties(StatePropertiesPredicate.Builder.properties()
                    .hasProperty(HerbCropBlock.AGE, HerbCropBlock.MAX_AGE));
            
            return applyExplosionDecay(cropBlock, LootTable.lootTable()
                // When mature: drop product item (not the flower block, but the processed product)
                .withPool(LootPool.lootPool()
                    .add(LootItem.lootTableItem(productItem))
                    .when(matureCondition))
                // Always drop seeds (1-2 when mature, 1 when not)
                .withPool(LootPool.lootPool()
                    .add(LootItem.lootTableItem(seedItem))
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))
                        .when(matureCondition)))
            );
        }
        
        @Override
        protected Iterable<Block> getKnownBlocks() {
            // Return all registered blocks
            return ModBlocks.BLOCKS.getEntries().stream()
                .map(holder -> (Block)holder.get())
                .toList();
        }
    }
}
