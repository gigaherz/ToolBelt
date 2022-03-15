package dev.gigaherz.toolbelt.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import java.util.Optional;

class LockedSlot extends Slot
{
    public LockedSlot(IInventory inventoryIn, int index, int xPosition, int yPosition)
    {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPickup(PlayerEntity playerIn)
    {
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack stack)
    {
        return false;
    }

    @Override
    public ItemStack remove(int quantity)
    {
        return ItemStack.EMPTY;
    }
}
