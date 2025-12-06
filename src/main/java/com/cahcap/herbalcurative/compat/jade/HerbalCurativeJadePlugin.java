package com.cahcap.herbalcurative.compat.jade;

import com.cahcap.herbalcurative.block.HerbCabinetBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade compatibility plugin for Herbal Curative.
 * Provides custom multiblock structure icon display.
 */
@WailaPlugin
public class HerbalCurativeJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // Register icon provider for Herb Cabinet multiblock
        registration.registerBlockIcon(new HerbCabinetIconProvider(), HerbCabinetBlock.class);
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        // Common registration (server-side data providers)
    }
}
