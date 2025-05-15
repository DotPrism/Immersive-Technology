package mctmods.immersivetech.common.data.generators;

import mctmods.immersivetech.core.lib.ITLib;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
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

    @Override
    protected void registerModels()
    {
        generateBlockItem("reinforced_coke_brick", "reinforced_coke_brick");
    }
}
