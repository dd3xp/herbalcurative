package com.cahcap.neoforge.common.datagen.models;

import com.cahcap.HerbalCurativeCommon;
import com.google.gson.*;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Data generator that splits a multiblock Blockbench model into per-position
 * model JSONs suitable for chunk rendering. Each position's model contains only
 * the elements (or portions of elements) that fall within that block's 16×16×16 region,
 * with UV coordinates proportionally adjusted.
 * <p>
 * Output: models/block/{name}_part_{dx}_{dy}_{dz}.json for each occupied position
 */
public class ModelSplitProvider implements DataProvider {

    private final PackOutput output;
    private final Path commonResourcesDir;

    public ModelSplitProvider(PackOutput output, Path commonResourcesDir) {
        this.output = output;
        this.commonResourcesDir = commonResourcesDir;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        futures.add(processModel(cache, "cauldron"));
        futures.add(processModel(cache, "herb_cabinet"));
        futures.add(processModel(cache, "herb_vault"));
        futures.add(processModel(cache, "kiln"));
        futures.add(processModel(cache, "workbench"));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> processModel(CachedOutput cache, String modelName) {
        Path modelPath = commonResourcesDir
                .resolve("assets")
                .resolve(HerbalCurativeCommon.MOD_ID)
                .resolve("models/block")
                .resolve(modelName + ".json");

        if (!Files.exists(modelPath)) {
            HerbalCurativeCommon.LOGGER.warn("Model file not found for model splitting: {}", modelPath);
            return CompletableFuture.completedFuture(null);
        }

        try {
            JsonObject model = readJson(modelPath);
            Map<String, JsonObject> splitModels = splitModel(model, modelName);
            Map<String, JsonObject> mirroredModels = generateMirroredModels(splitModels, modelName);

            List<CompletableFuture<?>> futures = new ArrayList<>();
            Path modelsDir = output.getOutputFolder()
                    .resolve("assets")
                    .resolve(HerbalCurativeCommon.MOD_ID)
                    .resolve("models/block");

            for (Map.Entry<String, JsonObject> entry : splitModels.entrySet()) {
                futures.add(DataProvider.saveStable(cache, entry.getValue(), modelsDir.resolve(entry.getKey() + ".json")));
            }
            for (Map.Entry<String, JsonObject> entry : mirroredModels.entrySet()) {
                futures.add(DataProvider.saveStable(cache, entry.getValue(), modelsDir.resolve(entry.getKey() + ".json")));
            }
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        } catch (IOException e) {
            throw new RuntimeException("Failed to split model: " + modelName, e);
        }
    }

    /**
     * Split a model into per-position fragments.
     *
     * @return map of model name → model JSON for each occupied position
     */
    private Map<String, JsonObject> splitModel(JsonObject model, String baseName) {
        JsonArray elements = model.getAsJsonArray("elements");
        if (elements == null || elements.isEmpty()) {
            throw new IllegalArgumentException("Model has no elements");
        }

        // Compute bounding box
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

        int bxMin = (int) Math.floor(minX / 16.0);
        int bxMax = (int) Math.ceil(maxX / 16.0);
        int byMin = (int) Math.floor(minY / 16.0);
        int byMax = (int) Math.ceil(maxY / 16.0);
        int bzMin = (int) Math.floor(minZ / 16.0);
        int bzMax = (int) Math.ceil(maxZ / 16.0);

        // Extract shared properties
        JsonElement textures = model.get("textures");
        JsonElement textureSize = model.get("texture_size");

        Map<String, JsonObject> result = new TreeMap<>();

        for (int dy = byMin; dy < byMax; dy++) {
            for (int dx = bxMin; dx < bxMax; dx++) {
                for (int dz = bzMin; dz < bzMax; dz++) {
                    double baseX = dx * 16.0, baseY = dy * 16.0, baseZ = dz * 16.0;
                    JsonArray clippedElements = new JsonArray();

                    for (JsonElement el : elements) {
                        JsonObject clipped = clipElement(el.getAsJsonObject(), baseX, baseY, baseZ);
                        if (clipped != null) {
                            clippedElements.add(clipped);
                        }
                    }

                    if (!clippedElements.isEmpty()) {
                        JsonObject fragment = new JsonObject();
                        if (textureSize != null) fragment.add("texture_size", textureSize.deepCopy());
                        if (textures != null) fragment.add("textures", textures.deepCopy());
                        fragment.add("elements", clippedElements);

                        String key = baseName + "_part_" + dx + "_" + dy + "_" + dz;
                        result.put(key, fragment);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Generate X-mirrored variants of split models.
     * For position (dx, dy, dz), the mirrored model takes content from (-dx, dy, dz)
     * and flips all X coordinates within the block.
     */
    private Map<String, JsonObject> generateMirroredModels(Map<String, JsonObject> normalModels, String baseName) {
        Map<String, JsonObject> mirrored = new TreeMap<>();

        for (Map.Entry<String, JsonObject> entry : normalModels.entrySet()) {
            String normalKey = entry.getKey(); // e.g. "cauldron_part_-1_0_-1"
            String suffix = normalKey.substring(baseName.length() + "_part_".length()); // "-1_0_-1"
            String[] parts = suffix.split("_");
            int dx = Integer.parseInt(parts[0]);
            int dy = Integer.parseInt(parts[1]);
            int dz = Integer.parseInt(parts[2]);

            // Mirrored model at (-dx, dy, dz) takes content from this (dx, dy, dz) fragment
            String mirroredKey = baseName + "_part_" + (-dx) + "_" + dy + "_" + dz + "_mirrored";

            JsonObject source = entry.getValue();
            JsonObject mirroredModel = new JsonObject();

            // Copy texture info
            if (source.has("texture_size")) mirroredModel.add("texture_size", source.get("texture_size").deepCopy());
            if (source.has("textures")) mirroredModel.add("textures", source.get("textures").deepCopy());

            // Mirror each element on X axis
            JsonArray mirroredElements = new JsonArray();
            for (JsonElement el : source.getAsJsonArray("elements")) {
                mirroredElements.add(mirrorElementX(el.getAsJsonObject()));
            }
            mirroredModel.add("elements", mirroredElements);

            mirrored.put(mirroredKey, mirroredModel);
        }

        return mirrored;
    }

    /**
     * Mirror a single element on the X axis within a 16-wide block.
     * - Flip X coordinates: new_x = 16 - old_x
     * - Swap east↔west face entries
     * - Reverse U for faces where U maps to X (north, south, up, down)
     * - Negate Y-axis rotation angle
     */
    private JsonObject mirrorElementX(JsonObject original) {
        JsonObject result = new JsonObject();

        if (original.has("name")) result.addProperty("name", original.get("name").getAsString());

        // Flip X coordinates
        JsonArray from = original.getAsJsonArray("from");
        JsonArray to = original.getAsJsonArray("to");
        JsonArray newFrom = new JsonArray();
        newFrom.add(16.0 - to.get(0).getAsDouble());
        newFrom.add(from.get(1).getAsDouble());
        newFrom.add(from.get(2).getAsDouble());
        JsonArray newTo = new JsonArray();
        newTo.add(16.0 - from.get(0).getAsDouble());
        newTo.add(to.get(1).getAsDouble());
        newTo.add(to.get(2).getAsDouble());
        result.add("from", newFrom);
        result.add("to", newTo);

        // Mirror rotation origin on X, negate Y-axis rotation angle
        if (original.has("rotation")) {
            JsonObject rot = original.getAsJsonObject("rotation").deepCopy();
            JsonArray origin = rot.getAsJsonArray("origin");
            JsonArray newOrigin = new JsonArray();
            newOrigin.add(16.0 - origin.get(0).getAsDouble());
            newOrigin.add(origin.get(1).getAsDouble());
            newOrigin.add(origin.get(2).getAsDouble());
            rot.add("origin", newOrigin);
            if ("y".equals(rot.get("axis").getAsString())) {
                rot.addProperty("angle", -rot.get("angle").getAsDouble());
            }
            result.add("rotation", rot);
        }

        // Mirror faces: swap east↔west, reverse U for X-dependent faces
        if (original.has("faces")) {
            JsonObject faces = original.getAsJsonObject("faces");
            JsonObject newFaces = new JsonObject();

            for (Map.Entry<String, JsonElement> faceEntry : faces.entrySet()) {
                String faceName = faceEntry.getKey();
                JsonObject face = faceEntry.getValue().getAsJsonObject().deepCopy();

                // Swap east↔west
                String targetName = switch (faceName) {
                    case "east" -> "west";
                    case "west" -> "east";
                    default -> faceName;
                };

                // Reverse U (swap u1↔u2) for faces where U maps to X axis
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

    /**
     * Clip an element to a 16×16×16 block region and adjust coordinates + UVs.
     *
     * @return clipped element JSON, or null if no overlap
     */
    private JsonObject clipElement(JsonObject original, double baseX, double baseY, double baseZ) {
        JsonArray from = original.getAsJsonArray("from");
        JsonArray to = original.getAsJsonArray("to");

        double fx = from.get(0).getAsDouble(), fy = from.get(1).getAsDouble(), fz = from.get(2).getAsDouble();
        double tx = to.get(0).getAsDouble(), ty = to.get(1).getAsDouble(), tz = to.get(2).getAsDouble();

        // Ensure min/max ordering
        double oxMin = Math.min(fx, tx), oxMax = Math.max(fx, tx);
        double oyMin = Math.min(fy, ty), oyMax = Math.max(fy, ty);
        double ozMin = Math.min(fz, tz), ozMax = Math.max(fz, tz);

        // Clip to block region
        double cxMin = Math.max(oxMin, baseX), cxMax = Math.min(oxMax, baseX + 16);
        double cyMin = Math.max(oyMin, baseY), cyMax = Math.min(oyMax, baseY + 16);
        double czMin = Math.max(ozMin, baseZ), czMax = Math.min(ozMax, baseZ + 16);

        if (cxMin >= cxMax || cyMin >= cyMax || czMin >= czMax) return null;

        // Clipping fractions for each axis
        double xRange = oxMax - oxMin, yRange = oyMax - oyMin, zRange = ozMax - ozMin;
        double xFracMin = xRange > 0 ? (cxMin - oxMin) / xRange : 0;
        double xFracMax = xRange > 0 ? (cxMax - oxMin) / xRange : 1;
        double yFracMin = yRange > 0 ? (cyMin - oyMin) / yRange : 0;
        double yFracMax = yRange > 0 ? (cyMax - oyMin) / yRange : 1;
        double zFracMin = zRange > 0 ? (czMin - ozMin) / zRange : 0;
        double zFracMax = zRange > 0 ? (czMax - ozMin) / zRange : 1;

        // Build clipped element
        JsonObject result = new JsonObject();

        // Name (optional)
        if (original.has("name")) {
            result.addProperty("name", original.get("name").getAsString());
        }

        // Local coordinates (0-16)
        JsonArray newFrom = new JsonArray();
        newFrom.add(cxMin - baseX);
        newFrom.add(cyMin - baseY);
        newFrom.add(czMin - baseZ);
        result.add("from", newFrom);

        JsonArray newTo = new JsonArray();
        newTo.add(cxMax - baseX);
        newTo.add(cyMax - baseY);
        newTo.add(czMax - baseZ);
        result.add("to", newTo);

        // Rotation — adjust origin to local space
        if (original.has("rotation")) {
            JsonObject rot = original.getAsJsonObject("rotation").deepCopy();
            JsonArray origin = rot.getAsJsonArray("origin");
            JsonArray newOrigin = new JsonArray();
            newOrigin.add(origin.get(0).getAsDouble() - baseX);
            newOrigin.add(origin.get(1).getAsDouble() - baseY);
            newOrigin.add(origin.get(2).getAsDouble() - baseZ);
            rot.add("origin", newOrigin);
            result.add("rotation", rot);
        }

        // Faces — adjust UVs
        if (original.has("faces")) {
            JsonObject faces = original.getAsJsonObject("faces");
            JsonObject newFaces = new JsonObject();

            for (Map.Entry<String, JsonElement> faceEntry : faces.entrySet()) {
                String faceName = faceEntry.getKey();
                JsonObject face = faceEntry.getValue().getAsJsonObject();

                // Skip faces that are at the clipped boundary (internal faces)
                // Actually, keep all faces for correctness — internal culling is handled by the engine

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

    /**
     * Adjust UV coordinates for a face based on how much of the element was clipped.
     * <p>
     * Minecraft face-to-axis UV mapping (derived from default UV formulas):
     * <pre>
     * NORTH: u1↔max_x, u2↔min_x  (U inverted on X),  v1↔max_y, v2↔min_y  (V inverted on Y)
     * SOUTH: u1↔min_x, u2↔max_x  (U normal on X),    v1↔max_y, v2↔min_y  (V inverted on Y)
     * EAST:  u1↔max_z, u2↔min_z  (U inverted on Z),  v1↔max_y, v2↔min_y  (V inverted on Y)
     * WEST:  u1↔min_z, u2↔max_z  (U normal on Z),    v1↔max_y, v2↔min_y  (V inverted on Y)
     * UP:    u1↔min_x, u2↔max_x  (U normal on X),    v1↔min_z, v2↔max_z  (V normal on Z)
     * DOWN:  u1↔min_x, u2↔max_x  (U normal on X),    v1↔max_z, v2↔min_z  (V inverted on Z)
     * </pre>
     */
    private double[] adjustUV(String face, double u1, double v1, double u2, double v2,
                               double xFracMin, double xFracMax,
                               double yFracMin, double yFracMax,
                               double zFracMin, double zFracMax) {
        double newU1 = u1, newV1 = v1, newU2 = u2, newV2 = v2;

        switch (face) {
            case "north" -> {
                // U inverted on X: u1↔max_x, u2↔min_x
                newU1 = lerp(u1, u2, 1 - xFracMax);
                newU2 = lerp(u1, u2, 1 - xFracMin);
                // V inverted on Y: v1↔max_y, v2↔min_y
                newV1 = lerp(v1, v2, 1 - yFracMax);
                newV2 = lerp(v1, v2, 1 - yFracMin);
            }
            case "south" -> {
                // U normal on X
                newU1 = lerp(u1, u2, xFracMin);
                newU2 = lerp(u1, u2, xFracMax);
                // V inverted on Y
                newV1 = lerp(v1, v2, 1 - yFracMax);
                newV2 = lerp(v1, v2, 1 - yFracMin);
            }
            case "east" -> {
                // U inverted on Z
                newU1 = lerp(u1, u2, 1 - zFracMax);
                newU2 = lerp(u1, u2, 1 - zFracMin);
                // V inverted on Y
                newV1 = lerp(v1, v2, 1 - yFracMax);
                newV2 = lerp(v1, v2, 1 - yFracMin);
            }
            case "west" -> {
                // U normal on Z
                newU1 = lerp(u1, u2, zFracMin);
                newU2 = lerp(u1, u2, zFracMax);
                // V inverted on Y
                newV1 = lerp(v1, v2, 1 - yFracMax);
                newV2 = lerp(v1, v2, 1 - yFracMin);
            }
            case "up" -> {
                // U normal on X
                newU1 = lerp(u1, u2, xFracMin);
                newU2 = lerp(u1, u2, xFracMax);
                // V normal on Z
                newV1 = lerp(v1, v2, zFracMin);
                newV2 = lerp(v1, v2, zFracMax);
            }
            case "down" -> {
                // U normal on X
                newU1 = lerp(u1, u2, xFracMin);
                newU2 = lerp(u1, u2, xFracMax);
                // V inverted on Z
                newV1 = lerp(v1, v2, 1 - zFracMax);
                newV2 = lerp(v1, v2, 1 - zFracMin);
            }
        }

        return new double[]{newU1, newV1, newU2, newV2};
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static double roundUV(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    private static JsonObject readJson(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    @Override
    public String getName() {
        return "ModelSplit";
    }
}
