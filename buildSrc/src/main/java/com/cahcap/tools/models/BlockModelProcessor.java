package com.cahcap.tools.models;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Strips {@code "rotation": {..., "angle": 0, ...}} blocks from Blockbench-exported
 * model JSONs. Writes the cleaned result back to the same path (in-place rewrite).
 * <p>
 * Source/output: {@code common/.../models/block/<name>.json}
 */
public final class BlockModelProcessor {

    /** Models that Minecraft renders directly and must be kept clean of angle:0 rotations. */
    private static final List<String> MODELS = List.of(
            "herb_basket_floor",
            "herb_basket_wall",
            "herb_pot",
            "incense_burner",
            "shelf",
            "cauldron",
            "herb_cabinet",
            "herb_vault",
            "kiln",
            "obelisk",
            "workbench",
            "workbench_tool_cutting_knife",
            "workbench_tool_feather_quill",
            "workbench_tool_forge_hammer",
            "workbench_tool_woven_rope"
    );

    private static final Pattern ANGLE_ZERO_ROTATION = Pattern.compile(
            "(?m)^[ \\t]*\"rotation\":\\s*\\{[^{}]*?\"angle\":\\s*0\\b[^{}]*?\\},?\\s*\\n"
    );

    private final Path modelsBlockDir;

    public BlockModelProcessor(Path modelsBlockDir) {
        this.modelsBlockDir = modelsBlockDir;
    }

    public void run() throws IOException {
        for (String name : MODELS) {
            Path file = modelsBlockDir.resolve(name + ".json");
            if (!Files.exists(file)) {
                System.err.println("[BlockModel] source missing: " + file);
                continue;
            }
            String original = Files.readString(file, StandardCharsets.UTF_8);
            String processed = ANGLE_ZERO_ROTATION.matcher(original).replaceAll("");
            if (!processed.equals(original)) {
                Files.writeString(file, processed, StandardCharsets.UTF_8);
                System.out.println("[BlockModel] cleaned " + name + ".json");
            }
        }
    }
}
