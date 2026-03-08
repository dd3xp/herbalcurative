package com.cahcap.common.multiblock;

import com.cahcap.common.blockentity.HerbBasketBlockEntity;
import com.cahcap.common.blockentity.RedCherryShelfBlockEntity;
import com.cahcap.common.recipe.HerbalBlendingRecipe;
import com.cahcap.common.recipe.HerbalBlendingRecipe.IngredientWithCount;
import com.cahcap.common.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * Herbal Blending Rack Multiblock Structure (5x3)
 * 
 * Layout (wall-mounted view):
 * [Basket][Shelf0][Shelf1][Shelf2][Basket]   <- Top row
 * [Basket][Shelf3][Shelf4][Shelf5][Basket]   <- Middle row, Shelf4 is center
 * [Basket][Shelf6][Shelf7][Shelf8][Basket]   <- Bottom row
 * 
 * Shelf positions (like crafting table 3x3):
 * [0][1][2]
 * [3][4][5]  <- 4 is center, output replaces it
 * [6][7][8]
 * 
 * The center shelf (position 4) item is replaced with the output.
 * No transformation happens - the structure stays as individual blocks.
 */
public class MultiblockHerbalBlending {
    
    public static final MultiblockHerbalBlending INSTANCE = new MultiblockHerbalBlending();
    
    private static final int CENTER_SHELF_INDEX = 4;
    
    /**
     * Check if a block can trigger the multiblock check.
     */
    public boolean isBlockTrigger(BlockState state) {
        return state.is(ModRegistries.RED_CHERRY_SHELF.get());
    }
    
