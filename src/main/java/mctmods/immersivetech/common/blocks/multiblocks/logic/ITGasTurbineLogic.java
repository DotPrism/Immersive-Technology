package mctmods.immersivetech.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.energy.NullEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.util.CachedRecipe;
import blusunrize.immersiveengineering.common.util.sound.MultiblockSound;
import io.netty.buffer.Unpooled;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.BoilerRecipe;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.GasTurbineRecipe;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.SteamTurbineRecipe;
import mctmods.immersivetech.common.blocks.multiblocks.shapes.FullblockShape;
import mctmods.immersivetech.common.blocks.multiblocks.shapes.GasTurbineShape;
import mctmods.immersivetech.common.blocks.multiblocks.shapes.GenericShape;
import mctmods.immersivetech.core.lib.ITLib;
import mctmods.immersivetech.core.lib.ITMultiblockSound;
import mctmods.immersivetech.core.registration.ITFluids;
import mctmods.immersivetech.core.registration.ITSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class ITGasTurbineLogic implements IMultiblockLogic<ITGasTurbineLogic.State>, IServerTickableComponent<ITGasTurbineLogic.State>, IClientTickableComponent<ITGasTurbineLogic.State>
{
    private static final BlockPos MASTER_POS = new BlockPos(1, 1, 0);
    private static final List<BlockPos> FLUID_POS = List.of(new BlockPos(2, 1, 7));
    private static final List<BlockPos> FLUID_POS2 = List.of(new BlockPos(1, 0, 0));
    public static final BlockPos REDSTONE_POS = new BlockPos(0, 1, 7);
    private static final Set<CapabilityPosition> ENERGY_INPUTS_HV = Set.of(
            new CapabilityPosition(2,0,5, RelativeBlockFace.LEFT));
    private static final Set<CapabilityPosition> ENERGY_INPUTS_MV = Set.of(
            new CapabilityPosition(0,0,5, RelativeBlockFace.RIGHT));

    public static final int TANK_CAPACITY = 12* FluidType.BUCKET_VOLUME;
    private static final int ENERGY_CAPACITY = 8196;
    private static final int ENERGY_CAPACITY_MV = 2048;

    @Override
    public void tickClient(IMultiblockContext<State> ctx)
    {
        final State state = ctx.getState();

        float level = ITLib.remapRange(0, state.maxSpeed, 0.5f, 1.5f, state.speed);

        if(!state.isSoundPlayingActive.getAsBoolean())
        {
            final Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(2.5, 1.5, 1.5));
            state.isSoundPlayingActive = ITMultiblockSound.startSound(
                    () -> (state.speed >= 0 && state.ignited), ctx.isValid(), soundPos, ITSounds.gasRunning, 1f, level
            );
        }
        if(!state.isSoundPlayingSpark.getAsBoolean())
        {
            final Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(2.5, 1.5, 1.5));
            state.isSoundPlayingSpark = ITMultiblockSound.startSound(
                    () -> state.ignited, ctx.isValid(), soundPos, ITSounds.gasSpark, 1f, 1f
            );
        }
        if(!state.isSoundPlayingArc.getAsBoolean())
        {
            final Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(2.5, 1.5, 1.5));
            state.isSoundPlayingArc = ITMultiblockSound.startSound(
                    () -> (state.speed >= state.maxSpeed / 4), ctx.isValid(), soundPos, ITSounds.gasArc, 1f, 1f
            );
        }
        if(!state.isSoundPlayingStarter.getAsBoolean())
        {
            final Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(2.5, 1.5, 1.5));
            state.isSoundPlayingStarter = ITMultiblockSound.startSound(
                    () -> state.starterRunning, ctx.isValid(), soundPos, ITSounds.gasStarter, 1f, 1f
            );
        }
    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx)
    {
        final State state = ctx.getState();

        boolean isRSEnabled = state.rsState.isEnabled(ctx);

        state.rotationSpeed = state.speed == 0 ? 0f : ((float) state.speed / (float) state.maxSpeed) * state.maxRotationSpeed;

        state.ignited = state.ignitionGracePeriod > 0;
        boolean canRun = !isRSEnabled;
        if (canRun && state.electricStarterConsumption <= state.starterStorage.getEnergyStored()) {
            state.starterRunning = true;
            state.starterStorage.extractEnergy(state.electricStarterConsumption, false);
        } else state.starterRunning = false;

        if (state.speed < state.maxSpeed / 4) {
            if (canRun) {
                if (state.ignitionGracePeriod > 0) state.ignitionGracePeriod--;
                state.speedUp();
            } else state.speedDown();
        } else {
            if(state.burnRemaining > 0 && (state.ignited || state.canIgnite())) {
                state.burnRemaining--;
                if (!state.ignited) state.ignite();
                state.speedUp();
            } else if(canRun && state.tanks.input.getFluid() != null && state.tanks.input.getFluid().getFluid() != null && (state.ignited || state.canIgnite())) {
                GasTurbineRecipe recipe = state.recipeGetter.apply(
                        ctx.getLevel().getRawLevel(), state.tanks.input.getFluid().getFluid()
                );
                if(recipe != null && recipe.fluidInput.getAmount() <= state.tanks.input.getFluidAmount()) {
                    state.lastRecipe = recipe;
                    state.burnRemaining = recipe.getTotalProcessTime() - 1;
                    state.tanks.input.drain(recipe.fluidInput.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                    if(recipe.fluidOutput != null) state.tanks.flue_gas_tank.fill(recipe.fluidOutput, IFluidHandler.FluidAction.EXECUTE);
                    if (!state.ignited) state.ignite();
                    ctx.markMasterDirty();
                    ctx.requestMasterBESync();
                    state.speedUp();
                } else state.speedDown();
            } else state.speedDown();
        }
    }

    @Override
    public State createInitialState(IInitialMultiblockContext<State> ctx) {
        return new State(ctx);
    }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) {
        return GasTurbineShape.GETTER;
    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap)
    {
        final State state = ctx.getState();
        if(cap == ForgeCapabilities.ENERGY && (position.side()==null || ENERGY_INPUTS_HV.contains(position)))
        {
            return state.energyCapHV.cast(ctx);
        }
        if(cap == ForgeCapabilities.ENERGY && (position.side()==null || ENERGY_INPUTS_MV.contains(position)))
        {
            return state.energyCapMV.cast(ctx);
        }
        if(cap == ForgeCapabilities.FLUID_HANDLER)
        {
            if(position.side()==null||(position.side()== RelativeBlockFace.BACK&&FLUID_POS.contains(position.posInMultiblock())))
                return ctx.getState().fluidCap.cast(ctx);
            else if(position.side()==null||(position.side()==RelativeBlockFace.FRONT&&FLUID_POS2.contains(position.posInMultiblock())))
                return ctx.getState().fluidCapExhaust.cast(ctx);
        }

        return LazyOptional.empty();
    }

    public class State implements IMultiblockState
    {
        private final GasTurbineTank tanks = new GasTurbineTank();
        private final StoredCapability<IFluidHandler> fluidCap = new StoredCapability<>(tanks.input);
        private final StoredCapability<IFluidHandler> fluidCapExhaust = new StoredCapability<>(tanks.flue_gas_tank);
        private final StoredCapability<IEnergyStorage> energyCapHV;
        private final StoredCapability<IEnergyStorage> energyCapMV;
        private final AveragingEnergyStorage starterStorage;
        private final AveragingEnergyStorage sparkplugStorage;
        private final BiFunction<Level, Fluid, GasTurbineRecipe> recipeGetter = CachedRecipe.cached(GasTurbineRecipe::getRecipeFor);

        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public int speed;
        public boolean ignited;
        public boolean starterRunning = false;
        public int maxSpeed = 1800;

        public Set<Fluid> allowedFuels = Set.of(IEFluids.BIODIESEL.getStill());

        private static final int speedGainPerTick = 3;
        private static final int speedLossPerTick = 6;
        public float maxRotationSpeed = 72;
        public int burnRemaining;

        public int ignitionGracePeriod = 0;
        int sparkplugConsumption = 1024;
        int electricStarterConsumption = 3072;

        public float rotationSpeed;

        public GasTurbineRecipe lastRecipe;

        private BooleanSupplier isSoundPlayingActive = () -> false;
        private BooleanSupplier isSoundPlayingSpark = () -> false;
        private BooleanSupplier isSoundPlayingArc = () -> false;
        private BooleanSupplier isSoundPlayingStarter = () -> false;

        boolean active = false;

        public State(IInitialMultiblockContext<State> ctx)
        {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            this.starterStorage = new AveragingEnergyStorage(ENERGY_CAPACITY);
            this.sparkplugStorage = new AveragingEnergyStorage(ENERGY_CAPACITY_MV);
            this.energyCapHV = new StoredCapability<>(starterStorage);
            this.energyCapMV = new StoredCapability<>(sparkplugStorage);
            this.tanks.input.setValidator(f -> allowedFuels.contains(f.getFluid()));
        }

        public void ignite() {
            sparkplugStorage.extractEnergy(sparkplugConsumption, false);
            ignited = true;
            ignitionGracePeriod = 60;
        }

        public boolean canIgnite() {
            boolean canFuelCombust = true;
            return sparkplugConsumption <= sparkplugStorage.getEnergyStored() && canFuelCombust;
        }

        private void speedUp() {
            if (starterRunning) {
                if (speed >= maxSpeed / 4) speed = Math.max(Math.min(maxSpeed, speed + speedGainPerTick - speedLossPerTick), maxSpeed / 4);
                else speed = Math.min(maxSpeed / 4, speed + speedGainPerTick);
            } else {
                if (speed >= maxSpeed / 4) speed = Math.min(maxSpeed, speed + speedGainPerTick);
                else speedDown();
            }
        }

        private void speedDown() {
            if (ignitionGracePeriod > 0) ignitionGracePeriod--;
            speed = Math.max(0, speed - speedLossPerTick);
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt)
        {
            nbt.putInt("speed", speed);
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT());
        }

        @Override
        public void readSaveNBT(CompoundTag nbt)
        {
            speed = nbt.getInt("speed");
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"));
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

        public boolean isActive()
        {
            return (speed >= 0 && ignited);
        }
    }

    public record GasTurbineTank(FluidTank input, FluidTank flue_gas_tank)
    {
        public GasTurbineTank()
        {
            this(new FluidTank(TANK_CAPACITY), new FluidTank(TANK_CAPACITY));
        }

        public GasTurbineTank(FluidTank input, FluidTank flue_gas_tank)
        {
            this.input = input;
            this.flue_gas_tank = flue_gas_tank;
        }

        public Tag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("input", this.input.writeToNBT(new CompoundTag()));
            tag.put("flue_gas", this.flue_gas_tank.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) {
            this.input.readFromNBT(tag.getCompound("input"));
            this.flue_gas_tank.readFromNBT(tag.getCompound("flue_gas"));
        }

        public int getCapacity() {
            return TANK_CAPACITY;
        }
    }
}
