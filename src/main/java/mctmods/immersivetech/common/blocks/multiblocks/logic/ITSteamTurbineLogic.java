package mctmods.immersivetech.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import mctmods.immersivetech.common.blocks.multiblocks.shapes.FullblockShape;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import java.util.function.Function;

public class ITSteamTurbineLogic implements IMultiblockLogic<ITSteamTurbineLogic.State>,  IServerTickableComponent<ITSteamTurbineLogic.State>, IClientTickableComponent<ITSteamTurbineLogic.State>
{

    @Override
    public void tickClient(IMultiblockContext<State> iMultiblockContext)
    {

    }

    @Override
    public void tickServer(IMultiblockContext<State> iMultiblockContext)
    {

    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap)
    {
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
        return FullblockShape.GETTER;
    }

    public class State implements IMultiblockState
    {
        public State()
        {

        }

        @Override
        public void writeSaveNBT(CompoundTag compoundTag)
        {

        }

        @Override
        public void readSaveNBT(CompoundTag compoundTag)
        {

        }

        @Override
        public void writeSyncNBT(CompoundTag nbt)
        {
            writeSyncNBT(nbt);
        }

        @Override
        public void readSyncNBT(CompoundTag nbt)
        {
            readSyncNBT(nbt);
        }
    }
}
