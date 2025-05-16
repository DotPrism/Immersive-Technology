package mctmods.immersivetech.common.blocks.multiblocks.logic.helper;

import net.minecraftforge.fluids.FluidStack;

public interface ITurbineHelper
{
    int angle = 72;

    void speedUp(int radiansPerTick, FluidStack stack);
}
