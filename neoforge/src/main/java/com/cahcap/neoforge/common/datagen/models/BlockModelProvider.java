package com.cahcap.neoforge.common.datagen.models;

import com.cahcap.HerbalCurativeCommon;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Reads block model JSONs from {@code common/src/main/resources/.../models/block/}
 * and writes processed copies to {@code generated/.../models/custom/<name>.json}.
 * <p>
 * Currently the only transformation is "strip {@code rotation: {angle: 0, ...}}",
 * which fixes the AO lighting grid bug. The provider is named generically so future
 * processing passes — e.g. fixing texture paths, applying common metadata — can be
 * added inside the same processing pipeline without renaming or splitting providers.
 * <p>
 * Architecture:
 * <ul>
 *   <li>Source files in {@code common/.../models/block/} are <strong>never modified</strong>
 *       by datagen. They can carry whatever Blockbench exports verbatim.</li>
 *   <li>Processed copies go to {@code neoforge/.../generated/resources/.../models/custom/<name>.json}.</li>
 *   <li>The {@code custom/} subdirectory exists only in {@code generated/}, so it cannot
 *       collide with {@code common/}'s {@code models/block/} contents — Minecraft always
 *       loads the cleaned version from a path that no other pack provides.</li>
 *   <li>Blockstate JSONs and item models reference these processed copies as
 *       {@code herbalcurative:custom/<name>}.</li>
 *   <li>Multiblock split parts live under {@code models/split/} (written by
 *       {@link ModelSplitProvider}) so the three output sets ({@code block/} for vanilla-style,
 *       {@code custom/} for cleaned, {@code split/} for multiblock parts) never collide.</li>
 * </ul>
 */
public class BlockModelProvider implements DataProvider {

    /**
     * Block models that need processing. These are the JSONs that Minecraft loads
     * directly to render a block in the world or as an inventory item:
     * <ul>
     *   <li>Single-block custom shapes (herb basket, herb pot, incense burner, ...)</li>
     *   <li>Multiblock source models that may be referenced as {@code unformedModel}
     *       (e.g. {@code herb_cabinet})</li>
     *   <li>Independent tool models loaded by {@code WorkbenchRenderer}</li>
     * </ul>
     * When adding a new custom-shape block, add its model name here.
     */
    private static final List<String> MODELS = List.of(
            // Single-block custom shapes
            "herb_basket_floor",
            "herb_basket_wall",
            "herb_pot",
            "incense_burner",
            "red_cherry_shelf",
            // Multiblock source models (also processed by ModelSplitProvider, but the
            // unformed-state renderer points back to the source for some multiblocks
            // like herb_cabinet, so they need to be clean too)
            "cauldron",
            "herb_cabinet",
            "herb_vault",
            "kiln",
            "obelisk",
            "workbench",
            // Independent tool models rendered by WorkbenchRenderer
            "workbench_tool_cutting_knife",
            "workbench_tool_feather_quill",
            "workbench_tool_forge_hammer",
            "workbench_tool_woven_rope"
    );

    /**
     * Matches a whole-line {@code "rotation": {...}} whose {@code angle} is 0.
     * Handles both Blockbench compact ({@code {"angle": 0, "axis": "y", "origin": [...]}}
     * on one line) and pretty-printed multi-line formats. {@code [^{}]*?} keeps the
     * match inside a single rotation object since rotation objects never contain
     * nested braces.
     * <p>
     * The {@code \b} after {@code 0} prevents matching {@code 0.5}, {@code 10}, etc.
     */
    private static final Pattern ANGLE_ZERO_ROTATION = Pattern.compile(
            "(?m)^[ \\t]*\"rotation\":\\s*\\{[^{}]*?\"angle\":\\s*0\\b[^{}]*?\\},?\\s*\\n"
    );

    private final PackOutput output;
    private final Path commonResourcesDir;

    /**
     * @param output             datagen pack output (where processed models will be written)
     * @param commonResourcesDir path to {@code common/src/main/resources} (where source models are read from)
     */
    public BlockModelProvider(PackOutput output, Path commonResourcesDir) {
        this.output = output;
        this.commonResourcesDir = commonResourcesDir;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path sourceDir = commonResourcesDir
                .resolve("assets")
                .resolve(HerbalCurativeCommon.MOD_ID)
                .resolve("models/block");

        Path outputDir = output.getOutputFolder()
                .resolve("assets")
                .resolve(HerbalCurativeCommon.MOD_ID)
                .resolve("models/custom");

        for (String name : MODELS) {
            Path source = sourceDir.resolve(name + ".json");
            if (!Files.exists(source)) {
                HerbalCurativeCommon.LOGGER.warn("[BlockModel] source missing: {}", source);
                continue;
            }
            try {
                String original = Files.readString(source, StandardCharsets.UTF_8);
                String processed = process(original);
                byte[] bytes = processed.getBytes(StandardCharsets.UTF_8);
                HashCode hash = Hashing.sha1().hashBytes(bytes);
                cache.writeIfNeeded(outputDir.resolve(name + ".json"), bytes, hash);
            } catch (IOException e) {
                throw new RuntimeException("Failed to process block model " + name + ".json", e);
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Apply all per-model transformations. Currently this is just rotation cleanup,
     * but new passes (texture path normalization, etc.) can be chained here.
     */
    private static String process(String content) {
        return ANGLE_ZERO_ROTATION.matcher(content).replaceAll("");
    }

    @Override
    public String getName() {
        return "BlockModels";
    }
}
