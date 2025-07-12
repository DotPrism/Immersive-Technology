package mctmods.immersivetech.common.blocks.multiblocks.recipe;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import blusunrize.immersiveengineering.api.utils.FastEither;
import blusunrize.immersiveengineering.api.utils.TagUtils;
import com.google.common.collect.Lists;
import mctmods.immersivetech.core.registration.ITRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GasTurbineRecipe extends MultiblockRecipe
{
    public static RegistryObject<IERecipeSerializer<GasTurbineRecipe>> SERIALIZER;

    public static final CachedRecipeList<GasTurbineRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.GAS_TURBINE);

    public static float timeModifier = 1;

    public final FluidStack fluidOutput;
    public final FluidStack fluidInput;

    private static ResourceLocation id;

    int totalProcessTime;

    public GasTurbineRecipe(ResourceLocation recipe, FluidStack fluidOutput, FluidStack fluidInput, int time) {
        super(LAZY_EMPTY, ITRecipeTypes.GAS_TURBINE, recipe);
        this.id = recipe;
        this.fluidOutput = fluidOutput;
        this.fluidInput = fluidInput;
        this.totalProcessTime = (int)Math.floor(time * timeModifier);
        this.fluidInputList = Lists.newArrayList();
        this.fluidOutputList = Lists.newArrayList(this.fluidOutput);
    }

    public static ArrayList<GasTurbineRecipe> recipeList = new ArrayList<>();

    public static GasTurbineRecipe addFuel(FluidStack fluidOutput, FluidStack fluidInput, int time) {
        GasTurbineRecipe recipe = new GasTurbineRecipe(id, fluidOutput, fluidInput, time);
        recipeList.add(recipe);
        return recipe;
    }

    public static GasTurbineRecipe findFuel(FluidStack fluidInput) {
        if(fluidInput == null) return null;
        for(GasTurbineRecipe recipe : recipeList) {
            if(recipe.fluidInput != null && (fluidInput.containsFluid(recipe.fluidInput))) return recipe;
        }
        return null;
    }

    public static GasTurbineRecipe findFuelByFluid(Fluid fluidInput) {
        if(fluidInput == null) return null;
        for(GasTurbineRecipe recipe : recipeList) {
            if(recipe.fluidInput != null && fluidInput == recipe.fluidInput.getFluid()) return recipe;
        }
        return null;
    }

    @Override
    protected IERecipeSerializer<?> getIESerializer()
    {
        return SERIALIZER.get();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess)
    {
        return ItemStack.EMPTY;
    }

    public boolean matches(Fluid in)
    {
        return this.fluidInput.equals(in);
    }

    public static GasTurbineRecipe getRecipeFor(Level level, Fluid in, @Nullable GasTurbineRecipe hint)
    {
        if(hint!=null&&hint.matches(in))
            return hint;
        for(GasTurbineRecipe fuel : RECIPES.getRecipes(level))
            if(fuel.matches(in))
                return fuel;
        return null;
    }

    @Override
    public int getTotalProcessTime()
    {
        return this.totalProcessTime;
    }

    @Override
    public int getMultipleProcessTicks() {
        return 0;
    }
}
