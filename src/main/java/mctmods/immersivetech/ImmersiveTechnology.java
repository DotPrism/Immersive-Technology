package mctmods.immersivetech;

import mctmods.immersivetech.client.ITClientRenderHandler;
import mctmods.immersivetech.core.ITConfig;
import mctmods.immersivetech.core.lib.ITLib;
import mctmods.immersivetech.core.proxy.ClientProxy;
import mctmods.immersivetech.core.proxy.CommonProxy;
import mctmods.immersivetech.core.registration.ITContent;
import mctmods.immersivetech.core.registration.ITRecipeSerializers;
import mctmods.immersivetech.core.registration.ITRegistrationHolder;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

import static mctmods.immersivetech.core.lib.ITLib.MODID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MODID)
public class ImmersiveTechnology
{
    public static CommonProxy proxy = Util.make(() ->
    {
        if(FMLLoader.getDist().isClient()) return new ClientProxy();
        return new CommonProxy();
    });

    public ImmersiveTechnology(FMLJavaModLoadingContext ctx)
    {
        IEventBus modEventBus =  ctx.getModEventBus();
        ITLib.IT_LOGGER.info("IT Starting");
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        ITRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);

        ITRegistrationHolder.addRegistersToEventBus(modEventBus);

        proxy.modConstruction(modEventBus);

        ctx.registerConfig(ModConfig.Type.COMMON, ITConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        ITLib.IT_LOGGER.info("HELLO FROM COMMON SETUP");

        //ITConfig.items.forEach((item) -> ITLib.IT_LOGGER.info("ITEM >> {}", item.toString()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        ITLib.IT_LOGGER.info("HELLO from server starting");
    }

    private void clientSetup(FMLClientSetupEvent event)
    {
        ITClientRenderHandler.register();
        ITClientRenderHandler.init(event);
        ITContent.initializeManualEntries();
        ITContent.registerContainersAndScreens();
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            ITLib.IT_LOGGER.info("HELLO FROM CLIENT SETUP");
            ITLib.IT_LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
