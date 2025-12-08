package com.cahcap.herbalcurative.neoforge.registry;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = 
            DeferredRegister.create(BuiltInRegistries.MENU, HerbalCurativeCommon.MOD_ID);
}

