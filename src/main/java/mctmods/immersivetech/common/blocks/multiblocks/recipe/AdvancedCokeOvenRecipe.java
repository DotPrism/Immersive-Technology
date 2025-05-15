package mctmods.immersivetech.common.blocks.multiblocks.recipe;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MBInventoryUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.AdvBlastFurnaceLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.NonMirrorableWithActiveBlock;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.util.CachedRecipe;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler;
import mctmods.immersivetech.core.registration.ITRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fluids.FluidType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AdvancedCokeOvenRecipe extends MultiblockRecipe
{
    public static RegistryObject<IERecipeSerializer<AdvancedCokeOvenRecipe>> SERIALIZER;
    public static final CachedRecipeList<AdvancedCokeOvenRecipe> RECIPES = new CachedRecipeList<>(ITRecipeTypes.ADVANCED_COKE_OVEN);

    public final IngredientWithSize input;
    public final Lazy<ItemStack> output;
    public final int time;
    public final int creosoteOutput;

    public AdvancedCokeOvenRecipe(ResourceLocation id, Lazy<ItemStack> output, IngredientWithSize input, int time, int creosoteOutput)
    {
        super(output, ITRecipeTypes.ADVANCED_COKE_OVEN, id);
        this.output = output;
        this.input = input;
        this.time = time;
        this.creosoteOutput = creosoteOutput;
    }

    public boolean matches(ItemStack stack) {
        return input.test(stack);
    }

    @Override
    protected IERecipeSerializer getIESerializer()
    {
        return SERIALIZER.get();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access)
    {
        return this.output.get();
    }

    public static AdvancedCokeOvenRecipe findRecipe(Level level, ItemStack input)
    {
        return findRecipe(level, input, null);
    }

    public static AdvancedCokeOvenRecipe findRecipe(Level level, ItemStack input, @Nullable AdvancedCokeOvenRecipe hint)
    {
        if (input.isEmpty())
            return null;
        if (hint != null && hint.matches(input))
            return hint;
        for(AdvancedCokeOvenRecipe recipe : RECIPES.getRecipes(level))
            if(recipe.matches(input))
                return recipe;
        return null;
    }

    @Override
    public int getMultipleProcessTicks() {
        return 0;
    }
}
