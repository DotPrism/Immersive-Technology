package mctmods.immersivetech.common.data.generators;

import mctmods.immersivetech.client.renderer.AdvancedCokeOvenRenderer;
import mctmods.immersivetech.client.renderer.CokeOvenPreheaterRenderer;
import mctmods.immersivetech.core.lib.ITLib;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.client.model.generators.loaders.ObjModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.util.Map.Entry;

@SuppressWarnings("all")
public class ITDynamicModelProvider extends ModelProvider<ITDynamicModelProvider.SimpleModelBuilder>
{
    private final ITBlockStateProvider multiblocks;

    public ITDynamicModelProvider(ITBlockStateProvider multiblocks, PackOutput output, ExistingFileHelper existingFileHelper)
    {
        super(output, ITLib.MODID, "dynamic", rl -> new SimpleModelBuilder(rl, existingFileHelper), existingFileHelper);
        this.multiblocks = multiblocks;
    }

    @Override
    protected void registerModels()
    {
        for(Entry<Block, ModelFile> multiblock : multiblocks.unsplitModels.entrySet())
            withExistingParent(BuiltInRegistries.BLOCK.getKey(multiblock.getKey()).getPath(), multiblock.getValue().getLocation());

        //getBuilder(AdvancedCokeOvenRenderer.NAME)
                //.customLoader(ObjModelBuilder::begin)
                //.modelLocation(rl("models/block/multiblock/obj/coke_oven_advanced/coke_oven_advanced.obj"))
                //.flipV(true)
                //.end();
        getBuilder(CokeOvenPreheaterRenderer.NAME)
                .customLoader(ObjModelBuilder::begin)
                .modelLocation(rl("models/block/coke_oven_preheater.obj"))
                .flipV(true)
                .end();

    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(ITLib.MODID, path);
    }

    @Nonnull
    @Override
    public String getName()
    {
        return "IT Dynamic models";
    }

    public static class SimpleModelBuilder extends ModelBuilder<SimpleModelBuilder>
    {

        public SimpleModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper)
        {
            super(outputLocation, existingFileHelper);
        }
    }
}
