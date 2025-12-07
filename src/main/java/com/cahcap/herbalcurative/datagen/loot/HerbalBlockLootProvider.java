package com.cahcap.herbalcurative.datagen.loot;

import com.cahcap.herbalcurative.registry.ModBlocks;
import com.cahcap.herbalcurative.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Block loot table provider
 * Generates loot tables for 18 blocks
 */
public class HerbalBlockLootProvider extends LootTableProvider {
    
    public HerbalBlockLootProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Set.of(), List.of(
            new SubProviderEntry(HerbalBlockLoot::new, LootContextParamSets.BLOCK)
        ), lookupProvider);
    }
    
    private static class HerbalBlockLoot extends BlockLootSubProvider {
        
        protected HerbalBlockLoot(HolderLookup.Provider provider) {
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
            
            // Forest Heartwood blocks
            this.dropSelf(ModBlocks.FOREST_HEARTWOOD_LOG.get());
            this.dropSelf(ModBlocks.FOREST_HEARTWOOD_PLANKS.get());
            this.dropSelf(ModBlocks.FOREST_HEARTWOOD_SAPLING.get());
            
            // Herb Cabinet
            this.dropSelf(ModBlocks.HERB_CABINET.get());
            
            // ==================== Crop blocks (drop seeds and crops when mature) ====================
            
            // Crystbud crop (mature at age 7)
            this.add(ModBlocks.CRYSTBUD_CROP.get(), block -> 
                createHerbCropDrops(block, ModBlocks.CRYSTBUD.get(), ModItems.CRYSTBUD_SEED.get()));
            
            // Dewpetal crop (mature at age 7)
            this.add(ModBlocks.DEWPETAL_CROP.get(), block -> 
                createHerbCropDrops(block, ModBlocks.DEWPETAL.get(), ModItems.DEWPETAL_SEED.get()));
            
            // Pyrisage crop (mature at age 7)
            this.add(ModBlocks.PYRISAGE_CROP.get(), block -> 
                createHerbCropDrops(block, ModBlocks.PYRISAGE.get(), ModItems.PYRISAGE_SEED.get()));
            
            // Rosynia crop (mature at age 7)
            this.add(ModBlocks.ROSYNIA_CROP.get(), block -> 
                createHerbCropDrops(block, ModBlocks.ROSYNIA.get(), ModItems.ROSYNIA_SEED.get()));
            
            // Verdscale Fern crop (mature at age 7)
            this.add(ModBlocks.VERDSCALE_FERN_CROP.get(), block -> 
                createHerbCropDrops(block, ModBlocks.VERDSCALE_FERN.get(), ModItems.VERDSCALE_FERN_SEED.get()));
            
            // Zephyr Lily crop (mature at age 7)
            this.add(ModBlocks.ZEPHYR_LILY_CROP.get(), block -> 
                createHerbCropDrops(block, ModBlocks.ZEPHYR_LILY.get(), ModItems.ZEPHYR_LILY_SEED.get()));
            
            // ==================== Special blocks ====================
            
            // Forest Heartwood Leaves (drops saplings and sticks)
            this.add(ModBlocks.FOREST_HEARTWOOD_LEAVES.get(), block ->
                createLeavesDrops(block, ModBlocks.FOREST_HEARTWOOD_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES)
                    .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1.0F, 2.0F))
                        .add(applyExplosionDecay(block, LootItem.lootTableItem(Items.STICK)
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))))
                        .when(HAS_SHEARS.invert()))
            );
            
            // Forest Berry Bush (similar to sweet berry bush)
            this.add(ModBlocks.FOREST_BERRY_BUSH.get(), block -> 
                applyExplosionDecay(block, LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                        // age 3: drops 2-3 berries
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                .hasProperty(SweetBerryBushBlock.AGE, 3)))
                        .add(LootItem.lootTableItem(ModItems.FOREST_BERRY.get()))
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 3.0F))))
                    .withPool(LootPool.lootPool()
                        // age 2: drops 1-2 berries
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                .hasProperty(SweetBerryBushBlock.AGE, 2)))
                        .add(LootItem.lootTableItem(ModItems.FOREST_BERRY.get()))
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
                )
            );
        }
        
        /**
         * Create herb crop drops (mature at age 7)
         * When mature: drops 1 crop + 1-2 seeds
         * When not mature: only drops 1 seed
         */
        protected LootTable.Builder createHerbCropDrops(Block cropBlock, Block cropItem, Item seedItem) {
            LootItemCondition.Builder matureCondition = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(cropBlock)
                .setProperties(StatePropertiesPredicate.Builder.properties()
                    .hasProperty(CropBlock.AGE, 7));
            
            return applyExplosionDecay(cropBlock, LootTable.lootTable()
                // When mature: drop crop item
                .withPool(LootPool.lootPool()
                    .add(LootItem.lootTableItem(cropItem))
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

