package com.cahcap.common.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Incense Powder item that holds entity type and color information.
 * Each powder type corresponds to a specific mob and has a unique color.
 * The color is stored in the item itself for tintindex rendering.
 */
public class IncensePowderItem extends Item {
    
    private final ResourceLocation entityTypeId;
    private final int color;
    
    public IncensePowderItem(Properties properties, ResourceLocation entityTypeId, int color) {
        super(properties);
        this.entityTypeId = entityTypeId;
        this.color = color;
    }
    
    /**
     * Get the entity type this powder summons.
     */
    public ResourceLocation getEntityTypeId() {
        return entityTypeId;
    }
    
    /**
     * Get the entity type.
     */
    public EntityType<?> getEntityType() {
        return BuiltInRegistries.ENTITY_TYPE.get(entityTypeId);
    }
    
    /**
     * Get the color for this powder.
     * Used for ItemColor tinting and renderer coloring.
     */
    public int getColor(ItemStack stack) {
        return color;
    }
    
    /**
     * Get the raw color value.
     */
    public int getColor() {
        return color;
    }
}
