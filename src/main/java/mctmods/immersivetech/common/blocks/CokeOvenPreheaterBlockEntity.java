package mctmods.immersivetech.common.blocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetech.core.lib.ITLib;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class CokeOvenPreheaterBlockEntity extends IEBaseBlockEntity implements IEBlockInterfaces.IStateBasedDirectional,
        IEBlockInterfaces.IHasDummyBlocks, IModelOffsetProvider, IEClientTickableBE, IEBlockInterfaces.ISoundBE
{
    public static final float ANGLE_PER_TICK = (float)Math.toRadians(20);
    public boolean active;
    public int dummy = 0;
    public boolean isDummy = false;
    public final MutableEnergyStorage energyStorage = new MutableEnergyStorage(8000);
    public float angle = 0;
    private final MultiblockCapability<IEnergyStorage> energyCap = MultiblockCapability.make(
            this, be -> be.energyCap, CokeOvenPreheaterBlockEntity::master, registerEnergyInput(energyStorage)
    );

    public CokeOvenPreheaterBlockEntity(BlockEntityType<CokeOvenPreheaterBlockEntity> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public int doSpeedup()
    {
        ITLib.IT_LOGGER.info("Called doSpeedup");
        int consumed = 32;
        if(this.energyStorage.extractEnergy(consumed, true)==consumed)
        {
            if(!active)
            {
                active = true;
                this.markContainingBlockForUpdate(null);
            }
            this.energyStorage.extractEnergy(consumed, false);
            return 1;
        }
        else
            turnOff();
        return 0;
    }

    @Override
    public void tickClient()
    {
        if(active)
            angle = (angle+ANGLE_PER_TICK)%Mth.PI;
        ImmersiveEngineering.proxy.handleTileSound(IESounds.preheater, this, active, 0.5f, 1f);
    }

    public void turnOff()
    {
        if(active)
        {
            active = false;
            this.markContainingBlockForUpdate(null);
        }
    }

    @Override
    public boolean isDummy()
    {
        if (dummy == -1)
            return true;
        if (dummy == 1)
            return true;
        else
            return false;
    }

    @Nullable
    @Override
    public CokeOvenPreheaterBlockEntity master()
    {
        ITLib.IT_LOGGER.info("Placing Master");
        BlockPos masterPos = getBlockPos().below(dummy);
        BlockEntity te = Utils.getExistingTileEntity(level, masterPos);
        if (te == null)
            ITLib.IT_LOGGER.info("Master is Null");
        return te instanceof CokeOvenPreheaterBlockEntity heater?heater: null;
    }

    @Override
    public void placeDummies(BlockPlaceContext ctx, BlockState state)
    {
        ITLib.IT_LOGGER.info("Placing Dummies");
        state = state.setValue(IEProperties.MULTIBLOCKSLAVE, true);

        assert level != null;
        level.setBlockAndUpdate(worldPosition.offset(0, 0, -1), state);
        ((CokeOvenPreheaterBlockEntity) Objects.requireNonNull(level.getBlockEntity(worldPosition.offset(0, 0, -1)))).dummy = -1;
        ((CokeOvenPreheaterBlockEntity) Objects.requireNonNull(level.getBlockEntity(worldPosition.offset(0, 0, -1)))).setFacing(this.getFacing());

        level.setBlockAndUpdate(worldPosition.offset(0, 0, 1), state);
        ((CokeOvenPreheaterBlockEntity) Objects.requireNonNull(level.getBlockEntity(worldPosition.offset(0, 0, 1)))).dummy = 1;
        ((CokeOvenPreheaterBlockEntity) Objects.requireNonNull(level.getBlockEntity(worldPosition.offset(0, 0, 1)))).setFacing(this.getFacing());

    }

    @Override
    public void breakDummies(BlockPos pos, BlockState state)
    {
        ITLib.IT_LOGGER.info("Breaking Dummies");
        //if(level.getBlockEntity(getBlockPos().offset(0, 0, -dummy).offset(0, 0, -1)) instanceof CokeOvenPreheaterBlockEntity)
            //level.removeBlock(getBlockPos().offset(0, 0, -dummy).offset(0, 0, -1), false);
        if(level.getBlockEntity(getBlockPos().offset(0, 0, -dummy).offset(0, 0, 1)) instanceof CokeOvenPreheaterBlockEntity)
            level.removeBlock(getBlockPos().offset(0, 0, -dummy).offset(0, 0, 1), false);
    }

    @Override
    public void readCustomNBT(CompoundTag nbt, boolean descPacket)
    {
        dummy = nbt.getInt("dummy");
        active = nbt.getBoolean("active");
        if(descPacket)
            this.markContainingBlockForUpdate(null);
        else
            EnergyHelper.deserializeFrom(energyStorage, nbt);
    }

    @Override
    public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
    {
        nbt.putInt("dummy", dummy);
        nbt.putBoolean("active", active);
        if(!descPacket)
            EnergyHelper.serializeTo(energyStorage, nbt);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
        if(cap==ForgeCapabilities.ENERGY&&(side==null||(dummy==0&&side==Direction.UP)))
            return energyCap.get().cast();
        return super.getCapability(cap, side);
    }

    @Override
    public Property<Direction> getFacingProperty()
    {
        return IEProperties.FACING_HORIZONTAL;
    }

    @Override
    public PlacementLimitation getFacingLimitation()
    {
        return PlacementLimitation.HORIZONTAL;
    }

    @Override
    public void afterRotation(Direction oldDir, Direction newDir)
    {
        for(int i = 0; i <= 2; i++)
        {
            BlockEntity te = level.getBlockEntity(getBlockPos().offset(0, -dummy+i, 0));
            if(te instanceof CokeOvenPreheaterBlockEntity dummy)
            {
                dummy.setFacing(newDir);
                dummy.setChanged();
                dummy.markContainingBlockForUpdate(null);
            }
        }
    }

    @Override
    public BlockPos getModelOffset(BlockState state, @Nullable Vec3i size)
    {
        return new BlockPos(0, dummy, 0);
    }

    @Override
    public boolean shouldPlaySound(String sound)
    {
        return active;
    }
}
