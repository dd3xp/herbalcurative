package com.cahcap.neoforge.common.registry;

import com.cahcap.HerbalCurativeCommon;
import com.cahcap.common.blockentity.CauldronBlockEntity;
import com.cahcap.common.blockentity.HerbBasketBlockEntity;
import com.cahcap.common.blockentity.HerbCabinetBlockEntity;
import com.cahcap.common.blockentity.HerbPotBlockEntity;
import com.cahcap.common.blockentity.IncenseBurnerBlockEntity;
import com.cahcap.common.blockentity.HerbVaultBlockEntity;
import com.cahcap.common.blockentity.KilnBlockEntity;
import com.cahcap.common.blockentity.RedCherryShelfBlockEntity;
import com.cahcap.common.blockentity.WorkbenchBlockEntity;
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
    
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WorkbenchBlockEntity>> WORKBENCH =
            BLOCK_ENTITIES.register("workbench", () -> BlockEntityType.Builder.of(
                    WorkbenchBlockEntity::new,
                    ModBlocks.WORKBENCH.get()
            ).build(null));
    
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CauldronBlockEntity>> CAULDRON =
            BLOCK_ENTITIES.register("cauldron", () -> BlockEntityType.Builder.of(
                    CauldronBlockEntity::new,
                    ModBlocks.CAULDRON.get()
            ).build(null));
    
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HerbPotBlockEntity>> HERB_POT =
            BLOCK_ENTITIES.register("herb_pot", () -> BlockEntityType.Builder.of(
                    HerbPotBlockEntity::new,
                    ModBlocks.HERB_POT.get()
            ).build(null));
    
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IncenseBurnerBlockEntity>> INCENSE_BURNER =
            BLOCK_ENTITIES.register("incense_burner", () -> BlockEntityType.Builder.of(
                    IncenseBurnerBlockEntity::new,
                    ModBlocks.INCENSE_BURNER.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<KilnBlockEntity>> KILN =
            BLOCK_ENTITIES.register("kiln", () -> BlockEntityType.Builder.of(
                    KilnBlockEntity::new,
                    ModBlocks.KILN.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HerbVaultBlockEntity>> HERB_VAULT =
            BLOCK_ENTITIES.register("herb_vault", () -> BlockEntityType.Builder.of(
                    HerbVaultBlockEntity::new,
                    ModBlocks.HERB_VAULT.get()
            ).build(null));
}

