package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.client.utils.TextUtils;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class CreativeBarrelBlockEntity extends IEBaseBlockEntity implements IEBlockInterfaces.IBlockEntityDrop, IEBlockInterfaces.IPlayerInteraction, IEBlockInterfaces.IBlockOverlayText {
    private static final int OUTPUT_RATE = 1000;
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
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (!selectedFluid.isEmpty() && resource.getFluid() == selectedFluid.getFluid()) {
                int amount = Math.min(OUTPUT_RATE, resource.getAmount());
                return new FluidStack(selectedFluid.getFluid(), amount);
            }
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            if (!selectedFluid.isEmpty()) {
                int amount = Math.min(OUTPUT_RATE, maxDrain);
                return new FluidStack(selectedFluid.getFluid(), amount);
            }
            return FluidStack.EMPTY;
        }
    });

    public CreativeBarrelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void setOutputFluid(FluidStack fluidStack) {
        this.selectedFluid = fluidStack.isEmpty() ? FluidStack.EMPTY : new FluidStack(fluidStack.getFluid(), 1);
        setChanged();
    }

    public FluidStack getSelectedFluid() {
        return selectedFluid;
    }

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
    public void readCustomNBT(CompoundTag compoundTag, boolean b) {
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER && side == Direction.DOWN) {
            return fluidHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidHandler.invalidate();
    }

    @Override
    public void getBlockEntityDrop(LootContext context, Consumer<ItemStack> drop) {
        ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        if (!tag.isEmpty()) {
            stack.setTag(tag);
        }
        drop.accept(stack);
    }

    @Override
    public void onBEPlaced(BlockPlaceContext ctx) {
        onBEPlaced(ctx.getItemInHand());
    }

    public void onBEPlaced(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag.contains("SelectedFluid")) {
                ResourceLocation fluidId = new ResourceLocation(tag.getString("SelectedFluid"));
                if (ForgeRegistries.FLUIDS.containsKey(fluidId)) {
                    this.selectedFluid = new FluidStack(ForgeRegistries.FLUIDS.getValue(fluidId), 1);
                }
            }
        }
    }

    @Override
    public boolean interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
        if (Utils.isFluidRelatedItemStack(heldItem)) {
            FluidStack contained = FluidUtil.getFluidContained(heldItem).orElse(FluidStack.EMPTY);

            if (!contained.isEmpty()) {
                setOutputFluid(contained);

                if (!player.isCreative()) {
                    IFluidHandler dummySink = new IFluidHandler() {
                        @Override
                        public int getTanks() {
                            return 1;
                        }

                        @Override
                        public @NotNull FluidStack getFluidInTank(int tank) {
                            return FluidStack.EMPTY;
                        }

                        @Override
                        public int getTankCapacity(int tank) {
                            return Integer.MAX_VALUE;
                        }

                        @Override
                        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
                            return true;
                        }

                        @Override
                        public int fill(FluidStack resource, FluidAction action) {
                            return resource.getAmount();
                        }

                        @Override
                        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
                            return FluidStack.EMPTY;
                        }

                        @Override
                        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
                            return FluidStack.EMPTY;
                        }
                    };

                    FluidActionResult result = FluidUtil.tryEmptyContainer(heldItem, dummySink, Integer.MAX_VALUE, player, true);
                    if (result.success) {
                        player.setItemInHand(hand, result.result);
                    }
                }
                return true;
            } else if (!selectedFluid.isEmpty()) {
                FluidActionResult result = FluidUtil.tryFillContainer(heldItem, fluidHandler.orElseThrow(RuntimeException::new), Integer.MAX_VALUE, player, true);
                if (result.success) {
                    player.setItemInHand(hand, result.result);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Component[] getOverlayText(Player player, HitResult rtr, boolean hammer) {
        if (rtr.getType() == HitResult.Type.MISS) {
            return null;
        }
        if (Utils.isFluidRelatedItemStack(player.getItemInHand(InteractionHand.MAIN_HAND))) {
            FluidStack displayFluid = selectedFluid.isEmpty() ? FluidStack.EMPTY : new FluidStack(selectedFluid.getFluid(), 1000);
            return new Component[]{TextUtils.formatFluidStack(displayFluid)};
        }
        return null;
    }

    @Override
    public boolean useNixieFont(Player player, HitResult mop) {
        return false;
    }
}