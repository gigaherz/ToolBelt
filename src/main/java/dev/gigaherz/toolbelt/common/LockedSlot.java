package dev.gigaherz.toolbelt.common;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

class LockedSlot extends Slot
{
    public LockedSlot(Container container, int index, int xPosition, int yPosition)
    {
        super(container, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPickup(Player player)
    {
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack stack)
    {
        return false;
    }

    @Override
    public boolean allowModification(Player player)
    {
        return false;
    }

    @Override
    public Optional<ItemStack> tryRemove(int p_150642_, int p_150643_, Player player)
    {
        return Optional.empty();
    }

    @Override
    public ItemStack safeTake(int p_150648_, int p_150649_, Player player)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack safeInsert(ItemStack stack)
    {
        return stack;
    }

    @Override
    public ItemStack safeInsert(ItemStack stack, int quantity)
    {
        return stack;
    }

    @Override
    public ItemStack remove(int quantity)
    {
        return ItemStack.EMPTY;
    }
}
