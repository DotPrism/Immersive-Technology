package mctmods.immersivetech.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;

import java.util.ArrayList;
import java.util.List;

public class ITMultiblocks
{
    public static final List<MultiblockHandler.IMultiblock> IT_MULTIBLOCKS = new ArrayList<>();

    //public static ITTemplateMultiblock ADV_COKE_OVEN;

    public static void init()
    {
        //ADV_COKE_OVEN = register(new ITAdvancedCokeOven());
    }

    private static <T extends MultiblockHandler.IMultiblock>
    T register(T multiblock) {
        IT_MULTIBLOCKS.add(multiblock);
        MultiblockHandler.registerMultiblock(multiblock);
        return multiblock;
    }
}
