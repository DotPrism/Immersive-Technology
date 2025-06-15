package mctmods.immersivetech.common.blocks.multiblocks.recipe.serializer;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.network.PacketUtils;
import com.google.gson.JsonObject;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.BoilerRecipe;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.SteamTurbineRecipe;
import mctmods.immersivetech.core.registration.ITMultiblockProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
        FluidStack output = ApiUtils.jsonDeserializeFluidStack(GsonHelper.getAsJsonObject(json, "result"));
        int heatPerTick = GsonHelper.getAsInt(json, "heatPerTick");
        FluidTagInput input0 = FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input0"));
        FluidTagInput input1 = json.has("input1")?FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input1")): null;
        BoilerRecipe recipe = new BoilerRecipe(recipeID, output, input0, input1, heatPerTick);
        return recipe;
    }

    @Override
    public @Nullable BoilerRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
    {
        FluidStack output = buffer.readFluidStack();
        FluidTagInput input0 = FluidTagInput.read(buffer);
        FluidTagInput input1 = FluidTagInput.read(buffer);
        int heatPerTick = buffer.readInt();
        return new BoilerRecipe(recipeId, output, input0, input1, heatPerTick);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, BoilerRecipe recipe)
    {
        buffer.writeFluidStack(recipe.output);
        recipe.water.write(buffer);
        if(recipe.fuel!=null)
        {
            buffer.writeBoolean(true);
            recipe.fuel.write(buffer);
        }
        else
            buffer.writeBoolean(false);
        buffer.writeInt(recipe.getHeatPerTick());
    }
}
