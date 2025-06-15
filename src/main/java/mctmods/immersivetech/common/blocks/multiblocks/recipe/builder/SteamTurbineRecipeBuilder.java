package mctmods.immersivetech.common.blocks.multiblocks.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.builders.GeneratorFuelBuilder;
import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import blusunrize.immersiveengineering.api.energy.GeneratorFuel;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.SteamTurbineRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public class SteamTurbineRecipeBuilder extends IEFinishedRecipe<SteamTurbineRecipeBuilder>
{
    public static final String FLUID_TAG_KEY = "fluidTag";
    public static final String BURN_TIME_KEY = "burnTime";

    private SteamTurbineRecipeBuilder(TagKey<Fluid> fluid, int burnTime)
    {
        super(SteamTurbineRecipe.SERIALIZER.get());
        addWriter(obj -> obj.addProperty(FLUID_TAG_KEY, fluid.location().toString()));
        addWriter(obj -> obj.addProperty(BURN_TIME_KEY, burnTime));
    }

    public static SteamTurbineRecipeBuilder builder(TagKey<Fluid> fluid, int burnTime)
    {
        return new SteamTurbineRecipeBuilder(fluid, burnTime);
    }
}
