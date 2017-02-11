package gigaherz.toolbelt.belt;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import com.google.common.collect.Lists;
import gigaherz.common.ItemRegistered;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.common.GuiHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;

public class ItemToolBelt extends ItemRegistered implements IBauble
{
    @CapabilityInject(IItemHandler.class)
    public static Capability<IItemHandler> ITEM_HANDLER;

    public ItemToolBelt(String name)
    {
        super(name);
        maxStackSize = 1;
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        playerIn.openGui(ToolBelt.instance, GuiHandler.BELT, worldIn, hand.ordinal(), 0, 0);

        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        playerIn.openGui(ToolBelt.instance, GuiHandler.BELT, worldIn, hand.ordinal(), 0, 0);

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        super.addInformation(stack, playerIn, tooltip, advanced);

        int size = getSlotsCount(stack);

        tooltip.add(I18n.format("text.toolbelt.tooltip", size-2, size));
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemStack)
    {
        return BaubleType.BELT;
    }

    @Override
    public boolean willAutoSync(ItemStack itemstack, EntityLivingBase player)
    {
        return true;
    }

    public static boolean isItemValid(@Nullable ItemStack stack)
    {
        if (stack == null)
            return true;
        if (stack.getItem() instanceof ItemToolBelt)
            return false;
        if (stack.getMaxStackSize() != 1)
            return false;
        return true;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt)
    {
        return new ICapabilityProvider()
        {
            final ItemStack itemStack = stack;

            @Override
            public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
            {
                if (capability == ITEM_HANDLER)
                    return true;
                return false;
            }

            @SuppressWarnings("unchecked")
            @Nullable
            @Override
            public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
            {
                if (capability == ITEM_HANDLER)
                    return (T) getItems(stack);
                return null;
            }
        };
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return !ItemStack.areItemsEqual(oldStack, newStack); // super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    public static int getSlotsCount(ItemStack stack)
    {
        int size = 2;

        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt != null)
        {
            size = nbt.getInteger("Size");
        }
        return size;
    }

    public static void setSlotsCount(ItemStack stack, int newSize)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
            nbt.setTag("Items", new NBTTagList());
        }

        nbt.setInteger("Size", newSize);
        stack.setTagCompound(nbt);
    }

    public static ToolBeltInventory getItems(ItemStack stack)
    {
        return new ToolBeltInventory(stack);
    }

    private static int[] xpCost = {3,5,8,12,15,20,30};
    public static int getUpgradeXP(ItemStack stack)
    {
        int slots = getSlotsCount(stack);

        if (slots >= 9)
            return -1;

        if (slots < 2)
            return 1;

        return xpCost[slots-2];
    }

    @Nullable
    public static ItemStack upgrade(ItemStack stack)
    {
        int slots = getSlotsCount(stack);

        if (slots >= 9)
            return stack.copy();

        stack = stack.copy();
        setSlotsCount(stack, slots+1);
        return stack;
    }
}