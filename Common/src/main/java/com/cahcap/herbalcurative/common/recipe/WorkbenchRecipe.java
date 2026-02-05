package com.cahcap.herbalcurative.common.recipe;

import com.cahcap.herbalcurative.common.blockentity.WorkbenchBlockEntity;
import com.cahcap.herbalcurative.common.registry.ModRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Recipe for the Workbench.
 * 
 * Tool slots: 0=top-left, 1=top-right, 2=bot-left, 3=bot-right
 * Materials: Must match exactly (types only, not amounts)
 * Input: The item in the center slot
 * Result: What the input transforms into
 */
public class WorkbenchRecipe implements Recipe<WorkbenchRecipe.WorkbenchInput> {
    
    private final List<ToolRequirement> tools;
    private final Ingredient input;
    private final List<MaterialRequirement> materials;
    private final ItemStack result;
    private final int experienceCost;
    
    public WorkbenchRecipe(List<ToolRequirement> tools, Ingredient input, 
                           List<MaterialRequirement> materials, ItemStack result) {
        this(tools, input, materials, result, 0);
    }
    
    public WorkbenchRecipe(List<ToolRequirement> tools, Ingredient input, 
                           List<MaterialRequirement> materials, ItemStack result, int experienceCost) {
        this.tools = tools;
        this.input = input;
        this.materials = materials;
        this.result = result;
        this.experienceCost = experienceCost;
    }
    
    @Override
    public boolean matches(WorkbenchInput container, Level level) {
        // Check input item
        if (!input.test(container.getInput())) {
            return false;
        }
        
        // Check tools - can be in any slot
        for (ToolRequirement tool : tools) {
            boolean found = false;
            for (int i = 0; i < 4; i++) {
                ItemStack toolStack = container.getTool(i);
                if (!toolStack.isEmpty() && toolStack.is(tool.item())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        
        // Merge container materials by item type
        Map<Item, Integer> availableMaterials = mergeContainerMaterials(container.getMaterials());
        
        // Check materials - types must match exactly (no more, no less)
        // Build required materials map
        Map<Item, Integer> requiredMaterials = new java.util.HashMap<>();
        for (MaterialRequirement req : materials) {
            requiredMaterials.merge(req.item(), req.count(), Integer::sum);
        }
        
        // Check if types match exactly
        if (!availableMaterials.keySet().equals(requiredMaterials.keySet())) {
            return false;
        }
        
        // Check if we have enough of each material
        for (Map.Entry<Item, Integer> req : requiredMaterials.entrySet()) {
            Integer available = availableMaterials.get(req.getKey());
            if (available == null || available < req.getValue()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Merge container materials by item type.
     */
    private Map<Item, Integer> mergeContainerMaterials(List<ItemStack> containerMaterials) {
        Map<Item, Integer> merged = new java.util.HashMap<>();
        for (ItemStack stack : containerMaterials) {
            if (!stack.isEmpty()) {
                merged.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }
        return merged;
    }
    
    /**
     * Calculate how many times this recipe can be crafted with current materials.
     */
    public int getMaxCraftCount(WorkbenchInput container) {
        if (!matches(container, null)) {
            return 0;
        }
        
        // Merge container materials by item type
        Map<Item, Integer> availableMaterials = mergeContainerMaterials(container.getMaterials());
        
        // Build required materials map
        Map<Item, Integer> requiredMaterials = new java.util.HashMap<>();
        for (MaterialRequirement req : materials) {
            requiredMaterials.merge(req.item(), req.count(), Integer::sum);
        }
        
        int maxCount = Integer.MAX_VALUE;
        
        for (Map.Entry<Item, Integer> req : requiredMaterials.entrySet()) {
            int available = availableMaterials.getOrDefault(req.getKey(), 0);
            int canMake = available / req.getValue();
            maxCount = Math.min(maxCount, canMake);
        }
        
        // Also limited by input count
        maxCount = Math.min(maxCount, container.getInput().getCount());
        
        return maxCount;
    }
    
    @Override
    public ItemStack assemble(WorkbenchInput container, HolderLookup.Provider registries) {
        return result.copy();
    }
    
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }
    
    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }
    
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRegistries.WORKBENCH_RECIPE_SERIALIZER.get();
    }
    
    @Override
    public RecipeType<?> getType() {
        return ModRegistries.WORKBENCH_RECIPE_TYPE.get();
    }
    
    public List<ToolRequirement> getTools() {
        return tools;
    }
    
    public Ingredient getInput() {
        return input;
    }
    
    public List<MaterialRequirement> getMaterials() {
        return materials;
    }
    
    public ItemStack getResult() {
        return result.copy();
    }
    
    public int getExperienceCost() {
        return experienceCost;
    }
    
    // ==================== Nested Types ====================
    
    public record ToolRequirement(Item item, int damage) {
        public static final Codec<ToolRequirement> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(ToolRequirement::item),
                Codec.INT.optionalFieldOf("damage", 1).forGetter(ToolRequirement::damage)
            ).apply(instance, ToolRequirement::new)
        );
        
        public static final StreamCodec<RegistryFriendlyByteBuf, ToolRequirement> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(BuiltInRegistries.ITEM.key()), ToolRequirement::item,
            ByteBufCodecs.INT, ToolRequirement::damage,
            ToolRequirement::new
        );
    }
    
    public record MaterialRequirement(Item item, int count) {
        public static final Codec<MaterialRequirement> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(MaterialRequirement::item),
                Codec.INT.optionalFieldOf("count", 1).forGetter(MaterialRequirement::count)
            ).apply(instance, MaterialRequirement::new)
        );
        
