package mctmods.immersivetech.core.registration;

import mctmods.immersivetech.common.blocks.multiblocks.recipe.AdvancedCokeOvenFuel;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.BoilerRecipe;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.SteamTurbineRecipe;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.serializer.BoilerRecipeSerializer;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.serializer.SteamTurbineRecipeSerializer;
import mctmods.immersivetech.core.lib.ITLib;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.serializer.AdvancedCokeOvenFuelSerializer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ITRecipeSerializers
{
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ITLib.MODID);
}
