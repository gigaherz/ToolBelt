package dev.gigaherz.toolbelt.belt;

import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.common.Screens;
import dev.gigaherz.toolbelt.slot.BeltAttachment;
import dev.gigaherz.toolbelt.slot.IBeltSlotItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.ComponentItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ToolBeltItem extends Item implements IBeltSlotItem
{
    public static void register(RegisterCapabilitiesEvent event)
    {
        event.registerItem(
                Capabilities.ItemHandler.ITEM,
                (stack, context) -> getComponentItemHandler(stack),
                ToolBelt.BELT
        );
        event.registerItem(
                IBeltSlotItem.CAPABILITY,
                (stack, context) -> (IBeltSlotItem)stack.getItem(),
                ToolBelt.BELT
        );
    }

    public static @NotNull ComponentItemHandler getComponentItemHandler(ItemStack stack)
    {
        var size = stack.get(ToolBelt.BELT_SIZE);
        return new ComponentItemHandler(stack, DataComponents.CONTAINER, size != null ? size : 2);
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

    public static ItemStack of(int level)
    {
        return ToolBelt.BELT.get().forSize(level);
    }

    public ItemStack forSize(int size)
    {
        return setSlotsCount(new ItemStack(this), Math.clamp(size,2,9));
    }

    public static int getSlotsCount(ItemStack stack)
    {
        var actualSize = stack.get(ToolBelt.BELT_SIZE);
        return Math.clamp(Objects.requireNonNullElse(actualSize,2), 2, 9);
    }

    public static ItemStack setSlotsCount(ItemStack stack, int newSize)
    {
        var oldCount = stack.get(ToolBelt.BELT_SIZE);
        var oldSize = oldCount != null ? oldCount : 2;
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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn)
    {
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