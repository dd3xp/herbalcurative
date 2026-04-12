package com.cahcap.neoforge.common.datagen.models;

import com.cahcap.HerbalCurativeCommon;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
 * Data generator that reads Blockbench model JSONs and produces per-position
 * VoxelShape JSON files for multiblock collision boxes.
 * <p>
 * Input:  model JSON with elements using extended coordinates (e.g. -16 to 32)
 * Output: JSON with per-block-position boxes in local 0-16 coordinates
 */
public class VoxelShapeProvider implements DataProvider {

    private final PackOutput output;
    private final Path commonResourcesDir;

    /**
     * @param output              datagen output
     * @param commonResourcesDir  path to common/src/main/resources
     */
    public VoxelShapeProvider(PackOutput output, Path commonResourcesDir) {
        this.output = output;
        this.commonResourcesDir = commonResourcesDir;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        // Register all multiblock models to process
        futures.add(processModel(cache, "cauldron"));
        futures.add(processModel(cache, "herb_cabinet"));
        futures.add(processModel(cache, "herb_vault"));
        futures.add(processModel(cache, "kiln"));
        futures.add(processModel(cache, "obelisk"));
        futures.add(processModel(cache, "workbench"));

        // Single-block models (non-vanilla custom shapes)
        futures.add(processModel(cache, "herb_basket_floor"));
        // Wall basket excludes Rope and Nails groups from the voxelshape
        // so players can't click or collide with decorative elements.
        futures.add(processModelExcludingGroups(cache, "herb_basket_wall",
                "herb_basket_wall", Set.of("Rope", "Nails")));
        // Incense burner excludes Incense group (the two sticks) from voxelshape.
        futures.add(processModelExcludingGroups(cache, "incense_burner",
                "incense_burner", Set.of("Incense")));
        futures.add(processModel(cache, "herb_pot"));
        futures.add(processModel(cache, "red_cherry_shelf"));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> processModel(CachedOutput cache, String modelName) {
        Path modelPath = commonResourcesDir
                .resolve("assets")
                .resolve(HerbalCurativeCommon.MOD_ID)
                .resolve("models/block")
                .resolve(modelName + ".json");

        if (!Files.exists(modelPath)) {
            HerbalCurativeCommon.LOGGER.warn("Model file not found for voxelshape generation: {}", modelPath);
            return CompletableFuture.completedFuture(null);
        }

        try {
            JsonObject model = readJson(modelPath);
            JsonObject result = splitModel(model);

            Path outputPath = output.getOutputFolder()
                    .resolve("assets")
                    .resolve(HerbalCurativeCommon.MOD_ID)
                    .resolve("voxelshapes")
                    .resolve(modelName + ".json");

            return DataProvider.saveStable(cache, result, outputPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process model: " + modelName, e);
        }
    }

    /**
     * Split a Blockbench model's elements into per-block-position boxes.
     * Elements with coordinates beyond 0-16 are clipped and offset to local space.
     * Elements with a non-zero rotation are expanded to the axis-aligned bounding box
     * of their rotated geometry (since VoxelShape only supports axis-aligned boxes).
     */
    private JsonObject splitModel(JsonObject model) {
        JsonArray elements = model.getAsJsonArray("elements");
        if (elements == null || elements.isEmpty()) {
            throw new IllegalArgumentException("Model has no elements");
        }

        // Precompute each element's post-rotation AABB. Each entry is
        // [minX, minY, minZ, maxX, maxY, maxZ] in model-space (not yet clipped to any block).
        double[][] elementAabbs = new double[elements.size()][];
        for (int i = 0; i < elements.size(); i++) {
            elementAabbs[i] = computeElementAabb(elements.get(i).getAsJsonObject());
        }

        // Calculate overall bounding box from rotated AABBs
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;

        for (double[] aabb : elementAabbs) {
            minX = Math.min(minX, aabb[0]); maxX = Math.max(maxX, aabb[3]);
            minY = Math.min(minY, aabb[1]); maxY = Math.max(maxY, aabb[4]);
            minZ = Math.min(minZ, aabb[2]); maxZ = Math.max(maxZ, aabb[5]);
        }

        // Block ranges (in block units)
        int bxMin = (int) Math.floor(minX / 16.0);
        int bxMax = (int) Math.ceil(maxX / 16.0);
        int byMin = (int) Math.floor(minY / 16.0);
        int byMax = (int) Math.ceil(maxY / 16.0);
        int bzMin = (int) Math.floor(minZ / 16.0);
        int bzMax = (int) Math.ceil(maxZ / 16.0);

        // Collect boxes per position
        Map<String, List<double[]>> shapesByPos = new TreeMap<>();

        for (int dy = byMin; dy < byMax; dy++) {
            for (int dx = bxMin; dx < bxMax; dx++) {
                for (int dz = bzMin; dz < bzMax; dz++) {
                    List<double[]> boxes = new ArrayList<>();
                    double baseX = dx * 16.0, baseY = dy * 16.0, baseZ = dz * 16.0;

                    for (double[] aabb : elementAabbs) {
                        double[] clipped = clipToLocal(
                                aabb[0], aabb[1], aabb[2],
                                aabb[3], aabb[4], aabb[5],
                                baseX, baseY, baseZ);

                        if (clipped != null) {
                            boxes.add(clipped);
                        }
                    }

                    if (!boxes.isEmpty()) {
                        String key = dx + "," + dy + "," + dz;
                        shapesByPos.put(key, boxes);
                    }
                }
            }
        }

        // Build output JSON
        JsonObject result = new JsonObject();

        // Axis ranges metadata (model space, NORTH-oriented)
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
                for (double v : box) {
                    boxArray.add(v);
                }
                boxesArray.add(boxArray);
            }
            shapesJson.add(entry.getKey(), boxesArray);
        }

        result.add("shapes", shapesJson);
        return result;
    }

