package com.cahcap.neoforge.compat.jei;

import com.cahcap.common.recipe.HerbalBlendingRecipe;
import com.cahcap.common.registry.ModRegistries;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

@JeiPlugin
public class HerbalCurativeJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath("herbalcurative", "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        reg.addRecipeCategories(new HerbalBlendingCategory(reg.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        RecipeManager rm = level.getRecipeManager();
        List<HerbalBlendingRecipe> recipes = rm
                .getAllRecipesFor(ModRegistries.HERBAL_BLENDING_RECIPE_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();
        reg.addRecipes(HerbalBlendingCategory.RECIPE_TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        reg.addRecipeCatalyst(
                new ItemStack(ModRegistries.SHELF_ITEM.get()),
                HerbalBlendingCategory.RECIPE_TYPE);
    }
}
