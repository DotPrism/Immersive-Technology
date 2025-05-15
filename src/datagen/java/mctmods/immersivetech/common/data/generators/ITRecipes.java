package mctmods.immersivetech.common.data.generators;

import blusunrize.immersiveengineering.api.IETags;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.builder.AdvancedCokeOvenRecipeBuilder;
import mctmods.immersivetech.core.lib.ITLib;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.function.Consumer;

public class ITRecipes extends RecipeProvider
{
    private final HashMap<String, Integer> PATH_COUNT = new HashMap<>();

    public ITRecipes(PackOutput pOutput)
    {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer)
    {
        multiblockRecipes(consumer);
        itemRecipes(consumer);
        recipesCoke(consumer);
    }

    private void itemRecipes(Consumer<FinishedRecipe> consumer)
    {

    }

    private void multiblockRecipes(Consumer<FinishedRecipe> consumer)
    {
        ITLib.IT_LOGGER.info("Starting Multiblock Recipe Registration");
    }

    private void recipesCoke(@Nonnull Consumer<FinishedRecipe> out)
    {
        AdvancedCokeOvenRecipeBuilder.builder(IETags.coalCoke, 1)
                .addInput(Items.COAL)
                .setOil(FluidType.BUCKET_VOLUME/2)
                .setTime(600)
                .build(out, toRL("cokeovenadv/coke"));
        AdvancedCokeOvenRecipeBuilder.builder(IETags.getItemTag(IETags.coalCokeBlock), 1)
                .addInput(Blocks.COAL_BLOCK)
                .setOil(FluidType.BUCKET_VOLUME*5)
                .setTime(9*600)
                .build(out, toRL("cokeovenadv/coke_block"));
        AdvancedCokeOvenRecipeBuilder.builder(Items.CHARCOAL)
                .addInput(ItemTags.LOGS)
                .setOil(FluidType.BUCKET_VOLUME/4)
                .setTime(600)
                .build(out, toRL("cokeovenadv/charcoal"));
    }

    private ResourceLocation toRL(String s)
    {
        if(!s.contains("/"))
            s = "crafting/"+s;
        if(PATH_COUNT.containsKey(s))
        {
            int count = PATH_COUNT.get(s)+1;
            PATH_COUNT.put(s, count);
            return new ResourceLocation(ITLib.MODID, s+count);
        }
        PATH_COUNT.put(s, 1);
        return new ResourceLocation(ITLib.MODID, s);
    }
}