    /**
     * Clip an already-computed AABB to a 16x16x16 block at the given base coordinates,
     * then convert to local 0-16 space.
     *
     * @return [localX1, localY1, localZ1, localX2, localY2, localZ2] or null if no overlap
     */
    private static double[] clipToLocal(double minX, double minY, double minZ,
                                         double maxX, double maxY, double maxZ,
                                         double baseX, double baseY, double baseZ) {
        double loX = Math.max(minX, baseX);
        double hiX = Math.min(maxX, baseX + 16);
        if (loX >= hiX) return null;

        double loY = Math.max(minY, baseY);
        double hiY = Math.min(maxY, baseY + 16);
        if (loY >= hiY) return null;

        double loZ = Math.max(minZ, baseZ);
        double hiZ = Math.min(maxZ, baseZ + 16);
        if (loZ >= hiZ) return null;

        return new double[]{
                loX - baseX, loY - baseY, loZ - baseZ,
                hiX - baseX, hiY - baseY, hiZ - baseZ
        };
    }

    /**
     * Compute the axis-aligned bounding box of a Blockbench element, taking any
     * per-element rotation into account. Blockbench rotations are defined as
     * {angle, axis, origin}; we rotate the 8 corners of the from/to box around
     * origin along axis by angle, then return the AABB that contains them.
     * <p>
     * Collision shapes in Minecraft are axis-aligned, so a tilted element's
     * collision box is necessarily a bit larger than the visual geometry — this
     * is the closest faithful approximation possible.
     *
     * @return [minX, minY, minZ, maxX, maxY, maxZ] in model space
     */
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
        if (angleDeg == 0.0) {
            return new double[]{minX, minY, minZ, maxX, maxY, maxZ};
        }

        String axis = rotation.has("axis") ? rotation.get("axis").getAsString() : "y";
        JsonArray origin = rotation.getAsJsonArray("origin");
        double ox = origin.get(0).getAsDouble();
        double oy = origin.get(1).getAsDouble();
        double oz = origin.get(2).getAsDouble();

        double angleRad = Math.toRadians(angleDeg);
        double cos = Math.cos(angleRad);
        double sin = Math.sin(angleRad);

        double rMinX = Double.MAX_VALUE, rMaxX = -Double.MAX_VALUE;
        double rMinY = Double.MAX_VALUE, rMaxY = -Double.MAX_VALUE;
        double rMinZ = Double.MAX_VALUE, rMaxZ = -Double.MAX_VALUE;

