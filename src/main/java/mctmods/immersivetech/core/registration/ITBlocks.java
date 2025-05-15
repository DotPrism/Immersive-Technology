package mctmods.immersivetech.core.registration;

import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import mctmods.immersivetech.common.blocks.CokeOvenPreheaterBlock;
import mctmods.immersivetech.common.blocks.helper.ITBlockBase;
import mctmods.immersivetech.common.items.helper.BlockItemIT;
import mctmods.immersivetech.core.lib.ITLib;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.common.register.IEBlocks.METAL_PROPERTIES_NO_OCCLUSION;

public class ITBlocks {
    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, ITLib.MODID);;

    public static final class MetalDevices
    {
        public static final BlockEntry<CokeOvenPreheaterBlock> COKE_OVEN_PREHEATER = new BlockEntry<>(
                "coke_oven_preheater", METAL_PROPERTIES_NO_OCCLUSION, CokeOvenPreheaterBlock::new
        );

        private static void init()
        {

        }
    }

    public static void init(IEventBus event)
    {
        REGISTER.register(event);
        for(BlockEntry<?> entry : BlockEntry.ALL_ENTRIES)
        {
            Function<Block, BlockItemIT> toItem;

                toItem = BlockItemIT::new;
            Function<Block, BlockItemIT> finalToItem = toItem;
            ITItems.REGISTER.register(entry.getId().getPath(), () -> finalToItem.apply(entry.get()));
        }
    }

    public static final class BlockEntry<T extends Block> implements Supplier<T>, ItemLike
    {
        public static final Collection<BlockEntry<?>> ALL_ENTRIES = new ArrayList<>();

        private final RegistryObject<T> regObject;
        private final Supplier<BlockBehaviour.Properties> properties;

        public static BlockEntry<ITBlockBase> simple(String name, Supplier<BlockBehaviour.Properties> properties, Consumer<ITBlockBase> extra)
        {
            return new BlockEntry<>(name, properties, p -> Util.make(new ITBlockBase(p), extra));
        }

        public static BlockEntry<ITBlockBase> simple(String name, Supplier<BlockBehaviour.Properties> properties)
        {
            return simple(name, properties, $ -> {
            });
        }

        public BlockEntry(String name, Supplier<BlockBehaviour.Properties> properties, Function<BlockBehaviour.Properties, T> make)
        {
            this.properties = properties;
            this.regObject = REGISTER.register(name, () -> make.apply(properties.get()));
            ALL_ENTRIES.add(this);
        }

        public BlockEntry(T existing)
        {
            this.properties = () -> BlockBehaviour.Properties.copy(existing);
            this.regObject = RegistryObject.create(BuiltInRegistries.BLOCK.getKey(existing), ForgeRegistries.BLOCKS);
        }

        @SuppressWarnings("unchecked")
        public BlockEntry(BlockEntry<? extends T> toCopy)
        {
            this.properties = toCopy.properties;
            this.regObject = (RegistryObject<T>)toCopy.regObject;
        }

        @Override
        public T get()
        {
            return regObject.get();
        }

        public BlockState defaultBlockState()
        {
            return get().defaultBlockState();
        }

        public ResourceLocation getId()
        {
            return regObject.getId();
        }

        public BlockBehaviour.Properties getProperties()
        {
            return properties.get();
        }

        @Nonnull
        @Override
        public Item asItem()
        {
            return get().asItem();
        }

        public RegistryObject<? extends Block> getRegObject()
        {
            return regObject;
        }
    }
}
