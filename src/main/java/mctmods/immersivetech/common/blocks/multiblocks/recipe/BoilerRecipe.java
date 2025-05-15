package mctmods.immersivetech.common.blocks.multiblocks.recipe;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import mctmods.immersivetech.core.registration.ITRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Iterator;

public class BoilerRecipe extends MultiblockRecipe
{
    public static RegistryObject<IERecipeSerializer<BoilerRecipe>> SERIALIZER;
    public static final CachedRecipeList<BoilerRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.BOILER);

    private FluidStack fluidOutput;
    private FluidTagInput fluidInputWater;
    private FluidTagInput fluidInputFuel;
    private int waterInputAmount;
    private int fuelInputAmount;
    private int steamOutputAmount;
    private int time;
    private int heatPerTick;

    public <T extends Recipe<?>>
    BoilerRecipe(ResourceLocation id, int waterInputAmount,
        int fuelInputAmount, int steamOutputAmount, int time, int heatPerTick,
        FluidStack steamOutput, FluidTagInput waterInput, FluidTagInput fuelInput)
    {
        super(LAZY_EMPTY, ITRecipeTypes.BOILER, id);
        this.fluidInputFuel = fuelInput;
        this.fluidInputWater = waterInput;
        this.fluidOutput = steamOutput;
        this.waterInputAmount = waterInputAmount;
        this.fuelInputAmount = fuelInputAmount;
        this.steamOutputAmount = steamOutputAmount;
        this.time = time;
        this.heatPerTick = heatPerTick;
    }

    public static BoilerRecipe findRecipe(Level level, FluidStack fluidWater,FluidStack fluidFuel, NonNullList<FluidStack> components)
    {
        if(fluidFuel.isEmpty()||fluidWater.isEmpty())
            return null;
        for(BoilerRecipe recipe : RECIPES.getRecipes(level))
            if(recipe.matches(fluidWater, fluidFuel, components))
                return recipe;
        return null;
    }

    public FluidStack getFluidOutput(FluidStack input, NonNullList<FluidStack> components)
    {
        return this.fluidOutput;
    }

    public boolean matches(FluidStack fluidWater,FluidStack fluidFuel, NonNullList<FluidStack> components)
    {
        return compareToInputs(fluidWater, fluidFuel, components, this.fluidInputFuel, this.fluidInputWater);
    }

    protected boolean compareToInputs(FluidStack fluidWater,FluidStack fluidFuel, NonNullList<FluidStack> components, FluidTagInput fluidInputFuel,
                                      FluidTagInput fluidInputWater)
    {
        if(fluidWater!=null&&fluidInputFuel.test(fluidFuel)&&fluidFuel!=null&&fluidInputWater.test(fluidWater))
        {
            ArrayList<FluidStack> queryList = new ArrayList<>(components.size());
            for(FluidStack s : components)
                if(!s.isEmpty())
                    queryList.add(s.copy());
            return true;
        }
        return false;
    }

    @Override
    protected IERecipeSerializer<?> getIESerializer()
    {
        return SERIALIZER.get();
    }

    @Override
    public int getMultipleProcessTicks()
    {
        return 0;
    }
}
