package com.cahcap.herbalcurative.compat.wthit;

import com.cahcap.herbalcurative.block.HerbCabinetBlock;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.ICommonRegistrar;
import mcp.mobius.waila.api.IWailaClientPlugin;
import mcp.mobius.waila.api.IWailaCommonPlugin;

/**
 * WTHIT (What The Hell Is That) plugin for Herbal Curative.
 * Provides custom multiblock structure icon display.
 */
public class HerbalCurativeWTHITPlugin implements IWailaClientPlugin, IWailaCommonPlugin {

    @Override
    public void register(IClientRegistrar registrar) {
        // Register icon provider for Herb Cabinet multiblock
        registrar.icon(HerbalCurativeIconProvider.INSTANCE, HerbCabinetBlock.class);
    }

    @Override
    public void register(ICommonRegistrar registrar) {
        // Common registration (server-side data providers)
    }
}
