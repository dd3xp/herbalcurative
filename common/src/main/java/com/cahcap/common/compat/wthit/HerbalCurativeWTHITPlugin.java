package com.cahcap.common.compat.wthit;

import com.cahcap.common.block.CauldronBlock;
import com.cahcap.common.block.HerbCabinetBlock;
import com.cahcap.common.block.HerbVaultBlock;
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
        // Register component providers for multiblock structures (icon only)
        registrar.icon(HerbCabinetComponentProvider.INSTANCE, HerbCabinetBlock.class);
        registrar.icon(CauldronComponentProvider.INSTANCE, CauldronBlock.class);
        registrar.icon(KilnComponentProvider.INSTANCE, KilnBlock.class);
        registrar.icon(HerbVaultComponentProvider.INSTANCE, HerbVaultBlock.class);
    }

    @Override
    public void register(ICommonRegistrar registrar) {
        // Common registration (server-side data providers)
    }
}
