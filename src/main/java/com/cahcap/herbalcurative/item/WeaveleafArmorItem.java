package com.cahcap.herbalcurative.item;

import com.cahcap.herbalcurative.HerbalCurative;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class WeaveleafArmorItem extends ArmorItem {
    
    private static final ResourceLocation ARMOR_TEXTURE = 
            ResourceLocation.fromNamespaceAndPath(HerbalCurative.MODID, "textures/models/armor/weaveleaf_armor.png");
    
    public WeaveleafArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }
    
    @Override
    public @Nullable ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return ARMOR_TEXTURE;
    }
}
