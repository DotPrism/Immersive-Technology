package mctmods.immersivetech.common.blocks.multiblocks.recipe.serializer;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.BoilerRecipe;
import mctmods.immersivetech.core.registration.ITMultiblockProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class BoilerRecipeSerializer extends IERecipeSerializer<BoilerRecipe>
{
    @Override
    public ItemStack getIcon()
    {
        return ITMultiblockProvider.BOILER.iconStack();
    }

    @Override
    public BoilerRecipe readFromJson(ResourceLocation recipeID, JsonObject json, ICondition.IContext iContext)
    {
        FluidStack fluidOutput = ApiUtils.jsonDeserializeFluidStack(GsonHelper.getAsJsonObject(json, "result"));
        FluidTagInput fluidInputWater = FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "fluidWater"));
        FluidTagInput fluidInputFuel = FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "fluidFuel"));
        int waterInputAmount = GsonHelper.getAsInt(json, "waterAmount");
        int fuelInputAmount = GsonHelper.getAsInt(json, "fuelAmount");
        int steamOutputAmount = GsonHelper.getAsInt(json, "steamAmount");
        int time = GsonHelper.getAsInt(json, "time");
        int heatPerTick = GsonHelper.getAsInt(json, "heatPerTick");

        return new BoilerRecipe(recipeID, waterInputAmount, fuelInputAmount, steamOutputAmount, time, heatPerTick, fluidOutput, fluidInputWater, fluidInputFuel);
    }

    @Override
    public @Nullable BoilerRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf)
    {
        return null;
    }

    @Override
    public void toNetwork(FriendlyByteBuf friendlyByteBuf, BoilerRecipe boilerRecipe)
    {

    }
}
