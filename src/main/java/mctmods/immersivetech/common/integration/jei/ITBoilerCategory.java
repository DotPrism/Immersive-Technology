package mctmods.immersivetech.common.integration.jei;

import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import com.igteam.immersivegeology.core.lib.IGLib;
import com.igteam.immersivegeology.core.material.data.enums.ChemicalEnum;
import com.igteam.immersivegeology.core.material.helper.flags.BlockCategoryFlags;
import com.igteam.immersivegeology.core.registration.IGMultiblockProvider;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.BoilerRecipe;
import mctmods.immersivetech.core.registration.ITMultiblockProvider;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

import java.util.Arrays;

public class ITBoilerCategory extends ITRecipeCategory<BoilerRecipe>
{
    private final IDrawableStatic tankOverlay;

    public ITBoilerCategory(IGuiHelper helper)
    {
        super(helper, JEIRecipeTypes.BOILER, "block.immersivetech.boiler");
        ResourceLocation background = new ResourceLocation(IGLib.MODID, "textures/gui/boiler_gui.png");
        IDrawableStatic back = guiHelper.drawableBuilder(background, 0, 0, 176, 77).setTextureSize(176,166).build();
        setBackground(back);
        tankOverlay = helper.createDrawable(background, 177, 31, 20, 51);
        setIcon(ITMultiblockProvider.BOILER.iconStack());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BoilerRecipe recipe, IFocusGroup focuses)
    {
        assert Minecraft.getInstance().level!=null;
    }

    @Override
    public void draw(BoilerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
    }
}
