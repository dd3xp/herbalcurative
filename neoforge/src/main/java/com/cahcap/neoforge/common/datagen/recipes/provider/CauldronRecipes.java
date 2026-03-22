package com.cahcap.neoforge.common.datagen.recipes.provider;

import com.cahcap.neoforge.common.datagen.recipes.builder.*;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

/**
 * Cauldron recipes: Brewing and Infusing.
 */
public class CauldronRecipes {

    public void build(RecipeOutput output) {
        // ==================== Brewing Recipes ====================
        CauldronBrewingRecipeBuilder.builder()
                .material(Items.ENCHANTED_GOLDEN_APPLE, 8)
                .effect("minecraft:regeneration")
                .color(0xCD5CAB)
                .defaultDuration(30).defaultAmplifier(0)
                .maxDuration(90).maxAmplifier(1)
                .durationPerHerb(5).herbsPerLevel(12)
                .build(output, "regeneration_potion");

        CauldronBrewingRecipeBuilder.builder()
                .material(Items.GLISTERING_MELON_SLICE, 8)
                .effect("minecraft:instant_health")
                .color(0xF82423)
                .defaultDuration(0).defaultAmplifier(0)
                .maxDuration(0).maxAmplifier(1)
                .durationPerHerb(0).herbsPerLevel(12)
                .build(output, "instant_health_potion");

        CauldronBrewingRecipeBuilder.builder()
                .material(Items.FERMENTED_SPIDER_EYE, 8)
                .effect("minecraft:instant_damage")
                .color(0x430A09)
                .defaultDuration(0).defaultAmplifier(0)
                .maxDuration(0).maxAmplifier(1)
                .durationPerHerb(0).herbsPerLevel(12)
                .build(output, "instant_damage_potion");

        CauldronBrewingRecipeBuilder.builder()
                .material(Items.BLAZE_ROD, 8)
                .effect("minecraft:strength")
                .color(0x932423)
                .defaultDuration(120).defaultAmplifier(0)
                .maxDuration(480).maxAmplifier(1)
                .durationPerHerb(30).herbsPerLevel(12)
                .build(output, "strength_potion");

        CauldronBrewingRecipeBuilder.builder()
                .material(Items.SUGAR, 8)
                .material(Items.RABBIT_FOOT, 8)
                .effects("minecraft:speed", "minecraft:jump_boost")
                .color(0x4FD789)
                .defaultDuration(120).defaultAmplifier(0)
                .maxDuration(480).maxAmplifier(1)
                .durationPerHerb(30).herbsPerLevel(12)
                .build(output, "travel_potion");

        CauldronBrewingRecipeBuilder.builder()
                .material(Items.COBWEB, 8)
                .material(Items.BREEZE_ROD, 8)
                .material(Items.SLIME_BALL, 8)
                .material(Items.STONE, 8)
                .effects("minecraft:weaving", "minecraft:wind_charged", "minecraft:oozing", "minecraft:infested")
                .color(0x1A0A20)
                .defaultDuration(120).defaultAmplifier(0)
                .maxDuration(480).maxAmplifier(0)
                .durationPerHerb(30).herbsPerLevel(12)
                .build(output, "chaos_potion");

        // ==================== Infusing Recipes ====================
        CauldronInfusingRecipeBuilder.builder()
                .requireFluid(Fluids.WATER)
                .input(Items.SPONGE, 1)
                .output(Items.WET_SPONGE)
                .build(output, "wet_sponge");

        CauldronInfusingRecipeBuilder.builder()
                .requirePotion("minecraft:regeneration", 90, 2)
                .input(Items.APPLE, 1)
                .output(Items.ENCHANTED_GOLDEN_APPLE)
                .fluidCost(1)
                .build(output, "enchanted_golden_apple");

        // ==================== Flowweave Ring ====================
        CauldronInfusingRecipeBuilder.builder()
                .flowweaveRingBinding()
                .requirePotion("")
                .build(output, "flowweave_ring_binding");

        CauldronInfusingRecipeBuilder.builder()
                .flowweaveRingUnbinding()
                .requireFluid("minecraft:water")
                .build(output, "flowweave_ring_unbinding");
    }
}
