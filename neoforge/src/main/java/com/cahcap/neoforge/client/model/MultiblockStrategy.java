package com.cahcap.neoforge.client.model;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Strategy for multiblocks whose elements span several cells.
 * <p>
 * Cleanup → compute grid bounds → for each (mirror, cell) clip elements and bake per-cell
 * quads → hand the table to {@link BakedSplitModel} which dispatches on block state.
 */
public final class MultiblockStrategy implements BakeStrategy {

    public static final MultiblockStrategy INSTANCE = new MultiblockStrategy();
    private static final RandomSource RANDOM = RandomSource.create();

    private MultiblockStrategy() {}

    @Override
    public BakedModel bake(JsonObject rawModel,
                           IGeometryBakingContext ctx,
                           ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter,
                           ModelState modelState,
                           ItemOverrides overrides) {
        JsonArray cleaned = ElementProcessing.stripZeroRotation(rawModel.getAsJsonArray("elements"));
        JsonObject textures = rawModel.has("textures") && rawModel.get("textures").isJsonObject()
                ? rawModel.getAsJsonObject("textures").deepCopy() : new JsonObject();
        JsonElement textureSize = rawModel.get("texture_size");

        boolean useAO = !rawModel.has("ambientocclusion")
                || rawModel.get("ambientocclusion").getAsBoolean();

        double[] bounds = ElementProcessing.computeBounds(cleaned);
        int bxMin = (int) Math.floor(bounds[0] / 16.0);
        int bxMax = (int) Math.ceil(bounds[1] / 16.0);
        int byMin = (int) Math.floor(bounds[2] / 16.0);
        int byMax = (int) Math.ceil(bounds[3] / 16.0);
        int bzMin = (int) Math.floor(bounds[4] / 16.0);
        int bzMax = (int) Math.ceil(bounds[5] / 16.0);
        int xSize = bxMax - bxMin, ySize = byMax - byMin, zSize = bzMax - bzMin;
        int totalPositions = xSize * ySize * zSize;

        @SuppressWarnings("unchecked")
        List<BakedQuad>[][][] table = (List<BakedQuad>[][][]) new List[2][totalPositions][7];

        for (int mir = 0; mir < 2; mir++) {
            JsonArray base = (mir == 1) ? ElementProcessing.mirrorX(cleaned) : cleaned;
            for (int dy = byMin; dy < byMax; dy++) {
                for (int dx = bxMin; dx < bxMax; dx++) {
                    for (int dz = bzMin; dz < bzMax; dz++) {
                        JsonArray clipped = new JsonArray();
                        for (JsonElement el : base) {
                            JsonObject c = ElementProcessing.clipToCell(
                                    el.getAsJsonObject(), dx * 16.0, dy * 16.0, dz * 16.0);
                            if (c != null) clipped.add(c);
                        }
                        int pos = (dy - byMin) * xSize * zSize + (dx - bxMin) * zSize + (dz - bzMin);
                        if (clipped.isEmpty()) {
                            for (int s = 0; s < 7; s++) table[mir][pos][s] = ImmutableList.of();
                        } else {
                            BakedModel baked = ElementProcessing.bakeSubset(
                                    clipped, textures, textureSize, baker, spriteGetter, modelState, useAO);
                            fillSides(table[mir][pos], baked);
                        }
                    }
                }
            }
        }

        BakedModel itemBaked = ElementProcessing.bakeSubset(
                cleaned, textures, textureSize, baker, spriteGetter, modelState, useAO);
        @SuppressWarnings("unchecked")
        List<BakedQuad>[] itemSides = (List<BakedQuad>[]) new List[7];
        fillSides(itemSides, itemBaked);

        TextureAtlasSprite particle = resolveParticle(textures, spriteGetter);

        return new BakedSplitModel(table, itemSides, ctx.getTransforms(), particle,
                totalPositions, itemBaked.useAmbientOcclusion(), itemBaked.isGui3d(),
                itemBaked.usesBlockLight(), overrides);
    }

    private static void fillSides(List<BakedQuad>[] sides, BakedModel baked) {
        for (int s = 0; s < 6; s++) {
            Direction dir = Direction.values()[s];
            List<BakedQuad> q = baked.getQuads(null, dir, RANDOM, ModelData.EMPTY, null);
            sides[s] = q == null ? ImmutableList.of() : ImmutableList.copyOf(q);
        }
        List<BakedQuad> nullSide = baked.getQuads(null, null, RANDOM, ModelData.EMPTY, null);
        sides[6] = nullSide == null ? ImmutableList.of() : ImmutableList.copyOf(nullSide);
    }

    private static TextureAtlasSprite resolveParticle(JsonObject textures,
                                                      Function<Material, TextureAtlasSprite> spriteGetter) {
        String ref = textures.has("particle") ? textures.get("particle").getAsString() : null;
        if (ref == null || ref.startsWith("#")) {
            for (Map.Entry<String, JsonElement> e : textures.entrySet()) {
                String v = e.getValue().getAsString();
                if (!v.startsWith("#")) { ref = v; break; }
            }
        }
        if (ref == null) return spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS,
                ResourceLocation.withDefaultNamespace("block/stone")));
        return spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.parse(ref)));
    }
}
