package dev.gigaherz.toolbelt.common;

import dev.gigaherz.toolbelt.ConfigData;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class BeltSlot extends Slot
{
    private static final Logger LOGGER = LogManager.getLogger();

    public BeltSlot(Container playerInventory, ItemStack heldItem, int blockedSlot, int index, int xPosition, int yPosition)
    {
        super(new Container()
        {
            final Container sourceInventory = playerInventory;
            final int slot = blockedSlot;
            final int subSlot = index;
            final ItemStack fallbackItem = heldItem;

            ItemStack beltStack = null;
            IItemHandlerModifiable inventory = null;

            IItemHandlerModifiable findStack()
            {
                ItemStack stack = slot >= 0 ? sourceInventory.getItem(slot) : fallbackItem;
                if (stack != beltStack)
                {
                    beltStack = stack;
                    inventory = (IItemHandlerModifiable)Objects.requireNonNullElse(stack.getCapability(Capabilities.ItemHandler.ITEM), new ItemStackHandler(9));
                }
                return inventory;
            }

            @Override
            public int getContainerSize()
            {
                return 1;
            }

            @Override
            public boolean isEmpty()
            {
                return getItem(0).getCount() <= 0;
            }

            @Override
            public ItemStack getItem(int n)
            {
                return findStack().getStackInSlot(subSlot);
            }

            @Override
            public ItemStack removeItem(int n, int count)
            {
                return findStack().extractItem(subSlot, count, false);
            }

            @Override
            public ItemStack removeItemNoUpdate(int n)
            {
                ItemStack existing = getItem(0);
                setItem(n, ItemStack.EMPTY);
                return existing;
            }

            @Override
            public void setItem(int n, ItemStack stack)
            {
                findStack().setStackInSlot(subSlot, stack);
            }

            @Override
            public int getMaxStackSize()
            {
                return findStack().getSlotLimit(subSlot);
            }

            @Override
            public void setChanged()
            {

            }

            @Override
            public boolean stillValid(Player player)
            {
                return false;
            }

            @Override
            public void startOpen(Player player)
            {

            }

            @Override
            public void stopOpen(Player player)
            {

            }

            @Override
            public boolean canPlaceItem(int index, ItemStack stack)
            {
                return ConfigData.isItemStackAllowed(stack);
            }

            @Override
            public void clearContent()
            {

            }
        }, blockedSlot, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack)
    {
        return ConfigData.isItemStackAllowed(stack);
    }
}
