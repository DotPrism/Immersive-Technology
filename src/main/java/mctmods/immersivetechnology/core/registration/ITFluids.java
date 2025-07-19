package mctmods.immersivetechnology.core.registration;

import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.common.fluids.ITFluid;
import mctmods.immersivetechnology.common.fluids.ITFluidBlock;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static mctmods.immersivetechnology.core.lib.ITLib.rl;


public class ITFluids
{
    public static final DeferredRegister<Fluid> REGISTER = DeferredRegister.create(ForgeRegistries.FLUIDS, ITLib.MODID);
    public static final DeferredRegister<FluidType> TYPE_REGISTER = DeferredRegister.create(
            ForgeRegistries.Keys.FLUID_TYPES, ITLib.MODID
    );
    public static final List<ITFluids.FluidEntry> ALL_ENTRIES = new ArrayList<>();
    public static final Set<ITBlocks.BlockEntry<? extends LiquidBlock>> ALL_FLUID_BLOCKS = new HashSet<>();

    public static final ITFluids.FluidEntry STEAM = FluidEntry.make(
            "steam", rl("block/fluid/steam_still"), rl("block/fluid/steam_flow")
    );

    public static final ITFluids.FluidEntry STEAM_EXHAUST = FluidEntry.make(
            "steam_exhaust", rl("block/fluid/steam_still"), rl("block/fluid/steam_flow")
    );

    public static final ITFluids.FluidEntry FLUE_GAS = FluidEntry.make(
            "flue_gas", rl("block/fluid/flue_gas"), rl("block/fluid/flue_gas")
    );

    public record FluidEntry(
            RegistryObject<ITFluid> flowing,
            RegistryObject<ITFluid> still,
            ITBlocks.BlockEntry<ITFluidBlock> block,
            RegistryObject<BucketItem> bucket,
            RegistryObject<FluidType> type,
            List<Property<?>> properties
    )
    {
        private static ITFluids.FluidEntry make(String name, ResourceLocation stillTex, ResourceLocation flowingTex)
        {
            return make(name, 0, stillTex, flowingTex);
        }

        private static ITFluids.FluidEntry make(
                String name, ResourceLocation stillTex, ResourceLocation flowingTex, Consumer<FluidType.Properties> buildAttributes
        )
        {
            return make(name, 0, stillTex, flowingTex, buildAttributes);
        }

        private static ITFluids.FluidEntry make(String name, int burnTime, ResourceLocation stillTex, ResourceLocation flowingTex)
        {
            return make(name, burnTime, stillTex, flowingTex, null);
        }

        private static ITFluids.FluidEntry make(
                String name, int burnTime,
                ResourceLocation stillTex, ResourceLocation flowingTex,
                @Nullable Consumer<FluidType.Properties> buildAttributes
        )
        {
            return make(
                    name, burnTime, stillTex, flowingTex, ITFluid::new, ITFluid.Flowing::new, buildAttributes,
                    ImmutableList.of()
            );
        }

        private static ITFluids.FluidEntry make(
                String name, ResourceLocation stillTex, ResourceLocation flowingTex,
                Function<ITFluids.FluidEntry, ? extends ITFluid> makeStill, Function<ITFluids.FluidEntry, ? extends ITFluid> makeFlowing,
                @Nullable Consumer<FluidType.Properties> buildAttributes, ImmutableList<Property<?>> properties
        )
        {
            return make(name, 0, stillTex, flowingTex, makeStill, makeFlowing, buildAttributes, properties);
        }

        private static ITFluids.FluidEntry make(
                String name, int burnTime,
                ResourceLocation stillTex, ResourceLocation flowingTex,
                Function<ITFluids.FluidEntry, ? extends ITFluid> makeStill, Function<ITFluids.FluidEntry, ? extends ITFluid> makeFlowing,
                @Nullable Consumer<FluidType.Properties> buildAttributes, List<Property<?>> properties)
        {
            FluidType.Properties builder = FluidType.Properties.create()
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY);
            if(buildAttributes!=null)
                buildAttributes.accept(builder);
            RegistryObject<FluidType> type = TYPE_REGISTER.register(
                    name, () -> makeTypeWithTextures(builder, stillTex, flowingTex)
            );
            Mutable<ITFluids.FluidEntry> thisMutable = new MutableObject<>();
            RegistryObject<ITFluid> still = REGISTER.register(name, () -> ITFluid.makeFluid(
                    makeStill, thisMutable.getValue()
            ));
            RegistryObject<ITFluid> flowing = REGISTER.register(name+"_flowing", () -> ITFluid.makeFluid(
                    makeFlowing, thisMutable.getValue()
            ));
            ITBlocks.BlockEntry<ITFluidBlock> block = new ITBlocks.BlockEntry<>(
                    name+"_fluid_block",
                    () -> BlockBehaviour.Properties.copy(Blocks.WATER),
                    p -> new ITFluidBlock(thisMutable.getValue(), p)
            );
            RegistryObject<BucketItem> bucket = ITItems.REGISTER.register(name+"_bucket", () -> makeBucket(still, burnTime));
            ITFluids.FluidEntry entry = new ITFluids.FluidEntry(flowing, still, block, bucket, type, properties);
            thisMutable.setValue(entry);
            ALL_FLUID_BLOCKS.add(block);
            ALL_ENTRIES.add(entry);
            return entry;
        }

        private static FluidType makeTypeWithTextures(
                FluidType.Properties builder, ResourceLocation stillTex, ResourceLocation flowingTex
        )
        {
            return new FluidType(builder)
            {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer)
                {
                    consumer.accept(new IClientFluidTypeExtensions()
                    {
                        @Override
                        public ResourceLocation getStillTexture()
                        {
                            return stillTex;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture()
                        {
                            return flowingTex;
                        }
                    });
                }
            };
        }

        public ITFluid getFlowing()
        {
            return flowing.get();
        }

        public ITFluid getStill()
        {
            return still.get();
        }

        public ITFluidBlock getBlock()
        {
            return block.get();
        }

        public BucketItem getBucket()
        {
            return bucket.get();
        }

        private static BucketItem makeBucket(RegistryObject<ITFluid> still, int burnTime)
        {
            return new BucketItem(
                    still, new Item.Properties()
                    .stacksTo(1)
                    .craftRemainder(Items.BUCKET))
            {
                @Override
                public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt)
                {
                    return new FluidBucketWrapper(stack);
                }

                @Override
                public int getBurnTime(ItemStack itemStack, RecipeType<?> type)
                {
                    return burnTime;
                }
            };
        }

        public RegistryObject<ITFluid> getStillGetter()
        {
            return still;
        }
    }
}
