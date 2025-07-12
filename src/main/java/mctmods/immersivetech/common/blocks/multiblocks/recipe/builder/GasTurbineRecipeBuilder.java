package mctmods.immersivetech.common.blocks.multiblocks.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.GasTurbineRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class GasTurbineRecipeBuilder extends IEFinishedRecipe<GasTurbineRecipeBuilder>
{
    public static final String FLUID_TAG_KEY = "fluidTag";
    public static final String BURN_TIME_KEY = "burnTime";

    private GasTurbineRecipeBuilder(TagKey<Fluid> fluid, int burnTime)
    {
        super(GasTurbineRecipe.SERIALIZER.get());
        addWriter(obj -> obj.addProperty(FLUID_TAG_KEY, fluid.location().toString()));
        addWriter(obj -> obj.addProperty(BURN_TIME_KEY, burnTime));
    }

    public static GasTurbineRecipeBuilder builder(TagKey<Fluid> fluid, int burnTime)
    {
        return new GasTurbineRecipeBuilder(fluid, burnTime);
    }
}
