package dev.gigaherz.toolbelt.belt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.gigaherz.toolbelt.common.Screens;
import dev.gigaherz.toolbelt.customslots.ExtensionSlotItemHandler;
import dev.gigaherz.toolbelt.customslots.IExtensionContainer;
import dev.gigaherz.toolbelt.customslots.IExtensionSlot;
import dev.gigaherz.toolbelt.customslots.IExtensionSlotItem;
import dev.gigaherz.toolbelt.customslots.example.RpgEquipment;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.item.Item.Properties;

public class ToolBeltItem extends Item implements IExtensionSlotItem
{
    @CapabilityInject(IItemHandler.class)
    public static Capability<IItemHandler> ITEM_HANDLER;

    @CapabilityInject(IExtensionSlotItem.class)
    public static Capability<IExtensionSlotItem> EXTENSION_SLOT_ITEM;

    public static final ImmutableSet<ResourceLocation> BELT_SLOT_LIST = ImmutableSet.of(RpgEquipment.BELT);

    public ToolBeltItem(Properties properties)
    {
        super(properties);
    }

    private static int getSlotFor(PlayerInventory inv, ItemStack stack)
    {
        if (inv.getSelected() == stack)
            return inv.selected;

        for (int i = 0; i < inv.items.size(); ++i)
        {
            ItemStack invStack = inv.items.get(i);
            if (invStack == stack)
            {
                return i;
            }
        }

        // Couldn't find the exact instance, can not ensure we have the right slot.
        return -1;
    }

    private ActionResultType openBeltScreen(@Nullable PlayerEntity player, ItemStack stack, World world)
    {
        int slot = player != null ? getSlotFor(player.inventory, stack) : -1;
        if (slot == -1)
            return ActionResultType.FAIL;

        if (!world.isClientSide && player instanceof ServerPlayerEntity)
        {
            Screens.openBeltScreen((ServerPlayerEntity) player, slot);
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType useOn(ItemUseContext context)
    {
        if (context.getHand() != Hand.MAIN_HAND)
            return ActionResultType.PASS;

        return openBeltScreen(context.getPlayer(), context.getItemInHand(), context.getLevel());
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != Hand.MAIN_HAND)
            return new ActionResult<>(ActionResultType.PASS, stack);
        ActionResultType result = openBeltScreen(player, stack, world);
        return new ActionResult<>(result, stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        int size = getSlotsCount(stack);

        tooltip.add(new TranslationTextComponent("text.toolbelt.tooltip", size - 2, size));
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
        if (entityIn instanceof LivingEntity)
        {
            tickAllSlots(stack, (LivingEntity) entityIn);
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(final ItemStack stack, CompoundNBT nbt)
    {
        return new ICapabilityProvider()
        {
            final ToolBeltInventory itemHandler = new ToolBeltInventory(stack);

            final LazyOptional<IItemHandler> itemHandlerInstance = LazyOptional.of(() -> itemHandler);
            final LazyOptional<IExtensionSlotItem> extensionSlotInstance = LazyOptional.of(() -> ToolBeltItem.this);

            @Override
            @Nonnull
            public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, final @Nullable Direction side)
            {
                if (cap == ITEM_HANDLER)
                    return itemHandlerInstance.cast();
                if (cap == EXTENSION_SLOT_ITEM)
                    return extensionSlotInstance.cast();
                return LazyOptional.empty();
            }
        };
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return !ItemStack.isSame(oldStack, newStack); // super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    public static int getSlotsCount(ItemStack stack)
    {
        int size = 2;

        CompoundNBT nbt = stack.getTag();
        if (nbt != null)
        {
            size = MathHelper.clamp(nbt.getInt("Size"), 2, 9);
        }
        return size;
    }

    public static void setSlotsCount(ItemStack stack, int newSize)
    {
        CompoundNBT nbt = stack.getTag();
        if (nbt == null)
        {
            nbt = new CompoundNBT();
            nbt.put("Items", new ListNBT());
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

    public int getLevel(ItemStack stack)
    {
        return getSlotsCount(stack)-2;
    }

    private void tickAllSlots(ItemStack source, LivingEntity player)
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
        private final LivingEntity owner;
        private final ImmutableList<IExtensionSlot> slots;

        public BeltExtensionContainer(ItemStack source, LivingEntity owner)
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
        public LivingEntity getOwner()
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