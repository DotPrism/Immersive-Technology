package mctmods.immersivetech.common.blocks.multiblocks.gui;

import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.CokeOvenLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.FurnaceHandler;
import blusunrize.immersiveengineering.common.gui.IEContainerMenu;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import mctmods.immersivetech.common.blocks.multiblocks.logic.ITBoilerLogic;
import mctmods.immersivetech.common.blocks.multiblocks.recipe.BoilerRecipe;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class BoilerMenu extends IEContainerMenu
{
    public final ITBoilerLogic.BoilerTank tank;

    public static BoilerMenu makeServer(
            MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<ITBoilerLogic.State> ctx
    )
    {
        final ITBoilerLogic.State state = ctx.mbContext().getState();
        return new BoilerMenu(
                multiblockCtx(type, id, ctx), invPlayer, state.getInventory().getRawHandler(), state, state.getTanks()
        );
    }

    public static BoilerMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
    {
        return new BoilerMenu(
                clientCtx(type, id),
                invPlayer,
                new ItemStackHandler(ITBoilerLogic.NUM_SLOTS),
                new SimpleContainerData(FurnaceHandler.StateView.NUM_SLOTS),
                new ITBoilerLogic.BoilerTank());
    }

    protected BoilerMenu(MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, ContainerData data, ITBoilerLogic.BoilerTank tank) {
        super(ctx);
        this.addSlot(new IESlot.NewOutput(inv, 0, 37, 54));
        this.addSlot(new IESlot.NewFluidContainer(inv, 1, 37, 15, IESlot.NewFluidContainer.Filter.ANY));
        this.addSlot(new IESlot.NewOutput(inv, 2, 76, 54));
        this.addSlot(new IESlot.NewFluidContainer(inv, 3, 76, 15, IESlot.NewFluidContainer.Filter.ANY));
        this.addSlot(new IESlot.NewOutput(inv, 4, 149, 54));
        this.addSlot(new IESlot.NewFluidContainer(inv, 5, 149, 15, IESlot.NewFluidContainer.Filter.ANY));

        ownSlotCount = 6;

        for(int i = 0; i < 3; i++)
            for(int j = 0; j < 9; j++)
                addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
        for(int i = 0; i < 9; i++)
            addSlot(new Slot(inventoryPlayer, i, 8+i*18, 142));

        this.tank = tank;
        addGenericData(GenericContainerData.fluid(tank.fuelInput()));
        addGenericData(GenericContainerData.fluid(tank.waterInput()));
        addGenericData(GenericContainerData.fluid(tank.output()));
    }
}
