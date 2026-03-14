package com.cahcap.neoforge.client.renderer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * NeoForge-specific wrapper for KilnRenderer.
 */
public class KilnRenderer extends com.cahcap.client.renderer.KilnRenderer {

    public KilnRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }
}
