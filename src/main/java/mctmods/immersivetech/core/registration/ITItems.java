package mctmods.immersivetech.core.registration;

import mctmods.immersivetech.common.items.helper.ITBaseItem;
import mctmods.immersivetech.core.lib.ITLib;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ITItems
{
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, ITLib.MODID);

    public static void init(IEventBus event)
    {
        REGISTER.register(event);
    }

    private static <T> Consumer<T> nothing()
    {
        return $ -> {
        };
    }

    private static ITItems.ItemRegObject<ITBaseItem> simpleWithStackSize(String name, int maxSize)
    {
        return simple(name, p -> p.stacksTo(maxSize), i -> {
        });
    }

    private static ITItems.ItemRegObject<ITBaseItem> simple(String name)
    {
        return simple(name, $ -> {
        }, $ -> {
        });
    }

    private static ITItems.ItemRegObject<ITBaseItem> simple(
            String name, Consumer<Item.Properties> makeProps, Consumer<ITBaseItem> processItem
    )
    {
        return register(
                name, () -> Util.make(new ITBaseItem(Util.make(new Item.Properties(), makeProps)), processItem)
        );
    }

    static <T extends Item> ITItems.ItemRegObject<T> register(String name, Supplier<? extends T> make)
    {
        return new ITItems.ItemRegObject<>(REGISTER.register(name, make));
    }

    private static <T extends Item> ITItems.ItemRegObject<T> of(T existing)
    {
        return new ITItems.ItemRegObject<>(RegistryObject.create(BuiltInRegistries.ITEM.getKey(existing), ForgeRegistries.ITEMS));
    }

    public record ItemRegObject<T extends Item>(RegistryObject<T> regObject) implements Supplier<T>, ItemLike
    {
        @Override
        @Nonnull
        public T get()
        {
            return regObject.get();
        }

        @Nonnull
        @Override
        public Item asItem()
        {
            return regObject.get();
        }

        public ResourceLocation getId()
        {
            return regObject.getId();
        }
    }
}
