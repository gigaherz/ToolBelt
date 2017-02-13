package gigaherz.toolbelt.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class ContainerBelt extends Container
{
    public final int beltSlots;

    public ContainerBelt(IInventory playerInventory, IItemHandler beltInventory, int blockedSlot)
    {
        beltSlots = beltInventory.getSlots();
        int xoff = ((9 - beltSlots) * 18) / 2;
        for (int k = 0; k < beltSlots; ++k)
        {
            this.addSlotToContainer(new SlotBelt(beltInventory, k, 8 + xoff + k * 18, 20));
        }

        bindPlayerInventory(playerInventory, blockedSlot);
    }

    private void bindPlayerInventory(IInventory playerInventory, int blockedSlot)
    {
        for (int l = 0; l < 3; ++l)
        {
            for (int j1 = 0; j1 < 9; ++j1)
            {
                int index = j1 + l * 9 + 9;
                this.addSlotToContainer(
                        blockedSlot == index
                                ? new SlotLocked(playerInventory, index, 8 + j1 * 18, l * 18 + 51)
                                : new Slot(playerInventory, index, 8 + j1 * 18, l * 18 + 51)
                );
            }
        }

        for (int i1 = 0; i1 < 9; ++i1)
        {
            this.addSlotToContainer(
                    blockedSlot == i1
                            ? new SlotLocked(playerInventory, i1, 8 + i1 * 18, 109)
                            : new Slot(playerInventory, i1, 8 + i1 * 18, 109)
            );
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return true;
    }

    @Override
    @Nullable
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemstack = null;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            assert itemstack1 != null;
            itemstack = itemstack1.copy();

            if (index < beltSlots)
            {
                if (!this.mergeItemStack(itemstack1, beltSlots, this.inventorySlots.size(), true))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 0, beltSlots, false))
            {
                return null;
            }

            if (itemstack1.stackSize == 0)
            {
                slot.putStack(null);
            }
            else
            {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }
}