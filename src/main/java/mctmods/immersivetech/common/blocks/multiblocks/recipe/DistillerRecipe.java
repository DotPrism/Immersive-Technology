package mctmods.immersivetech.common.blocks.multiblocks.recipe;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import mctmods.immersivetech.core.registration.ITRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;


public class DistillerRecipe extends MultiblockRecipe
{
    public static RegistryObject<IERecipeSerializer<DistillerRecipe>> SERIALIZER;
    public static final CachedRecipeList<DistillerRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.DISTILLER);

    public final FluidStack output;
    public final FluidTagInput water;
    Lazy<Integer> totalProcessTime;
    Lazy<Integer> totalProcessEnergy;

    public <T extends Recipe<?>> DistillerRecipe(ResourceLocation id, FluidStack output, FluidTagInput water) {
        super(LAZY_EMPTY, ITRecipeTypes.DISTILLER, id);
        this.output = output;
        this.water = water;
        totalProcessTime = Lazy.of(() -> 100);
        totalProcessEnergy = Lazy.of(() -> 3200);
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess)
    {
        return ItemStack.EMPTY;
    }

    public static DistillerRecipe findRecipe(Level level, FluidStack input0)
    {
        for(DistillerRecipe recipe : RECIPES.getRecipes(level))
        {
            if(!input0.isEmpty())
            {
                if((recipe.water==null&&input0.isEmpty())||(recipe.water!=null&&recipe.water.test(input0)))
                    return recipe;
            }
        }
        return null;
    }

    @Override
    protected IERecipeSerializer<?> getIESerializer() {
        return SERIALIZER.get();
    }

    @Override
    public int getTotalProcessTime()
    {
        return totalProcessTime.get();
    }

    @Override
    public int getTotalProcessEnergy()
    {
        return totalProcessEnergy.get();
    }

    @Override
    public int getMultipleProcessTicks() {
        return 0;
    }
}
