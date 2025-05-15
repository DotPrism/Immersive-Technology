package mctmods.immersivetech.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.interfaces.MBOverlayText;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler;
import com.google.common.collect.ImmutableList;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.BoilerRecipe;
import mctmods.immersivetech.common.blocks.multiblocks.shapes.FullblockShape;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("all")
public class ITBoilerLogic implements IMultiblockLogic<ITBoilerLogic.State>, IServerTickableComponent<ITBoilerLogic.State>, IClientTickableComponent<ITBoilerLogic.State>, MBOverlayText<ITBoilerLogic.State>
{
    public static final BlockPos MASTER_OFFSET = new BlockPos(0,0,0);
    public static final BlockPos WATER_INPUT_OFFSET = new BlockPos(1,0,4);
    public static final BlockPos FUEL_INPUT_OFFSET = new BlockPos(1,0,-1);
    public static final BlockPos STEAM_OUTPUT_OFFSET = new BlockPos(1,2,4);
    public static final BlockPos REDSTONE_IN_OFFSET = new BlockPos(0,1,-1);
    public static final int TANK_CAPACITY = 2* FluidType.BUCKET_VOLUME;
    private static final CapabilityPosition FLUID_OUTPUT_CAP;
    private static final CapabilityPosition WATER_INPUT_CAP;
    private static final CapabilityPosition FUEL_INPUT_CAP;
    public static final int MAX_HEAT = 1000;
    public static final int NUM_SLOTS = 6;
    public static final int SLOT_FUEL_IN = 0;
    public static final int SLOT_FUEL_EMPTY_OUT = 1;
    public static final int SLOT_WATER_IN = 2;
    public static final int SLOT_WATER_EMPTY_OUT = 3;
    public static final int SLOT_STEAM_EMPTY_IN = 4;
    public static final int SLOT_STEAM_FULL_OUT = 5;

    static
    {
        FLUID_OUTPUT_CAP = new CapabilityPosition(STEAM_OUTPUT_OFFSET, RelativeBlockFace.UP);
        WATER_INPUT_CAP = new CapabilityPosition(WATER_INPUT_OFFSET, RelativeBlockFace.BACK);
        FUEL_INPUT_CAP = new CapabilityPosition(FUEL_INPUT_OFFSET, RelativeBlockFace.FRONT);
    }

    @Override
    public void tickClient(IMultiblockContext<State> ctx)
    {

    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx)
    {
        State state = ctx.getState();
        boolean isEnabled = state.rsState.isEnabled(ctx);
        if (isEnabled) insertRecipeToProcess(state, ctx);

        
    }

    private static void insertRecipeToProcess(State state, IMultiblockContext<State>  ctx)
    {

    }

