package com.cahcap.neoforge.client.model;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

/**
 * Entry point for custom-shape block model loaders. Registers two NeoForge geometry loaders
 * whose {@code "loader"} key picks the {@link BakeStrategy} used:
 * <ul>
 *     <li>{@code herbalcurative:cleanup} — single-cell, just strips {@code rotation:{angle:0}}.</li>
 *     <li>{@code herbalcurative:split} — multi-cell, cleanup + per-cell clip/mirror + state dispatch.</li>
 * </ul>
 */
public final class CustomModelLoaders {

    public static final ResourceLocation CLEANUP = ResourceLocation.fromNamespaceAndPath("herbalcurative", "cleanup");
    public static final ResourceLocation SPLIT = ResourceLocation.fromNamespaceAndPath("herbalcurative", "split");

    private CustomModelLoaders() {}

    public static void register(ModelEvent.RegisterGeometryLoaders event) {
        IGeometryLoader<CustomUnbakedGeometry> cleanup =
                (json, ctx) -> new CustomUnbakedGeometry(json, SingleBlockStrategy.INSTANCE);
        IGeometryLoader<CustomUnbakedGeometry> split =
                (json, ctx) -> new CustomUnbakedGeometry(json, MultiblockStrategy.INSTANCE);
        event.register(CLEANUP, cleanup);
        event.register(SPLIT, split);
    }
}
