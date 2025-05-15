package mctmods.immersivetech.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEEntityBlock;
import mctmods.immersivetech.common.blocks.helper.ITEntityBlock;
import mctmods.immersivetech.core.registration.ITRegistrationHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import java.util.Properties;

public class CokeOvenPreheaterBlock extends IEEntityBlock<CokeOvenPreheaterBlockEntity>
{
    public CokeOvenPreheaterBlock(Properties props)
    {
        super(ITRegistrationHolder.COKEOVEN_PREHEATER, props);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(IEProperties.FACING_HORIZONTAL, IEProperties.MULTIBLOCKSLAVE);
    }

    @Override
    public boolean canIEBlockBePlaced(BlockState newState, BlockPlaceContext context)
    {
        BlockPos start = context.getClickedPos();
        Level w = context.getLevel();
        return areAllReplaceable(start, start.north(2), context);
    }
}
