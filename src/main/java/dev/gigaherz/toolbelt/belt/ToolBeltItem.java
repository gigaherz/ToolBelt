package dev.gigaherz.toolbelt.belt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.gigaherz.toolbelt.common.Screens;
import dev.gigaherz.toolbelt.customslots.ExtensionSlotItemHandler;
import dev.gigaherz.toolbelt.customslots.IExtensionContainer;
import dev.gigaherz.toolbelt.customslots.IExtensionSlot;
import dev.gigaherz.toolbelt.customslots.IExtensionSlotItem;
import dev.gigaherz.toolbelt.customslots.example.RpgEquipment;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ToolBeltItem extends Item implements IExtensionSlotItem, DyeableLeatherItem
{
    public static Capability<IItemHandler> ITEM_HANDLER = CapabilityManager.get(new CapabilityToken<>(){});

    public static Capability<IExtensionSlotItem> EXTENSION_SLOT_ITEM = CapabilityManager.get(new CapabilityToken<>(){});

    public static final ImmutableSet<ResourceLocation> BELT_SLOT_LIST = ImmutableSet.of(RpgEquipment.BELT);

    public ToolBeltItem(Properties properties)
    {
        super(properties);
    }

    private static int getSlotFor(Inventory inv, ItemStack stack)
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

    private InteractionResult openBeltScreen(@Nullable Player player, ItemStack stack, Level world)
    {
        int slot = player != null ? getSlotFor(player.getInventory(), stack) : -1;
        if (slot == -1)
            return InteractionResult.FAIL;

        if (!world.isClientSide && player instanceof ServerPlayer)
        {
            Screens.openBeltScreen((ServerPlayer) player, slot);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        if (context.getHand() != InteractionHand.MAIN_HAND)
            return InteractionResult.PASS;

        return openBeltScreen(context.getPlayer(), context.getItemInHand(), context.getLevel());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND)
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        InteractionResult result = openBeltScreen(player, stack, world);
        return new InteractionResultHolder<>(result, stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
    {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        int size = getSlotsCount(stack);

        tooltip.add(Component.translatable("text.toolbelt.tooltip", size - 2, size));
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
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        if (entityIn instanceof LivingEntity)
        {
            tickAllSlots(stack, (LivingEntity) entityIn);
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(final ItemStack stack, CompoundTag nbt)
    {
        return new ICapabilityProvider()
        {
            final ItemStack owner = stack;
            final ToolBeltInventory itemHandler = new ToolBeltInventory(stack);

            final LazyOptional<IItemHandler> itemHandlerInstance = LazyOptional.of(() -> itemHandler);
            final LazyOptional<IExtensionSlotItem> extensionSlotInstance = LazyOptional.of(() -> ToolBeltItem.this);
            //final LazyOptional<ICurio> curioItemInstance = CURIO_ITEM != null ? LazyOptional.of(CURIO_ITEM::getDefaultInstance) : LazyOptional.empty();

            @Override
            @Nonnull
            public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, final @Nullable Direction side)
            {
                if (cap == ITEM_HANDLER)
                    return itemHandlerInstance.cast();
                if (cap == EXTENSION_SLOT_ITEM)
                    return extensionSlotInstance.cast();
                //if (cap == CURIO_ITEM && CURIO_ITEM != null)
                //    return curioItemInstance.cast();
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

        CompoundTag nbt = stack.getTag();
        if (nbt != null)
        {
            size = Mth.clamp(nbt.getInt("Size"), 2, 9);
        }
        return size;
    }

    public static void setSlotsCount(ItemStack stack, int newSize)
    {
        CompoundTag nbt = stack.getTag();
        if (nbt == null)
        {
            nbt = new CompoundTag();
            nbt.put("Items", new ListTag());
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
        return getSlotsCount(stack) - 2;
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