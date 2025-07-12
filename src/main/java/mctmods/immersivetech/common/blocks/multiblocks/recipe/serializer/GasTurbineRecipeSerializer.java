package mctmods.immersivetech.common.blocks.multiblocks.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.GasTurbineRecipe;
import mctmods.immersivetech.core.registration.ITMultiblockProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class GasTurbineRecipeSerializer extends IERecipeSerializer<GasTurbineRecipe>
{
    @Override
    public ItemStack getIcon() {
        return ITMultiblockProvider.GAS_TURBINE.iconStack();
    }

    @Override
    public GasTurbineRecipe readFromJson(ResourceLocation recipeId, JsonObject json, ICondition.IContext iContext)
    {

        int amount = json.get("burnTime").getAsInt();
        return new GasTurbineRecipe(recipeId, fluidStackin, fluidStackout, amount);
    }

    @Nullable
    @Override
    public GasTurbineRecipe fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer)
    {

        int burnTime = buffer.readInt();
        return new GasTurbineRecipe(recipeId, fluidStackin, fluidStackout, burnTime);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, GasTurbineRecipe recipe)
    {

        buffer.writeInt(recipe.getTotalProcessTime());
    }
}
