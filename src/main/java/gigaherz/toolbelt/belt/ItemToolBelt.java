package gigaherz.toolbelt.belt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import gigaherz.toolbelt.common.GuiHandler;
import gigaherz.toolbelt.customslots.ExtensionSlotItemHandler;
import gigaherz.toolbelt.customslots.IExtensionContainer;
import gigaherz.toolbelt.customslots.IExtensionSlot;
import gigaherz.toolbelt.customslots.IExtensionSlotItem;
import gigaherz.toolbelt.customslots.example.RpgEquipment;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemToolBelt extends Item implements IExtensionSlotItem
{
    @CapabilityInject(IItemHandler.class)
    public static Capability<IItemHandler> ITEM_HANDLER;

    @CapabilityInject(IExtensionSlotItem.class)
    public static Capability<IExtensionSlotItem> EXTENSION_SLOT_ITEM;

    public static final ImmutableSet<ResourceLocation> BELT_SLOT_LIST = ImmutableSet.of(RpgEquipment.BELT);

    public ItemToolBelt(Properties properties)
    {
        super(properties);
    }

    private EnumActionResult openBeltGui(EntityPlayer player, ItemStack stack, World world)
    {
        int slot = player.inventory.getSlotFor(stack);
        if (slot == -1)
            return EnumActionResult.FAIL;

        if (!world.isRemote && player instanceof EntityPlayerMP)
        {
            GuiHandler.openBeltGui((EntityPlayerMP) player, slot);
        }

        return EnumActionResult.SUCCESS;
    }

    @Override
    public EnumActionResult onItemUse(ItemUseContext context)
    {
        EntityPlayer player = context.getPlayer();
        ItemStack stack = context.getItem();
        World world = context.getWorld();

        return openBeltGui(player, stack, world);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        ItemStack stack = player.getHeldItem(hand);
        return new ActionResult<>(openBeltGui(player, stack, world), stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        int size = getSlotsCount(stack);

        tooltip.add(new TextComponentTranslation("text.toolbelt.tooltip", size - 2, size));
    }

    @Nonnull
    @Override
    public ImmutableSet<ResourceLocation> getAcceptableSlots(@Nonnull ItemStack stack)
    {
        return BELT_SLOT_LIST;
    }

    @Override
    public void onWornTick(ItemStack itemstack, IExtensionSlot slot)
    {
        tickAllSlots(itemstack, slot.getContainer().getOwner());
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
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

            final LazyOptional<IItemHandler> itemHandlerInstance = LazyOptional.of(() -> itemHandler);
            final LazyOptional<IExtensionSlotItem> extensionSlotInstance = LazyOptional.of(() -> ItemToolBelt.this);

            @Override
            @Nonnull
            @SuppressWarnings("unchecked")
            public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, final @Nullable EnumFacing side)
            {
                if (cap == ITEM_HANDLER)
                    return (LazyOptional<T>)itemHandlerInstance;
                if (cap == EXTENSION_SLOT_ITEM)
                    return (LazyOptional<T>)extensionSlotInstance;
                return LazyOptional.empty();
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

        NBTTagCompound nbt = stack.getTag();
        if (nbt != null)
        {
            size = MathHelper.clamp(nbt.getInt("Size"), 2, 9);
        }
        return size;
    }

    public static void setSlotsCount(ItemStack stack, int newSize)
    {
        NBTTagCompound nbt = stack.getTag();
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
            nbt.put("Items", new NBTTagList());
        }

        nbt.putInt("Size", newSize);
        stack.setTag(nbt);
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
            this.inventory = (ToolBeltInventory) source.getCapability(ITEM_HANDLER, null).orElseThrow(() -> new RuntimeException("No inventory!"));
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
    }

}