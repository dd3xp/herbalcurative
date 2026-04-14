package com.cahcap.tools.models;

import com.google.gson.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Reads multiblock/custom-shape model JSONs, splits elements into per-block-position
 * axis-aligned boxes, and writes VoxelShape JSONs to {@code voxelshapes/<name>.json}.
 */
public final class VoxelShapeProcessor {

    private final Path modelsBlockDir;
    private final Path voxelshapesDir;

    public VoxelShapeProcessor(Path modelsBlockDir, Path voxelshapesDir) {
        this.modelsBlockDir = modelsBlockDir;
        this.voxelshapesDir = voxelshapesDir;
    }

    public void run() throws IOException {
        for (String name : List.of(
                "cauldron", "herb_cabinet", "herb_vault", "kiln",
                "obelisk", "workbench",
                "herb_basket_floor", "herb_pot", "shelf")) {
            process(name, name, Collections.emptySet());
        }
        // Wall basket excludes Rope + Nails groups; incense burner excludes Incense group.
        process("herb_basket_wall", "herb_basket_wall", Set.of("Rope", "Nails"));
        process("incense_burner", "incense_burner", Set.of("Incense"));
    }

    private void process(String modelName, String outputName, Set<String> excludeGroups) throws IOException {
        Path modelPath = modelsBlockDir.resolve(modelName + ".json");
        if (!Files.exists(modelPath)) {
            System.err.println("[VoxelShape] source missing: " + modelPath);
            return;
        }
        JsonObject model = JsonIO.readJson(modelPath);

        JsonArray allElements = model.getAsJsonArray("elements");
        JsonArray elements;
        if (excludeGroups.isEmpty()) {
            elements = allElements;
        } else {
            Set<Integer> excludedIndices = collectGroupElementIndices(model, excludeGroups);
            elements = new JsonArray();
            for (int i = 0; i < allElements.size(); i++) {
                if (!excludedIndices.contains(i)) {
                    elements.add(allElements.get(i));
                }
            }
        }

        JsonObject result = splitModel(elements);
        JsonIO.writeJson(voxelshapesDir.resolve(outputName + ".json"), result);
    }

    private JsonObject splitModel(JsonArray elements) {
        if (elements.isEmpty()) throw new IllegalArgumentException("Model has no elements");

        double[][] elementAabbs = new double[elements.size()][];
        for (int i = 0; i < elements.size(); i++) {
            elementAabbs[i] = computeElementAabb(elements.get(i).getAsJsonObject());
        }

        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (double[] aabb : elementAabbs) {
            minX = Math.min(minX, aabb[0]); maxX = Math.max(maxX, aabb[3]);
            minY = Math.min(minY, aabb[1]); maxY = Math.max(maxY, aabb[4]);
            minZ = Math.min(minZ, aabb[2]); maxZ = Math.max(maxZ, aabb[5]);
        }

        int bxMin = (int) Math.floor(minX / 16.0);
        int bxMax = (int) Math.ceil(maxX / 16.0);
        int byMin = (int) Math.floor(minY / 16.0);
        int byMax = (int) Math.ceil(maxY / 16.0);
        int bzMin = (int) Math.floor(minZ / 16.0);
        int bzMax = (int) Math.ceil(maxZ / 16.0);

        Map<String, List<double[]>> shapesByPos = new TreeMap<>();
        for (int dy = byMin; dy < byMax; dy++) {
            for (int dx = bxMin; dx < bxMax; dx++) {
                for (int dz = bzMin; dz < bzMax; dz++) {
                    List<double[]> boxes = new ArrayList<>();
                    double baseX = dx * 16.0, baseY = dy * 16.0, baseZ = dz * 16.0;
                    for (double[] aabb : elementAabbs) {
                        double[] clipped = clipToLocal(aabb[0], aabb[1], aabb[2],
                                aabb[3], aabb[4], aabb[5], baseX, baseY, baseZ);
                        if (clipped != null) boxes.add(clipped);
                    }
                    if (!boxes.isEmpty()) shapesByPos.put(dx + "," + dy + "," + dz, boxes);
                }
            }
        }

        JsonObject result = new JsonObject();
        JsonObject axisRanges = new JsonObject();
        axisRanges.addProperty("dx_min", bxMin);
        axisRanges.addProperty("dx_max", bxMax - 1);
        axisRanges.addProperty("dy_min", byMin);
        axisRanges.addProperty("dy_max", byMax - 1);
        axisRanges.addProperty("dz_min", bzMin);
        axisRanges.addProperty("dz_max", bzMax - 1);
        result.add("axis_ranges", axisRanges);

        JsonObject shapesJson = new JsonObject();
        for (Map.Entry<String, List<double[]>> entry : shapesByPos.entrySet()) {
            JsonArray boxesArray = new JsonArray();
            for (double[] box : entry.getValue()) {
                JsonArray boxArray = new JsonArray();
                for (double v : box) boxArray.add(v);
                boxesArray.add(boxArray);
            }
            shapesJson.add(entry.getKey(), boxesArray);
        }
        result.add("shapes", shapesJson);
        return result;
    }

