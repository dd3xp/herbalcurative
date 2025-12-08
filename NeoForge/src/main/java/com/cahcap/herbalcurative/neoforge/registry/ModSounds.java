package com.cahcap.herbalcurative.neoforge.registry;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    
    public static final DeferredRegister<SoundEvent> SOUNDS = 
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, HerbalCurativeCommon.MOD_ID);
}

