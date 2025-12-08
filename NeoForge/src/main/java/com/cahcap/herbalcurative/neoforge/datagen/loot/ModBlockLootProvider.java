package com.cahcap.herbalcurative.neoforge.datagen.loot;

import com.cahcap.herbalcurative.block.RedCherryBushBlock;
import com.cahcap.herbalcurative.block.HerbCropBlock;
import com.cahcap.herbalcurative.neoforge.registry.ModBlocks;
import com.cahcap.herbalcurative.neoforge.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
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
            
            // Red Cherry blocks
            this.dropSelf(ModBlocks.RED_CHERRY_LOG.get());
            this.dropSelf(ModBlocks.RED_CHERRY_PLANKS.get());
            this.dropSelf(ModBlocks.RED_CHERRY_SAPLING.get());
            
            // Herb Cabinet
            this.dropSelf(ModBlocks.HERB_CABINET.get());
            
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
            
            // Red Cherry Leaves (drops saplings and sticks)
            this.add(ModBlocks.RED_CHERRY_LEAVES.get(), block ->
                createLeavesDrops(block, ModBlocks.RED_CHERRY_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES)
                    .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1.0F, 2.0F))
                        .add(applyExplosionDecay(block, LootItem.lootTableItem(Items.STICK)
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))))
                        .when(HAS_SHEARS.invert()))
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
