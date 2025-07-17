package mctmods.immersivetech.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import mctmods.immersivetech.common.blocks.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetech.core.lib.ITLib;
import mctmods.immersivetech.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class ITDistiller extends ITTemplateMultiblock
{
    public static final ITDistiller INSTANCE = new ITDistiller();

    public ITDistiller()
    {
        super(new ResourceLocation(ITLib.MODID, "multiblocks/distiller"), new BlockPos(0,0,0), new BlockPos(1,1,1), new BlockPos(3,3,3), ITMultiblockProvider.DISTILLER);
    }

    @Override
    public void disassemble(Level world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation)
    {
        super.disassemble(world, origin, mirrored, clickDirectionAtCreation);
    }

    @Override
    public float getManualScale()
    {
        return 16;
    }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer)
    {
        consumer.accept(new ITClientMultiblockProperties(this, 0, 0, 0));
    }
}
