package com.cahcap.common.compat.jade;

import com.cahcap.common.block.CauldronBlock;
import com.cahcap.common.block.HerbCabinetBlock;
import com.cahcap.common.block.HerbVaultBlock;
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
        // Register component providers for multiblock structures (icon only)
        registration.registerBlockIcon(new HerbCabinetComponentProvider(), HerbCabinetBlock.class);
        registration.registerBlockIcon(new CauldronComponentProvider(), CauldronBlock.class);
        registration.registerBlockIcon(new KilnComponentProvider(), KilnBlock.class);
        registration.registerBlockIcon(new HerbVaultComponentProvider(), HerbVaultBlock.class);
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        // Common registration (server-side data providers)
    }
}

