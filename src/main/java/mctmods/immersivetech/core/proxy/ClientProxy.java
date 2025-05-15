package mctmods.immersivetech.core.proxy;

import blusunrize.immersiveengineering.api.client.ieobj.IEOBJCallbacks;
import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import mctmods.immersivetech.client.models.ITDynamicModel;
import mctmods.immersivetech.client.renderer.AdvancedCokeOvenRenderer;
import mctmods.immersivetech.client.renderer.CokeOvenPreheaterRenderer;
import mctmods.immersivetech.core.lib.ITLib;
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
        CokeOvenPreheaterRenderer.MODEL = new ITDynamicModel(CokeOvenPreheaterRenderer.NAME);
        //AdvancedCokeOvenRenderer.DRUM = new ITDynamicModel(AdvancedCokeOvenRenderer.NAME);
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
    }
}
