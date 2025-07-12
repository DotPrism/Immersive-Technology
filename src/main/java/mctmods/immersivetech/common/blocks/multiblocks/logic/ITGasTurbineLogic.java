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
import mctmods.immersivetech.common.blocks.multiblocks.recipe.BoilerRecipe;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.GasTurbineRecipe;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.SteamTurbineRecipe;
import mctmods.immersivetech.common.blocks.multiblocks.shapes.FullblockShape;
import mctmods.immersivetech.common.blocks.multiblocks.shapes.GasTurbineShape;
import mctmods.immersivetech.common.blocks.multiblocks.shapes.GenericShape;
import mctmods.immersivetech.core.registration.ITSounds;
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
    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx)
    {
        final State state = ctx.getState();
        boolean active = state.isActive();

        boolean isRSEnabled = state.rsState.isEnabled(ctx);
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
        private final AveragingEnergyStorage energyStorageHV;
        private final AveragingEnergyStorage energyStorageMV;
        private final BiFunction<Level, Fluid, GasTurbineRecipe> recipeGetter = CachedRecipe.cached(GasTurbineRecipe::getRecipeFor);

        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public int speed;
        public boolean ignited;
        public boolean starterRunning = false;
        public int maxSpeed = 1800;

        private static final int speedGainPerTick = 3;
        private static final int speedLossPerTick = 6;

        private boolean active = false;
        public float animation_fanRotationStep = 0;
        public float animation_fanRotation = 0;
        private int animation_fanFadeIn = 0;
        private int animation_fanFadeOut = 0;

        private float base = 180f;

        public State(IInitialMultiblockContext<State> ctx)
        {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            this.energyStorageHV = new AveragingEnergyStorage(ENERGY_CAPACITY);
            this.energyStorageMV = new AveragingEnergyStorage(ENERGY_CAPACITY_MV);
            this.energyCapHV = new StoredCapability<>(energyStorageHV);
            this.energyCapMV = new StoredCapability<>(energyStorageMV);
            Set<Fluid> allowedFuels = Set.of(IEFluids.BIODIESEL.getStill());
            this.tanks.input.setValidator(f -> allowedFuels.contains(f.getFluid()));
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
            final boolean oldActive = active;
            active = nbt.getBoolean("active");
            if(active&&!oldActive)
                animation_fanFadeIn = 80;
            else if(!active&&oldActive)
                animation_fanFadeOut = 80;
        }

        public boolean isActive()
        {
            return active;
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
            tag.put("steam", this.input.writeToNBT(new CompoundTag()));
            tag.put("exhaust_steam", this.flue_gas_tank.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) {
            this.input.readFromNBT(tag.getCompound("steam"));
            this.flue_gas_tank.readFromNBT(tag.getCompound("exhaust_steam"));
        }

        public int getCapacity() {
            return TANK_CAPACITY;
        }
    }
}
