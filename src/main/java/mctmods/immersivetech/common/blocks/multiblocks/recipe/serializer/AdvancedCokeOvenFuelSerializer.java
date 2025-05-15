package mctmods.immersivetech.common.blocks.multiblocks.recipe.serializer;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.common.register.IEItems;
import com.google.gson.JsonObject;
import com.igteam.immersivegeology.common.block.multiblocks.recipe.BloomeryFuel;
import com.igteam.immersivegeology.common.block.multiblocks.recipe.ChemicalRecipe;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.AdvancedCokeOvenFuel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class AdvancedCokeOvenFuelSerializer extends IERecipeSerializer<AdvancedCokeOvenFuel>
{
    public AdvancedCokeOvenFuelSerializer()
    {

    }

    public ItemStack getIcon() {
        return new ItemStack(IEItems.Ingredients.COAL_COKE);
    }

    public AdvancedCokeOvenFuel readFromJson(ResourceLocation recipeId, JsonObject json, ICondition.IContext context) {
        Ingredient input = Ingredient.fromJson(json.getAsJsonObject("input"));
        int time = GsonHelper.getAsInt(json, "time", 1200);
        return new AdvancedCokeOvenFuel(recipeId, input, time);
    }

    @Nullable
    public AdvancedCokeOvenFuel fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        Ingredient input = Ingredient.fromNetwork(buffer);
        int time = buffer.readInt();
        return new AdvancedCokeOvenFuel(recipeId, input, time);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, AdvancedCokeOvenFuel recipe) {
        recipe.input.toNetwork(buffer);
        buffer.writeInt(recipe.burnTime);
    }

}
