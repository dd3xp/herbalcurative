package com.cahcap.herbalcurative.neoforge.common.registry;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public class ModArmorMaterials {
    
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = 
            DeferredRegister.create(Registries.ARMOR_MATERIAL, HerbalCurativeCommon.MOD_ID);
    
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> LEAFWEAVE = ARMOR_MATERIALS.register("leafweave",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 1);
                        map.put(ArmorItem.Type.LEGGINGS, 4);
                        map.put(ArmorItem.Type.CHESTPLATE, 6);
                        map.put(ArmorItem.Type.HELMET, 1);
                        map.put(ArmorItem.Type.BODY, 6);
                    }),
                    15, // enchantability (higher than diamond's 10)
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    () -> Ingredient.of(ModItems.SCALEPLATE.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "leafweave"))),
                    0.5F, // toughness
                    0.0F  // knockback resistance
            ));
    
    public static void register(IEventBus eventBus) {
        ARMOR_MATERIALS.register(eventBus);
    }
}

