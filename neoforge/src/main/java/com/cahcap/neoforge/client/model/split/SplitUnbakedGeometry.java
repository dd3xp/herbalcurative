package com.cahcap.neoforge.client.model.split;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
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
// ResourceLocation still needed for particle sprite parsing
import net.neoforged.neoforge.client.model.ExtendedBlockModelDeserializer;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Runtime equivalent of the old {@code ModelSplitProcessor} + {@code BlockModelProcessor}:
 * <ol>
 *     <li>Strip {@code rotation:{angle:0}} from each element.</li>
 *     <li>Compute the model's block-cell bounds from element AABBs.</li>
 *     <li>For each (mirrored, position) pair, clip elements to that cell's local coords
 *         with UV re-interpolation, then bake those clipped elements into {@link BakedQuad}s.</li>
 *     <li>Also bake the full unclipped model for item / no-state rendering.</li>
 * </ol>
 * The resulting {@link BakedSplitModel} dispatches on block state {@code position}/{@code mirrored}
 * at render time.
 */
public final class SplitUnbakedGeometry implements IUnbakedGeometry<SplitUnbakedGeometry> {

    private static final Gson GSON = new Gson();
    private static final RandomSource RANDOM = RandomSource.create();

    private final JsonObject rawModel;

    public SplitUnbakedGeometry(JsonObject rawModel) {
        this.rawModel = rawModel;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext ctx,
                           ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter,
                           ModelState modelState,
                           ItemOverrides overrides) {

        JsonArray originalElements = cleanElements(rawModel.getAsJsonArray("elements"));
        JsonObject textures = rawModel.has("textures") && rawModel.get("textures").isJsonObject()
                ? rawModel.getAsJsonObject("textures").deepCopy() : new JsonObject();
        JsonElement textureSize = rawModel.get("texture_size");

        double[] bounds = computeElementBounds(originalElements);
        int bxMin = (int) Math.floor(bounds[0] / 16.0);
        int bxMax = (int) Math.ceil(bounds[1] / 16.0);
        int byMin = (int) Math.floor(bounds[2] / 16.0);
        int byMax = (int) Math.ceil(bounds[3] / 16.0);
        int bzMin = (int) Math.floor(bounds[4] / 16.0);
        int bzMax = (int) Math.ceil(bounds[5] / 16.0);
        int xSize = bxMax - bxMin, ySize = byMax - byMin, zSize = bzMax - bzMin;
        int totalPositions = xSize * ySize * zSize;

        // [mirror][position][side_ordinal_or_6_for_null] -> quads
        @SuppressWarnings("unchecked")
        List<BakedQuad>[][][] table = (List<BakedQuad>[][][]) new List[2][totalPositions][7];

        for (int mir = 0; mir < 2; mir++) {
            JsonArray base = (mir == 1) ? mirrorAllX(originalElements) : originalElements;
            for (int dy = byMin; dy < byMax; dy++) {
                for (int dx = bxMin; dx < bxMax; dx++) {
                    for (int dz = bzMin; dz < bzMax; dz++) {
                        JsonArray clipped = new JsonArray();
                        for (JsonElement el : base) {
                            JsonObject c = clipElement(el.getAsJsonObject(), dx * 16.0, dy * 16.0, dz * 16.0);
                            if (c != null) clipped.add(c);
                        }
                        int pos = (dy - byMin) * xSize * zSize + (dx - bxMin) * zSize + (dz - bzMin);
                        if (clipped.isEmpty()) {
                            for (int s = 0; s < 7; s++) table[mir][pos][s] = ImmutableList.of();
                        } else {
                            BakedModel baked = bakeSubset(clipped, textures, textureSize, baker, spriteGetter, modelState);
                            fillSides(table[mir][pos], baked);
                        }
                    }
                }
            }
        }

        BakedModel itemBaked = bakeSubset(originalElements, textures, textureSize, baker, spriteGetter, modelState);
        List<BakedQuad>[] itemSides = newSideArray();
        fillSides(itemSides, itemBaked);

        TextureAtlasSprite particle = resolveParticle(textures, spriteGetter);

        return new BakedSplitModel(table, itemSides, ctx.getTransforms(), particle,
                totalPositions, itemBaked.useAmbientOcclusion(), itemBaked.isGui3d(),
                itemBaked.usesBlockLight(), overrides);
    }

    // ==================== Baking helpers ====================

