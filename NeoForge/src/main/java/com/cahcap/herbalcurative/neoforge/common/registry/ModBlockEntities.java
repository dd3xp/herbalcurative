package com.cahcap.herbalcurative.neoforge.common.registry;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import com.cahcap.herbalcurative.common.blockentity.HerbBasketBlockEntity;
import com.cahcap.herbalcurative.common.blockentity.HerbCabinetBlockEntity;
import com.cahcap.herbalcurative.common.blockentity.RedCherryShelfBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, HerbalCurativeCommon.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HerbCabinetBlockEntity>> HERB_CABINET =
            BLOCK_ENTITIES.register("herb_cabinet", () -> BlockEntityType.Builder.of(
                    HerbCabinetBlockEntity::new,
                    ModBlocks.HERB_CABINET.get()
            ).build(null));
    
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HerbBasketBlockEntity>> HERB_BASKET =
            BLOCK_ENTITIES.register("herb_basket", () -> BlockEntityType.Builder.of(
                    HerbBasketBlockEntity::new,
                    ModBlocks.HERB_BASKET.get()
            ).build(null));
    
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RedCherryShelfBlockEntity>> RED_CHERRY_SHELF =
            BLOCK_ENTITIES.register("red_cherry_shelf", () -> BlockEntityType.Builder.of(
                    RedCherryShelfBlockEntity::new,
                    ModBlocks.RED_CHERRY_SHELF.get()
            ).build(null));
}

