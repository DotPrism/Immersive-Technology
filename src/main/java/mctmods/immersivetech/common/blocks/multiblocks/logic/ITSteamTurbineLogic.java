package mctmods.immersivetech.common.blocks.multiblocks.logic;

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
import blusunrize.immersiveengineering.common.util.CachedRecipe;
import blusunrize.immersiveengineering.common.util.sound.MultiblockSound;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.SteamTurbineRecipe;
import mctmods.immersivetech.common.blocks.multiblocks.shapes.FullblockShape;
import mctmods.immersivetech.common.blocks.multiblocks.shapes.SteamTurbineShape;
import mctmods.immersivetech.core.lib.ITLib;
import mctmods.immersivetech.core.lib.ITMultiblockSound;
import mctmods.immersivetech.core.registration.ITFluids;
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
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public class ITSteamTurbineLogic implements IMultiblockLogic<ITSteamTurbineLogic.State>,  IServerTickableComponent<ITSteamTurbineLogic.State>, IClientTickableComponent<ITSteamTurbineLogic.State>
{
    private static final BlockPos MASTER_POS = new BlockPos(1, 1, 0);
    private static final List<BlockPos> FLUID_POS = List.of(new BlockPos(2, 1, 9), new BlockPos(1, 0, 1));
    private static final List<BlockPos> FLUID_POS2 = List.of(new BlockPos(1, 0, 1));
    public static final BlockPos REDSTONE_POS = new BlockPos(0, 1, 9);

    public static final int TANK_CAPACITY = 12* FluidType.BUCKET_VOLUME;

    @Override
    public void tickClient(IMultiblockContext<State> ctx)
    {
        final State state = ctx.getState();
        float level = ITLib.remapRange(0, state.maxSpeed, 0.5f, 1.5f, state.speed);

        if(state.active||state.animation_fanFadeIn > 0||state.animation_fanFadeOut > 0)
        {
            float base = 180f;
            float step = state.active?base: 0;
            if(state.animation_fanFadeIn > 0)
            {
                step -= (state.animation_fanFadeIn/80f)*base;
                state.animation_fanFadeIn--;
            }
            if(state.animation_fanFadeOut > 0)
            {
                step += (state.animation_fanFadeOut/80f)*base;
                state.animation_fanFadeOut--;
            }
            state.animation_fanRotationStep = step;
            state.animation_fanRotation += step;
            state.animation_fanRotation %= 360;
        }

        if(!state.isSoundPlaying.getAsBoolean())
        {
            final Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(2.5, 1.5, 1.5));
            state.isSoundPlaying = ITMultiblockSound.startSound(
                    () -> state.active, ctx.isValid(), soundPos, ITSounds.steamTurbine, 1f, level
            );
        }
    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        boolean active = state.isActive();

        SteamTurbineRecipe recipe = state.recipeGetter.apply(
                ctx.getLevel().getRawLevel(), state.tanks.steam.getFluid().getFluid()
        );
        if(state.rsState.isEnabled(ctx)&&!state.tanks.steam.getFluid().isEmpty())
        {
            if(recipe != null)
            {
                state.consumeTick--;
                if(state.consumeTick <= 0) //Consume 10*tick-amount every 10ticks to allow for 1/10th mB amounts
                {
                    int burnTime = recipe.getBurnTime();
                    int fluidConsumed = (10*FluidType.BUCKET_VOLUME)/burnTime;
                    if(state.tanks.steam.getFluidAmount() >= fluidConsumed)
                    {
                        if(!active)
                            active = true;
                        state.tanks.steam.drain(fluidConsumed, IFluidHandler.FluidAction.EXECUTE);
                        state.consumeTick = 10;
                        if (state.speed <= state.maxSpeed)
                            state.speed += state.speedUpRate;
                    }
                    else if(active)
                        active = false;
                }
            }
        }
        else if(active)
        {
            active = false;
            if (state.speed >= 0)
            {
                state.speed -= state.slowDownRate;
            }
        }

        if(active!=state.active)
        {
            state.active = active;
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap)
    {
        if(cap==ForgeCapabilities.FLUID_HANDLER)
        {
            if(position.side()==null||(position.side()==RelativeBlockFace.BACK&&FLUID_POS.contains(position.posInMultiblock())))
                return ctx.getState().fluidCap.cast(ctx);
            else if(position.side()==null||(position.side()==RelativeBlockFace.FRONT&&FLUID_POS2.contains(position.posInMultiblock())))
                return ctx.getState().fluidCapExhaust.cast(ctx);

        }

        return LazyOptional.empty();
    }

    @Override
    public State createInitialState(IInitialMultiblockContext<State> ctx)
    {
        return new ITSteamTurbineLogic.State();
    }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType)
    {
        return SteamTurbineShape.GETTER;
    }

    public int speedUp(IMultiblockContext<State> ctx)
    {
        final State state = ctx.getState();

        int speedOut = 0;

        speedOut += state.speedUpRate;

        return speedOut;
    }

    public int slowDown(IMultiblockContext<State> ctx)
    {
        final State state = ctx.getState();

        int speedOut = 0;

        speedOut -= state.slowDownRate;

        return speedOut;
    }

    public class State implements IMultiblockState
    {
        private final SteamTurbineTank tanks = new SteamTurbineTank();
        private final StoredCapability<IFluidHandler> fluidCap = new StoredCapability<>(tanks.steam);
        private final StoredCapability<IFluidHandler> fluidCapExhaust = new StoredCapability<>(tanks.exhaust_steam);
        private final BiFunction<Level, Fluid, SteamTurbineRecipe> recipeGetter = CachedRecipe.cached(SteamTurbineRecipe::getRecipeFor);

        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();

        public float angle = 0.2f;
        private final int slowDownRate = 6;
        private final int speedUpRate = 3;
        public int maxSpeed = 1800;
        public int speed = 0;

        private int consumeTick = 0;

        private boolean active = false;
        private BooleanSupplier isSoundPlaying = () -> false;
        public float animation_fanRotationStep = 0;
        public float animation_fanRotation = 0;
        private int animation_fanFadeIn = 0;
        private int animation_fanFadeOut = 0;

        public State()
        {
            Set<Fluid> allowedFuels = Set.of(ITFluids.STEAM.getStill());
            Set<Fluid> exhaustSteam = Set.of(ITFluids.STEAM_EXHAUST.getStill());
            this.tanks.steam.setValidator(f -> allowedFuels.contains(f.getFluid()));
            this.tanks.exhaust_steam.setValidator(f -> exhaustSteam.contains(f.getFluid()));
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt)
        {
            nbt.putInt("speed", speed);
            nbt.putBoolean("active", active);
            nbt.putInt("consumeTick", consumeTick);
            nbt.put("tanks", tanks.toNBT());
        }

        @Override
        public void readSaveNBT(CompoundTag nbt)
        {
            speed = nbt.getInt("speed");
            active = nbt.getBoolean("active");
            consumeTick = nbt.getInt("consumeTick");
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
    public record SteamTurbineTank(FluidTank steam, FluidTank exhaust_steam)
    {
        public SteamTurbineTank()
        {
            this(new FluidTank(TANK_CAPACITY), new FluidTank(TANK_CAPACITY));
        }

        public SteamTurbineTank(FluidTank steam, FluidTank exhaust_steam)
        {
            this.steam = steam;
            this.exhaust_steam = exhaust_steam;
        }

        public Tag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("steam", this.steam.writeToNBT(new CompoundTag()));
            tag.put("exhaust_steam", this.exhaust_steam.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) {
            this.steam.readFromNBT(tag.getCompound("steam"));
            this.exhaust_steam.readFromNBT(tag.getCompound("exhaust_steam"));
        }

        public int getCapacity() {
            return TANK_CAPACITY;
        }
    }
}
