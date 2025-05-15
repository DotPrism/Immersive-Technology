package mctmods.immersivetech.client.menu.multiblock;

import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.CokeOvenLogic;
import blusunrize.immersiveengineering.common.gui.CokeOvenMenu;
import com.google.common.collect.ImmutableList;
import mctmods.immersivetech.common.blocks.multiblocks.gui.AdvancedCokeOvenMenu;
import mctmods.immersivetech.common.blocks.multiblocks.helper.ITFurnaceHandler;
import mctmods.immersivetech.common.blocks.multiblocks.logic.ITAdvancedCokeOvenLogic;
import mctmods.immersivetech.core.lib.ITLib;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;

import javax.annotation.Nonnull;
import java.util.List;

public class AdvCokeOvenScreen extends IEContainerScreen<AdvancedCokeOvenMenu> {
    private static final ResourceLocation TEXTURE = ITLib.makeTextureLocation("coke_oven_advanced");

    public AdvCokeOvenScreen(AdvancedCokeOvenMenu container, Inventory inventoryPlayer, Component title) {
        super(container, inventoryPlayer, title, TEXTURE);
    }

    @Nonnull
    @Override
    protected List<InfoArea> makeInfoAreas()
    {
        return ImmutableList.of(
                new FluidInfoArea(menu.tank, new Rect2i(leftPos+129, topPos+20, 16, 47), 176, 31, 20, 51, TEXTURE)
        );
    }

    @Override
    protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float f, int mx, int my)
    {
        int processMax = menu.state.get(ITAdvancedCokeOvenLogic.State.MAX_BURN_TIME);
        int process = menu.state.get(ITAdvancedCokeOvenLogic.State.BURN_TIME);
        if(processMax > 0&&process > 0)
        {
            int h = (int)(12*(process/(float)processMax));
            graphics.blit(TEXTURE, leftPos+59, topPos+37+12-h, 179, 1+12-h, 9, h);
        }
    }
}