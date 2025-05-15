package mctmods.immersivetech.client.renderer;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.client.utils.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import mctmods.immersivetech.client.models.ITDynamicModel;
import mctmods.immersivetech.common.blocks.multiblocks.logic.ITAdvancedCokeOvenLogic;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import java.util.List;

public class AdvancedCokeOvenRenderer extends ITBlockEntityRenderer<MultiblockBlockEntityMaster<ITAdvancedCokeOvenLogic.State>>
{
    public static final String NAME = "coke_oven_advanced";

    public static ITDynamicModel DRUM;

    @Override
    public void render(MultiblockBlockEntityMaster<ITAdvancedCokeOvenLogic.State> tile, float pPartialTick, PoseStack poseStack, @NotNull MultiBufferSource buffer, int pPackedLight, int pPackedOverlay)
    {
        IMultiblockBEHelperMaster<ITAdvancedCokeOvenLogic.State> helper = tile.getHelper();
        IMultiblockContext<ITAdvancedCokeOvenLogic.State> context = helper.getContext();

        final MultiblockOrientation orientation = context.getLevel().getOrientation();
        BlockPos pos = tile.getBlockPos();
        Level level = tile.getLevel();
        Direction dir = orientation.front();
        poseStack.pushPose();
        {
            rotateForFacing(poseStack, dir);
            poseStack.pushPose();
            {
                renderDynamicModel(DRUM, poseStack, buffer, dir, level, pos, pPackedLight, pPackedOverlay);
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private void renderDynamicModel(ITDynamicModel model, PoseStack matrix, MultiBufferSource buffer, Direction facing, Level level, BlockPos pos, int light, int overlay) {
        matrix.pushPose();
        List<BakedQuad> quads = model.get().getQuads(null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
        RenderUtils.renderModelTESRFancy(quads, buffer.getBuffer(RenderType.cutout()), matrix, level, pos, false, 0xffffff, light);
        matrix.popPose();

    }
}
