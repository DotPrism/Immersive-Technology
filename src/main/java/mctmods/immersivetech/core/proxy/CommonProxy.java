package mctmods.immersivetech.core.proxy;

import mctmods.immersivetech.common.ITTags;
import mctmods.immersivetech.core.registration.ITItems;
import mctmods.immersivetech.core.lib.ITLib;
import mctmods.immersivetech.core.registration.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;

public class CommonProxy
{
    public static void modConstruction(IEventBus event)
    {
        ITLib.IT_LOGGER.info("Registering Multiblocks to Immersive Engineering");
        ITMultiblockProvider.forceClassLoad();
        ITRegistrationHolder.initialize();
        ITTags.initialize();
        ITRecipeTypes.init();
        ITItems.init(event);
        ITBlocks.init(event);
    }

    public void reinitializeGUI(){}

    public Level getClientWorld()
    {
        return null;
    }

    public Player getClientPlayer()
    {
        return null;
    }
}