        // Rotate all 8 corners around the rotation origin and accumulate the AABB
        for (int ci = 0; ci < 8; ci++) {
            double cx = ((ci & 1) == 0) ? minX : maxX;
            double cy = ((ci & 2) == 0) ? minY : maxY;
            double cz = ((ci & 4) == 0) ? minZ : maxZ;

            // Translate so origin is at (0,0,0)
            double x = cx - ox, y = cy - oy, z = cz - oz;
            double rx, ry, rz;
            // Minecraft model rotations follow the right-hand rule around the given axis
            switch (axis) {
                case "x":
                    rx = x;
                    ry = y * cos - z * sin;
                    rz = y * sin + z * cos;
                    break;
                case "y":
                    rx = x * cos + z * sin;
                    ry = y;
                    rz = -x * sin + z * cos;
                    break;
                case "z":
                default:
                    rx = x * cos - y * sin;
                    ry = x * sin + y * cos;
                    rz = z;
                    break;
            }
            // Translate back
            rx += ox; ry += oy; rz += oz;

            if (rx < rMinX) rMinX = rx; if (rx > rMaxX) rMaxX = rx;
            if (ry < rMinY) rMinY = ry; if (ry > rMaxY) rMaxY = ry;
            if (rz < rMinZ) rMinZ = rz; if (rz > rMaxZ) rMaxZ = rz;
        }

        return new double[]{rMinX, rMinY, rMinZ, rMaxX, rMaxY, rMaxZ};
    }

    /**
     * Process a model but exclude elements belonging to named Blockbench groups.
     * Reads the {@code "groups"} array from the model JSON to map group names to
     * element indices, then runs {@link #splitModel} on the filtered element list.
     *
     * @param outputName    name for the output voxelshape file (can differ from modelName)
     * @param excludeGroups set of Blockbench group names whose elements should be excluded
     */
    private CompletableFuture<?> processModelExcludingGroups(CachedOutput cache, String modelName,
                                                              String outputName, Set<String> excludeGroups) {
        Path modelPath = commonResourcesDir
                .resolve("assets")
                .resolve(HerbalCurativeCommon.MOD_ID)
                .resolve("models/block")
                .resolve(modelName + ".json");

        if (!Files.exists(modelPath)) {
            HerbalCurativeCommon.LOGGER.warn("Model file not found for filtered voxelshape generation: {}", modelPath);
            return CompletableFuture.completedFuture(null);
        }

        try {
            JsonObject model = readJson(modelPath);

            // Collect element indices to exclude based on group names
            Set<Integer> excludedIndices = collectGroupElementIndices(model, excludeGroups);

            // Build a filtered elements array
            JsonArray allElements = model.getAsJsonArray("elements");
            JsonArray filtered = new JsonArray();
            for (int i = 0; i < allElements.size(); i++) {
                if (!excludedIndices.contains(i)) {
                    filtered.add(allElements.get(i));
                }
            }

            // Replace elements in a copy of the model and run the normal split logic
            JsonObject filteredModel = model.deepCopy();
            filteredModel.add("elements", filtered);
            JsonObject result = splitModel(filteredModel);

            Path outputPath = output.getOutputFolder()
                    .resolve("assets")
                    .resolve(HerbalCurativeCommon.MOD_ID)
                    .resolve("voxelshapes")
                    .resolve(outputName + ".json");

            return DataProvider.saveStable(cache, result, outputPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process filtered model: " + modelName, e);
        }
    }

    /**
     * Walk the Blockbench {@code "groups"} tree and collect element indices belonging
     * to any group whose name is in {@code targetNames}. Groups can be nested; this
     * method recurses into children.
     */
    private static Set<Integer> collectGroupElementIndices(JsonObject model, Set<String> targetNames) {
        Set<Integer> indices = new HashSet<>();
        JsonArray groups = model.getAsJsonArray("groups");
        if (groups != null) {
            collectFromGroups(groups, targetNames, indices, false);
        }
        return indices;
    }

    private static void collectFromGroups(JsonArray groups, Set<String> targetNames,
                                           Set<Integer> out, boolean collecting) {
        for (JsonElement el : groups) {
            if (el.isJsonPrimitive()) {
                // Bare integer = element index. Collect if we're inside a target group.
                if (collecting) {
                    out.add(el.getAsInt());
                }
            } else if (el.isJsonObject()) {
                JsonObject group = el.getAsJsonObject();
                String name = group.has("name") ? group.get("name").getAsString() : "";
                boolean match = collecting || targetNames.contains(name);
                JsonArray children = group.getAsJsonArray("children");
                if (children != null) {
                    collectFromGroups(children, targetNames, out, match);
                }
            }
        }
    }

    private static JsonObject readJson(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    @Override
    public String getName() {
        return "VoxelShapes";
    }
}
