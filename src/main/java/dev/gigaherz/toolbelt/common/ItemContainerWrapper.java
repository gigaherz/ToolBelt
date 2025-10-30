package dev.gigaherz.toolbelt.common;

import dev.gigaherz.toolbelt.BeltFinder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class ItemContainerWrapper implements Container
{
    private final ItemStack heldItem;
    private final int actualSlots;
    private final BeltFinder.BeltGetter beltGetter;
    private ItemContainerContents inv;

    public ItemContainerWrapper(@Nullable ItemContainerContents container, int actualSlots, ItemStack heldItem, BeltFinder.BeltGetter beltGetter)
    {
        this.heldItem = heldItem;
        this.inv = container;
        this.actualSlots = actualSlots;
        this.beltGetter = beltGetter;
    }

    @Override
    public int getContainerSize()
    {
        return inv != null ? inv.getSlots() : 0;
    }

    @Override
    public boolean isEmpty()
    {
        if (inv == null) return true;
        for (int i = 0; i < inv.getSlots(); i++)
        {
            if (!inv.getStackInSlot(i).isEmpty())
                return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot)
    {
        if (inv == null)
            return ItemStack.EMPTY;

        return slot < inv.getSlots() ? inv.getStackInSlot(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int count)
    {
        if (inv == null)
            return ItemStack.EMPTY;

        var existing = getItem(slot);

        if (count >= existing.getCount())
        {
            setItem(slot, ItemStack.EMPTY);
        }
        else
        {
            var remaining = existing.copy();
            existing = existing.split(count);
            setItem(slot, remaining);
        }

        return existing;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot)
    {
        if (inv == null)
            return ItemStack.EMPTY;

        var existing = getItem(slot);

        setItem(slot, ItemStack.EMPTY);

        return existing;
    }

    @Override
    public void setItem(int slot, ItemStack newStack)
    {
        var newItems = new ArrayList<ItemStack>(inv.getSlots());
        boolean didWork = false;
        for (int i = 0; i < actualSlots; i++)
        {
            var stack = inv != null && i < inv.getSlots() ? inv.getStackInSlot(i) : ItemStack.EMPTY;
            if (i == slot)
            {
                newItems.add(newStack);
                didWork = true;
            }
            else
            {
                newItems.add(stack);
            }
        }

        if (didWork)
        {
            inv = ItemContainerContents.fromItems(newItems);
            heldItem.set(DataComponents.CONTAINER, inv);
            beltGetter.setBelt(heldItem.copy());
        }
    }

    @Override
    public int getMaxStackSize()
    {
        return 64;
    }

    @Override
    public void setChanged()
    {

    }

    @Override
    public boolean stillValid(Player player)
    {
        return inv != null;
    }

    @Override
    public void clearContent()
    {
        heldItem.remove(DataComponents.CONTAINER);
    }
}
