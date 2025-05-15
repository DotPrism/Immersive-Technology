package mctmods.immersivetech.core;

import mctmods.immersivetech.core.lib.ITLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("all")
@Mod.EventBusSubscriber(modid = ITLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ITConfig
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // a list of strings that are treated as resource locations for items
    //private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            //.comment("A list of items to log on common setup.")
            //.defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), ITConfig::validateItemName);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    //public static Set<Item> items;

    private static boolean validateItemName(final Object obj)
    {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        // convert the list of strings into a set of items
        //items = ITEM_STRINGS.get().stream()
                //.map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
                //.collect(Collectors.toSet());

    }
}
