package mctmods.immersivetech.common.blocks.multiblocks.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.GasTurbineRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class GasTurbineRecipeBuilder extends IEFinishedRecipe<GasTurbineRecipeBuilder>
{
    public static final String FLUID_TAG_KEY_IN = "fluidTagIn";
    public static final String FLUID_TAG_KEY_OUT = "fluidTagOut";
    public static final String BURN_TIME_KEY = "burnTime";

    private GasTurbineRecipeBuilder(TagKey<Fluid> fluidIn, TagKey<Fluid> fluidout, int burnTime)
    {
        super(GasTurbineRecipe.SERIALIZER.get());
        addWriter(obj -> obj.addProperty(FLUID_TAG_KEY_IN, fluidIn.location().toString()));
        addWriter(obj -> obj.addProperty(FLUID_TAG_KEY_OUT, fluidout.location().toString()));
        addWriter(obj -> obj.addProperty(BURN_TIME_KEY, burnTime));
    }

    public static GasTurbineRecipeBuilder builder(TagKey<Fluid> fluidin, TagKey<Fluid> fluidout, int burnTime)
    {
        return new GasTurbineRecipeBuilder(fluidin, fluidout, burnTime);
    }
}
