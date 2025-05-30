package mctmods.immersivetech.common.items.helper;

import blusunrize.immersiveengineering.common.items.IEBaseItem;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ITBaseItem extends Item
{
    private int burnTime = -1;
    private boolean isHidden = false;

    public ITBaseItem()
    {
        this(new Properties());
    }

    public ITBaseItem(Properties props)
    {
        super(props);
    }

    public ITBaseItem setBurnTime(int burnTime)
    {
        this.burnTime = burnTime;
        return this;
    }

    @Override
    public int getBurnTime(ItemStack itemStack, RecipeType<?> type)
    {
        return burnTime;
    }

    public boolean isHidden()
    {
        return isHidden;
    }

    protected void openGui(Player player, InteractionHand hand)
    {
        openGui(player, hand==InteractionHand.MAIN_HAND? EquipmentSlot.MAINHAND: EquipmentSlot.OFFHAND);
    }

    public void fillCreativeTab(CreativeModeTab.Output out)
    {
        out.accept(this);
    }

    protected void openGui(Player player, EquipmentSlot slot)
    {
        ItemStack stack = player.getItemBySlot(slot);
        IEMenuTypes.ItemContainerTypeNew<?> typeNew = getContainerTypeNew();
        if(typeNew!=null)
            NetworkHooks.openScreen(
                    (ServerPlayer)player,
                    new SimpleMenuProvider((id, inv, p) -> typeNew.create(id, inv, slot, stack), Component.empty())
            );
        else
        {
            IEMenuTypes.ItemContainerType<?> typeOld = getContainerType();
            if(typeOld!=null)
                NetworkHooks.openScreen((ServerPlayer)player, new SimpleMenuProvider(
                        (id, inv, p) -> typeOld.create(id, inv, player.level(), slot, stack),
                        Component.empty()
                ), buffer -> buffer.writeInt(slot.ordinal()));
        }
    }

    @Override
    public boolean isRepairable(@Nonnull ItemStack stack)
    {
        return false;
    }

    public boolean isIERepairable(@Nonnull ItemStack stack)
    {
        return super.isRepairable(stack);
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book)
    {
        return false;
    }

    @Nullable
    protected IEMenuTypes.ItemContainerType<?> getContainerType()
    {
        return null;
    }

    @Nullable
    protected IEMenuTypes.ItemContainerTypeNew<?> getContainerTypeNew()
    {
        return null;
    }

    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity)
    {
        return Mob.getEquipmentSlotForItem(stack)==armorType||getEquipmentSlot(stack)==armorType;
    }

    @Override
    public int getBarColor(ItemStack pStack)
    {
        // All our items use the vanilla color gradient, even if they use different getBarWidth implementation
        return Mth.hsvToRgb(Math.max(0.0F, getBarWidth(pStack)/(float)MAX_BAR_WIDTH)/3.0F, 1.0F, 1.0F);
    }
}