    @Override
    public State createInitialState(IInitialMultiblockContext<State> context)
    {
        return new ITBoilerLogic.State(context);
    }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) {
        return FullblockShape.GETTER;
    }

    @Override
    public <T>
    LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap)
    {
        return LazyOptional.empty();
    }

    private int heatUp()
    {
        return 0;
    }

    @Override
    public InteractionResult click(IMultiblockContext<State> ctx, BlockPos pos, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient)
    {
        return InteractionResult.FAIL;
    }

    @Override
    public void dropExtraItems(State state, Consumer<ItemStack> drop)
    {
        MBInventoryUtils.dropItems(state.getInventory(), drop);
    }

    @Nullable
    @Override
    public List<Component> getOverlayText(State state, Player player, boolean b) {
        return List.of();
    }

    public static class State implements IMultiblockState, ProcessContext.ProcessContextInWorld, ContainerData
    {
        public static final int MAX_PROCESS_TIME = 0;
        public static final int PROCESS_TIME = 1;
        public static final int NUM_SLOTS = 6;

        private int process = 0;
        private int processMax = 0;

        private static BoilerTank tanks = new BoilerTank();
        private final StoredCapability<IFluidHandler> inputCapFuel;
        private final StoredCapability<IFluidHandler> inputCapWater;
        private final StoredCapability<IFluidHandler> inputCapSteam;

        private final SlotwiseItemHandler inventory;

        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();

        public State(IInitialMultiblockContext<State> ctx)
        {
            final Supplier<@Nullable Level> getLevel = ctx.levelSupplier();
            ImmutableList.Builder<CapabilityReference<IEnergyStorage>> outputs = ImmutableList.builder();
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            this.inputCapFuel = new StoredCapability<>(new ArrayFluidHandler(false, true, markDirty, this.tanks.fuelInput));
            this.inputCapWater = new StoredCapability<>(new ArrayFluidHandler(false, true, markDirty, this.tanks.waterInput));
            this.inputCapSteam = new StoredCapability<>(new ArrayFluidHandler(true, false, markDirty, this.tanks.output));
            inventory = new SlotwiseItemHandler(
                    List.of(
                            SlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            SlotwiseItemHandler.IOConstraint.OUTPUT,
                            SlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            SlotwiseItemHandler.IOConstraint.OUTPUT,
                            SlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            SlotwiseItemHandler.IOConstraint.OUTPUT
                    ),
                    ctx.getMarkDirtyRunnable()
            );
        }

        @Nullable
        @Override
        public AveragingEnergyStorage getEnergy() {
            return null;
        }

        public SlotwiseItemHandler getInventory()
        {
            return inventory;
        }

        public BoilerTank getTanks()
        {
            return tanks;
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt)
        {
            nbt.put("tanks", this.tanks.toNBT());
        }

        @Override
        public void readSaveNBT(CompoundTag nbt)
        {
            this.tanks.readNBT(nbt.getCompound("tanks"));
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt)
        {
            writeSaveNBT(nbt);
        }

        @Override
        public void readSyncNBT(CompoundTag nbt)
        {
            readSaveNBT(nbt);
        }

        @Override
        public int get(int index)
        {
            return switch(index)
            {
                case MAX_PROCESS_TIME -> processMax;
                case PROCESS_TIME -> process;
                default -> throw new IllegalArgumentException("Unknown index "+index);
            };
        }

        @Override
        public void set(int index, int value)
        {
            switch(index)
            {
                case MAX_PROCESS_TIME -> processMax = value;
                case PROCESS_TIME -> process = value;
                default -> throw new IllegalArgumentException("Unknown index "+index);
            }
        }

        @Override
        public int getCount() {
            return 0;
        }
    }

    public record BoilerTank(FluidTank output, FluidTank fuelInput, FluidTank waterInput)
    {
        public BoilerTank()
        {
            this(new FluidTank(TANK_CAPACITY), new FluidTank(TANK_CAPACITY), new FluidTank(TANK_CAPACITY));
        }

        public BoilerTank(FluidTank output, FluidTank fuelInput, FluidTank waterInput)
        {
            this.output = output;
            this.fuelInput = fuelInput;
            this.waterInput = waterInput;
        }

        public Tag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("out", this.output.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) {
            this.output.readFromNBT(tag.getCompound("out"));
        }

        public FluidTank output() {
            return this.output;
        }

        public BlockPos getFuelTankPos(boolean isMirrored) {
            BlockPos pos = new BlockPos(-4, 1, 0);
            if (isMirrored) pos = new BlockPos(3, 1, 0);
            return pos;
        }

        public BlockPos getWaterTankPos(boolean isMirrored) {
            BlockPos pos = new BlockPos(0, 1, -3);
            if (isMirrored) pos = new BlockPos(-1, 1, -3);
            return pos;
        }

        public BlockPos getOutputTankPos(boolean isMirrored) {
            BlockPos pos = new BlockPos(1, 0, 4);
            if (isMirrored) pos = new BlockPos(-1, 0, -1);
            return pos;
        }

        public int getCapacity() {
            return TANK_CAPACITY;
        }
    }
}
