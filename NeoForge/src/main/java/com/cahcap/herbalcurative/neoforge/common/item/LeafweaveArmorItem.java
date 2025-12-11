package com.cahcap.herbalcurative.neoforge.common.item;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge-specific LeafweaveArmorItem with getArmorTexture override
 */
public class LeafweaveArmorItem extends com.cahcap.herbalcurative.common.item.LeafweaveArmorItem {
    
    public LeafweaveArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }
    
    @Override
    public @Nullable ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return getArmorTextureLocation();
    }
}
