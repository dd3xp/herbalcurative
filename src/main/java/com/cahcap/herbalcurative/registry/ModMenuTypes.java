package com.cahcap.herbalcurative.registry;

import com.cahcap.herbalcurative.HerbalCurative;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = 
            DeferredRegister.create(BuiltInRegistries.MENU, HerbalCurative.MODID);

}
