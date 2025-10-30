package dev.gigaherz.toolbelt.belt;

import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.common.Screens;
import dev.gigaherz.toolbelt.slot.BeltAttachment;
import dev.gigaherz.toolbelt.slot.IBeltSlotItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemAccessItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ToolBeltItem extends Item implements IBeltSlotItem
{
    public static void register(RegisterCapabilitiesEvent event)
    {
        event.registerItem(
                Capabilities.Item.ITEM,
                (stack, context) -> getComponentItemHandler(stack),
                ToolBelt.BELT
        );
        event.registerItem(
                IBeltSlotItem.CAPABILITY,
                (stack, context) -> (IBeltSlotItem)stack.getItem(),
                ToolBelt.BELT
        );
    }

    public static @NotNull ItemAccessItemHandler getComponentItemHandler(ItemStack stack)
    {
        int size = getBeltSize(stack);
        return new ItemAccessItemHandler(ItemAccess.forStack(stack), DataComponents.CONTAINER, size);
    }

    private static int getSlotFor(Inventory inv, ItemStack stack)
    {
        if (inv.getSelectedItem() == stack)
            return inv.getSelectedSlot();

        for (int i = 0; i < inv.getContainerSize(); ++i)
        {
            ItemStack invStack = inv.getItem(i);
            if (invStack == stack)
            {
                return i;
            }
        }

        // Couldn't find the exact instance, can not ensure we have the right slot.
        return -1;
    }

    public static ItemStack of(int level)
    {
        return ToolBelt.BELT.get().forSize(level);
    }

    public ItemStack forSize(int size)
    {
        return setBeltSize(new ItemStack(this), Math.clamp(size,2,9));
    }

    public static int getBeltSize(ItemStack stack)
    {
        var count = Objects.requireNonNullElse(stack.get(ToolBelt.BELT_SIZE), 2);
        return Math.clamp(count, 2, 9);
    }

    public static ItemStack setBeltSize(ItemStack stack, int newSize)
    {
        var oldSize = getBeltSize(stack);
        if (newSize != oldSize)
        {
            var oldInv = stack.get(DataComponents.CONTAINER);
            if (oldInv != null)
            {
                List<ItemStack> newItems = new ArrayList<>();
                int fill = Math.min(oldInv.getSlots(), Math.min(oldSize, newSize));
                for (int i = 0; i < fill; i++)
                    newItems.add(oldInv.getStackInSlot(i));
                var newInv = ItemContainerContents.fromItems(newItems);
                stack.set(DataComponents.CONTAINER, newInv);
            }
            stack.set(ToolBelt.BELT_SIZE, newSize);
        }
        return stack;
    }

    // ----------- Begin implementation ----------

    public ToolBeltItem(Properties properties)
    {
        super(properties);
    }

    private InteractionResult openBeltScreen(@Nullable Player player, ItemStack stack, Level world)
    {
        int slot = player != null ? getSlotFor(player.getInventory(), stack) : -1;
        if (slot == -1)
            return InteractionResult.FAIL;

        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer)
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
    public InteractionResult use(Level world, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND)
            return InteractionResult.PASS;
        return openBeltScreen(player, stack, world);
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> consumer, TooltipFlag advanced)
    {
        int size = getBeltSize(stack);

        consumer.accept(Component.translatable("text.toolbelt.tooltip", size - 2, size));
    }

    @Override
    public void onWornTick(ItemStack itemstack, BeltAttachment slot)
    {
        tickAllSlots(itemstack);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel p_401805_, Entity entity, @org.jetbrains.annotations.Nullable EquipmentSlot slot)
    {
        tickAllSlots(stack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return !ItemStack.isSameItem(oldStack, newStack); // super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    public static ItemStack makeUpgradedStack(ItemStack stack)
    {
        int slots = getBeltSize(stack);

        if (slots >= 9)
            return stack.copy();

        stack = stack.copy();
        setBeltSize(stack, slots + 1);
        return stack;
    }

    private void tickAllSlots(ItemStack source)
    {
        var inventory = source.get(DataComponents.CONTAINER);
        if (inventory == null) return;

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