package mctmods.immersivetech.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
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
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.util.CachedRecipe;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.GasTurbineRecipe;
import mctmods.immersivetech.common.blocks.multiblocks.shapes.GasTurbineShape;
import mctmods.immersivetech.core.lib.ITLib;
import mctmods.immersivetech.core.lib.ITMultiblockSound;
import mctmods.immersivetech.core.registration.ITFluids;
import mctmods.immersivetech.core.registration.ITSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
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
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
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
    private static final int ENERGY_CAPACITY = 8192;
    private static final int ENERGY_CAPACITY_MV = 2048;

    private static final int electricStarterConsumption = 4096;
    private static final int sparkplugConsumption = 1024;

    @Override
    public void tickClient(IMultiblockContext<State> ctx)
    {
        final State state = ctx.getState();
        final Level level = ctx.getLevel().getRawLevel();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        float targetLevel = ITLib.remapRange(0, state.maxSpeed, 0.5f, 1.0f, state.speed);
        if (state.currentLevel == 0f) state.currentLevel = targetLevel;
        else state.currentLevel = state.currentLevel * 0.9f + targetLevel * 0.1f;
        float smoothedLevel = state.currentLevel;

        if(state.active || state.animation_fanFadeIn > 0 || state.animation_fanFadeOut > 0)
        {
            float currentBase = (state.speed / (float)state.maxSpeed) * 72f;
            float step = state.active ? currentBase : 0f;
            if(state.animation_fanFadeIn > 0)
            {
                step -= (80f - state.animation_fanFadeIn) / 80f * currentBase;
                state.animation_fanFadeIn--;
            }
            if(state.animation_fanFadeOut > 0)
            {
                step += (state.animation_fanFadeOut / 80f) * currentBase;
                state.animation_fanFadeOut--;
            }
            state.animation_fanRotationStep = step;
            state.animation_fanRotation += step;
            state.animation_fanRotation %= 360;
        }

        Vec3 runningPos = ctx.getLevel().toAbsolute(new Vec3(1.5, 1.5, 3.5));
        Vec3 starterPos = ctx.getLevel().toAbsolute(new Vec3(1.5, 0.5, 1.5));
        Vec3 arcPos = ctx.getLevel().toAbsolute(new Vec3(0.5, 1.5, 1.5));
        Vec3 ignitionPos = ctx.getLevel().toAbsolute(new Vec3(1.5, 1.5, 1.5));

        if (state.speed > 0) {
            if (!state.runningSoundPlaying.getAsBoolean()) {
                state.runningSoundId++;
                int thisId = state.runningSoundId;
                state.runningSoundPlaying = ITMultiblockSound.startSound(
                        () -> state.speed > 0 && state.runningSoundId == thisId,
                        ctx.isValid(),
                        runningPos,
                        ITSounds.gasRunning,
                        () -> {
                            LocalPlayer p = Minecraft.getInstance().player;
                            if (p == null) return 0f;
                            float a = (float) Math.max(p.distanceToSqr(runningPos) / 8, 1);
                            return (11 * (smoothedLevel - 0.5f)) / a;
                        },
                        () -> smoothedLevel
                );
            }
        }

        if (state.starterRunning) {
            if (!state.starterSoundPlaying.getAsBoolean()) {
                state.starterSoundId++;
                int thisId = state.starterSoundId;
                state.starterSoundPlaying = ITMultiblockSound.startSound(
                        () -> state.starterRunning && state.starterSoundId == thisId,
                        ctx.isValid(),
                        starterPos,
                        ITSounds.gasStarter,
                        () -> {
                            LocalPlayer p = Minecraft.getInstance().player;
                            if (p == null) return 0f;
                            float a = (float) Math.max(p.distanceToSqr(starterPos) / 64, 1);
                            return Math.min((smoothedLevel - 0.5f) / a, 0.2f);
                        },
                        () -> 1f
                );
            }
            if (state.speed >= state.maxSpeed / 4) {
                if (!state.arcSoundPlaying.getAsBoolean()) {
                    state.arcSoundId++;
                    int thisId = state.arcSoundId;
                    state.arcSoundPlaying = ITMultiblockSound.startSound(
                            () -> state.starterRunning && state.speed >= state.maxSpeed / 4 && state.arcSoundId == thisId,
                            ctx.isValid(),
                            arcPos,
                            ITSounds.gasArc,
                            () -> {
                                LocalPlayer p = Minecraft.getInstance().player;
                                if (p == null) return 0f;
                                float a = (float) Math.max(p.distanceToSqr(arcPos) / 64, 1);
                                return Math.min((smoothedLevel - 0.5f) / a, 0.2f);
                            },
                            () -> 1f
                    );
                }
            }
        }

        if (state.ignited && !state.lastIgnited) {
            state.lastIgnited = state.ignited;
            float ignitionAtt = (float) Math.max(player.distanceToSqr(ignitionPos) / 64, 1);
            level.playLocalSound(ignitionPos.x, ignitionPos.y, ignitionPos.z, ITSounds.gasSpark.get(), SoundSource.BLOCKS, 1 / ignitionAtt, 1, false);
        } else {
            state.lastIgnited = state.ignited;
        }

        if(state.starterRunning && state.speed >= state.maxSpeed / 4){
            if (level.random.nextInt(40) == 0) return;
            Vec3 particlePos = ctx.getLevel().toAbsolute(new Vec3(1.5, 0.5, 1.5));
            double distSq = player.distanceToSqr(particlePos);
            if(distSq > 64 * 64) return;
            double px = particlePos.x + 2 - level.random.nextFloat() * 3;
            double py = particlePos.y + 0.5;
            double pz = particlePos.z + 2 - level.random.nextFloat() * 3;
            level.addParticle(ParticleTypes.SMOKE, px, py, pz, 0, 0.02, 0);
        }
    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx)
    {
        final State state = ctx.getState();
        boolean wasActive = state.active;
        state.active = false;

        boolean isRSEnabled = state.rsState.isEnabled(ctx);
        state.ignited = state.ignitionGracePeriod > 0;

        state.starterRunning = false;
        if (isRSEnabled && electricStarterConsumption <= state.energyStorageHV.getEnergyStored()) {
            state.starterRunning = true;
            state.energyStorageHV.extractEnergy(electricStarterConsumption, false);
        }

        if (state.speed <= 0) {
            state.speed = 0;
            state.isShutdown = false;
        }

        if (!isRSEnabled) {
            state.isShutdown = true;
            state.ignitionGracePeriod = 0;
            state.burnRemaining = 0;
        }

        if (state.speed < state.maxSpeed / 4) {
            if (isRSEnabled && !state.isShutdown) {
                if (state.ignitionGracePeriod > 0) state.ignitionGracePeriod--;
                speedUp(state);
            } else {
                speedDown(state);
            }
        } else {
            if (state.isShutdown) {
                speedDown(state);
            } else {
                if (isRSEnabled && state.burnRemaining > 0 && (state.ignited || canIgnite(state))) {
                    state.burnRemaining--;
                    if (!state.ignited) ignite(state, ctx);
                    speedUp(state);
                } else if (isRSEnabled && state.tanks.input.getFluid() != null && (state.ignited || canIgnite(state))) {
                    FluidStack fluid = state.tanks.input.getFluid();
                    GasTurbineRecipe recipe = state.recipeGetter.apply(ctx.getLevel().getRawLevel(), fluid);
                    if (recipe != null && fluid.getAmount() >= recipe.inputAmount) {
                        state.tanks.input.drain(recipe.inputAmount, FluidAction.EXECUTE);
                        if (recipe.fluidOutput != null) {
                            state.tanks.flue_gas_tank.fill(recipe.fluidOutput, FluidAction.EXECUTE);
                        }
                        state.burnRemaining = recipe.getTotalProcessTime() - 1;
                        if (!state.ignited) ignite(state, ctx);
                        speedUp(state);
                        ctx.markMasterDirty();
                    } else {
                        speedDown(state);
                    }
                } else {
                    speedDown(state);
                }
            }
        }

        pumpOutputOut(ctx);

        if (wasActive != state.active || state.speed % 20 == 0) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    private void speedUp(State state) {
        if (state.starterRunning) {
            if (state.speed >= state.maxSpeed / 4) {
                state.speed = Math.max(Math.min(state.maxSpeed, state.speed + State.speedGainPerTick - State.speedLossPerTick), state.maxSpeed / 4);
            } else {
                state.speed = Math.min(state.maxSpeed / 4, state.speed + State.speedGainPerTick);
            }
        } else {
            if (state.speed >= state.maxSpeed / 4) {
                state.speed = Math.min(state.maxSpeed, state.speed + State.speedGainPerTick);
            } else {
                speedDown(state);
            }
        }
        state.active = true;
    }

    private void speedDown(State state) {
        if (state.ignitionGracePeriod > 0) state.ignitionGracePeriod--;
        state.speed = Math.max(0, state.speed - State.speedLossPerTick);
    }

    private boolean canIgnite(State state) {
        return sparkplugConsumption <= state.energyStorageMV.getEnergyStored();
    }

    private void ignite(State state, IMultiblockContext<State> ctx) {
        state.energyStorageMV.extractEnergy(sparkplugConsumption, false);
        state.ignited = true;
        state.ignitionGracePeriod = 60;
        ctx.requestMasterBESync();
    }

    private void pumpOutputOut(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        if (state.tanks.flue_gas_tank.isEmpty()) return;
        BlockPos outputPos = ctx.getLevel().toAbsolute(new BlockPos(1, 0, 0));
        Direction facing = ctx.getLevel().getOrientation().front();
        BlockPos adjacentPos = outputPos.relative(facing);
        Level world = ctx.getLevel().getRawLevel();
        BlockEntity te = world.getBlockEntity(adjacentPos);
        if (te != null) {
            LazyOptional<IFluidHandler> handlerOpt = te.getCapability(ForgeCapabilities.FLUID_HANDLER, facing.getOpposite());
            if (handlerOpt.isPresent()) {
                IFluidHandler handler = handlerOpt.orElseThrow(RuntimeException::new);
                FluidStack out = state.tanks.flue_gas_tank.getFluid();
                int accepted = handler.fill(out, FluidAction.SIMULATE);
                if (accepted > 0) {
                    int drained = handler.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false), FluidAction.EXECUTE);
                    state.tanks.flue_gas_tank.drain(drained, FluidAction.EXECUTE);
                    ctx.markMasterDirty();
                }
            }
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
        if (cap == ForgeCapabilities.ENERGY && ENERGY_INPUTS_HV.contains(position))
        {
            return state.energyCapHV.cast(ctx);
        }
        if (cap == ForgeCapabilities.ENERGY && ENERGY_INPUTS_MV.contains(position))
        {
            return state.energyCapMV.cast(ctx);
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER)
        {
            if (position.side() == RelativeBlockFace.BACK && FLUID_POS.contains(position.posInMultiblock()))
                return ctx.getState().fluidCap.cast(ctx);
            else if (position.side() == RelativeBlockFace.FRONT && FLUID_POS2.contains(position.posInMultiblock()))
                return ctx.getState().fluidCapExhaust.cast(ctx);
        }

        return LazyOptional.empty();
    }

    public static class State implements IMultiblockState
    {
        private GasTurbineTank tanks = new GasTurbineTank();
        private StoredCapability<IFluidHandler> fluidCap = new StoredCapability<>(tanks.input);
        private StoredCapability<IFluidHandler> fluidCapExhaust = new StoredCapability<>(tanks.flue_gas_tank);
        private StoredCapability<IEnergyStorage> energyCapHV;
        private StoredCapability<IEnergyStorage> energyCapMV;
        private AveragingEnergyStorage energyStorageHV;
        private AveragingEnergyStorage energyStorageMV;
        private BiFunction<Level, FluidStack, GasTurbineRecipe> recipeGetter = CachedRecipe.cached(GasTurbineRecipe::findFuel);

        public RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public int speed;
        public boolean ignited;
        public boolean starterRunning = false;
        public int maxSpeed = 1800;

        private static final int speedGainPerTick = 3;
        private static final int speedLossPerTick = 6;

        public int burnRemaining = 0;
        public int ignitionGracePeriod = 0;
        public boolean isShutdown = false;

        private boolean active = false;
        public float animation_fanRotationStep = 0;
        public float animation_fanRotation = 0;
        private int animation_fanFadeIn = 0;
        private int animation_fanFadeOut = 0;

        private BooleanSupplier runningSoundPlaying = () -> false;
        private BooleanSupplier starterSoundPlaying = () -> false;
        private BooleanSupplier arcSoundPlaying = () -> false;
        private boolean lastIgnited = false;
        private transient int runningSoundId = 0;
        private transient int starterSoundId = 0;
        private transient int arcSoundId = 0;
        private transient float currentLevel = 0f;

        public State(IInitialMultiblockContext<State> ctx)
        {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            this.energyStorageHV = new AveragingEnergyStorage(ENERGY_CAPACITY);
            this.energyStorageMV = new AveragingEnergyStorage(ENERGY_CAPACITY_MV);
            this.energyCapHV = new StoredCapability<>(energyStorageHV);
            this.energyCapMV = new StoredCapability<>(energyStorageMV);
            this.tanks = new GasTurbineTank();
            this.recipeGetter = CachedRecipe.cached(GasTurbineRecipe::findFuel);
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt)
        {
            nbt.putInt("speed", speed);
            nbt.putBoolean("active", active);
            nbt.putBoolean("starterRunning", starterRunning);
            nbt.putBoolean("ignited", ignited);
            nbt.putInt("burnRemaining", burnRemaining);
            nbt.putInt("ignitionGracePeriod", ignitionGracePeriod);
            nbt.putBoolean("isShutdown", isShutdown);
            nbt.put("tanks", tanks.toNBT());
        }

        @Override
        public void readSaveNBT(CompoundTag nbt)
        {
            speed = nbt.getInt("speed");
            active = nbt.getBoolean("active");
            starterRunning = nbt.getBoolean("starterRunning");
            ignited = nbt.getBoolean("ignited");
            burnRemaining = nbt.getInt("burnRemaining");
            ignitionGracePeriod = nbt.getInt("ignitionGracePeriod");
            isShutdown = nbt.getBoolean("isShutdown");
            tanks.readNBT(nbt.getCompound("tanks"));
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt)
        {
            nbt.putBoolean("active", active);
            nbt.putBoolean("starterRunning", starterRunning);
            nbt.putBoolean("ignited", ignited);
            nbt.putInt("speed", speed);
            nbt.putBoolean("isShutdown", isShutdown);
        }

        @Override
        public void readSyncNBT(CompoundTag nbt)
        {
            final boolean oldActive = active;
            final boolean oldStarterRunning = starterRunning;
            final boolean oldIgnited = ignited;
            final int oldSpeed = speed;
            final boolean oldIsShutdown = isShutdown;

            active = nbt.getBoolean("active");
            starterRunning = nbt.getBoolean("starterRunning");
            ignited = nbt.getBoolean("ignited");
            speed = nbt.getInt("speed");
            isShutdown = nbt.getBoolean("isShutdown");

            if(active && !oldActive)
                animation_fanFadeIn = 80;
            else if(!active && oldActive)
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