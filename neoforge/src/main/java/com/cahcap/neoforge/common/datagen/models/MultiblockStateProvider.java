package com.cahcap.neoforge.common.datagen.models;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.util.MultiblockShapes;
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
 * Generates blockstate JSONs for multiblock structures using split models.
 * Each position in the multiblock maps to its corresponding _part_ model file.
 */
public class MultiblockStateProvider implements DataProvider {

    private final PackOutput output;
    private final Path commonResourcesDir;

    private record MultiblockConfig(
            String blockName,
            String unformedModel,
            boolean unformedHasRotation,
            String particleTexture,
            List<String> extraProperties
    ) {}

    public MultiblockStateProvider(PackOutput output, Path commonResourcesDir) {
        this.output = output;
        this.commonResourcesDir = commonResourcesDir;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        futures.add(generate(cache, new MultiblockConfig(
                "cauldron", "herbalcurative:block/lumistone", false,
                "herbalcurative:block/cauldron", List.of())));
        futures.add(generate(cache, new MultiblockConfig(
                "herb_cabinet", "herbalcurative:block/herb_cabinet", true,
                "herbalcurative:block/herb_cabinet", List.of())));
        futures.add(generate(cache, new MultiblockConfig(
                "herb_vault", "herbalcurative:block/lumistone_bricks", false,
                "herbalcurative:block/herb_vault", List.of())));
        futures.add(generate(cache, new MultiblockConfig(
                "kiln", "herbalcurative:block/stone_bricks", false,
                "herbalcurative:block/kiln", List.of("lit"))));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> generate(CachedOutput cache, MultiblockConfig config) {
        Path modelPath = commonResourcesDir
                .resolve("assets")
                .resolve(HerbalCurativeCommon.MOD_ID)
                .resolve("models/block")
                .resolve(config.blockName + ".json");

        if (!Files.exists(modelPath)) {
            HerbalCurativeCommon.LOGGER.warn("Model not found for blockstate generation: {}", modelPath);
            return CompletableFuture.completedFuture(null);
        }

        try {
            JsonObject model = readJson(modelPath);
            AxisRanges ranges = computeAxisRanges(model);
            Set<String> occupiedPositions = findOccupiedPositions(config.blockName, model, ranges);

            JsonObject blockstate = buildBlockstate(config, ranges, occupiedPositions);

            Path outputPath = output.getOutputFolder()
                    .resolve("assets")
                    .resolve(HerbalCurativeCommon.MOD_ID)
                    .resolve("blockstates")
                    .resolve(config.blockName + ".json");

            return DataProvider.saveStable(cache, blockstate, outputPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate blockstate for: " + config.blockName, e);
        }
    }

    private record AxisRanges(int dxMin, int dxMax, int dyMin, int dyMax, int dzMin, int dzMax) {
        int xSize() { return dxMax - dxMin + 1; }
        int ySize() { return dyMax - dyMin + 1; }
        int zSize() { return dzMax - dzMin + 1; }
        int totalPositions() { return xSize() * ySize() * zSize(); }

        int toIndex(int dx, int dy, int dz) {
            return (dy - dyMin) * xSize() * zSize() + (dx - dxMin) * zSize() + (dz - dzMin);
        }

        int[] fromIndex(int index) {
            int yzSlice = xSize() * zSize();
            int dy = index / yzSlice + dyMin;
            int rem = index % yzSlice;
            int dx = rem / zSize() + dxMin;
            int dz = rem % zSize() + dzMin;
            return new int[]{dx, dy, dz};
        }
    }

    private AxisRanges computeAxisRanges(JsonObject model) {
        JsonArray elements = model.getAsJsonArray("elements");
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

        return new AxisRanges(
                (int) Math.floor(minX / 16.0), (int) Math.ceil(maxX / 16.0) - 1,
                (int) Math.floor(minY / 16.0), (int) Math.ceil(maxY / 16.0) - 1,
                (int) Math.floor(minZ / 16.0), (int) Math.ceil(maxZ / 16.0) - 1);
    }

    /**
     * Compute which positions have model elements by checking the model JSON directly.
     * This avoids race conditions with ModelSplitProvider which may run in parallel.
     */
    private Set<String> findOccupiedPositions(String blockName, JsonObject model, AxisRanges ranges) {
        JsonArray elements = model.getAsJsonArray("elements");
        Set<String> occupied = new HashSet<>();

        for (int dy = ranges.dyMin; dy <= ranges.dyMax; dy++) {
            for (int dx = ranges.dxMin; dx <= ranges.dxMax; dx++) {
                for (int dz = ranges.dzMin; dz <= ranges.dzMax; dz++) {
                    double baseX = dx * 16.0, baseY = dy * 16.0, baseZ = dz * 16.0;
                    boolean hasElements = false;

                    for (JsonElement el : elements) {
                        JsonObject elem = el.getAsJsonObject();
                        JsonArray from = elem.getAsJsonArray("from");
                        JsonArray to = elem.getAsJsonArray("to");
                        double fx = from.get(0).getAsDouble(), fy = from.get(1).getAsDouble(), fz = from.get(2).getAsDouble();
                        double tx = to.get(0).getAsDouble(), ty = to.get(1).getAsDouble(), tz = to.get(2).getAsDouble();

                        if (Math.max(Math.min(fx, tx), baseX) < Math.min(Math.max(fx, tx), baseX + 16) &&
                            Math.max(Math.min(fy, ty), baseY) < Math.min(Math.max(fy, ty), baseY + 16) &&
                            Math.max(Math.min(fz, tz), baseZ) < Math.min(Math.max(fz, tz), baseZ + 16)) {
                            hasElements = true;
                            break;
                        }
                    }

                    if (hasElements) {
                        occupied.add(blockName + "_part_" + dx + "_" + dy + "_" + dz);
                    }
                }
            }
        }
        return occupied;
    }

    private JsonObject buildBlockstate(MultiblockConfig config, AxisRanges ranges, Set<String> occupiedPositions) {
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();

        String[] facings = {"north", "south", "east", "west"};
        int[] rotations = {0, 180, 90, 270}; // north=0, south=180, east=90, west=270

        int totalPositions = ranges.totalPositions();

        // Generate extra property combinations (e.g., lit=true/false for kiln)
        List<Map<String, String>> extraCombinations = generateExtraCombinations(config.extraProperties);

        for (Map<String, String> extras : extraCombinations) {
            for (int fi = 0; fi < 4; fi++) {
                String facing = facings[fi];
                int rotation = rotations[fi];

                for (boolean formed : new boolean[]{false, true}) {
                    for (boolean mirrored : new boolean[]{false, true}) {
                        for (int position = 0; position < totalPositions; position++) {
                            String key = buildVariantKey(facing, formed, mirrored, position, extras);

                            JsonObject variant;
                            if (!formed) {
                                variant = buildUnformedVariant(config, facing, rotation);
                            } else {
                                variant = buildFormedVariant(config, ranges, occupiedPositions,
                                        position, mirrored, rotation);
                            }
                            variants.add(key, variant);
                        }
                    }
                }
            }
        }

        root.add("variants", variants);
        return root;
    }

    private String buildVariantKey(String facing, boolean formed, boolean mirrored,
                                    int position, Map<String, String> extras) {
        StringBuilder sb = new StringBuilder();
        sb.append("facing=").append(facing);
        sb.append(",formed=").append(formed);
        // Extra properties in alphabetical order
        for (Map.Entry<String, String> e : new TreeMap<>(extras).entrySet()) {
            sb.append(",").append(e.getKey()).append("=").append(e.getValue());
        }
        sb.append(",mirrored=").append(mirrored);
        sb.append(",position=").append(position);
        return sb.toString();
    }

    private JsonObject buildUnformedVariant(MultiblockConfig config, String facing, int rotation) {
        JsonObject variant = new JsonObject();
        variant.addProperty("model", config.unformedModel);
        if (config.unformedHasRotation && rotation != 0) {
            variant.addProperty("y", rotation);
        }
        return variant;
    }

    private JsonObject buildFormedVariant(MultiblockConfig config, AxisRanges ranges,
                                           Set<String> occupiedPositions,
                                           int position, boolean mirrored, int rotation) {
        // Position index is in model space — convert directly to model-space offset
        int[] modelOffset = ranges.fromIndex(position);
        int modelDx = modelOffset[0], dy = modelOffset[1], modelDz = modelOffset[2];

        String partName;
        String sourceCheck; // Name to check in occupiedPositions (always the normal model name)
        if (mirrored) {
            // Mirrored model: X-flipped content from the source position
            String sourceName = config.blockName + "_part_" + modelDx + "_" + dy + "_" + modelDz;
            partName = config.blockName + "_part_" + (-modelDx) + "_" + dy + "_" + modelDz + "_mirrored";
            sourceCheck = sourceName;
        } else {
            partName = config.blockName + "_part_" + modelDx + "_" + dy + "_" + modelDz;
            sourceCheck = partName;
        }
        String modelRef;

        if (occupiedPositions.contains(sourceCheck)) {
            modelRef = HerbalCurativeCommon.MOD_ID + ":block/" + partName;
        } else {
            modelRef = HerbalCurativeCommon.MOD_ID + ":block/" + config.blockName + "_invisible";
        }

        JsonObject variant = new JsonObject();
        variant.addProperty("model", modelRef);
        if (rotation != 0) {
            variant.addProperty("y", rotation);
        }
        return variant;
    }

    private List<Map<String, String>> generateExtraCombinations(List<String> extraProperties) {
        if (extraProperties.isEmpty()) {
            return List.of(Map.of());
        }

        List<Map<String, String>> result = new ArrayList<>();
        result.add(new HashMap<>());

        for (String prop : extraProperties) {
            List<Map<String, String>> expanded = new ArrayList<>();
            for (Map<String, String> existing : result) {
                for (String value : new String[]{"false", "true"}) {
                    Map<String, String> copy = new HashMap<>(existing);
                    copy.put(prop, value);
                    expanded.add(copy);
                }
            }
            result = expanded;
        }

        return result;
    }

    private static JsonObject readJson(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    @Override
    public String getName() {
        return "MultiblockBlockStates";
    }
}
