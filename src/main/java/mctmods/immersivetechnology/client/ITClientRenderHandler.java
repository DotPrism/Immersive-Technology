package mctmods.immersivetechnology.client;

import mctmods.immersivetechnology.common.blocks.helper.ITBlockType;
import mctmods.immersivetechnology.common.items.helper.ITFlagItem;
import mctmods.immersivetechnology.core.registration.ITRegistrationHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ITClientRenderHandler implements ItemColor, BlockColor
{
    @OnlyIn(Dist.CLIENT)
    private static Map<RenderTypeSkeleton, RenderType> renderTypes;

    private static final Map<Block, RenderTypeSkeleton> mapping = new HashMap<>();
    private static final Map<Block, Block> inheritances = new HashMap<>();

    public static ITClientRenderHandler INSTANCE = new ITClientRenderHandler();

    // Register This as the color handler for all IT Items and Blocks
    public static void register(){
        for(Supplier<Item> holder : ITRegistrationHolder.getItemRegistryMap().values()){
            Item i = holder.get();
            if(i instanceof ITFlagItem){
                Minecraft.getInstance().getItemColors().register(INSTANCE, i);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void init(FMLClientSetupEvent event) {
        for(Block b : inheritances.keySet()) {
            Block inherit = inheritances.get(b);
            if(mapping.containsKey(inherit))
                mapping.put(b, mapping.get(inherit));
        }

        for(Block b : mapping.keySet()) {
            ItemBlockRenderTypes.setRenderLayer(b, renderTypes.get(mapping.get(b)));
        }

        inheritances.clear();
        mapping.clear();
    }

    // Color Function for IT Blocks
    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter getter, @Nullable BlockPos pos, int index) {
        if(state.getBlock() instanceof ITBlockType type)
            return type.getColor(index);
        return 0xffffff;
    }

    // Color Function for IT Items
    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if(stack.getItem() instanceof ITFlagItem type)
            return type.getColor(tintIndex);
        return 0xffffff;
    }

    @OnlyIn(Dist.CLIENT)
    public static void setRenderType(Block block, RenderTypeSkeleton skeleton) {
        setRenderTypeClient(block, skeleton);
    }

    @OnlyIn(Dist.CLIENT)
    private static void setRenderTypeClient(Block block, RenderTypeSkeleton skeleton) {
        resolveRenderTypes();
        mapping.put(block, skeleton);
    }

    @OnlyIn(Dist.CLIENT)
    private static void resolveRenderTypes() {
        if(renderTypes == null) {
            renderTypes = new HashMap<>();

            renderTypes.put(RenderTypeSkeleton.SOLID, RenderType.solid());
            renderTypes.put(RenderTypeSkeleton.CUTOUT, RenderType.cutout());
            renderTypes.put(RenderTypeSkeleton.CUTOUT_MIPPED, RenderType.cutoutMipped());
            renderTypes.put(RenderTypeSkeleton.TRANSLUCENT, RenderType.translucent());
        }
    }

    public enum RenderTypeSkeleton {
        SOLID,
        CUTOUT,
        CUTOUT_MIPPED,
        TRANSLUCENT;
    }
}