        public static final StreamCodec<RegistryFriendlyByteBuf, MaterialRequirement> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(BuiltInRegistries.ITEM.key()), MaterialRequirement::item,
            ByteBufCodecs.INT, MaterialRequirement::count,
            MaterialRequirement::new
        );
    }
    
    // ==================== Input ====================
    
    /**
     * Input container for WorkbenchRecipe matching.
     */
    public static class WorkbenchInput implements RecipeInput {
        
        private final ItemStack[] tools;
        private final ItemStack input;
        private final List<ItemStack> materials;
        
        public WorkbenchInput(WorkbenchBlockEntity workbench) {
            this.tools = new ItemStack[4];
            for (int i = 0; i < 4; i++) {
                this.tools[i] = workbench.getToolAt(i);
            }
            this.input = workbench.getInputItem();
            this.materials = workbench.getMaterials();
        }
        
        public ItemStack getTool(int slot) {
            if (slot < 0 || slot >= 4) return ItemStack.EMPTY;
            return tools[slot];
        }
        
        public ItemStack getInput() {
            return input;
        }
        
        public List<ItemStack> getMaterials() {
            return materials;
        }
        
        @Override
        public ItemStack getItem(int slot) {
            if (slot < 4) {
                return tools[slot];
            } else if (slot == 4) {
                return input;
            } else if (slot - 5 < materials.size()) {
                return materials.get(slot - 5);
            }
            return ItemStack.EMPTY;
        }
        
        @Override
        public int size() {
            return 4 + 1 + materials.size();
        }
    }
    
    // ==================== Serializer ====================
    
    public static class Serializer implements RecipeSerializer<WorkbenchRecipe> {
        
        public static final MapCodec<WorkbenchRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                ToolRequirement.CODEC.listOf().fieldOf("tools").forGetter(WorkbenchRecipe::getTools),
                Ingredient.CODEC.fieldOf("input").forGetter(WorkbenchRecipe::getInput),
                MaterialRequirement.CODEC.listOf().fieldOf("materials").forGetter(WorkbenchRecipe::getMaterials),
                ItemStack.CODEC.fieldOf("result").forGetter(r -> r.result),
                Codec.INT.optionalFieldOf("experience_cost", 0).forGetter(WorkbenchRecipe::getExperienceCost)
            ).apply(instance, WorkbenchRecipe::new)
        );
        
        public static final StreamCodec<RegistryFriendlyByteBuf, WorkbenchRecipe> STREAM_CODEC = StreamCodec.composite(
            ToolRequirement.STREAM_CODEC.apply(ByteBufCodecs.list()), WorkbenchRecipe::getTools,
            Ingredient.CONTENTS_STREAM_CODEC, WorkbenchRecipe::getInput,
            MaterialRequirement.STREAM_CODEC.apply(ByteBufCodecs.list()), WorkbenchRecipe::getMaterials,
            ItemStack.STREAM_CODEC, r -> r.result,
            ByteBufCodecs.INT, WorkbenchRecipe::getExperienceCost,
            WorkbenchRecipe::new
        );
        
        @Override
        public MapCodec<WorkbenchRecipe> codec() {
            return CODEC;
        }
        
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, WorkbenchRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
