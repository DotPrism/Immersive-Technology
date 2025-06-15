package mctmods.immersivetech.common.blocks.multiblocks.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.common.network.PacketUtils;
import com.google.gson.JsonObject;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.GasTurbineRecipe;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.SteamTurbineRecipe;
import mctmods.immersivetech.core.registration.ITMultiblockProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class GasTurbineRecipeSerializer extends IERecipeSerializer<GasTurbineRecipe>
{
    @Override
    public ItemStack getIcon() {
        return ITMultiblockProvider.GAS_TURBINE.iconStack();
    }

    @Override
    public GasTurbineRecipe readFromJson(ResourceLocation recipeId, JsonObject json, ICondition.IContext iContext)
    {
        ResourceLocation tagName = new ResourceLocation(json.get("fluidTag").getAsString());
        TagKey<Fluid> tag = TagKey.create(Registries.FLUID, tagName);
        int amount = json.get("burnTime").getAsInt();
        return new GasTurbineRecipe(recipeId, tag, amount);
    }

    @Nullable
    @Override
    public GasTurbineRecipe fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer)
    {
        List<Fluid> fluids = PacketUtils.readList(buffer, buf -> buf.readRegistryIdUnsafe(ForgeRegistries.FLUIDS));
        int burnTime = buffer.readInt();
        return new GasTurbineRecipe(recipeId, fluids, burnTime);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, GasTurbineRecipe recipe)
    {
        PacketUtils.writeList(
                buffer, recipe.getFluids(), (f, buf) -> buf.writeRegistryIdUnsafe(ForgeRegistries.FLUIDS, f)
        );
        buffer.writeInt(recipe.getBurnTime());
    }
}
