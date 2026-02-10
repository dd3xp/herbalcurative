package com.cahcap.herbalcurative.neoforge.common.registry;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.common.entity.FlowweaveProjectile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntityTypes {
    
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = 
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, HerbalCurativeCommon.MOD_ID);
    
    public static final DeferredHolder<EntityType<?>, EntityType<FlowweaveProjectile>> FLOWWEAVE_PROJECTILE =
            ENTITY_TYPES.register("flowweave_projectile", () -> 
                    EntityType.Builder.<FlowweaveProjectile>of(FlowweaveProjectile::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("flowweave_projectile")
            );
}

