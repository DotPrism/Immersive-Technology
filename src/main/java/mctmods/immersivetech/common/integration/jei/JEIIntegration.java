package mctmods.immersivetech.common.integration.jei;

import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import mctmods.immersivetech.client.menu.multiblock.AdvCokeOvenScreen;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.AdvancedCokeOvenRecipe;
import mctmods.immersivetech.core.lib.ITLib;
import mctmods.immersivetech.core.registration.ITMultiblockProvider;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;
import java.util.function.Predicate;

@JeiPlugin
public class JEIIntegration implements IModPlugin
{
    private static final ResourceLocation ID = new ResourceLocation(ITLib.MODID, "main");
    @Override
    public ResourceLocation getPluginUid()
    {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration)
    {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new ITAdvancedCokeOvenCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(JEIRecipeTypes.ADV_COKE_OVEN, getRecipes(AdvancedCokeOvenRecipe.RECIPES));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ITMultiblockProvider.ADV_COKE_OVEN.iconStack(), JEIRecipeTypes.ADV_COKE_OVEN);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        IModPlugin.super.registerGuiHandlers(registration);
        registration.addRecipeClickArea(AdvCokeOvenScreen.class, 56, 36, 14, 14, JEIRecipeTypes.ADV_COKE_OVEN);
    }

    private <T extends Recipe<?>> List<T> getRecipes(CachedRecipeList<T> cachedList)
    {
        return getFiltered(cachedList, $ -> true);
    }

    private <T extends Recipe<?>> List<T> getFiltered(CachedRecipeList<T> cachedList, Predicate<T> include)
    {
        return cachedList.getRecipes(Minecraft.getInstance().level).stream()
                .filter(include)
                .toList();
    }

}
