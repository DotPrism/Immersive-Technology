package mctmods.immersivetech.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.api.energy.NullEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityDummy;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.sound.MultiblockSound;
import com.google.common.collect.ImmutableList;
import mctmods.immersivetech.common.blocks.multiblocks.shapes.FullblockShape;
import mctmods.immersivetech.core.lib.ITLib;
import mctmods.immersivetech.core.registration.ITSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ITAlternatorLogic implements IMultiblockLogic<ITAlternatorLogic.State>, IServerTickableComponent<ITAlternatorLogic.State>, IClientTickableComponent<ITAlternatorLogic.State>
{
    private static final List<BlockPos> ENERGY_OUTPUTS_LEFT = List.of(new BlockPos(-1, 0, 3), new BlockPos(-1, 1, 3), new BlockPos(-1, 2, 3));
    private static final List<BlockPos> ENERGY_OUTPUTS_RIGHT = List.of(new BlockPos(3, 0, 3), new BlockPos(3, 1, 3), new BlockPos(3, 2, 3));

    public static final int ENERGY_CAPACITY = 1200000;
    public static final BlockPos MASTER_OFFSET = new BlockPos(0,0,0);
    public static final BlockPos ROTATIONAL_INPUT_OFFSET = new BlockPos(1,1,-1);

    @Override
    public void tickClient(IMultiblockContext<State> ctx)
    {
        final State state = ctx.getState();
        boolean active = state.active;

        if (active)
        {
            if(!state.isSoundPlaying.getAsBoolean())
            {
                final Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(2.5, 1.5, 1.5));
                state.isSoundPlaying = MultiblockSound.startSound(
                        () -> state.active, ctx.isValid(), soundPos, ITSounds.alternator, 0.5f
                );
            }
        }
    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx)
    {
        final State state = ctx.getState();
        boolean active = state.active;
        IMultiblockLevel multiblockLevel = ctx.getLevel();
        Level level = multiblockLevel.getRawLevel();

        int output = 8192;

        BlockPos turbineAbsolutePos = multiblockLevel.toAbsolute(ROTATIONAL_INPUT_OFFSET);

        BlockEntity entity = level.getBlockEntity(turbineAbsolutePos);

        if (entity instanceof MultiblockBlockEntityMaster<?> master)
        {
            if (master.getHelper().getState() instanceof ITSteamTurbineLogic.State stateTurbine)
            {
                if (stateTurbine.isActive())
                    state.active = true;
                else if (!stateTurbine.isActive())
                    state.active = false;
            }
            if (master.getHelper().getState() instanceof ITGasTurbineLogic.State stateTurbine)
            {
                if (stateTurbine.isActive())
                    state.active = true;
                else if (!stateTurbine.isActive())
                    state.active = false;
            }
        }
        if (active)
        {
            List<IEnergyStorage> presentOutputs = state.energyOutputs.stream()
                    .map(CapabilityReference::getNullable)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            List<IEnergyStorage> presentOutputs2 = state.energyOutputs2.stream()
                    .map(CapabilityReference::getNullable)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!presentOutputs.isEmpty() && EnergyHelper.distributeFlux(presentOutputs, output, true) < output)
            {
                int i = EnergyHelper.distributeFlux(presentOutputs, output, false);
            }

            if (!presentOutputs2.isEmpty() && EnergyHelper.distributeFlux(presentOutputs2, output, true) < output)
            {
                int i2 = EnergyHelper.distributeFlux(presentOutputs2, output, false);
            }
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
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
        if(cap==ForgeCapabilities.ENERGY)
        {
            if(position.side()==null||(
                    position.side()==RelativeBlockFace.RIGHT&&ENERGY_OUTPUTS_LEFT.contains(position.posInMultiblock()))||(
                    position.side()==RelativeBlockFace.LEFT&&ENERGY_OUTPUTS_RIGHT.contains(position.posInMultiblock()
                    )))
            {
                return ctx.getState().energyCap.cast(ctx);
            }
        }

        return LazyOptional.empty();
    }

    public static class State implements IMultiblockState
    {
        public final MutableEnergyStorage energy = new MutableEnergyStorage(ENERGY_CAPACITY, 8192, 4096);
        private final List<CapabilityReference<IEnergyStorage>> energyOutputs;
        private final List<CapabilityReference<IEnergyStorage>> energyOutputs2;

        private boolean active = false;
        private final StoredCapability<IEnergyStorage> energyCap;

        private BooleanSupplier isSoundPlaying = () -> false;

        public State(IInitialMultiblockContext<State> ctx)
        {
            this.energyCap = new StoredCapability<>(NullEnergyStorage.INSTANCE);
            ImmutableList.Builder<CapabilityReference<IEnergyStorage>> outputs = ImmutableList.builder();
            ImmutableList.Builder<CapabilityReference<IEnergyStorage>> outputs2 = ImmutableList.builder();
            for(BlockPos pos : ENERGY_OUTPUTS_LEFT)
            {
                outputs.add(ctx.getCapabilityAt(ForgeCapabilities.ENERGY, pos, RelativeBlockFace.LEFT));
            }
            for(BlockPos pos : ENERGY_OUTPUTS_RIGHT)
            {
                outputs2.add(ctx.getCapabilityAt(ForgeCapabilities.ENERGY, pos, RelativeBlockFace.RIGHT));
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
            nbt.putBoolean("active", active);
        }

        @Override
        public void readSyncNBT(CompoundTag nbt)
        {
            active = nbt.getBoolean("active");
        }
    }
}
