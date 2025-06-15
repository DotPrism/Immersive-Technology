package mctmods.immersivetech.common.data.generators;

import blusunrize.immersiveengineering.common.register.IEFluids;
import mctmods.immersivetech.core.lib.ITLib;
import mctmods.immersivetech.core.registration.ITFluids;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.slf4j.Logger;

public class ITItemModelProvider extends ItemModelProvider
{
    private final Logger logger = ITLib.getNewLogger();
    public ITItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), ITLib.MODID, existingFileHelper);
    }

    private void generateBlockItem(String item_name, String parent_loc)
    {
        String itemLocation = new ResourceLocation(ITLib.MODID, "item/"+ item_name).getPath();
        ResourceLocation parentLocation = new ResourceLocation(ITLib.MODID, "block/"+parent_loc);

        withExistingParent(itemLocation, parentLocation);
    }

    private void createBucket(ITFluids.FluidEntry entry)
    {
        withExistingParent(name(entry.getBucket()), forgeLoc("item/bucket"))
                .customLoader(DynamicFluidContainerModelBuilder::begin)
                .fluid(entry.getStill());
    }

    @Override
    protected void registerModels()
    {
        generateBlockItem("reinforced_coke_brick", "reinforced_coke_brick");
        ITFluids.ALL_ENTRIES.forEach(this::createBucket);
    }

    private String name(ItemLike item)
    {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
    }

    private ResourceLocation forgeLoc(String s)
    {
        return new ResourceLocation("forge", s);
    }
}
