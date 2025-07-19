package mctmods.immersivetechnology.common.blocks.helper;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.common.blocks.*;
import blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.gui.IEBaseContainerOld;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

public class ITEntityBlock <T extends BlockEntity> extends ITBlockBase implements IEBlockInterfaces.IColouredBlock, EntityBlock
{
    private boolean hasColours = false;
    private final BiFunction<BlockPos, BlockState, T> makeEntity;
    private ITEntityBlock.BEClassInspectedData classData;

    public ITEntityBlock(BiFunction<BlockPos, BlockState, T> makeEntity, Properties blockProps)
    {
        this(makeEntity, blockProps, true);
    }

    public ITEntityBlock(
            BiFunction<BlockPos, BlockState, T> makeEntity, Properties blockProps, boolean fitsIntoContainer
    )
    {
        super(blockProps, fitsIntoContainer);
        this.makeEntity = makeEntity;
    }

    public ITEntityBlock(RegistryObject<BlockEntityType<T>> tileType, Properties blockProps)
    {
        this(tileType, blockProps, true);
    }

    public ITEntityBlock(RegistryObject<BlockEntityType<T>> tileType, Properties blockProps, boolean fitsIntoContainer)
    {
        this((bp, state) -> tileType.get().create(bp, state), blockProps, fitsIntoContainer);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState)
    {
        return makeEntity.apply(pPos, pState);
    }

    @Nullable
    @Override
    public <T2 extends BlockEntity>
    BlockEntityTicker<T2> getTicker(Level world, BlockState state, BlockEntityType<T2> type)
    {
        BlockEntityTicker<T2> baseTicker = getClassData().makeBaseTicker(world.isClientSide);
        if(makeEntity instanceof MultiblockBEType<?> multiBEType && type != multiBEType.master())
            return null;
        return baseTicker;
    }

    private static final List<BooleanProperty> DEFAULT_OFF = ImmutableList.of(
            IEProperties.MULTIBLOCKSLAVE, IEProperties.ACTIVE, IEProperties.MIRRORED
    );

