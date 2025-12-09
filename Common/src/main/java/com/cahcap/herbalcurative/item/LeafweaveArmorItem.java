package com.cahcap.herbalcurative.item;

import com.cahcap.herbalcurative.HerbalCurativeCommon;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

public class LeafweaveArmorItem extends ArmorItem {
    
    public static final ResourceLocation ARMOR_TEXTURE = 
            ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "textures/models/armor/leafweave_armor.png");
    
    public LeafweaveArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }
    
    /**
     * Returns the armor texture location.
     * This is used by NeoForge's IClientItemExtensions or getArmorTexture override.
     */
    public ResourceLocation getArmorTextureLocation() {
        return ARMOR_TEXTURE;
    }
}

