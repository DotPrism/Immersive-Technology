package mctmods.immersivetech.common.blocks.multiblocks.recipe;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import blusunrize.immersiveengineering.api.utils.FastEither;
import blusunrize.immersiveengineering.api.utils.TagUtils;
import mctmods.immersivetech.core.registration.ITRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class GasTurbineRecipe extends MultiblockRecipe
{
    public static RegistryObject<IERecipeSerializer<GasTurbineRecipe>> SERIALIZER;

    public static final CachedRecipeList<GasTurbineRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.GAS_TURBINE);

    private final FastEither<TagKey<Fluid>, List<Fluid>> fluids;
    private final int burnTime;

    public GasTurbineRecipe(ResourceLocation id, TagKey<Fluid> fluids, int burnTime)
    {
        super(LAZY_EMPTY, ITRecipeTypes.GAS_TURBINE, id);
        this.fluids = FastEither.left(fluids);
        this.burnTime = burnTime;
    }

    public GasTurbineRecipe(ResourceLocation id, List<Fluid> fluids, int burnTime)
    {
        super(LAZY_EMPTY, ITRecipeTypes.GAS_TURBINE, id);
        this.fluids = FastEither.right(fluids);
        this.burnTime = burnTime;
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
        if(this.fluids.isLeft())
            return in.is(this.fluids.leftNonnull());
        else
            return this.fluids.rightNonnull().contains(in);
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

    public List<Fluid> getFluids()
    {
        return fluids.map(t -> TagUtils.elementStream(BuiltInRegistries.FLUID, t).toList(), Function.identity());
    }

    public int getBurnTime()
    {
        return burnTime;
    }

    @Override
    public int getMultipleProcessTicks() {
        return 0;
    }
}
