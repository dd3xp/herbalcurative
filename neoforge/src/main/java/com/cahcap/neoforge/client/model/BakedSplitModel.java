package com.cahcap.neoforge.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Dispatches pre-baked per-cell quads based on the block's {@code position} / {@code mirrored}
 * state properties. When queried without a state (item rendering), returns the full unclipped
 * model's quads.
 * <p>
 * The {@code formed} check is NOT performed here — the blockstate JSON redirects
 * {@code formed=false} variants to a placeholder block model before reaching this bake result.
 */
public final class BakedSplitModel implements BakedModel {

    // [mirror][position][side(0-5) or 6 for null]
    private final List<BakedQuad>[][][] quadsByMirrorByPosBySide;
    private final List<BakedQuad>[] itemQuadsBySide;
    private final ItemTransforms transforms;
    private final TextureAtlasSprite particle;
    private final int totalPositions;
    private final boolean ambientOcclusion;
    private final boolean gui3d;
    private final boolean usesBlockLight;
    private final ItemOverrides overrides;

    public BakedSplitModel(List<BakedQuad>[][][] quadsByMirrorByPosBySide,
                           List<BakedQuad>[] itemQuadsBySide,
                           ItemTransforms transforms,
                           TextureAtlasSprite particle,
                           int totalPositions,
                           boolean ambientOcclusion,
                           boolean gui3d,
                           boolean usesBlockLight,
                           ItemOverrides overrides) {
        this.quadsByMirrorByPosBySide = quadsByMirrorByPosBySide;
        this.itemQuadsBySide = itemQuadsBySide;
        this.transforms = transforms;
        this.particle = particle;
        this.totalPositions = totalPositions;
        this.ambientOcclusion = ambientOcclusion;
        this.gui3d = gui3d;
        this.usesBlockLight = usesBlockLight;
        this.overrides = overrides;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        int sideIdx = side == null ? 6 : side.ordinal();
        if (state == null) {
            return itemQuadsBySide[sideIdx];
        }

        int position = readPosition(state);
        if (position < 0 || position >= totalPositions) {
            return ImmutableList.of();
        }
        int mirror = readMirror(state) ? 1 : 0;
        List<BakedQuad> q = quadsByMirrorByPosBySide[mirror][position][sideIdx];
        return q == null ? ImmutableList.of() : q;
    }

    private static int readPosition(BlockState state) {
        Property<?> prop = state.getBlock().getStateDefinition().getProperty("position");
        if (prop instanceof IntegerProperty ip) {
            return state.getValue(ip);
        }
        return 0;
    }

    private static boolean readMirror(BlockState state) {
        Property<?> prop = state.getBlock().getStateDefinition().getProperty("mirrored");
        if (prop instanceof BooleanProperty bp) {
            return state.getValue(bp);
        }
        return false;
    }

    @Override public boolean useAmbientOcclusion() { return ambientOcclusion; }
    @Override public boolean isGui3d() { return gui3d; }
    @Override public boolean usesBlockLight() { return usesBlockLight; }
    @Override public boolean isCustomRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleIcon() { return particle; }
    @Override public ItemTransforms getTransforms() { return transforms; }
    @Override public ItemOverrides getOverrides() { return overrides; }
}
