package com.cahcap.neoforge.compat.jei;

import com.cahcap.common.recipe.HerbalBlendingRecipe;
import com.cahcap.common.recipe.HerbalBlendingRecipe.IngredientWithCount;
import com.cahcap.common.registry.ModRegistries;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

public class HerbalBlendingCategory implements IRecipeCategory<HerbalBlendingRecipe> {

    public static final RecipeType<HerbalBlendingRecipe> RECIPE_TYPE =
            RecipeType.create("herbalcurative", "herbal_blending", HerbalBlendingRecipe.class);

    private static final int WIDTH = 144;
    private static final int HEIGHT = 62;

    // Basket inputs: 2 cols x 3 rows, item area 16x16 with 1px gutter -> slot stride 18
    private static final int BASKET_X = 4;
    private static final int BASKET_Y = 4;

    // Shelf 3x3 grid
    private static final int SHELF_X = 44;
    private static final int SHELF_Y = 4;

    // Vanilla furnace progress arrow
    private static final ResourceLocation FURNACE_TEX =
            ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final int ARROW_X = 100;
    private static final int ARROW_Y = 23;

    // Output slot
    private static final int OUTPUT_X = 124;
    private static final int OUTPUT_Y = 22;

    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;

    public HerbalBlendingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModRegistries.SHELF_ITEM.get()));
        this.slot = guiHelper.getSlotDrawable();
        this.arrow = guiHelper.createDrawable(FURNACE_TEX, 79, 34, 24, 17);
    }

    @Override
    public RecipeType<HerbalBlendingRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.herbalcurative.category.herbal_blending");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(HerbalBlendingRecipe recipe, IRecipeSlotsView view, GuiGraphics g, double mx, double my) {
        arrow.draw(g, ARROW_X, ARROW_Y);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, HerbalBlendingRecipe recipe, IFocusGroup focuses) {
        // Basket inputs (up to 6, 2 cols x 3 rows)
        List<IngredientWithCount> baskets = recipe.getBasketInputs();
        for (int i = 0; i < Math.min(baskets.size(), 6); i++) {
            int col = i % 2;
            int row = i / 2;
            int x = BASKET_X + col * 18;
            int y = BASKET_Y + row * 18;

            IngredientWithCount iwc = baskets.get(i);
            List<ItemStack> stacks = new ArrayList<>();
            for (ItemStack base : iwc.ingredient().getItems()) {
                ItemStack copy = base.copy();
                copy.setCount(iwc.count());
                stacks.add(copy);
            }
            builder.addSlot(RecipeIngredientRole.INPUT, x + 1, y + 1)
                    .setBackground(slot, -1, -1)
                    .addItemStacks(stacks);
        }

        // Shelf 3x3 pattern
        for (int i = 0; i < 9; i++) {
            int col = i % 3;
            int row = i / 3;
            int x = SHELF_X + col * 18;
            int y = SHELF_Y + row * 18;

            Ingredient ing = recipe.getShelfPattern().get(i);
            var sb = builder.addSlot(RecipeIngredientRole.INPUT, x + 1, y + 1)
                    .setBackground(slot, -1, -1);
            if (!ing.isEmpty()) {
                sb.addIngredients(ing);
            }
        }

        // Output
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X + 1, OUTPUT_Y + 1)
                .setBackground(slot, -1, -1)
                .addItemStack(recipe.getOutput());
    }
}
