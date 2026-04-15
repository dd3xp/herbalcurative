package com.cahcap.neoforge.client.model.split;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

public final class SplitGeometryLoader implements IGeometryLoader<SplitUnbakedGeometry> {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("herbalcurative", "split");
    public static final SplitGeometryLoader INSTANCE = new SplitGeometryLoader();

    private SplitGeometryLoader() {}

    @Override
    public SplitUnbakedGeometry read(JsonObject modelContents, JsonDeserializationContext ctx) {
        return new SplitUnbakedGeometry(modelContents);
    }
}
