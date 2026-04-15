package com.cahcap.neoforge.client.model;

import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;

import java.util.function.Function;

/**
 * Strategy for baking a Blockbench-sourced custom-shape block model at runtime.
 * <p>
 * Implementations combine shared {@link ElementProcessing} helpers as needed:
 * <ul>
 *     <li>Single-cell blocks: cleanup + vanilla bake.</li>
 *     <li>Multiblock: cleanup + per-cell clip/mirror + blockstate-based dispatch.</li>
 * </ul>
 */
public interface BakeStrategy {

    BakedModel bake(JsonObject rawModel,
                    IGeometryBakingContext ctx,
                    ModelBaker baker,
                    Function<Material, TextureAtlasSprite> spriteGetter,
                    ModelState modelState,
                    ItemOverrides overrides);
}
