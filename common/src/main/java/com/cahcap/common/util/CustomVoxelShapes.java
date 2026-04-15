package com.cahcap.common.util;

import com.cahcap.common.block.MultiblockPartBlock;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Runtime-derived collision shapes for blocks with custom geometry.
 * <p>
 * Reads a Blockbench block-model JSON at class-init time, parses {@code elements[]},
 * clips each element into per-cell axis-aligned boxes (with rotation-aware AABB expansion),
 * and precomputes rotated + mirrored variants for all 4 horizontal facings.
 * <p>
 * Works for both multiblocks (model spans several cells: produces {@code xSize*ySize*zSize}
 * VoxelShapes and you query with a position index) and single-cell custom-shape blocks
 * (model fits in [0,16]: produces one VoxelShape, query with index=0).
 * <p>
 * Also serves as the single source of truth for axis ranges and coordinate conversion
 * between blueprint space and model space (both use NORTH as default facing).
 */
public class CustomVoxelShapes {

    private final VoxelShape[] northShapes;
    private final VoxelShape[][] byFacing;
    private final VoxelShape[][] byFacingMirrored;

    private final int dxMin, dyMin, dzMin;
    private final int dxMax, dyMax, dzMax;
    private final int xSize, ySize, zSize;

    private CustomVoxelShapes(VoxelShape[] northShapes,
                             int dxMin, int dyMin, int dzMin,
                             int xSize, int ySize, int zSize) {
        this.northShapes = northShapes;
        this.dxMin = dxMin;
        this.dyMin = dyMin;
        this.dzMin = dzMin;
        this.dxMax = dxMin + xSize - 1;
        this.dyMax = dyMin + ySize - 1;
        this.dzMax = dzMin + zSize - 1;
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;

        this.byFacing = MultiblockPartBlock.precomputeRotatedShapes(northShapes);
        this.byFacingMirrored = MultiblockPartBlock.precomputeMirroredShapes(northShapes, i -> {
            int dy = i / (xSize * zSize);
            int dx = (i % (xSize * zSize)) / zSize;
            int dz = i % zSize;
            int mirroredDx = (xSize - 1) - dx;
            return dy * xSize * zSize + mirroredDx * zSize + dz;
        });
    }

    // ==================== Axis Ranges (Model Space) ====================

    /** Total number of block positions in this multiblock. */
    public int totalPositions() { return xSize * ySize * zSize; }

    public int dxMin() { return dxMin; }
    public int dyMin() { return dyMin; }
    public int dzMin() { return dzMin; }
    public int dxMax() { return dxMax; }
    public int dyMax() { return dyMax; }
    public int dzMax() { return dzMax; }
    public int xSize() { return xSize; }
    public int ySize() { return ySize; }
    public int zSize() { return zSize; }

    // ==================== Coordinate Conversion ====================

    /**
     * Convert model-space offset (NORTH-oriented) to position index.
     * Index formula: (dy - dyMin) * xSize * zSize + (dx - dxMin) * zSize + (dz - dzMin)
     */
    public int toIndex(int dx, int dy, int dz) {
        return (dy - dyMin) * xSize * zSize + (dx - dxMin) * zSize + (dz - dzMin);
    }

    /**
     * Convert position index to model-space offset (NORTH-oriented).
     * @return [dx, dy, dz]
     */
    public int[] fromIndex(int index) {
        int yzSlice = xSize * zSize;
        int dy = index / yzSlice + dyMin;
        int rem = index % yzSlice;
        int dx = rem / zSize + dxMin;
        int dz = rem % zSize + dzMin;
        return new int[]{dx, dy, dz};
    }

    /**
     * Convert blueprint-space offset to model-space offset.
     * Both use NORTH as default facing, so this is the identity transform.
     */
    public static int[] blueprintToModel(int bpDx, int bpDy, int bpDz) {
        return new int[]{bpDx, bpDy, bpDz};
    }

    /**
     * Convert model-space offset to blueprint-space offset.
     * Both use NORTH as default facing, so this is the identity transform.
     */
    public static int[] modelToBlueprint(int modelDx, int modelDy, int modelDz) {
        return new int[]{modelDx, modelDy, modelDz};
    }

    // ==================== Shape Lookup ====================

    /**
     * Look up the shape by position index, facing, and mirror state.
     */
    public VoxelShape getByIndex(Direction facing, int index, boolean mirrored) {
        if (index < 0 || index >= northShapes.length) {
            return Shapes.block();
        }
        VoxelShape[][] table = mirrored ? byFacingMirrored : byFacing;
        return table[facing.get2DDataValue()][index];
    }

