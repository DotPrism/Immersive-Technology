package mctmods.immersivetech.core.lib;

import com.igteam.immersivegeology.core.lib.IGLib;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class ITLib
{
    public static final String MODID = "immersivetech";
    public static final String VERSION = "2.0.0";

    public static final Logger IT_LOGGER = LogUtils.getLogger();

    public static final String DESC = "desc."+MODID+".";
    public static final String DESC_INFO = DESC+"info.";
    public static final String DESC_FLAVOUR = DESC+"flavour.";

    public static final String GUIID_AdvCokeOven = "coke_oven_advanced";
    public static final String GUIID_Boiler = "gui_boiler";

    public static Logger getNewLogger()
    {
        return  LogUtils.getLogger();
    }

    public static ResourceLocation makeTextureLocation(String name) {
        return rl("textures/gui/" + name + ".png");
    }

    public static double normalize(int val)
    {
        return val / 16;
    }

    public static double unnormalize(double val)
    {
        return val * 16;
    }

    public static ResourceLocation rl(String name)
    {
        return new ResourceLocation(ITLib.MODID, name);
    }
}
