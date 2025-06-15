package mctmods.immersivetech.common.data.generators;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import mctmods.immersivetech.core.lib.ITLib;
import mctmods.immersivetech.core.registration.ITMultiblockProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ITBlockTags extends BlockTagsProvider
{
    public ITBlockTags(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, ITLib.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(Provider provider)
    {
        ITLib.IT_LOGGER.info("IT Block Tags");

        IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tag = this.tag(BlockTags.MINEABLE_WITH_PICKAXE);
        IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tag2 = this.tag(BlockTags.NEEDS_IRON_TOOL);

        registerMineable(tag, ITMultiblockProvider.ADV_COKE_OVEN);
        registerMineable(tag, ITMultiblockProvider.BOILER);
        registerMineable(tag, ITMultiblockProvider.ALTERNATOR);
        registerMineable(tag, ITMultiblockProvider.STEAM_TURBINE);
        registerMineable(tag, ITMultiblockProvider.GAS_TURBINE);
        registerMineable(tag2, ITMultiblockProvider.ADV_COKE_OVEN);
        registerMineable(tag2, ITMultiblockProvider.BOILER);
        registerMineable(tag2, ITMultiblockProvider.ALTERNATOR);
        registerMineable(tag2, ITMultiblockProvider.STEAM_TURBINE);
        registerMineable(tag2, ITMultiblockProvider.GAS_TURBINE);
    }

    private void registerMineable(IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tag, MultiblockRegistration<?>... entries) {
        MultiblockRegistration[] var3 = entries;
        int var4 = entries.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            MultiblockRegistration<?> entry = var3[var5];
            tag.add((Block)entry.block().get());
        }
    }
}
