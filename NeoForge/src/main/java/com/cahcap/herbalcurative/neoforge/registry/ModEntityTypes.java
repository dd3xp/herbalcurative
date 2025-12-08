package com.cahcap.herbalcurative.neoforge.registry;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntityTypes {
    
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = 
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, HerbalCurativeCommon.MOD_ID);
}

