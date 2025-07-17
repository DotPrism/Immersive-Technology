package mctmods.immersivetech.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.client.utils.TextUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.interfaces.MBOverlayText;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.DistillerRecipe;
import mctmods.immersivetech.common.blocks.multiblocks.shapes.FullblockShape;
import mctmods.immersivetech.core.lib.ITLib;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ITDistillerLogic implements IMultiblockLogic<ITDistillerLogic.State>, IServerTickableComponent<ITDistillerLogic.State>, IClientTickableComponent<ITDistillerLogic.State>, MBOverlayText<ITDistillerLogic.State>
{
    public static final BlockPos MASTER_OFFSET = new BlockPos(0,0,0);
    public static final BlockPos REDSTONE_POS = new BlockPos(0, 1, 2);
    private static final CapabilityPosition FLUID_OUTPUT_CAP = new CapabilityPosition(0, 2, 1, RelativeBlockFace.RIGHT);

    private static final Set<CapabilityPosition> FLUID_INPUT_CAPS = Set.of(
            new CapabilityPosition(4, 0, 1, RelativeBlockFace.LEFT)
    );
    private static final Set<BlockPos> FLUID_INPUTS = FLUID_INPUT_CAPS.stream()
            .map(CapabilityPosition::posInMultiblock)
            .collect(Collectors.toSet());
    public static final int TANK_CAPACITY = 24* FluidType.BUCKET_VOLUME;
    private static final int ENERGY_CAPACITY = 32000;
    public static final int NUM_SLOTS = 4;
    public static final int SLOT_WATER_IN = 0;
    public static final int SLOT_WATER_EMPTY_OUT = 1;
    public static final int SLOT_DISTILLED_WATER_IN = 2;
    public static final int SLOT_DISTILLED_WATER_OUT = 3;

    private void tryEnqueueProcess(State state, Level level, DistillerRecipe recipe)
    {
        if(state.processor.getQueueSize() >= state.processor.getMaxQueueSize())
            return;
        final FluidStack leftInput = state.tanks.waterInput.getFluid();
        if(leftInput.isEmpty())
            return;
        if(recipe==null)
            return;

        MultiblockProcessInMachine<DistillerRecipe> process = new MultiblockProcessInMachine<>(recipe);
        if(!leftInput.isEmpty())
            process.setInputTanks(0);
        state.processor.addProcessToQueue(process, level, false);
    }

    @Nullable
    @Override
    public List<Component> getOverlayText(State state, Player player, boolean b)
    {
        if(Utils.isFluidRelatedItemStack(player.getItemInHand(InteractionHand.MAIN_HAND)))
            return List.of(TextUtils.formatFluidStack(state.tanks.waterInput.getFluid()), TextUtils.formatFluidStack(state.tanks.output.getFluid()), Component.literal("Processes: " + state.processor.getQueueSize()));
        return null;
    }

    @Override
    public void tickClient(IMultiblockContext<State> iMultiblockContext)
    {

    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx)
    {
        final State state = ctx.getState();
        final boolean wasActive = state.active;
        state.active = state.processor.tickServer(state, ctx.getLevel(), state.rsState.isEnabled(ctx));
        DistillerRecipe recipe = DistillerRecipe.findRecipe(ctx.getLevel().getRawLevel(), state.tanks.waterInput.getFluid());
        if(wasActive!=state.active)
            ctx.requestMasterBESync();
        tryEnqueueProcess(state, ctx.getLevel().getRawLevel(), recipe);
        FluidUtils.multiblockFluidOutput(
                state.fluidOutput, state.tanks.output(), SLOT_DISTILLED_WATER_IN, SLOT_DISTILLED_WATER_OUT, state.inventory
        );
    }

    @Override
    public State createInitialState(IInitialMultiblockContext<State> iInitialMultiblockContext)
    {
        return new State(iInitialMultiblockContext);
    }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType)
    {
        return FullblockShape.GETTER;
    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {

        final State state = ctx.getState();
        if(cap==ForgeCapabilities.FLUID_HANDLER)
        {
            if(FLUID_OUTPUT_CAP.equals(position))
                return state.outputCapSteam.cast(ctx);
            else if(FLUID_INPUT_CAPS.contains(position))
                return state.inputCap.cast(ctx);
        }
        return LazyOptional.empty();
    }

    @Override
    public void dropExtraItems(State state, Consumer<ItemStack> drop)
    {
        MBInventoryUtils.dropItems(state.getInventory(), drop);
    }

    public static class State implements IMultiblockState, ProcessContext.ProcessContextInMachine<DistillerRecipe>
    {
        public final AveragingEnergyStorage energy = new AveragingEnergyStorage(ENERGY_CAPACITY);

        public boolean active;

        private final StoredCapability<IFluidHandler> inputCap;
        private final StoredCapability<IFluidHandler> outputCapSteam;
        private final CapabilityReference<IFluidHandler> fluidOutput;

        public static final DistillerTank tanks = new DistillerTank();

        private final MultiblockProcessor.InMachineProcessor<DistillerRecipe> processor;

        private BooleanSupplier isSoundPlaying = () -> false;

        private final SlotwiseItemHandler inventory;

        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();

        public State(IInitialMultiblockContext<ITDistillerLogic.State> ctx)
        {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            this.processor = new MultiblockProcessor.InMachineProcessor<>(1, 0, 1, markDirty, DistillerRecipe.RECIPES::getById);
            this.inputCap = new StoredCapability<>(new ArrayFluidHandler(false, true, markDirty, this.tanks.waterInput));
            this.outputCapSteam = new StoredCapability<>(new ArrayFluidHandler(true, false, markDirty, this.tanks.output));
            inventory = new SlotwiseItemHandler(
                    List.of(
                            SlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            SlotwiseItemHandler.IOConstraint.OUTPUT,
                            SlotwiseItemHandler.IOConstraint.ANY_INPUT,
                            SlotwiseItemHandler.IOConstraint.OUTPUT
                    ),
                    ctx.getMarkDirtyRunnable()
            );
            this.fluidOutput = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, new MultiblockFace(FLUID_OUTPUT_CAP.side(), FLUID_OUTPUT_CAP.posInMultiblock().east()));
        }

        @Override
        public int[] getOutputTanks()
        {
            return new int[]{2};
        }

        @Override
        public AveragingEnergyStorage getEnergy() {
            return energy;
        }

        public SlotwiseItemHandler getInventory()
        {
            return inventory;
        }

        public DistillerTank getTanks()
        {
            return tanks;
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt)
        {
            nbt.put("energy", energy.serializeNBT());
            nbt.put("tanks", this.tanks.toNBT());
            nbt.put("processor", processor.toNBT());
            nbt.put("inventory", inventory.serializeNBT());
        }

        @Override
        public void readSaveNBT(CompoundTag nbt)
        {
            energy.deserializeNBT(nbt.get("energy"));
            this.tanks.readNBT(nbt.getCompound("tanks"));
            this.processor.fromNBT(nbt.get("processor"), MultiblockProcessInMachine::new);
            this.inventory.deserializeNBT(nbt.getCompound("inventory"));

        }

        @Override
        public void writeSyncNBT(CompoundTag nbt)
        {
            nbt.putBoolean("active", active);
        }

        @Override
        public void readSyncNBT(CompoundTag nbt)
        {
            active = nbt.getBoolean("active");
        }

        @Override
        public void onProcessFinish(MultiblockProcess<DistillerRecipe, ?> process, Level level)
        {
            try {
                DistillerRecipe recipe = process.getRecipe(level);
                assert recipe != null;
                tanks.output.fill(recipe.output, IFluidHandler.FluidAction.EXECUTE);
            } catch(Exception error)
            {
                ITLib.IT_LOGGER.error("Error: {}", error.getMessage());
            }
        }
    }

    public record DistillerTank(FluidTank output, FluidTank waterInput)
    {
        public DistillerTank()
        {
            this(new FluidTank(TANK_CAPACITY), new FluidTank(TANK_CAPACITY));
        }

        public DistillerTank(FluidTank output, FluidTank waterInput)
        {
            this.output = output;
            this.waterInput = waterInput;
        }

        public Tag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("waterIn", this.waterInput.writeToNBT(new CompoundTag()));
            tag.put("out", this.output.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) {
            this.waterInput.readFromNBT(tag.getCompound("waterIn"));
            this.output.readFromNBT(tag.getCompound("out"));
        }

        public int getCapacity() {
            return TANK_CAPACITY;
        }
    }
}
