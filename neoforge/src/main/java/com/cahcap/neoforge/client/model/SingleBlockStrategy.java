package com.cahcap.neoforge.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
 * Cleanup-only strategy for custom-shape blocks whose geometry fits inside a single cell.
 * <p>
 * Strips {@code rotation:{angle:0}} (the AO-breaking Blockbench export artifact) and hands
 * the cleaned elements to the vanilla baker. No clipping, no state dispatch.
 */
public final class SingleBlockStrategy implements BakeStrategy {

    public static final SingleBlockStrategy INSTANCE = new SingleBlockStrategy();

    private SingleBlockStrategy() {}

    @Override
    public BakedModel bake(JsonObject rawModel,
                           IGeometryBakingContext ctx,
                           ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter,
                           ModelState modelState,
                           ItemOverrides overrides) {
        JsonArray elements = ElementProcessing.stripZeroRotation(rawModel.getAsJsonArray("elements"));
        JsonObject textures = rawModel.has("textures") && rawModel.get("textures").isJsonObject()
                ? rawModel.getAsJsonObject("textures").deepCopy() : new JsonObject();
        JsonElement textureSize = rawModel.get("texture_size");

        boolean useAO = !rawModel.has("ambientocclusion")
                || rawModel.get("ambientocclusion").getAsBoolean();

        return ElementProcessing.bakeSubset(elements, textures, textureSize,
                baker, spriteGetter, modelState, useAO);
    }
}
