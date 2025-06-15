package mctmods.immersivetech.common.data.generators;

import blusunrize.immersiveengineering.api.utils.TagUtils;
import mctmods.immersivetech.core.lib.ITLib;
import mctmods.immersivetech.core.registration.ITFluids;
import mctmods.immersivetech.core.registration.ITTags;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
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
        tag(ITTags.fluidSteam).add(ITFluids.STEAM.getStill());
        tag(ITTags.fluidSteamForge).add(ITFluids.STEAM.getStill());
    }
}
