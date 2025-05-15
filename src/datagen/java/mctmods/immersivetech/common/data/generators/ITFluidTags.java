package mctmods.immersivetech.common.data.generators;

import mctmods.immersivetech.core.lib.ITLib;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ITFluidTags extends FluidTagsProvider
{
    public ITFluidTags(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, ITLib.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(Provider provider)
    {
    }
}
