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
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.SteamTurbineRecipe;
import mctmods.immersivetech.common.blocks.multiblocks.shapes.FullblockShape;
import mctmods.immersivetech.core.lib.ITLib;
import mctmods.immersivetech.core.lib.ITMultiblockSound;
import mctmods.immersivetech.core.registration.ITFluids;
import mctmods.immersivetech.core.registration.ITSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
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

public class ITSteamTurbineLogic implements IMultiblockLogic<ITSteamTurbineLogic.State>,  IServerTickableComponent<ITSteamTurbineLogic.State>, IClientTickableComponent<ITSteamTurbineLogic.State>
{
    private static final BlockPos MASTER_POS = new BlockPos(1, 1,0);
    private static final List<BlockPos> FLUID_POS = List.of(new BlockPos(2,1,9), new BlockPos(1,1,0));
    private static final List<BlockPos> FLUID_POS2 = List.of(new BlockPos(1,0,1));
    public static final BlockPos REDSTONE_POS = new BlockPos(0,1,9);

    public static final int TANK_CAPACITY = 12* FluidType.BUCKET_VOLUME;

    @Override
    public void tickClient(IMultiblockContext<State> ctx)
    {
        final State state = ctx.getState();
        float targetLevel = ITLib.remapRange(0, state.maxSpeed, 0.5f, 1.0f, state.speed);
        if (state.currentLevel == 0f) state.currentLevel = targetLevel;
        else state.currentLevel = state.currentLevel * 0.9f + targetLevel * 0.1f;
        float smoothedLevel = state.currentLevel;

        if(state.active||state.animation_fanFadeIn > 0||state.animation_fanFadeOut > 0)
        {
            float base = (state.speed / (float)state.maxSpeed) * 180f;
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

        if (!state.isSoundPlaying.getAsBoolean())
        {
            final Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(2.5, 1.5, 1.5));
            state.soundId++;
            int thisId = state.soundId;
            state.isSoundPlaying = ITMultiblockSound.startSound(
                    () -> state.active && state.soundId == thisId,
                    ctx.isValid(),
                    soundPos,
                    ITSounds.steamTurbine,
                    () -> {
                        LocalPlayer player = Minecraft.getInstance().player;
                        if (player == null) return 0f;
                        float attenuation = (float) Math.max(player.distanceToSqr(soundPos) / 8, 1);
                        return (11 * (smoothedLevel - 0.5f)) / attenuation;
                    },
                    () -> smoothedLevel
            );
        }
    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        boolean previouslyActive = state.active;
        state.active = false;

        if (state.burnRemaining > 0) {
            state.burnRemaining--;
            state.speed = Math.min(state.maxSpeed, state.speed + state.speedUpRate);
            state.active = true;
        } else if (state.rsState.isEnabled(ctx) && state.tanks.steam.getFluid() != null) {
            FluidStack fluid = state.tanks.steam.getFluid();
            SteamTurbineRecipe recipe = state.recipeGetter.apply(
                    ctx.getLevel().getRawLevel(), fluid
            );
            if (recipe != null && fluid.getAmount() >= recipe.inputAmount) {
                state.tanks.steam.drain(recipe.inputAmount, FluidAction.EXECUTE);
                if (recipe.fluidOutput != null) {
                    state.tanks.exhaust_steam.fill(recipe.fluidOutput, FluidAction.EXECUTE);
                }
                state.burnRemaining = recipe.getTotalProcessTime() - 1;
                state.speed = Math.min(state.maxSpeed, state.speed + state.speedUpRate);
                state.active = true;
            } else {
                state.speed = Math.max(0, state.speed - state.slowDownRate);
            }
        } else {
            state.speed = Math.max(0, state.speed - state.slowDownRate);
        }

        pumpOutputOut(ctx);

        if (previouslyActive != state.active) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    private void pumpOutputOut(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        if (state.tanks.exhaust_steam.getFluidAmount() == 0) return;
        BlockPos outputPos = ctx.getLevel().toAbsolute(new BlockPos(1, 0, 1));
        Direction facing = ctx.getLevel().getOrientation().front();
        BlockPos adjacentPos = outputPos.relative(facing);
        Level world = ctx.getLevel().getRawLevel();
        BlockEntity te = world.getBlockEntity(adjacentPos);
        if (te == null) return;
        LazyOptional<IFluidHandler> handlerOpt = te.getCapability(ForgeCapabilities.FLUID_HANDLER, facing.getOpposite());
        if (!handlerOpt.isPresent()) return;
        IFluidHandler handler = handlerOpt.orElseThrow(RuntimeException::new);
        FluidStack out = state.tanks.exhaust_steam.getFluid();
        int accepted = handler.fill(out, FluidAction.SIMULATE);
        if (accepted == 0) return;
        int drained = handler.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false), FluidAction.EXECUTE);
        state.tanks.exhaust_steam.drain(drained, FluidAction.EXECUTE);
        if (drained > 0) ctx.markMasterDirty();
    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap)
    {
        if(cap==ForgeCapabilities.FLUID_HANDLER)
        {
            if(position.side()==null || (position.side()==RelativeBlockFace.BACK && FLUID_POS.contains(position.posInMultiblock())))
                return ctx.getState().fluidCap.cast(ctx);
            else if(position.side()==null || (position.side()==RelativeBlockFace.FRONT && FLUID_POS2.contains(position.posInMultiblock())))
                return ctx.getState().fluidCapExhaust.cast(ctx);

        }

        return LazyOptional.empty();
    }

    @Override
    public State createInitialState(IInitialMultiblockContext<State> ctx)
    {
        return new State();
    }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType)
    {
        return FullblockShape.GETTER;
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

    public static class State implements IMultiblockState
    {
        private SteamTurbineTank tanks = new SteamTurbineTank();
        private StoredCapability<IFluidHandler> fluidCap = new StoredCapability<>(tanks.steam);
        private StoredCapability<IFluidHandler> fluidCapExhaust = new StoredCapability<>(tanks.exhaust_steam);
        private BiFunction<Level, FluidStack, SteamTurbineRecipe> recipeGetter = CachedRecipe.cached(SteamTurbineRecipe::findFuel);

        public RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();

        public float angle = 0.2f;
        private int slowDownRate = 6;
        private int speedUpRate = 3;
        public int maxSpeed = 1800;
        public int speed = 0;

        private int burnRemaining = 0;

        private boolean active = false;
        private BooleanSupplier isSoundPlaying = () -> false;
        private transient int soundId = 0;
        public float animation_fanRotationStep = 0;
        public float animation_fanRotation = 0;
        private int animation_fanFadeIn = 0;
        private int animation_fanFadeOut = 0;
        private transient float currentLevel = 0f;

        public State()
        {
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt)
        {
            nbt.putInt("speed", speed);
            nbt.putBoolean("active", active);
            nbt.putInt("burnRemaining", burnRemaining);
            nbt.put("tanks", tanks.toNBT());
        }

        @Override
        public void readSaveNBT(CompoundTag nbt)
        {
            speed = nbt.getInt("speed");
            active = nbt.getBoolean("active");
            burnRemaining = nbt.getInt("burnRemaining");
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