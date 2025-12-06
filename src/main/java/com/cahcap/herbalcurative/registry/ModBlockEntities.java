package com.cahcap.herbalcurative.registry;

import com.cahcap.herbalcurative.HerbalCurative;
import com.cahcap.herbalcurative.blockentity.HerbCabinetBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, HerbalCurative.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HerbCabinetBlockEntity>> HERB_CABINET =
            BLOCK_ENTITIES.register("herb_cabinet", () -> BlockEntityType.Builder.of(
                    HerbCabinetBlockEntity::new,
                    ModBlocks.HERB_CABINET.get()
            ).build(null));
}