    private static BakedModel bakeSubset(JsonArray elements,
                                         JsonObject textures,
                                         JsonElement textureSize,
                                         ModelBaker baker,
                                         Function<Material, TextureAtlasSprite> spriteGetter,
                                         ModelState modelState) {
        JsonObject synth = new JsonObject();
        synth.addProperty("parent", "minecraft:block/block");
        if (textureSize != null) synth.add("texture_size", textureSize);
        synth.add("textures", textures);
        synth.add("elements", elements);
        BlockModel bm = ExtendedBlockModelDeserializer.INSTANCE.fromJson(GSON.toJson(synth), BlockModel.class);
        return bm.bake(baker, bm, spriteGetter, modelState, true);
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

    @SuppressWarnings("unchecked")
    private static List<BakedQuad>[] newSideArray() {
        return (List<BakedQuad>[]) new List[7];
    }

    private static TextureAtlasSprite resolveParticle(JsonObject textures,
                                                      Function<Material, TextureAtlasSprite> spriteGetter) {
        String ref = textures.has("particle") ? textures.get("particle").getAsString() : null;
        if (ref == null || ref.startsWith("#")) {
            // Fall back to the first concrete texture
            for (Map.Entry<String, JsonElement> e : textures.entrySet()) {
                String v = e.getValue().getAsString();
                if (!v.startsWith("#")) { ref = v; break; }
            }
        }
        if (ref == null) return spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("block/stone")));
        return spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.parse(ref)));
    }

    // ==================== Element cleaning (strip angle=0) ====================

    private static JsonArray cleanElements(JsonArray elements) {
        JsonArray out = new JsonArray();
        for (JsonElement el : elements) {
            JsonObject copy = el.getAsJsonObject().deepCopy();
            if (copy.has("rotation")) {
                JsonObject rot = copy.getAsJsonObject("rotation");
                if (rot.has("angle") && rot.get("angle").getAsDouble() == 0.0) {
                    copy.remove("rotation");
                }
            }
            out.add(copy);
        }
        return out;
    }

    // ==================== Bounds ====================

    private static double[] computeElementBounds(JsonArray elements) {
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (JsonElement el : elements) {
            JsonObject elem = el.getAsJsonObject();
            JsonArray from = elem.getAsJsonArray("from");
            JsonArray to = elem.getAsJsonArray("to");
            for (int i = 0; i < 3; i++) {
                double f = from.get(i).getAsDouble(), t = to.get(i).getAsDouble();
                double lo = Math.min(f, t), hi = Math.max(f, t);
                switch (i) {
                    case 0 -> { minX = Math.min(minX, lo); maxX = Math.max(maxX, hi); }
                    case 1 -> { minY = Math.min(minY, lo); maxY = Math.max(maxY, hi); }
                    case 2 -> { minZ = Math.min(minZ, lo); maxZ = Math.max(maxZ, hi); }
                }
            }
        }
        return new double[]{minX, maxX, minY, maxY, minZ, maxZ};
    }

    // ==================== Clip (ported from ModelSplitProcessor) ====================

    private static JsonObject clipElement(JsonObject original, double baseX, double baseY, double baseZ) {
        JsonArray from = original.getAsJsonArray("from");
        JsonArray to = original.getAsJsonArray("to");
        double fx = from.get(0).getAsDouble(), fy = from.get(1).getAsDouble(), fz = from.get(2).getAsDouble();
        double tx = to.get(0).getAsDouble(), ty = to.get(1).getAsDouble(), tz = to.get(2).getAsDouble();

        double oxMin = Math.min(fx, tx), oxMax = Math.max(fx, tx);
        double oyMin = Math.min(fy, ty), oyMax = Math.max(fy, ty);
        double ozMin = Math.min(fz, tz), ozMax = Math.max(fz, tz);

        double cxMin = Math.max(oxMin, baseX), cxMax = Math.min(oxMax, baseX + 16);
        double cyMin = Math.max(oyMin, baseY), cyMax = Math.min(oyMax, baseY + 16);
        double czMin = Math.max(ozMin, baseZ), czMax = Math.min(ozMax, baseZ + 16);
        if (cxMin >= cxMax || cyMin >= cyMax || czMin >= czMax) return null;

        double xRange = oxMax - oxMin, yRange = oyMax - oyMin, zRange = ozMax - ozMin;
        double xFracMin = xRange > 0 ? (cxMin - oxMin) / xRange : 0;
        double xFracMax = xRange > 0 ? (cxMax - oxMin) / xRange : 1;
        double yFracMin = yRange > 0 ? (cyMin - oyMin) / yRange : 0;
        double yFracMax = yRange > 0 ? (cyMax - oyMin) / yRange : 1;
        double zFracMin = zRange > 0 ? (czMin - ozMin) / zRange : 0;
        double zFracMax = zRange > 0 ? (czMax - ozMin) / zRange : 1;

        JsonObject result = new JsonObject();
        if (original.has("name")) result.addProperty("name", original.get("name").getAsString());

        JsonArray newFrom = new JsonArray();
        newFrom.add(cxMin - baseX); newFrom.add(cyMin - baseY); newFrom.add(czMin - baseZ);
        result.add("from", newFrom);
        JsonArray newTo = new JsonArray();
        newTo.add(cxMax - baseX); newTo.add(cyMax - baseY); newTo.add(czMax - baseZ);
        result.add("to", newTo);

        if (original.has("rotation")) {
            JsonObject rot = original.getAsJsonObject("rotation");
            double angle = rot.has("angle") ? rot.get("angle").getAsDouble() : 0;
            if (angle != 0) {
                JsonObject rotCopy = rot.deepCopy();
                JsonArray origin = rotCopy.getAsJsonArray("origin");
                JsonArray newOrigin = new JsonArray();
                newOrigin.add(origin.get(0).getAsDouble() - baseX);
                newOrigin.add(origin.get(1).getAsDouble() - baseY);
                newOrigin.add(origin.get(2).getAsDouble() - baseZ);
                rotCopy.add("origin", newOrigin);
                result.add("rotation", rotCopy);
            }
        }

        if (original.has("faces")) {
            JsonObject faces = original.getAsJsonObject("faces");
            JsonObject newFaces = new JsonObject();
            for (Map.Entry<String, JsonElement> faceEntry : faces.entrySet()) {
                String faceName = faceEntry.getKey();
                JsonObject face = faceEntry.getValue().getAsJsonObject();
                JsonObject newFace = face.deepCopy();
                if (face.has("uv")) {
                    JsonArray uv = face.getAsJsonArray("uv");
                    double u1 = uv.get(0).getAsDouble(), v1 = uv.get(1).getAsDouble();
                    double u2 = uv.get(2).getAsDouble(), v2 = uv.get(3).getAsDouble();
                    double[] newUV = adjustUV(faceName, u1, v1, u2, v2,
                            xFracMin, xFracMax, yFracMin, yFracMax, zFracMin, zFracMax);
                    JsonArray newUVArray = new JsonArray();
                    newUVArray.add(roundUV(newUV[0]));
                    newUVArray.add(roundUV(newUV[1]));
                    newUVArray.add(roundUV(newUV[2]));
                    newUVArray.add(roundUV(newUV[3]));
                    newFace.add("uv", newUVArray);
                }
                newFaces.add(faceName, newFace);
            }
            result.add("faces", newFaces);
        }
        return result;
    }

    private static double[] adjustUV(String face, double u1, double v1, double u2, double v2,
                                     double xFracMin, double xFracMax,
                                     double yFracMin, double yFracMax,
                                     double zFracMin, double zFracMax) {
        double newU1 = u1, newV1 = v1, newU2 = u2, newV2 = v2;
        switch (face) {
            case "north" -> {
                newU1 = lerp(u1, u2, 1 - xFracMax); newU2 = lerp(u1, u2, 1 - xFracMin);
                newV1 = lerp(v1, v2, 1 - yFracMax); newV2 = lerp(v1, v2, 1 - yFracMin);
            }
            case "south" -> {
                newU1 = lerp(u1, u2, xFracMin); newU2 = lerp(u1, u2, xFracMax);
                newV1 = lerp(v1, v2, 1 - yFracMax); newV2 = lerp(v1, v2, 1 - yFracMin);
            }
            case "east" -> {
                newU1 = lerp(u1, u2, 1 - zFracMax); newU2 = lerp(u1, u2, 1 - zFracMin);
                newV1 = lerp(v1, v2, 1 - yFracMax); newV2 = lerp(v1, v2, 1 - yFracMin);
            }
            case "west" -> {
                newU1 = lerp(u1, u2, zFracMin); newU2 = lerp(u1, u2, zFracMax);
                newV1 = lerp(v1, v2, 1 - yFracMax); newV2 = lerp(v1, v2, 1 - yFracMin);
            }
            case "up" -> {
                newU1 = lerp(u1, u2, xFracMin); newU2 = lerp(u1, u2, xFracMax);
                newV1 = lerp(v1, v2, zFracMin); newV2 = lerp(v1, v2, zFracMax);
            }
            case "down" -> {
                newU1 = lerp(u1, u2, xFracMin); newU2 = lerp(u1, u2, xFracMax);
                newV1 = lerp(v1, v2, 1 - zFracMax); newV2 = lerp(v1, v2, 1 - zFracMin);
            }
        }
        return new double[]{newU1, newV1, newU2, newV2};
    }

    // ==================== Mirror in X (ported from ModelSplitProcessor) ====================

    private static JsonArray mirrorAllX(JsonArray elements) {
        JsonArray out = new JsonArray();
        for (JsonElement el : elements) out.add(mirrorElementX(el.getAsJsonObject()));
        return out;
    }

    private static JsonObject mirrorElementX(JsonObject original) {
        JsonObject result = new JsonObject();
        if (original.has("name")) result.addProperty("name", original.get("name").getAsString());

        JsonArray from = original.getAsJsonArray("from");
        JsonArray to = original.getAsJsonArray("to");
        // Mirror around x=0 (so post-mirror from is 0 - to, post-mirror to is 0 - from).
        // Note: ModelSplitProcessor mirrors around x=16 (single-cell), but here we mirror the
        // entire oversized model around x=0 so the clip loop below re-buckets into cells.
        JsonArray newFrom = new JsonArray();
        newFrom.add(-to.get(0).getAsDouble());
        newFrom.add(from.get(1).getAsDouble());
        newFrom.add(from.get(2).getAsDouble());
        JsonArray newTo = new JsonArray();
        newTo.add(-from.get(0).getAsDouble());
        newTo.add(to.get(1).getAsDouble());
        newTo.add(to.get(2).getAsDouble());
        result.add("from", newFrom);
        result.add("to", newTo);

        if (original.has("rotation")) {
            JsonObject rot = original.getAsJsonObject("rotation");
            double angle = rot.has("angle") ? rot.get("angle").getAsDouble() : 0;
            if (angle != 0) {
                JsonObject rotCopy = rot.deepCopy();
                JsonArray origin = rotCopy.getAsJsonArray("origin");
                JsonArray newOrigin = new JsonArray();
                newOrigin.add(-origin.get(0).getAsDouble());
                newOrigin.add(origin.get(1).getAsDouble());
                newOrigin.add(origin.get(2).getAsDouble());
                rotCopy.add("origin", newOrigin);
                if ("y".equals(rotCopy.get("axis").getAsString())) {
                    rotCopy.addProperty("angle", -rotCopy.get("angle").getAsDouble());
                }
                result.add("rotation", rotCopy);
            }
        }

        if (original.has("faces")) {
            JsonObject faces = original.getAsJsonObject("faces");
            JsonObject newFaces = new JsonObject();
            for (Map.Entry<String, JsonElement> faceEntry : faces.entrySet()) {
                String faceName = faceEntry.getKey();
                JsonObject face = faceEntry.getValue().getAsJsonObject().deepCopy();
                String targetName = switch (faceName) {
                    case "east" -> "west";
                    case "west" -> "east";
                    default -> faceName;
                };
                if (face.has("uv") && (faceName.equals("north") || faceName.equals("south")
                        || faceName.equals("up") || faceName.equals("down"))) {
                    JsonArray uv = face.getAsJsonArray("uv");
                    double u1 = uv.get(0).getAsDouble(), v1 = uv.get(1).getAsDouble();
                    double u2 = uv.get(2).getAsDouble(), v2 = uv.get(3).getAsDouble();
                    JsonArray newUv = new JsonArray();
                    newUv.add(u2); newUv.add(v1); newUv.add(u1); newUv.add(v2);
                    face.add("uv", newUv);
                }
                newFaces.add(targetName, face);
            }
            result.add("faces", newFaces);
        }
        return result;
    }

    // ==================== Utility ====================

    private static double lerp(double a, double b, double t) { return a + (b - a) * t; }
    private static double roundUV(double v) { return Math.round(v * 10000.0) / 10000.0; }
}
