package com.cahcap.herbalcurative.common.recipe;

import com.cahcap.herbalcurative.common.blockentity.HerbBasketBlockEntity;
import com.cahcap.herbalcurative.common.blockentity.RedCherryShelfBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Recipe for the Herbal Blending Rack multiblock structure.
 * 
 * The multiblock consists of:
 * - 6 Herb Baskets (left and right columns) for herb inputs
 * - 9 Red Cherry Shelves (center 3x3) for item inputs (position-sensitive like crafting table)
 * 
 * Layout (wall-mounted view):
 * [Basket][Shelf0][Shelf1][Shelf2][Basket]
 * [Basket][Shelf3][Shelf4][Shelf5][Basket]  <- Shelf4 is center, output replaces it
 * [Basket][Shelf6][Shelf7][Shelf8][Basket]
 * 
 * Shelf positions (like crafting table):
 * [0][1][2]
 * [3][4][5]  <- 4 is center
 * [6][7][8]
 * 
 * Output replaces the item on center shelf (position 4).
 */
public class HerbalBlendingRecipe implements Recipe<HerbalBlendingRecipe.BlendingInput> {
    
    private final List<IngredientWithCount> basketInputs;
    private final NonNullList<Ingredient> shelfPattern; // 9 slots, position-sensitive
    private final ItemStack output;
    
    public HerbalBlendingRecipe(List<IngredientWithCount> basketInputs, NonNullList<Ingredient> shelfPattern, ItemStack output) {
        this.basketInputs = basketInputs;
        this.shelfPattern = shelfPattern;
        this.output = output;
    }
    
    public List<IngredientWithCount> getBasketInputs() {
        return basketInputs;
    }
    
    public NonNullList<Ingredient> getShelfPattern() {
        return shelfPattern;
    }
    
    public ItemStack getOutput() {
        return output.copy();
    }
    
    @Override
    public boolean matches(BlendingInput input, Level level) {
        // Check basket inputs
        Map<Item, Integer> availableHerbs = new HashMap<>();
        for (HerbBasketBlockEntity basket : input.baskets()) {
            if (basket.isBound() && basket.getHerbCount() > 0) {
                Item herb = basket.getBoundHerb();
                availableHerbs.merge(herb, basket.getHerbCount(), Integer::sum);
            }
        }
        
        // Check if we have enough herbs for all basket inputs
        Map<Item, Integer> requiredHerbs = new HashMap<>();
        for (IngredientWithCount ingredientWithCount : basketInputs) {
            boolean matched = false;
            // Find which herb this ingredient matches
            for (Map.Entry<Item, Integer> entry : availableHerbs.entrySet()) {
                if (ingredientWithCount.ingredient().test(new ItemStack(entry.getKey()))) {
                    requiredHerbs.merge(entry.getKey(), ingredientWithCount.count(), Integer::sum);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false; // No matching herb found
            }
        }
        
        // Verify we have enough of each required herb
        for (Map.Entry<Item, Integer> required : requiredHerbs.entrySet()) {
            Integer available = availableHerbs.get(required.getKey());
            if (available == null || available < required.getValue()) {
                return false;
            }
        }
        
        // Check shelf pattern - position-sensitive matching
        List<RedCherryShelfBlockEntity> shelves = input.shelves();
        if (shelves.size() != 9) {
            return false;
        }
        
        for (int i = 0; i < 9; i++) {
            Ingredient required = shelfPattern.get(i);
            ItemStack actual = shelves.get(i).hasItem() ? shelves.get(i).getItem() : ItemStack.EMPTY;
            
            if (!required.test(actual)) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public ItemStack assemble(BlendingInput input, HolderLookup.Provider registries) {
        return output.copy();
    }
    
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }
    
    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.copy();
    }
    
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializerHolder.HERBAL_BLENDING.get();
    }
    
    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypeHolder.HERBAL_BLENDING.get();
    }
    
    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (IngredientWithCount iwc : basketInputs) {
            ingredients.add(iwc.ingredient());
        }
        ingredients.addAll(shelfPattern);
        return ingredients;
    }
    
    /**
     * Ingredient with a count (for herbs in baskets)
     */
    public record IngredientWithCount(Ingredient ingredient, int count) {
        public static IngredientWithCount of(Ingredient ingredient, int count) {
            return new IngredientWithCount(ingredient, count);
        }
    }
    
    /**
     * Input container for the blending recipe
     */
    public record BlendingInput(List<HerbBasketBlockEntity> baskets, List<RedCherryShelfBlockEntity> shelves) implements RecipeInput {
        @Override
        public ItemStack getItem(int index) {
            // First 6 slots are baskets (return empty for now, as they hold counts not stacks)
            if (index < 6) {
                HerbBasketBlockEntity basket = baskets.get(index);
                if (basket.isBound() && basket.getHerbCount() > 0) {
                    return new ItemStack(basket.getBoundHerb(), basket.getHerbCount());
                }
                return ItemStack.EMPTY;
            }
            // Next 9 slots are shelves (position 0-8)
            int shelfIndex = index - 6;
            if (shelfIndex < shelves.size()) {
                return shelves.get(shelfIndex).getItem();
            }
            return ItemStack.EMPTY;
        }
        
        @Override
        public int size() {
            return 15; // 6 baskets + 9 shelves
        }
    }
}