    @Override
    protected BlockState getInitDefaultState()
    {
        BlockState ret = super.getInitDefaultState();
        if(ret.hasProperty(IEProperties.FACING_ALL))
            ret = ret.setValue(IEProperties.FACING_ALL, getDefaultFacing());
        else if(ret.hasProperty(IEProperties.FACING_HORIZONTAL))
            ret = ret.setValue(IEProperties.FACING_HORIZONTAL, getDefaultFacing());
        for(BooleanProperty defaultOff : DEFAULT_OFF)
            if(ret.hasProperty(defaultOff))
                ret = ret.setValue(defaultOff, false);
        return ret;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving)
    {
        BlockEntity tile = world.getBlockEntity(pos);
        if(state.getBlock()!=newState.getBlock())
        {
            if(tile instanceof ITBlockEntity)
                ((ITBlockEntity)tile).setOverrideState(state);
            if(tile instanceof ITBlockInterfaces.IHasDummyBlocks)
                ((ITBlockInterfaces.IHasDummyBlocks)tile).breakDummies(pos, state);
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, BlockEntity tile, ItemStack stack)
    {
        if(tile instanceof IEBlockInterfaces.IAdditionalDrops)
        {
            //TODO remove or turn into loot entries?
            Collection<ItemStack> stacks = ((IEBlockInterfaces.IAdditionalDrops)tile).getExtraDrops(player, state);
            if(stacks!=null&&!stacks.isEmpty())
                for(ItemStack s : stacks)
                    if(!s.isEmpty())
                        popResource(world, pos, s);
        }
        super.playerDestroy(world, player, pos, state, tile, stack);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter world, BlockPos pos, Entity entity)
    {
        BlockEntity tile = world.getBlockEntity(pos);
        if(tile instanceof IEBlockInterfaces.IEntityProof)
            return ((IEBlockInterfaces.IEntityProof)tile).canEntityDestroy(entity);
        return super.canEntityDestroy(state, world, pos, entity);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player)
    {
        BlockEntity tile = world.getBlockEntity(pos);
        if(tile instanceof IEBlockInterfaces.IBlockEntityDrop &&target instanceof BlockHitResult)
        {
            ItemStack s = ((IEBlockInterfaces.IBlockEntityDrop)tile).getPickBlock(player, world.getBlockState(pos), target);
            if(!s.isEmpty())
                return s;
        }
        Item item = this.asItem();
        return item== Items.AIR?ItemStack.EMPTY: new ItemStack(item, 1);
    }


    @Override
    public boolean triggerEvent(BlockState state, Level worldIn, BlockPos pos, int eventID, int eventParam)
    {
        super.triggerEvent(state, worldIn, pos, eventID, eventParam);
        BlockEntity tileentity = worldIn.getBlockEntity(pos);
        return tileentity!=null&&tileentity.triggerEvent(eventID, eventParam);
    }

    protected Direction getDefaultFacing()
    {
        return Direction.NORTH;
    }

    @Override
    public void onIEBlockPlacedBy(BlockPlaceContext context, BlockState state)
    {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity tile = world.getBlockEntity(pos);
        Player placer = context.getPlayer();
        Direction side = context.getClickedFace();
        float hitX = (float)context.getClickLocation().x-pos.getX();
        float hitY = (float)context.getClickLocation().y-pos.getY();
        float hitZ = (float)context.getClickLocation().z-pos.getZ();

        if(tile instanceof ITBlockInterfaces.IDirectionalBE directionalBE)
        {
            Direction f = directionalBE.getFacingForPlacement(context);
            directionalBE.setFacing(f);
            if(tile instanceof ITBlockInterfaces.IAdvancedDirectionalBE advDirectional)
                advDirectional.onDirectionalPlacement(side, hitX, hitY, hitZ, placer);
        }
        if(tile instanceof ITBlockInterfaces.IHasDummyBlocks hasDummyBlocks)
            hasDummyBlocks.placeDummies(context, state);
        if(tile instanceof ITBlockInterfaces.IPlacementInteraction placementInteractionBE)
            placementInteractionBE.onBEPlaced(context);
    }

    @Override
    public InteractionResult hammerUseSide(Direction side, Player player, InteractionHand hand, Level w, BlockPos pos, BlockHitResult hit)
    {
        BlockEntity tile = w.getBlockEntity(pos);
        if(tile instanceof IEBlockInterfaces.IHammerInteraction)
        {
            boolean b = ((IEBlockInterfaces.IHammerInteraction)tile).hammerUseSide(side, player, hand, hit.getLocation());
            if(b)
                return InteractionResult.SUCCESS;
            else
                return InteractionResult.FAIL;
        }
        return super.hammerUseSide(side, player, hand, w, pos, hit);
    }

    @Override
    public InteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Level w, BlockPos pos, BlockHitResult hit)
    {
        BlockEntity tile = w.getBlockEntity(pos);
        if(tile instanceof IEBlockInterfaces.IScrewdriverInteraction)
        {
            InteractionResult teResult = ((IEBlockInterfaces.IScrewdriverInteraction)tile).screwdriverUseSide(side, player, hand, hit.getLocation());
            if(teResult!=InteractionResult.PASS)
                return teResult;
        }
        return super.screwdriverUseSide(side, player, hand, w, pos, hit);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        InteractionResult superResult = super.use(state, world, pos, player, hand, hit);
        if(superResult.consumesAction())
            return superResult;
        final Direction side = hit.getDirection();
        final float hitX = (float)hit.getLocation().x-pos.getX();
        final float hitY = (float)hit.getLocation().y-pos.getY();
        final float hitZ = (float)hit.getLocation().z-pos.getZ();
        ItemStack heldItem = player.getItemInHand(hand);
        BlockEntity tile = world.getBlockEntity(pos);
        if(tile instanceof IEBlockInterfaces.IDirectionalBE && Utils.isHammer(heldItem)&&((IEBlockInterfaces.IDirectionalBE)tile).canHammerRotate(
                side,
                hit.getLocation().subtract(Vec3.atLowerCornerOf(pos)),
                player)&&!world.isClientSide)
        {
            Direction f = ((IEBlockInterfaces.IDirectionalBE)tile).getFacing();
            Direction oldF = f;
            PlacementLimitation limit = ((IEBlockInterfaces.IDirectionalBE)tile).getFacingLimitation();
            switch(limit)
            {
                case SIDE_CLICKED:
                    f = DirectionUtils.VALUES[Math.floorMod(f.ordinal()+(player.isShiftKeyDown()?-1: 1), DirectionUtils.VALUES.length)];
                    break;
                case PISTON_LIKE:
                    f = player.isShiftKeyDown()!=(side.getAxisDirection()== Direction.AxisDirection.NEGATIVE)?DirectionUtils.rotateAround(f, side.getAxis()).getOpposite(): DirectionUtils.rotateAround(f, side.getAxis());
                    break;
                case HORIZONTAL:
                case HORIZONTAL_PREFER_SIDE:
                case HORIZONTAL_QUADRANT:
                case HORIZONTAL_AXIS:
                    f = player.isShiftKeyDown()!=side.equals(Direction.DOWN)?f.getCounterClockWise(): f.getClockWise();
                    break;
            }
            ((IEBlockInterfaces.IDirectionalBE)tile).setFacing(f);
            ((IEBlockInterfaces.IDirectionalBE)tile).afterRotation(oldF, f);
            tile.setChanged();
            world.sendBlockUpdated(pos, state, state, 3);
            world.blockEvent(tile.getBlockPos(), tile.getBlockState().getBlock(), 255, 0);
            return InteractionResult.SUCCESS;
        }
        if(tile instanceof IEBlockInterfaces.IPlayerInteraction)
        {
            boolean b = ((IEBlockInterfaces.IPlayerInteraction)tile).interact(side, player, hand, heldItem, hitX, hitY, hitZ);
            if(b)
                return InteractionResult.SUCCESS;
        }
        if(tile instanceof MenuProvider menuProvider&&hand==InteractionHand.MAIN_HAND&&!player.isShiftKeyDown())
        {
            if(player instanceof ServerPlayer serverPlayer)
            {
                if(menuProvider instanceof IEBlockInterfaces.IInteractionObjectIE<?> interaction)
                {
                    interaction = interaction.getGuiMaster();
                    if(interaction!=null&&interaction.canUseGui(player))
                    {
                        // This can be removed once IEBaseContainerOld is gone
                        var tempMenu = interaction.createMenu(0, player.getInventory(), player);
                        if(tempMenu instanceof IEBaseContainerOld<?>)
                            NetworkHooks.openScreen(serverPlayer, interaction, ((BlockEntity)interaction).getBlockPos());
                        else
                            NetworkHooks.openScreen(serverPlayer, interaction);
                    }
                }
                else
                    NetworkHooks.openScreen(serverPlayer, menuProvider);
            }
            return InteractionResult.SUCCESS;
        }
        return superResult;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
    {
        if(!world.isClientSide)
        {
            BlockEntity tile = world.getBlockEntity(pos);
            if(tile instanceof ITBlockEntity)
                ((ITBlockEntity)tile).onNeighborBlockChange(fromPos);
        }
    }

    public ITEntityBlock setHasColours()
    {
        this.hasColours = true;
        return this;
    }

    @Override
    public boolean hasCustomBlockColours()
    {
        return hasColours;
    }

    @Override
    public int getRenderColour(BlockState state, @Nullable BlockGetter worldIn, @Nullable BlockPos pos, int tintIndex)
    {
        if(worldIn!=null&&pos!=null)
        {
            BlockEntity tile = worldIn.getBlockEntity(pos);
            if(tile instanceof IEBlockInterfaces.IColouredBE)
                return ((IEBlockInterfaces.IColouredBE)tile).getRenderColour(tintIndex);
        }
        return 0xffffff;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        if(state.getBlock()==this)
        {
            BlockEntity te = world.getBlockEntity(pos);
            if(te instanceof IEBlockInterfaces.ISelectionBounds)
                return ((IEBlockInterfaces.ISelectionBounds)te).getSelectionShape(context);
        }
        return super.getShape(state, world, pos, context);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        if(getClassData().customCollisionBounds())
        {
            BlockEntity te = world.getBlockEntity(pos);
            if(te instanceof IEBlockInterfaces.ICollisionBounds collisionBounds)
                return collisionBounds.getCollisionShape(context);
            else
                // Temporary hack: The vanilla Entity#isInWall passes nonsense positions to this method (always the head
                // center rather than the actual block). This stops our blocks from suffocating people when this happens
                return Shapes.empty();
        }
        return super.getCollisionShape(state, world, pos, context);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos)
    {
        if(world.getBlockState(pos).getBlock()==this)
        {
            BlockEntity te = world.getBlockEntity(pos);
            if(te instanceof IEBlockInterfaces.ISelectionBounds)
                return ((IEBlockInterfaces.ISelectionBounds)te).getSelectionShape(null);
        }
        return super.getInteractionShape(state, world, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasAnalogOutputSignal(BlockState state)
    {
        return getClassData().hasComparatorOutput;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos)
    {
        BlockEntity te = world.getBlockEntity(pos);
        if(te instanceof IEBlockInterfaces.IComparatorOverride compOverride)
            return compOverride.getComparatorInputOverride();
        return 0;
    }


    @Override
    @SuppressWarnings("deprecation")
    public int getSignal(BlockState blockState, BlockGetter world, BlockPos pos, Direction side)
    {
        BlockEntity te = world.getBlockEntity(pos);
        if(te instanceof IEBlockInterfaces.IRedstoneOutput rsOutput)
            return rsOutput.getWeakRSOutput(side);
        return 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getDirectSignal(BlockState blockState, BlockGetter world, BlockPos pos, Direction side)
    {
        BlockEntity te = world.getBlockEntity(pos);
        if(te instanceof IEBlockInterfaces.IRedstoneOutput rsOutput)
            return rsOutput.getStrongRSOutput(side);
        return 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isSignalSource(BlockState state)
    {
        return getClassData().emitsRedstone();
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side)
    {
        BlockEntity te = world.getBlockEntity(pos);
        if(te instanceof IEBlockInterfaces.IRedstoneOutput rsOutput)
            return rsOutput.canConnectRedstone(side);
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity)
    {
        BlockEntity te = world.getBlockEntity(pos);
        if(te instanceof IEBaseBlockEntity)
            ((IEBaseBlockEntity)te).onEntityCollision(world, entity);
    }

    public static boolean areAllReplaceable(BlockPos start, BlockPos end, BlockPlaceContext context)
    {
        Level w = context.getLevel();
        return BlockPos.betweenClosedStream(start, end).allMatch(
                pos -> {
                    BlockPlaceContext subContext = BlockPlaceContext.at(context, pos, context.getClickedFace());
                    return w.getBlockState(pos).canBeReplaced(subContext);
                });
    }

    private ITEntityBlock.BEClassInspectedData getClassData()
    {
        if(this.classData==null)
        {
            T tempBE = makeEntity.apply(BlockPos.ZERO, getInitDefaultState());
            this.classData = new ITEntityBlock.BEClassInspectedData(
                    tempBE instanceof IEServerTickableBE,
                    tempBE instanceof ITClientTickableBE,
                    tempBE instanceof ITBlockInterfaces.IComparatorOverride,
                    tempBE instanceof ITBlockInterfaces.IRedstoneOutput,
                    tempBE instanceof ITBlockInterfaces.ICollisionBounds
            );
        }
        return this.classData;
    }

    private record BEClassInspectedData(
            boolean serverTicking,
            boolean clientTicking,
            boolean hasComparatorOutput,
            boolean emitsRedstone,
            boolean customCollisionBounds
    )
    {
        @Nullable
        public <T extends BlockEntity> BlockEntityTicker<T> makeBaseTicker(boolean isClient)
        {
            if(serverTicking&&!isClient)
                return IEServerTickableBE.makeTicker();
            else if(clientTicking&&isClient)
                return IEClientTickableBE.makeTicker();
            else
                return null;
        }
    }
}