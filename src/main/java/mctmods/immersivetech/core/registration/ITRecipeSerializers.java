package mctmods.immersivetech.core.registration;

import mctmods.immersivetech.common.blocks.multiblocks.recipe.AdvancedCokeOvenFuel;
import mctmods.immersivetech.core.lib.ITLib;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.serializer.AdvancedCokeOvenFuelSerializer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ITRecipeSerializers
{
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ITLib.MODID);

    static {
        //BurnerFuel.SERIALIZER = RECIPE_SERIALIZERS.register("burner", BurnerFuelSerializer::new);
        //AdvancedCokeOvenFuel.SERIALIZER = RECIPE_SERIALIZERS.register("burner", AdvancedCokeOvenFuelSerializer::new);
    }
}
