package dev.gigaherz.toolbelt.belt;

import com.google.common.collect.ImmutableSet;
import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.common.Screens;
import dev.gigaherz.toolbelt.slot.BeltAttachment;
import dev.gigaherz.toolbelt.slot.IBeltSlotItem;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class ToolBeltItem extends Item implements IBeltSlotItem, DyeableLeatherItem
{
    public static void register(RegisterCapabilitiesEvent event)
    {
        event.registerItem(
                Capabilities.ItemHandler.ITEM,
                (stack, context) -> new ToolBeltInventory(stack),
                ToolBelt.BELT
        );
        event.registerItem(
                IBeltSlotItem.CAPABILITY,
                (stack, context) -> (IBeltSlotItem)stack.getItem(),
                ToolBelt.BELT
        );
    }

    public static final ImmutableSet<ResourceLocation> BELT_SLOT_LIST = ImmutableSet.of(ToolBelt.location("belt"));

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

        if (!world.isClientSide && player instanceof ServerPlayer serverPlayer)
        {
            Screens.openBeltScreen(serverPlayer, slot);
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

    @Override
    public void onWornTick(ItemStack itemstack, BeltAttachment slot)
    {
        tickAllSlots(itemstack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        if (entityIn instanceof LivingEntity)
        {
            tickAllSlots(stack);
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return !ItemStack.isSameItem(oldStack, newStack); // super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
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

    private void tickAllSlots(ItemStack source)
    {
        var inventory = Objects.requireNonNull(source.getCapability(Capabilities.ItemHandler.ITEM), "No inventory!");

        for (int i = 0; i < inventory.getSlots(); i++)
        {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty())
                return;
            var extItem = stack.getCapability(IItemInBelt.CAPABILITY);
            if (extItem != null) {
                extItem.onWornTick(stack, source);
            }
        }
    }
}