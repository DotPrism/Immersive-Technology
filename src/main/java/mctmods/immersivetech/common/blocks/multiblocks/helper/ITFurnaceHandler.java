package mctmods.immersivetech.common.blocks.multiblocks.helper;

import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import com.igteam.immersivegeology.common.block.multiblocks.logic.RevFurnaceLogic;
import com.igteam.immersivegeology.common.block.multiblocks.recipe.RevFurnaceRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class ITFurnaceHandler<R extends IESerializableRecipe>
{
    // Used for simple furnace types
    private int process = 0;
    private int processMax = 0;
    private int burnTime = 0;
    private int lastBurnTime = 0;

    public final ITFurnaceStateView stateView = new ITFurnaceStateView();

    private final int fuelSlot;
    private final List<ITFurnaceHandler.InputSlot<R>> inputs;
    private final List<ITFurnaceHandler.OutputSlot<R>> outputs;
    private final ToIntFunction<R> getProcessingTime;
    private final Runnable setChanged;

    public ITFurnaceHandler(
            int fuelSlot,
            List<ITFurnaceHandler.InputSlot<R>> inputs,
            List<ITFurnaceHandler.OutputSlot<R>> outputs,
            ToIntFunction<R> getProcessingTime,
            Runnable setChanged
    )
    {
        this.fuelSlot = fuelSlot;
        this.inputs = inputs;
        this.outputs = outputs;
        this.getProcessingTime = getProcessingTime;
        this.setChanged = setChanged;
    }

    public boolean tickServer(IMultiblockContext<? extends ITFurnaceHandler.IFurnaceEnvironment<R>> ctx, int furnaceIndex)
    {
        boolean active = false;
        final ITFurnaceHandler.IFurnaceEnvironment<R> env = ctx.getState();

        if(burnTime > 0)
        {
            int processSpeed = 1;
            if(process > 0)
                processSpeed = env.getProcessSpeed(ctx.getLevel(), furnaceIndex);
            burnTime -= processSpeed;
            if(process > 0)
            {
                if(isAnyInputEmpty(env.getInventory(furnaceIndex)))
                {
                    process = 0;
                    processMax = 0;
                }
                else
                {
                    R recipe = getRecipe(env, furnaceIndex);
                    if(recipe!=null&&getProcessTime(recipe)!=processMax)
                    {
                        processMax = 0;
                        process = 0;
                    }
                    else
                    {
                        process -= processSpeed;
                        processSpeed = 0;//Process speed is "used up"
                        active = true;
                    }
                }
                setChanged.run();
            }

            if(process <= 0)
            {
                if(processMax > 0)
                {
                    doRecipeIO(env, furnaceIndex);
                    processMax = 0;
                    burnTime -= process;
                }
                R recipe = getRecipe(env, furnaceIndex);
                if(recipe!=null)
                {
                    final int time = getProcessTime(recipe);
                    this.process = time-processSpeed;
                    this.processMax = time;
                    active = true;
                }
            }
        }

        if(burnTime <= 0&&getRecipe(env, furnaceIndex)!=null)
        {
            final IItemHandlerModifiable inv = env.getInventory(furnaceIndex);
            final ItemStack fuel = inv.getStackInSlot(fuelSlot);
            final int addedBurntime = env.getBurnTimeOf(ctx.getLevel().getRawLevel(), fuel);
            if(addedBurntime > 0)
            {
                lastBurnTime = addedBurntime;
                burnTime += lastBurnTime;
                if(fuel.hasCraftingRemainingItem()&&fuel.getCount()==1)
                    inv.setStackInSlot(fuelSlot, fuel.getCraftingRemainingItem());
                else
                    fuel.shrink(1);
                setChanged.run();
            }
        }

        if(!active)
            env.turnOff(ctx.getLevel(), furnaceIndex);
        return active;
    }

    public Tag toNBT(int index)
    {
        final CompoundTag result = new CompoundTag();
        result.putInt("process" + index, process);
        result.putInt("processMax"+ index, processMax);
        result.putInt("burnTime"+ index, burnTime);
        result.putInt("lastBurnTime"+ index, lastBurnTime);
        return result;
    }

    public void readNBT(Tag nbt, int index)
    {
        if(!(nbt instanceof CompoundTag compound))
            return;
        process = compound.getInt("process"+ index);
        processMax = compound.getInt("processMax"+ index);
        burnTime = compound.getInt("burnTime"+ index);
        lastBurnTime = compound.getInt("lastBurnTime"+ index);
    }

    private boolean isAnyInputEmpty(IItemHandler inv)
    {
        for(ITFurnaceHandler.InputSlot<R> i : inputs)
            if(inv.getStackInSlot(i.slotIndex).isEmpty())
                return true;
        return false;
    }

    @Nullable
    private R getRecipe(ITFurnaceHandler.IFurnaceEnvironment<R> env, int furnaceIndex)
    {
        R recipe = env.getRecipeForInput(furnaceIndex);
        if(recipe==null)
            return null;
        final IItemHandlerModifiable inv = env.getInventory(furnaceIndex);
        for(ITFurnaceHandler.OutputSlot<R> out : outputs)
        {
            ItemStack currentStack = inv.getStackInSlot(out.slotIndex);
            ItemStack outputSlot = out.get(recipe);
            if(!currentStack.isEmpty())
            {
                if(!ItemStack.isSameItem(currentStack, outputSlot))
                    return null;
                else if(currentStack.getCount()+outputSlot.getCount() > inv.getSlotLimit(out.slotIndex))
                    return null;
            }
        }
        return recipe;
    }

    private void doRecipeIO(ITFurnaceHandler.IFurnaceEnvironment<R> env, int furnaceIndex)
    {
        R recipe = getRecipe(env, furnaceIndex);
        if(recipe==null)
            return;
        final IItemHandlerModifiable inv = env.getInventory(furnaceIndex);
        for(ITFurnaceHandler.InputSlot<R> slot : inputs)
        {
            int reqSize = inputs.stream()
                    .map(matchSlot -> matchSlot.get(recipe))
                    .filter(ingr -> ingr.test(inv.getStackInSlot(slot.slotIndex)))
                    .mapToInt(IngredientWithSize::getCount).findFirst().orElse(0);
            inv.getStackInSlot(slot.slotIndex).shrink(reqSize);
        }

        for(ITFurnaceHandler.OutputSlot<R> slot : outputs)
        {
            ItemStack result = slot.get(recipe);
            if(!result.isEmpty())
            {
                if(!inv.getStackInSlot(slot.slotIndex).isEmpty())
                    inv.getStackInSlot(slot.slotIndex).grow(result.getCount());
                else
                    inv.setStackInSlot(slot.slotIndex, result.copy());
            }
        }
        if(recipe instanceof RevFurnaceRecipe revRecipe)
        {
            if(env instanceof RevFurnaceLogic.State state)
            {
                state.addToTank(revRecipe.getWasteAmount());
            }
        }
    }

    private int getProcessTime(R recipe)
    {
        return getProcessingTime.applyAsInt(recipe);
    }

    public interface IFurnaceEnvironment<R extends IESerializableRecipe>
    {
        IItemHandlerModifiable getInventory(int furnaceIndex);

        @Nullable
        R getRecipeForInput(int furnaceIndex);

        int getBurnTimeOf(Level level, ItemStack fuel);

        default int getProcessSpeed(IMultiblockLevel level, int furnaceIndex)
        {
            return 1;
        }

        default void turnOff(IMultiblockLevel level, int furnaceIndex)
        {
        }

        int getProcessSpeed(IMultiblockLevel level);

        void turnOff(IMultiblockLevel level);
    }

    public class ITFurnaceStateView implements ContainerData
    {
        public static final int LAST_BURN_TIME = 0;
        public static final int BURN_TIME = 1;
        public static final int PROCESS_MAX = 2;
        public static final int CURRENT_PROCESS = 3;
        public static final int NUM_SLOTS = 4;

        public static int getLastBurnTime(ContainerData data)
        {
            return data.get(LAST_BURN_TIME);
        }

        public static int getBurnTime(ContainerData data)
        {
            return data.get(BURN_TIME);
        }

        public static int getMaxProcess(ContainerData data)
        {
            return data.get(PROCESS_MAX);
        }

        public static int getProcess(ContainerData data)
        {
            return data.get(CURRENT_PROCESS);
        }

        @Override
        public int get(int index)
        {
            switch(index)
            {
                case LAST_BURN_TIME:
                    return lastBurnTime;
                case BURN_TIME:
                    return burnTime;
                case PROCESS_MAX:
                    return processMax;
                case CURRENT_PROCESS:
                    return process;
                default:
                    throw new IllegalArgumentException("Unknown index "+index);
            }
        }

        @Override
        public void set(int index, int value)
        {
            switch(index)
            {
                case LAST_BURN_TIME:
                    lastBurnTime = value;
                    break;
                case BURN_TIME:
                    burnTime = value;
                    break;
                case PROCESS_MAX:
                    processMax = value;
                    break;
                case CURRENT_PROCESS:
                    process = value;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown index "+index);
            }
        }

        @Override
        public int getCount()
        {
            return NUM_SLOTS;
        }
    }

    public static class InputSlot<R>
    {
        private final Function<R, IngredientWithSize> getFromRecipe;
        private final int slotIndex;

        public InputSlot(Function<R, IngredientWithSize> getFromRecipe, int slotIndex)
        {
            this.getFromRecipe = getFromRecipe;
            this.slotIndex = slotIndex;
        }

        public IngredientWithSize get(R recipe)
        {
            return getFromRecipe.apply(recipe);
        }
    }

    public static class OutputSlot<R>
    {
        private final Function<R, Lazy<ItemStack>> getFromRecipe;
        private final int slotIndex;

        public OutputSlot(Function<R, Lazy<ItemStack>> getFromRecipe, int slotIndex)
        {
            this.getFromRecipe = getFromRecipe;
            this.slotIndex = slotIndex;
        }

        public ItemStack get(R recipe)
        {
            return getFromRecipe.apply(recipe).get();
        }
    }
}
