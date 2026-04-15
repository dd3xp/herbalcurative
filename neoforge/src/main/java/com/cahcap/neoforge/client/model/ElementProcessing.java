package com.cahcap.neoforge.client.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.neoforged.neoforge.client.model.ExtendedBlockModelDeserializer;

import java.util.Map;
import java.util.function.Function;

/**
 * Shared static helpers for Blockbench element processing.
 * <p>
 * Three responsibilities, each surfaced as a small set of pure methods:
 * <ul>
 *     <li><b>cleanup</b> — {@link #stripZeroRotation}: remove {@code "rotation":{"angle":0,...}}
 *         so Minecraft's vanilla baker doesn't take the AO-breaking "rotated element" codepath.</li>
 *     <li><b>split</b> — {@link #clipToCell} + {@link #mirrorX}: axis-aligned element clipping
 *         with UV re-interpolation, and X-mirror for multiblock "mirrored" variants.</li>
 *     <li><b>bake</b> — {@link #bakeSubset}: wrap a JsonArray of elements in a synthetic
 *         {@link BlockModel} and run it through the vanilla baker.</li>
 * </ul>
 */
public final class ElementProcessing {

    private static final Gson GSON = new Gson();

    private ElementProcessing() {}

    // ==================== cleanup ====================

    /** Remove {@code rotation:{angle:0, ...}} entries (AO fix). Returns a new JsonArray. */
    public static JsonArray stripZeroRotation(JsonArray elements) {
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

    // ==================== bake ====================

    /**
     * Wrap elements in a minimal synthetic BlockModel JSON and bake it through vanilla.
     * Returns the baked model whose {@code getQuads(null, side, ...)} yields the raw quads.
     */
    public static BakedModel bakeSubset(JsonArray elements,
                                        JsonObject textures,
                                        JsonElement textureSize,
                                        ModelBaker baker,
                                        Function<Material, TextureAtlasSprite> spriteGetter,
                                        ModelState modelState,
                                        boolean useAmbientOcclusion) {
        JsonObject synth = new JsonObject();
        synth.addProperty("parent", "minecraft:block/block");
        if (textureSize != null) synth.add("texture_size", textureSize);
        synth.add("textures", textures);
        synth.add("elements", elements);
        BlockModel bm = ExtendedBlockModelDeserializer.INSTANCE.fromJson(GSON.toJson(synth), BlockModel.class);
        return bm.bake(baker, bm, spriteGetter, modelState, useAmbientOcclusion);
    }

    // ==================== split: clip ====================

    /**
     * Clip a single element to a block-cell AABB at {@code (baseX, baseY, baseZ)} (pixel coords),
     * re-interpolating UVs for the clipped face extents. Returns {@code null} if the element
     * doesn't overlap the cell.
     */
    public static JsonObject clipToCell(JsonObject original, double baseX, double baseY, double baseZ) {
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

    // ==================== split: mirror ====================

    /** Mirror every element across the x=0 plane (for multiblock 'mirrored' variants). */
    public static JsonArray mirrorX(JsonArray elements) {
        JsonArray out = new JsonArray();
        for (JsonElement el : elements) out.add(mirrorElementX(el.getAsJsonObject()));
        return out;
    }

    private static JsonObject mirrorElementX(JsonObject original) {
        JsonObject result = new JsonObject();
        if (original.has("name")) result.addProperty("name", original.get("name").getAsString());

        JsonArray from = original.getAsJsonArray("from");
        JsonArray to = original.getAsJsonArray("to");
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

    // ==================== bounds ====================

    /** Compute element bounds [minX,maxX,minY,maxY,minZ,maxZ] over all from/to pixel coords. */
    public static double[] computeBounds(JsonArray elements) {
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

    private static double lerp(double a, double b, double t) { return a + (b - a) * t; }
    private static double roundUV(double v) { return Math.round(v * 10000.0) / 10000.0; }
}
