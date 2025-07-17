package mctmods.immersivetech.common.blocks.multiblocks.recipe.serializer;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.google.gson.JsonObject;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.BoilerRecipe;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.DistillerRecipe;
import mctmods.immersivetech.core.registration.ITMultiblockProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class DistillerRecipeSerializer extends IERecipeSerializer<DistillerRecipe> {
    @Override
    public ItemStack getIcon() {
        return ITMultiblockProvider.DISTILLER.iconStack();
    }

    @Override
    public DistillerRecipe readFromJson(ResourceLocation recipeID, JsonObject json, ICondition.IContext iContext) {
        FluidStack output = ApiUtils.jsonDeserializeFluidStack(GsonHelper.getAsJsonObject(json, "result"));
        FluidTagInput input0 = FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input0"));

        DistillerRecipe recipe = new DistillerRecipe(recipeID, output, input0);
        return recipe;
    }

    @Override
    public @Nullable DistillerRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        FluidStack output = buffer.readFluidStack();
        FluidTagInput input0 = FluidTagInput.read(buffer);
        return new DistillerRecipe(recipeId, output, input0);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, DistillerRecipe recipe) {
        buffer.writeFluidStack(recipe.output);
        recipe.water.write(buffer);
    }
}