    /**
     * Look up the shape for a given world offset, facing, and mirror state.
     */
    public VoxelShape get(Direction facing, int[] offset, boolean mirrored) {
        int[] model = MultiblockPartBlock.worldToModelOffset(facing, offset);
        int dx = model[0] - dxMin;
        int dy = model[1] - dyMin;
        int dz = model[2] - dzMin;

        if (dx < 0 || dx >= xSize || dy < 0 || dy >= ySize || dz < 0 || dz >= zSize) {
            return Shapes.block();
        }

        int index = dy * xSize * zSize + dx * zSize + dz;
        if (index < 0 || index >= northShapes.length) {
            return Shapes.block();
        }

        VoxelShape[][] table = mirrored ? byFacingMirrored : byFacing;
        return table[facing.get2DDataValue()][index];
    }

    // ==================== Loading ====================

    /**
     * Load shapes by parsing a block model JSON at runtime.
     * Walks {@code elements[]}, applies per-element rotation to derive AABBs,
     * then clips each AABB into per-block-position boxes. Group names in
     * {@code excludeGroups} are skipped (matches VoxelShapeProcessor behavior).
     *
     * @param modelResourcePath classpath path, e.g. "/assets/herbalcurative/models/block/cauldron.json"
     * @param excludeGroups     group names (from Blockbench "groups" tree) whose elements are omitted
     */
    public static CustomVoxelShapes loadFromModel(String modelResourcePath, Set<String> excludeGroups) {
        String clPath = modelResourcePath.startsWith("/") ? modelResourcePath.substring(1) : modelResourcePath;
        InputStream is = null;
        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(clPath);
            if (is == null) is = CustomVoxelShapes.class.getResourceAsStream(modelResourcePath);
            if (is == null) is = ClassLoader.getSystemResourceAsStream(clPath);
            if (is == null) {
                // Datagen fallback — same 2-position stub as load().
                return new CustomVoxelShapes(new VoxelShape[]{Shapes.block(), Shapes.block()}, 0, 0, 0, 2, 1, 1);
            }
            JsonObject model = JsonParser.parseReader(new InputStreamReader(is, StandardCharsets.UTF_8)).getAsJsonObject();
            return fromModelJson(model, excludeGroups == null ? Collections.emptySet() : excludeGroups);
        } catch (Exception e) {
            throw new RuntimeException("Failed to derive voxelshapes from model " + modelResourcePath, e);
        } finally {
            if (is != null) { try { is.close(); } catch (Exception ignored) {} }
        }
    }

    public static CustomVoxelShapes loadFromModel(String modelResourcePath) {
        return loadFromModel(modelResourcePath, Collections.emptySet());
    }

    private static CustomVoxelShapes fromModelJson(JsonObject model, Set<String> excludeGroups) {
        JsonArray allElements = model.getAsJsonArray("elements");
        if (allElements == null || allElements.isEmpty()) {
            throw new IllegalArgumentException("Model has no elements");
        }

        // Filter out excluded groups.
        Set<Integer> excludedIndices = excludeGroups.isEmpty()
                ? Collections.emptySet()
                : collectGroupElementIndices(model, excludeGroups);

        List<double[]> elementAabbs = new ArrayList<>();
        for (int i = 0; i < allElements.size(); i++) {
            if (excludedIndices.contains(i)) continue;
            elementAabbs.add(computeElementAabb(allElements.get(i).getAsJsonObject()));
        }
        if (elementAabbs.isEmpty()) {
            throw new IllegalArgumentException("All elements excluded");
        }

        // Compute block-cell bounds (ceil/floor over pixels / 16).
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

        int xSize = bxMax - bxMin;
        int ySize = byMax - byMin;
        int zSize = bzMax - bzMin;
        int totalSize = xSize * ySize * zSize;

        VoxelShape[] northShapes = new VoxelShape[totalSize];
        for (int i = 0; i < totalSize; i++) northShapes[i] = Shapes.empty();

        for (int dy = byMin; dy < byMax; dy++) {
            for (int dx = bxMin; dx < bxMax; dx++) {
                for (int dz = bzMin; dz < bzMax; dz++) {
                    double baseX = dx * 16.0, baseY = dy * 16.0, baseZ = dz * 16.0;
                    VoxelShape shape = Shapes.empty();
                    boolean any = false;
                    for (double[] aabb : elementAabbs) {
                        double[] clipped = clipToLocal(aabb[0], aabb[1], aabb[2],
                                aabb[3], aabb[4], aabb[5], baseX, baseY, baseZ);
                        if (clipped != null) {
                            // clipped values are in pixels, in the [0, 16] range; Block.box uses pixels directly.
                            shape = Shapes.or(shape, Block.box(
                                    clipped[0], clipped[1], clipped[2],
                                    clipped[3], clipped[4], clipped[5]));
                            any = true;
                        }
                    }
                    if (any) {
                        int idx = (dy - byMin) * xSize * zSize + (dx - bxMin) * zSize + (dz - bzMin);
                        northShapes[idx] = shape;
                    }
                }
            }
        }

        return new CustomVoxelShapes(northShapes, bxMin, byMin, bzMin, xSize, ySize, zSize);
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
