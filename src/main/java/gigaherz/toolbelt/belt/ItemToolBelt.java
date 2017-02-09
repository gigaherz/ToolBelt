package gigaherz.toolbelt.belt;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import gigaherz.common.ItemRegistered;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.common.GuiHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class ItemToolBelt extends ItemRegistered implements IBauble
{
    @CapabilityInject(IItemHandler.class)
    public static Capability<IItemHandler> CAP;

    public ItemToolBelt(String name)
    {
        super(name);
        maxStackSize = 1;
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        IItemHandler items = stack.getCapability(CAP, null);
        if (items == null)
            return EnumActionResult.FAIL;

        playerIn.openGui(ToolBelt.instance, GuiHandler.BELT, worldIn, hand.ordinal(), 0, 0);

        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        IItemHandler items = stack.getCapability(CAP, null);
        if (items == null)
            return new ActionResult<>(EnumActionResult.FAIL, stack);

        playerIn.openGui(ToolBelt.instance, GuiHandler.BELT, worldIn, hand.ordinal(), 0, 0);

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemStack)
    {
        return BaubleType.BELT;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt)
    {
        return new ICapabilitySerializable<NBTTagCompound>()
        {
            final ItemStackHandler items = new ItemStackHandler(9);

            @Override
            public NBTTagCompound serializeNBT()
            {
                return items.serializeNBT();
            }

            @Override
            public void deserializeNBT(NBTTagCompound nbt)
            {
                items.deserializeNBT(nbt);
            }

            @Override
            public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
            {
                if (capability == CAP)
                    return true;
                return false;
            }

            @SuppressWarnings("unchecked")
            @Nullable
            @Override
            public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
            {
                if (capability == CAP)
                    return (T)items;
                return null;
            }
        };
    }
}