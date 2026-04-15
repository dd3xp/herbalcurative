package com.cahcap.neoforge.client.model;

import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

/**
 * {@link IUnbakedGeometry} wrapper that stores the raw JSON and a {@link BakeStrategy},
 * deferring the actual work to the strategy. Each registered loader constructs this with
 * a different strategy, giving a single surface for NeoForge while keeping the behavior
 * modular.
 */
public final class CustomUnbakedGeometry implements IUnbakedGeometry<CustomUnbakedGeometry> {

    private final JsonObject rawModel;
    private final BakeStrategy strategy;

    public CustomUnbakedGeometry(JsonObject rawModel, BakeStrategy strategy) {
        this.rawModel = rawModel;
        this.strategy = strategy;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext ctx,
                           ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter,
                           ModelState modelState,
                           ItemOverrides overrides) {
        return strategy.bake(rawModel, ctx, baker, spriteGetter, modelState, overrides);
    }
}
