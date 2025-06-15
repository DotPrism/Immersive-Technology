package mctmods.immersivetech.core.proxy;

import blusunrize.immersiveengineering.api.client.ieobj.IEOBJCallbacks;
import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import mctmods.immersivetech.client.models.ITDynamicModel;
import mctmods.immersivetech.client.renderer.AdvancedCokeOvenRenderer;
import mctmods.immersivetech.client.renderer.CokeOvenPreheaterRenderer;
import mctmods.immersivetech.client.renderer.GasTurbineRenderer;
import mctmods.immersivetech.client.renderer.SteamTurbineRenderer;
import mctmods.immersivetech.core.lib.ITLib;
import mctmods.immersivetech.core.registration.ITMultiblockProvider;
import mctmods.immersivetech.core.registration.ITRegistrationHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ITLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientProxy extends CommonProxy
{
    @Override
    public void reinitializeGUI()
    {
        Screen currentScreen = Minecraft.getInstance().screen;
        if(currentScreen instanceof IEContainerScreen)
            currentScreen.init(Minecraft.getInstance(), currentScreen.width, currentScreen.height);
    }

    @Override
    public Level getClientWorld()
    {
        return Minecraft.getInstance().level;
    }

    @Override
    public Player getClientPlayer()
    {
        return Minecraft.getInstance().player;
    }

    @SubscribeEvent
    public static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders ev)
    {
        //AdvancedCokeOvenRenderer.MODEL_LEFT = new ITDynamicModel(AdvancedCokeOvenRenderer.NAME_DOOR_LEFT);
        //AdvancedCokeOvenRenderer.MODEL_MIDDLE = new ITDynamicModel(AdvancedCokeOvenRenderer.NAME_DOOR_MIDDLE);
        //AdvancedCokeOvenRenderer.MODEL_RIGHT = new ITDynamicModel(AdvancedCokeOvenRenderer.NAME_DOOR_RIGHT);
        CokeOvenPreheaterRenderer.MODEL = new ITDynamicModel(CokeOvenPreheaterRenderer.NAME);
        SteamTurbineRenderer.MODEL = new ITDynamicModel(SteamTurbineRenderer.NAME);
        SteamTurbineRenderer.MODEL_EAST_WEST = new ITDynamicModel(SteamTurbineRenderer.NAME_EAST_WEST);
        GasTurbineRenderer.MODEL = new ITDynamicModel(GasTurbineRenderer.NAME);
        GasTurbineRenderer.MODEL_EAST_WEST = new ITDynamicModel(GasTurbineRenderer.NAME_EAST_WEST);
    }

    @SubscribeEvent
    public static void registerRenders(EntityRenderersEvent.RegisterRenderers event)
    {
        registerBERenders(event);
        registerEntityRenders(event);
    }

    private static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event)
    {
    }

    private static <T extends BlockEntity>
    void registerBERenderNoContext(
            EntityRenderersEvent.RegisterRenderers event, Supplier<BlockEntityType<? extends T>> type, Supplier<BlockEntityRenderer<T>> render
    )
    {
        registerBERenderNoContext(event, type.get(), render);
    }

    private static <T extends BlockEntity>
    void registerBERenderNoContext(
            EntityRenderersEvent.RegisterRenderers event, BlockEntityType<? extends T> type, Supplier<BlockEntityRenderer<T>> render
    )
    {
        event.registerBlockEntityRenderer(type, $ -> render.get());
    }

    public static void registerBERenders(EntityRenderersEvent.RegisterRenderers event)
    {
        registerBERenderNoContext(event, ITRegistrationHolder.COKEOVEN_PREHEATER.master(), CokeOvenPreheaterRenderer::new);
        registerBERenderNoContext(event, ITMultiblockProvider.STEAM_TURBINE.masterBE(), SteamTurbineRenderer::new);
        registerBERenderNoContext(event, ITMultiblockProvider.GAS_TURBINE.masterBE(), GasTurbineRenderer::new);
        //registerBERenderNoContext(event, ITMultiblockProvider.ADV_COKE_OVEN.masterBE(), AdvancedCokeOvenRenderer::new);
    }
}
