package com.cahcap.common.compat.jade;

import com.cahcap.common.block.CauldronBlock;
import com.cahcap.common.block.HerbCabinetBlock;
import com.cahcap.common.block.KilnBlock;
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
        registration.registerBlockIcon(new HerbalCurativeIconProvider(), HerbCabinetBlock.class);
        
        // Register icon provider for Cauldron multiblock
        registration.registerBlockIcon(new CauldronIconProvider(), CauldronBlock.class);
        
        // Register icon and component provider for Kiln multiblock
        KilnComponentProvider kilnProvider = new KilnComponentProvider();
        registration.registerBlockIcon(kilnProvider, KilnBlock.class);
        registration.registerBlockComponent(kilnProvider, KilnBlock.class);
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        // Common registration (server-side data providers)
    }
}

