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
     */
    private JsonObject splitModel(JsonObject model) {
        JsonArray elements = model.getAsJsonArray("elements");
        if (elements == null || elements.isEmpty()) {
            throw new IllegalArgumentException("Model has no elements");
        }

        // Calculate bounding box of all elements
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;

        for (JsonElement el : elements) {
            JsonObject elem = el.getAsJsonObject();
            JsonArray from = elem.getAsJsonArray("from");
            JsonArray to = elem.getAsJsonArray("to");
            double fx = from.get(0).getAsDouble(), fy = from.get(1).getAsDouble(), fz = from.get(2).getAsDouble();
            double tx = to.get(0).getAsDouble(), ty = to.get(1).getAsDouble(), tz = to.get(2).getAsDouble();
            minX = Math.min(minX, Math.min(fx, tx)); maxX = Math.max(maxX, Math.max(fx, tx));
            minY = Math.min(minY, Math.min(fy, ty)); maxY = Math.max(maxY, Math.max(fy, ty));
            minZ = Math.min(minZ, Math.min(fz, tz)); maxZ = Math.max(maxZ, Math.max(fz, tz));
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

                    for (JsonElement el : elements) {
                        JsonObject elem = el.getAsJsonObject();
                        JsonArray from = elem.getAsJsonArray("from");
                        JsonArray to = elem.getAsJsonArray("to");

                        double[] clipped = clipToLocal(
                                from.get(0).getAsDouble(), from.get(1).getAsDouble(), from.get(2).getAsDouble(),
                                to.get(0).getAsDouble(), to.get(1).getAsDouble(), to.get(2).getAsDouble(),
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
     * Clip an element's box to a 16x16x16 block at the given base coordinates,
     * then convert to local 0-16 space.
     *
     * @return [localX1, localY1, localZ1, localX2, localY2, localZ2] or null if no overlap
     */
    private static double[] clipToLocal(double fx, double fy, double fz,
                                         double tx, double ty, double tz,
                                         double baseX, double baseY, double baseZ) {
        double loX = Math.max(Math.min(fx, tx), baseX);
        double hiX = Math.min(Math.max(fx, tx), baseX + 16);
        if (loX >= hiX) return null;

        double loY = Math.max(Math.min(fy, ty), baseY);
        double hiY = Math.min(Math.max(fy, ty), baseY + 16);
        if (loY >= hiY) return null;

        double loZ = Math.max(Math.min(fz, tz), baseZ);
        double hiZ = Math.min(Math.max(fz, tz), baseZ + 16);
        if (loZ >= hiZ) return null;

        return new double[]{
                loX - baseX, loY - baseY, loZ - baseZ,
                hiX - baseX, hiY - baseY, hiZ - baseZ
        };
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
