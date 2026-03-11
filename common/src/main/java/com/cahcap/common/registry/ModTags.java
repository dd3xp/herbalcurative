package com.cahcap.common.registry;

import com.cahcap.HerbalCurativeCommon;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * Custom tags for the mod.
 */
public class ModTags {
    
    public static class Blocks {
        /**
         * Blocks that act as heat sources for cauldrons and incense burners.
         * This tag can be extended via datapacks.
         */
        public static final TagKey<Block> HEAT_SOURCES = TagKey.create(
                Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(HerbalCurativeCommon.MOD_ID, "heat_sources")
        );
    }
}