    private static double[] clipToLocal(double minX, double minY, double minZ,
                                        double maxX, double maxY, double maxZ,
                                        double baseX, double baseY, double baseZ) {
        double loX = Math.max(minX, baseX), hiX = Math.min(maxX, baseX + 16);
        if (loX >= hiX) return null;
        double loY = Math.max(minY, baseY), hiY = Math.min(maxY, baseY + 16);
        if (loY >= hiY) return null;
        double loZ = Math.max(minZ, baseZ), hiZ = Math.min(maxZ, baseZ + 16);
        if (loZ >= hiZ) return null;
        return new double[]{loX - baseX, loY - baseY, loZ - baseZ,
                hiX - baseX, hiY - baseY, hiZ - baseZ};
    }

    private static double[] computeElementAabb(JsonObject elem) {
        JsonArray from = elem.getAsJsonArray("from");
        JsonArray to = elem.getAsJsonArray("to");
        double fx = from.get(0).getAsDouble(), fy = from.get(1).getAsDouble(), fz = from.get(2).getAsDouble();
        double tx = to.get(0).getAsDouble(), ty = to.get(1).getAsDouble(), tz = to.get(2).getAsDouble();
        double minX = Math.min(fx, tx), maxX = Math.max(fx, tx);
        double minY = Math.min(fy, ty), maxY = Math.max(fy, ty);
        double minZ = Math.min(fz, tz), maxZ = Math.max(fz, tz);

        JsonObject rotation = elem.has("rotation") && elem.get("rotation").isJsonObject()
                ? elem.getAsJsonObject("rotation") : null;
        if (rotation == null || !rotation.has("angle")) {
            return new double[]{minX, minY, minZ, maxX, maxY, maxZ};
        }
        double angleDeg = rotation.get("angle").getAsDouble();
        if (angleDeg == 0.0) return new double[]{minX, minY, minZ, maxX, maxY, maxZ};

        String axis = rotation.has("axis") ? rotation.get("axis").getAsString() : "y";
        JsonArray origin = rotation.getAsJsonArray("origin");
        double ox = origin.get(0).getAsDouble(), oy = origin.get(1).getAsDouble(), oz = origin.get(2).getAsDouble();

        double angleRad = Math.toRadians(angleDeg);
        double cos = Math.cos(angleRad), sin = Math.sin(angleRad);

        double rMinX = Double.MAX_VALUE, rMaxX = -Double.MAX_VALUE;
        double rMinY = Double.MAX_VALUE, rMaxY = -Double.MAX_VALUE;
        double rMinZ = Double.MAX_VALUE, rMaxZ = -Double.MAX_VALUE;

        for (int ci = 0; ci < 8; ci++) {
            double cx = ((ci & 1) == 0) ? minX : maxX;
            double cy = ((ci & 2) == 0) ? minY : maxY;
            double cz = ((ci & 4) == 0) ? minZ : maxZ;
            double x = cx - ox, y = cy - oy, z = cz - oz;
            double rx, ry, rz;
            switch (axis) {
                case "x" -> { rx = x; ry = y * cos - z * sin; rz = y * sin + z * cos; }
                case "y" -> { rx = x * cos + z * sin; ry = y; rz = -x * sin + z * cos; }
                default -> { rx = x * cos - y * sin; ry = x * sin + y * cos; rz = z; }
            }
            rx += ox; ry += oy; rz += oz;
            if (rx < rMinX) rMinX = rx; if (rx > rMaxX) rMaxX = rx;
            if (ry < rMinY) rMinY = ry; if (ry > rMaxY) rMaxY = ry;
            if (rz < rMinZ) rMinZ = rz; if (rz > rMaxZ) rMaxZ = rz;
        }
        return new double[]{rMinX, rMinY, rMinZ, rMaxX, rMaxY, rMaxZ};
    }

    private static Set<Integer> collectGroupElementIndices(JsonObject model, Set<String> targetNames) {
        Set<Integer> indices = new HashSet<>();
        JsonArray groups = model.getAsJsonArray("groups");
        if (groups != null) collectFromGroups(groups, targetNames, indices, false);
        return indices;
    }

    private static void collectFromGroups(JsonArray groups, Set<String> targetNames,
                                          Set<Integer> out, boolean collecting) {
        for (JsonElement el : groups) {
            if (el.isJsonPrimitive()) {
                if (collecting) out.add(el.getAsInt());
            } else if (el.isJsonObject()) {
                JsonObject group = el.getAsJsonObject();
                String name = group.has("name") ? group.get("name").getAsString() : "";
                boolean match = collecting || targetNames.contains(name);
                JsonArray children = group.getAsJsonArray("children");
                if (children != null) collectFromGroups(children, targetNames, out, match);
            }
        }
    }
}
