package mctmods.immersivetechnology.common.blocks;

import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class CreativeBarrelBlockEntity extends IEBaseBlockEntity
{
    private static final int OUTPUT_RATE = 1000; // mB per transfer
    private FluidStack selectedFluid = FluidStack.EMPTY;

    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> new IFluidHandler() {

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            if (selectedFluid.isEmpty()) {
                return FluidStack.EMPTY;
            }
            return new FluidStack(selectedFluid.getFluid(), Integer.MAX_VALUE);
        }

        @Override
        public int getTankCapacity(int tank) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return false; // No input allowed
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0; // Can't fill
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.getFluid() == selectedFluid.getFluid()) {
                return action.execute() ? new FluidStack(selectedFluid.getFluid(), Math.min(OUTPUT_RATE, resource.getAmount()))
                        : new FluidStack(selectedFluid.getFluid(), Math.min(OUTPUT_RATE, resource.getAmount()));
            }
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            if (!selectedFluid.isEmpty()) {
                return action.execute() ? new FluidStack(selectedFluid.getFluid(), Math.min(OUTPUT_RATE, maxDrain))
                        : new FluidStack(selectedFluid.getFluid(), Math.min(OUTPUT_RATE, maxDrain));
            }
            return FluidStack.EMPTY;
        }
    });

    public CreativeBarrelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Sets the output fluid.
     */
    public void setOutputFluid(FluidStack fluidStack) {
        this.selectedFluid = new FluidStack(fluidStack.getFluid(), 1);
        setChanged();
    }

    /**
     * Returns the currently selected output fluid.
     */
    public FluidStack getSelectedFluid() {
        return selectedFluid;
    }

    /**
     * NBT Save
     */
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        if (!selectedFluid.isEmpty()) {
            tag.putString("SelectedFluid", ForgeRegistries.FLUIDS.getKey(selectedFluid.getFluid()).toString());
        }
    }

    @Override
    public void writeCustomNBT(CompoundTag compoundTag, boolean b) {

    }

    /**
     * NBT Load
     */
    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("SelectedFluid")) {
            ResourceLocation fluidId = new ResourceLocation(tag.getString("SelectedFluid"));
            if (ForgeRegistries.FLUIDS.containsKey(fluidId)) {
                this.selectedFluid = new FluidStack(ForgeRegistries.FLUIDS.getValue(fluidId), 1);
            }
        }
    }

    @Override
    public void readCustomNBT(CompoundTag compoundTag, boolean b)
    {

    }

    /**
     * Capability exposure (bottom side only)
     */
    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER && side == net.minecraft.core.Direction.DOWN) {
            return fluidHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    /**
     * Cleanup on remove
     */
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidHandler.invalidate();
    }
}