    /**
     * Attempt to find and validate the multiblock structure.
     * Returns the structure data if valid, null otherwise.
     */
    public BlendingStructure findStructure(Level level, BlockPos clickedPos, Direction side, Player player) {
        if (level.isClientSide) {
            return null;
        }
        
        // Get facing direction from the clicked face
        Direction facing;
        if (side.getAxis() == Direction.Axis.Y) {
            facing = player.getDirection().getOpposite();
        } else {
            facing = side;
        }
        
        Direction right = facing.getClockWise();
        
        // The clicked shelf could be any of the 9 shelves
        // We need to figure out which one and find the center
        // Try each possible position offset for the center shelf
        
        for (int testRow = -1; testRow <= 1; testRow++) {
            for (int testCol = -1; testCol <= 1; testCol++) {
                BlockPos potentialCenter = clickedPos
                        .relative(right, -testCol)
                        .relative(Direction.UP, -testRow);
                
                BlendingStructure structure = validateStructure(level, potentialCenter, facing, right);
                if (structure != null) {
                    return structure;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Validate the structure with the given center shelf position.
     * Shelves are stored in order: [0,1,2,3,4,5,6,7,8] where 4 is center
     */
    private BlendingStructure validateStructure(Level level, BlockPos centerShelf, Direction facing, Direction right) {
        List<HerbBasketBlockEntity> baskets = new ArrayList<>();
        // Use array to ensure correct ordering: 0-8, top-to-bottom, left-to-right
        RedCherryShelfBlockEntity[] shelfArray = new RedCherryShelfBlockEntity[9];
        
        // Calculate top-left corner (relative to wall-mounted view)
        BlockPos topLeft = centerShelf
                .relative(right.getOpposite())   // Move left
                .relative(Direction.UP);          // Move up
        
        // Temporary storage for left and right baskets
        HerbBasketBlockEntity[] leftBaskets = new HerbBasketBlockEntity[3];
        HerbBasketBlockEntity[] rightBaskets = new HerbBasketBlockEntity[3];
        
        // Validate structure - iterate from top to bottom
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 5; col++) {
                BlockPos checkPos = topLeft
                        .relative(Direction.DOWN, row)
                        .relative(right, col);
                
                BlockState state = level.getBlockState(checkPos);
                
                if (col == 0 || col == 4) {
                    // Should be a basket
                    if (!state.is(ModRegistries.HERB_BASKET.get())) {
                        return null;
                    }
                    if (level.getBlockEntity(checkPos) instanceof HerbBasketBlockEntity basket) {
                        if (col == 0) {
                            leftBaskets[row] = basket;
                        } else {
                            rightBaskets[row] = basket;
                        }
                    } else {
                        return null;
                    }
                } else {
                    // Should be a shelf
                    if (!state.is(ModRegistries.RED_CHERRY_SHELF.get())) {
                        return null;
                    }
                    if (level.getBlockEntity(checkPos) instanceof RedCherryShelfBlockEntity shelf) {
                        // Calculate shelf index with mirrored columns for player perspective
                        // col is 1,2,3 for shelves, mirror to 2,1,0 so player's left = recipe's left
                        int shelfIndex = row * 3 + (3 - col);
                        shelfArray[shelfIndex] = shelf;
                    } else {
                        return null;
                    }
                }
            }
        }
        
        // Verify all shelves were found
        for (int i = 0; i < 9; i++) {
            if (shelfArray[i] == null) {
                return null;
            }
        }
        
        // Verify all baskets were found
        for (int i = 0; i < 3; i++) {
            if (leftBaskets[i] == null || rightBaskets[i] == null) {
                return null;
            }
        }
        
        // Add baskets in order: right-top, right-middle, right-bottom, left-top, left-middle, left-bottom
        baskets.add(rightBaskets[0]); // Right top
        baskets.add(rightBaskets[1]); // Right middle
        baskets.add(rightBaskets[2]); // Right bottom
        baskets.add(leftBaskets[0]);  // Left top
        baskets.add(leftBaskets[1]);  // Left middle
        baskets.add(leftBaskets[2]);  // Left bottom
        
        List<RedCherryShelfBlockEntity> shelves = Arrays.asList(shelfArray);
        RedCherryShelfBlockEntity centerShelfEntity = shelfArray[CENTER_SHELF_INDEX];
        
        return new BlendingStructure(baskets, shelves, centerShelfEntity, centerShelf);
    }
    
    /**
     * Attempt to craft using the multiblock structure.
     * Returns true if crafting was successful.
     */
    public boolean tryCraft(Level level, BlendingStructure structure, Player player) {
        if (level.isClientSide) {
            return false;
        }
        
        // Create recipe input
        HerbalBlendingRecipe.BlendingInput input = new HerbalBlendingRecipe.BlendingInput(
                structure.baskets(),
                structure.shelves()
        );
        
        // Find matching recipe
        Optional<RecipeHolder<HerbalBlendingRecipe>> recipeHolder = level.getRecipeManager()
                .getRecipeFor(ModRegistries.HERBAL_BLENDING_RECIPE_TYPE.get(), input, level);
        
        if (recipeHolder.isEmpty()) {
            return false;
        }
        
        HerbalBlendingRecipe recipe = recipeHolder.get().value();
        
        // Consume inputs and place output (remainder items stay on shelves)
        if (!consumeInputsAndPlaceOutput(structure, recipe, level)) {
            return false;
        }
        
        // Play sound
        level.playSound(null, structure.centerPos(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        
        return true;
    }
    
    /**
     * Consume the required inputs from baskets and shelves, then place output on center shelf.
     * Remainder items (like empty buckets) are left on their original shelves.
     */
    private boolean consumeInputsAndPlaceOutput(BlendingStructure structure, HerbalBlendingRecipe recipe, Level level) {
        // First, calculate which herbs we need and from which baskets
        Map<HerbBasketBlockEntity, Integer> basketConsumption = new HashMap<>();
        
        for (IngredientWithCount iwc : recipe.getBasketInputs()) {
            boolean found = false;
            for (HerbBasketBlockEntity basket : structure.baskets()) {
                if (!basket.isBound() || basket.getHerbCount() <= 0) continue;
                
                ItemStack herbStack = new ItemStack(basket.getBoundHerb());
                if (iwc.ingredient().test(herbStack)) {
                    int currentConsumption = basketConsumption.getOrDefault(basket, 0);
                    int newConsumption = currentConsumption + iwc.count();
                    
                    if (newConsumption <= basket.getHerbCount()) {
                        basketConsumption.put(basket, newConsumption);
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                return false; // Not enough herbs
            }
        }
        
        // Consume herbs from baskets
        for (Map.Entry<HerbBasketBlockEntity, Integer> entry : basketConsumption.entrySet()) {
            entry.getKey().removeHerb(entry.getValue());
        }
        
        // Consume items from shelves (except center which will become output)
        // Remainder items stay on their original shelves
        NonNullList<Ingredient> pattern = recipe.getShelfPattern();
        List<RedCherryShelfBlockEntity> shelves = structure.shelves();
        
        for (int i = 0; i < 9; i++) {
            Ingredient required = pattern.get(i);
            RedCherryShelfBlockEntity shelf = shelves.get(i);
            
            if (i == CENTER_SHELF_INDEX) {
                // Center shelf: check for remainder, then replace with output
                ItemStack currentItem = shelf.getItem();
                if (!currentItem.isEmpty()) {
                    Item remainderItem = currentItem.getItem().getCraftingRemainingItem();
                    if (remainderItem != null) {
                        // Drop remainder item (center shelf needs to hold the output)
                        BlockPos shelfPos = shelf.getBlockPos();
                        ItemEntity remainderEntity = new ItemEntity(level,
                                shelfPos.getX() + 0.5, shelfPos.getY() + 0.5, shelfPos.getZ() + 0.5,
                                new ItemStack(remainderItem));
                        remainderEntity.setDeltaMovement(0, 0.1, 0);
                        level.addFreshEntity(remainderEntity);
                    }
                }
                // Replace with output
                ItemStack output = recipe.getOutput();
                shelf.setItem(output);
            } else {
                // Other shelves: consume if ingredient is not empty
                if (!required.isEmpty()) {
                    // Get crafting remainder and leave it on the shelf
                    ItemStack currentItem = shelf.getItem();
                    if (!currentItem.isEmpty()) {
                        Item remainderItem = currentItem.getItem().getCraftingRemainingItem();
                        if (remainderItem != null) {
                            // Replace with remainder item (e.g., empty bucket)
                            shelf.setItem(new ItemStack(remainderItem));
                        } else {
                            // No remainder, just remove
                            shelf.removeItem();
                        }
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * Data class holding the validated multiblock structure.
     */
    public record BlendingStructure(
            List<HerbBasketBlockEntity> baskets,
            List<RedCherryShelfBlockEntity> shelves,
            RedCherryShelfBlockEntity centerShelf,
            BlockPos centerPos
    ) {}
}
