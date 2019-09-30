package gigaherz.toolbelt.belt;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import gigaherz.common.ItemRegistered;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.common.GuiHandler;
import gigaherz.toolbelt.customslots.ExtensionSlotItemHandler;
import gigaherz.toolbelt.customslots.IExtensionContainer;
import gigaherz.toolbelt.customslots.IExtensionSlot;
import gigaherz.toolbelt.customslots.IExtensionSlotItem;
import gigaherz.toolbelt.customslots.example.RpgEquipment;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Optional.Interface(modid = "baubles", iface = "baubles.api.IBauble")
public class ItemToolBelt extends ItemRegistered implements IBauble, IExtensionSlotItem
{
    @CapabilityInject(IItemHandler.class)
    public static Capability<IItemHandler> ITEM_HANDLER;

    @CapabilityInject(IExtensionSlotItem.class)
    public static Capability<IExtensionSlotItem> EXTENSION_SLOT_ITEM;

    public static final ImmutableSet<ResourceLocation> BELT_SLOT_LIST = ImmutableSet.of(RpgEquipment.BELT);

    public ItemToolBelt(String name)
    {
        super(name);
        maxStackSize = 1;
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (hand != EnumHand.MAIN_HAND)
            return EnumActionResult.PASS;

        player.openGui(ToolBelt.instance, GuiHandler.BELT, worldIn, hand.ordinal(), 0, 0);

        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if (hand != EnumHand.MAIN_HAND)
            return new ActionResult<>(EnumActionResult.PASS, playerIn.getHeldItem(hand));

        playerIn.openGui(ToolBelt.instance, GuiHandler.BELT, worldIn, hand.ordinal(), 0, 0);

        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced)
    {
        super.addInformation(stack, world, tooltip, advanced);

        int size = getSlotsCount(stack);

        tooltip.add(I18n.format("text.toolbelt.tooltip", size - 2, size));
    }

    @Optional.Method(modid = "baubles")
    @Override
    public BaubleType getBaubleType(ItemStack itemStack)
    {
        return BaubleType.BELT;
    }

    @Nonnull
    @Override
    public ImmutableSet<ResourceLocation> getAcceptableSlots(@Nonnull ItemStack stack)
    {
        return BELT_SLOT_LIST;
    }

    @Override
    public boolean willAutoSync(ItemStack itemstack, EntityLivingBase player)
    {
        return true;
    }

    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase player)
    {
        tickAllSlots(itemstack, player);
    }

    @Override
    public void onWornTick(ItemStack itemstack, IExtensionSlot slot)
    {
        tickAllSlots(itemstack, slot.getContainer().getOwner());
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        if (entityIn instanceof EntityLivingBase)
        {
            tickAllSlots(stack, (EntityLivingBase) entityIn);
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(final ItemStack stack, NBTTagCompound nbt)
    {
        return new ICapabilityProvider()
        {
            final ToolBeltInventory itemHandler = new ToolBeltInventory(stack);

            @Override
            public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
            {
                if (capability == ITEM_HANDLER)
                    return true;
                if (capability == EXTENSION_SLOT_ITEM)
                    return true;
                return false;
            }

            @SuppressWarnings("unchecked")
            @Nullable
            @Override
            public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
            {
                if (capability == ITEM_HANDLER)
                    return (T) itemHandler;
                if (capability == EXTENSION_SLOT_ITEM)
                    return (T) ItemToolBelt.this;
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
            size = MathHelper.clamp(nbt.getInteger("Size"), 2, 9);
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

    public static int[] xpCost = {3, 5, 8, 12, 15, 20, 30};

    public static int getUpgradeXP(ItemStack stack)
    {
        int slots = getSlotsCount(stack);

        if (slots >= 9)
            return -1;

        if (slots < 2)
            return 1;

        return xpCost[slots - 2];
    }

    public static ItemStack upgrade(ItemStack stack)
    {
        int slots = getSlotsCount(stack);

        if (slots >= 9)
            return stack.copy();

        stack = stack.copy();
        setSlotsCount(stack, slots + 1);
        return stack;
    }

    public ItemStack of(int upgradeLevel)
    {
        if (upgradeLevel < 0 || upgradeLevel >= 9)
            return ItemStack.EMPTY;

        ItemStack stack = new ItemStack(this);
        setSlotsCount(stack, upgradeLevel + 2);
        return stack;
    }

    private void tickAllSlots(ItemStack source, EntityLivingBase player)
    {
        BeltExtensionContainer container = new BeltExtensionContainer(source, player);
        for (IExtensionSlot slot : container.getSlots())
        {
            ((ExtensionSlotItemHandler) slot).onWornTick();
        }
    }

    public static class BeltExtensionContainer implements IExtensionContainer
    {
        private static final ResourceLocation SLOT_TYPE = new ResourceLocation("toolbelt", "pocket");
        private final ToolBeltInventory inventory;
        private final EntityLivingBase owner;
        private final ImmutableList<IExtensionSlot> slots;

        public BeltExtensionContainer(ItemStack source, EntityLivingBase owner)
        {
            this.inventory = (ToolBeltInventory) source.getCapability(ITEM_HANDLER, null);
            this.owner = owner;

            ExtensionSlotItemHandler[] slots = new ExtensionSlotItemHandler[inventory.getSlots()];

            for (int i = 0; i < inventory.getSlots(); i++)
            {
                slots[i] = new ExtensionSlotItemHandler(this, SLOT_TYPE, inventory, i)
                {
                    @Override
                    public boolean canEquip(@Nonnull ItemStack stack)
                    {
                        return BeltExtensionContainer.this.inventory.canInsertItem(this.slot, stack);
                    }
                };
            }

            this.slots = ImmutableList.copyOf(slots);
        }

        @Nonnull
        @Override
        public EntityLivingBase getOwner()
        {
            return owner;
        }

        @Nonnull
        @Override
        public ImmutableList<IExtensionSlot> getSlots()
        {
            return slots;
        }

        @Override
        public void onContentsChanged(IExtensionSlot slot)
        {

        }
    }

}