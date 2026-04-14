package com.cahcap.tools.models;

import com.google.gson.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Splits multiblock Blockbench models into per-position model JSONs under
 * {@code models/split/<name>_part_X_Y_Z.json}, with X-mirrored variants.
 */
public final class ModelSplitProcessor {

    private static final List<String> MULTIBLOCKS = List.of(
            "cauldron", "herb_cabinet", "herb_vault", "kiln", "obelisk", "workbench");

    private final Path modelsBlockDir;
    private final Path splitDir;

    public ModelSplitProcessor(Path modelsBlockDir, Path splitDir) {
        this.modelsBlockDir = modelsBlockDir;
        this.splitDir = splitDir;
    }

    public void run() throws IOException {
        for (String name : MULTIBLOCKS) process(name);
    }

    private void process(String modelName) throws IOException {
        Path modelPath = modelsBlockDir.resolve(modelName + ".json");
        if (!Files.exists(modelPath)) {
            System.err.println("[ModelSplit] source missing: " + modelPath);
            return;
        }

        JsonObject model = JsonIO.readJson(modelPath);
        Map<String, JsonObject> splits = splitModel(model, modelName);
        Map<String, JsonObject> mirrored = generateMirroredModels(splits, modelName);

        for (Map.Entry<String, JsonObject> entry : splits.entrySet()) {
            JsonIO.writeJson(splitDir.resolve(entry.getKey() + ".json"), entry.getValue());
        }
        for (Map.Entry<String, JsonObject> entry : mirrored.entrySet()) {
            JsonIO.writeJson(splitDir.resolve(entry.getKey() + ".json"), entry.getValue());
        }
    }

    private Map<String, JsonObject> splitModel(JsonObject model, String baseName) {
        JsonArray elements = model.getAsJsonArray("elements");
        if (elements == null || elements.isEmpty()) throw new IllegalArgumentException("Model has no elements");

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
                        if (clipped != null) clippedElements.add(clipped);
                    }
                    if (!clippedElements.isEmpty()) {
                        JsonObject fragment = new JsonObject();
                        if (textureSize != null) fragment.add("texture_size", textureSize.deepCopy());
                        if (textures != null) fragment.add("textures", textures.deepCopy());
                        fragment.add("elements", clippedElements);
                        result.put(baseName + "_part_" + dx + "_" + dy + "_" + dz, fragment);
                    }
                }
            }
        }
        return result;
    }

    private Map<String, JsonObject> generateMirroredModels(Map<String, JsonObject> normalModels, String baseName) {
        Map<String, JsonObject> mirrored = new TreeMap<>();
        for (Map.Entry<String, JsonObject> entry : normalModels.entrySet()) {
            String normalKey = entry.getKey();
            String suffix = normalKey.substring(baseName.length() + "_part_".length());
            String[] parts = suffix.split("_");
            int dx = Integer.parseInt(parts[0]);
            int dy = Integer.parseInt(parts[1]);
            int dz = Integer.parseInt(parts[2]);
            String mirroredKey = baseName + "_part_" + (-dx) + "_" + dy + "_" + dz + "_mirrored";

            JsonObject source = entry.getValue();
            JsonObject mirroredModel = new JsonObject();
            if (source.has("texture_size")) mirroredModel.add("texture_size", source.get("texture_size").deepCopy());
            if (source.has("textures")) mirroredModel.add("textures", source.get("textures").deepCopy());

            JsonArray mirroredElements = new JsonArray();
            for (JsonElement el : source.getAsJsonArray("elements")) {
                mirroredElements.add(mirrorElementX(el.getAsJsonObject()));
            }
            mirroredModel.add("elements", mirroredElements);
            mirrored.put(mirroredKey, mirroredModel);
        }
        return mirrored;
    }

    private JsonObject mirrorElementX(JsonObject original) {
        JsonObject result = new JsonObject();
        if (original.has("name")) result.addProperty("name", original.get("name").getAsString());

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

        if (original.has("rotation")) {
            JsonObject rot = original.getAsJsonObject("rotation");
            double angle = rot.has("angle") ? rot.get("angle").getAsDouble() : 0;
            if (angle != 0) {
                JsonObject rotCopy = rot.deepCopy();
                JsonArray origin = rotCopy.getAsJsonArray("origin");
                JsonArray newOrigin = new JsonArray();
                newOrigin.add(16.0 - origin.get(0).getAsDouble());
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

    private JsonObject clipElement(JsonObject original, double baseX, double baseY, double baseZ) {
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

    private double[] adjustUV(String face, double u1, double v1, double u2, double v2,
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

    private static double lerp(double a, double b, double t) { return a + (b - a) * t; }
    private static double roundUV(double value) { return Math.round(value * 10000.0) / 10000.0; }
}
