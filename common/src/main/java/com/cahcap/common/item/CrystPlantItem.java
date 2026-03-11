package com.cahcap.common.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Crystal Plant seedling item that holds ore type and color information.
 * Each plant type corresponds to a specific ore and has a unique color.
 * The color is stored in the item itself for tintindex rendering.
 * 
 * Uses a single grayscale texture (cryst_plant) that gets tinted to
 * the color of the ore it produces (iron = silver, coal = black, etc.)
 */
public class CrystPlantItem extends Item {
    
    private final String oreType;
    private final int color;
    
    public CrystPlantItem(Properties properties, String oreType, int color) {
        super(properties);
        this.oreType = oreType;
        this.color = color;
    }
    
    /**
     * Get the ore type this plant produces.
     */
    public String getOreType() {
        return oreType;
    }
    
    /**
     * Get the color for this plant.
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
