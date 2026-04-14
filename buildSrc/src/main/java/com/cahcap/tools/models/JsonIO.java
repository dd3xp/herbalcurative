package com.cahcap.tools.models;

import com.google.gson.*;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Tiny helpers for reading/writing JSON files from the model processors. */
final class JsonIO {
    private JsonIO() {}

    static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    static JsonObject readJson(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    static void writeJson(Path path, JsonElement json) throws IOException {
        Files.createDirectories(path.getParent());
        try (Writer w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            PRETTY.toJson(json, w);
        }
    }

    static void writeString(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }
}
