package com.cahcap.tools.models;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Gradle task that runs all four model processors against the common module's
 * resource tree. Inputs and outputs all live in
 * {@code common/src/main/resources/assets/herbalcurative/}.
 */
public abstract class ProcessModelsTask extends DefaultTask {

    @Input
    public abstract Property<String> getCommonResourcesRoot();

    @TaskAction
    public void run() throws IOException {
        Path root = Paths.get(getCommonResourcesRoot().get());
        Path assets = root.resolve("assets").resolve("herbalcurative");

        Path modelsBlock = assets.resolve("models").resolve("block");
        Path modelsSplit = assets.resolve("models").resolve("split");
        Path blockstates = assets.resolve("blockstates");
        Path voxelshapes = assets.resolve("voxelshapes");

        getLogger().lifecycle("[processModels] cleaning block models in-place ...");
        new BlockModelProcessor(modelsBlock).run();

        getLogger().lifecycle("[processModels] splitting multiblocks -> " + modelsSplit);
        new ModelSplitProcessor(modelsBlock, modelsSplit).run();

        getLogger().lifecycle("[processModels] generating voxelshapes -> " + voxelshapes);
        new VoxelShapeProcessor(modelsBlock, voxelshapes).run();

        getLogger().lifecycle("[processModels] generating multiblock blockstates -> " + blockstates);
        new MultiblockStateProcessor(modelsBlock, blockstates).run();
    }
}
