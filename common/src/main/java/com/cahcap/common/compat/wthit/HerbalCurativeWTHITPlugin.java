package com.cahcap.common.compat.wthit;

import com.cahcap.common.block.CauldronBlock;
import com.cahcap.common.block.HerbCabinetBlock;
import com.cahcap.common.block.KilnBlock;
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
        
        // Register icon provider for Cauldron multiblock
        registrar.icon(CauldronIconProvider.INSTANCE, CauldronBlock.class);
        
        // Register icon and component provider for Kiln multiblock
        registrar.icon(KilnComponentProvider.INSTANCE, KilnBlock.class);
        registrar.body(KilnComponentProvider.INSTANCE, KilnBlock.class);
    }

    @Override
    public void register(ICommonRegistrar registrar) {
        // Common registration (server-side data providers)
    }
}
