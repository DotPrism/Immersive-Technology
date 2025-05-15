package mctmods.immersivetech.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.energy.NullEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import com.google.common.collect.ImmutableList;
import mctmods.immersivetech.common.blocks.multiblocks.shapes.FullblockShape;
import mctmods.immersivetech.core.lib.ITLib;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ITAlternatorLogic implements IMultiblockLogic<ITAlternatorLogic.State>, IServerTickableComponent<ITAlternatorLogic.State>, IClientTickableComponent<ITAlternatorLogic.State>
{
    private static final List<BlockPos> ENERGY_OUTPUTS_LEFT = List.of(new BlockPos(0, 0, 3), new BlockPos(0, 1, 3), new BlockPos(0, 2, 3));
    private static final List<BlockPos> ENERGY_OUTPUTS_RIGHT = List.of(new BlockPos(2, 0, 3), new BlockPos(2, 1, 3), new BlockPos(2, 2, 3));

    public static final int ENERGY_CAPACITY = 1200000;
    public static final BlockPos MASTER_OFFSET = new BlockPos(0,0,0);

    @Override
    public void tickClient(IMultiblockContext<State> context)
    {

    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx)
    {
        ITLib.IT_LOGGER.info("Alternator: tickServer Start");
        final State state = ctx.getState();
        boolean active = ctx.getState().active;

        int output = 4096;

        state.energy.receiveEnergy(8192, false);
        List<IEnergyStorage> presentOutputs = state.energyOutputs.stream()
                .map(CapabilityReference::getNullable)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<IEnergyStorage> presentOutputs2 = state.energyOutputs2.stream()
                .map(CapabilityReference::getNullable)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!presentOutputs.isEmpty() && EnergyHelper.distributeFlux(presentOutputs, output, true) < ENERGY_CAPACITY) {
            // used for debugging
            //int i = EnergyHelper.distributeFlux(presentOutputs, output, false);
            //ITLib.IT_LOGGER.info("Alternator: tickServer Outputting energy 1: " + i);
            //

            EnergyHelper.distributeFlux(presentOutputs, output, false);
        }
        if (!presentOutputs2.isEmpty() && EnergyHelper.distributeFlux(presentOutputs2, output, true) < ENERGY_CAPACITY) {
            // used for debugging
            //int i = EnergyHelper.distributeFlux(presentOutputs2, output, false);
            //ITLib.IT_LOGGER.info("Alternator: tickServer Outputting energy 2: " + i);
            //

            EnergyHelper.distributeFlux(presentOutputs2, output, false);
        }
        // this just updates things, avoid running this if possible but it *is* required if you want things to sync properly
        ctx.markMasterDirty();
        ctx.requestMasterBESync();
    }

    @Override
    public State createInitialState(IInitialMultiblockContext<State> context)
    {
        return new ITAlternatorLogic.State(context);
    }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType)
    {
        return FullblockShape.GETTER;
    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap)
    {
        ITLib.IT_LOGGER.info("Alternator: getCapability start");
        final State state = ctx.getState();

        if(cap==ForgeCapabilities.ENERGY)
        {
            if(position.side()==null||(
                    position.side()==RelativeBlockFace.RIGHT&&ENERGY_OUTPUTS_LEFT.contains(position.posInMultiblock())
            ))
            {
                ITLib.IT_LOGGER.info("Alternator: getCapability returning energyCap.cast()");
                return state.energyCap.cast(ctx);
            }
            if(position.side()==null||(
                    position.side()==RelativeBlockFace.LEFT&&ENERGY_OUTPUTS_RIGHT.contains(position.posInMultiblock())
            ))
            {
                ITLib.IT_LOGGER.info("Alternator: getCapability returning energyCap.cast()");
                return state.energyCap.cast(ctx);
            }
        }

        return LazyOptional.empty();
    }

    public static class State implements IMultiblockState
    {
        public final AveragingEnergyStorage energy = new AveragingEnergyStorage(ENERGY_CAPACITY);
        private final List<CapabilityReference<IEnergyStorage>> energyOutputs;
        private final List<CapabilityReference<IEnergyStorage>> energyOutputs2;

        private boolean active = true;
        private final StoredCapability<IEnergyStorage> energyCap;

        public State(IInitialMultiblockContext<State> ctx)
        {
            ITLib.IT_LOGGER.info("Alternator: State");
            this.energyCap = new StoredCapability<>(this.energy);
            ImmutableList.Builder<CapabilityReference<IEnergyStorage>> outputs = ImmutableList.builder();
            ImmutableList.Builder<CapabilityReference<IEnergyStorage>> outputs2 = ImmutableList.builder();
            for(BlockPos pos : ENERGY_OUTPUTS_LEFT) {
                outputs.add(ctx.getCapabilityAt(ForgeCapabilities.ENERGY, pos, RelativeBlockFace.RIGHT));
                ITLib.IT_LOGGER.info("Alternator: State() LEFT");
                ITLib.IT_LOGGER.info("Alternator: State() energy 1: x " + pos.getX());
                ITLib.IT_LOGGER.info("Alternator: State() energy 1: y " + pos.getY());
                ITLib.IT_LOGGER.info("Alternator: State() energy 1: z " + pos.getZ());
            }
            for(BlockPos pos : ENERGY_OUTPUTS_RIGHT) {
                outputs2.add(ctx.getCapabilityAt(ForgeCapabilities.ENERGY, pos, RelativeBlockFace.LEFT));
                ITLib.IT_LOGGER.info("Alternator: State() RIGHT");
                ITLib.IT_LOGGER.info("Alternator: State() energy 2: x" + pos.getX());
                ITLib.IT_LOGGER.info("Alternator: State() energy 2: y" + pos.getY());
                ITLib.IT_LOGGER.info("Alternator: State() energy 2: z" + pos.getZ());
            }
            this.energyOutputs = outputs.build();
            this.energyOutputs2 = outputs2.build();
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt)
        {
            EnergyHelper.serializeTo(energy, nbt);
        }

        @Override
        public void readSaveNBT(CompoundTag nbt)
        {
            EnergyHelper.deserializeFrom(energy, nbt);
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
    }
}
