package mctmods.immersivetech.core.registration;

import blusunrize.immersiveengineering.api.crafting.IERecipeTypes.TypeWithClass;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.AdvancedCokeOvenFuel;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.AdvancedCokeOvenRecipe;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.BoilerRecipe;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.serializer.AdvancedCokeOvenRecipeSerializer;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.serializer.BoilerRecipeSerializer;
import mctmods.immersivetech.core.lib.ITLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("all")
public class ITRecipeTypes
{
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(
            ForgeRegistries.RECIPE_SERIALIZERS, ITLib.MODID
    );
    private static final DeferredRegister<RecipeType<?>> REGISTER = DeferredRegister.create(Registries.RECIPE_TYPE, ITLib.MODID);

    public static final TypeWithClass<AdvancedCokeOvenRecipe> ADVANCED_COKE_OVEN = register("coke_oven_advanced", AdvancedCokeOvenRecipe.class);
    public static final TypeWithClass<BoilerRecipe> BOILER = register("boiler", BoilerRecipe.class);
    public static final TypeWithClass<? extends Recipe<?>> ADV_COKE_OVEN_FUEL = register("coke_oven_advanced_fuel", AdvancedCokeOvenFuel.class);

    static
    {
        AdvancedCokeOvenRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("coke_oven_advanced", AdvancedCokeOvenRecipeSerializer::new);
        BoilerRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("boiler", BoilerRecipeSerializer::new);
    }

    private static <T extends Recipe<?>> TypeWithClass<T> register(String name, Class<T> type)
    {
        RegistryObject<RecipeType<T>> regObj = REGISTER.register(name, () -> new RecipeType<>()
        {
        });
        return new TypeWithClass<>(regObj, type);
    }

    public static void init()
    {
        REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
