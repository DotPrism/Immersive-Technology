package mctmods.immersivetech.common.blocks;

import blusunrize.immersiveengineering.common.blocks.IEEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

public class CreativeBarrelBlock extends IEEntityBlock<CreativeBarrelBlockEntity>
{
    public CreativeBarrelBlock(BiFunction<BlockPos, BlockState, CreativeBarrelBlockEntity> makeEntity, Properties blockProps) {
        super(makeEntity, blockProps);
    }
}
