package gigaherz.toolbelt.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class ContainerChestItem extends Container
{
    public ContainerChestItem(IInventory playerInventory, IItemHandler beltInventory, int blockedSlot)
    {
        for (int k = 0; k < 9; ++k)
        {
            this.addSlotToContainer(new SlotNoBelts(beltInventory, k, 8 + k * 18, 18));
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
                                ? new SlotLocked(playerInventory, index, 8 + j1 * 18, l * 18 + 49)
                                : new Slot(playerInventory, index, 8 + j1 * 18, l * 18 + 49)
                );
            }
        }

        for (int i1 = 0; i1 < 9; ++i1)
        {
            this.addSlotToContainer(
                    blockedSlot == i1
                            ? new SlotLocked(playerInventory, i1, 8 + i1 * 18, 107)
                            : new Slot(playerInventory, i1, 8 + i1 * 18, 107)
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

            if (index < 9)
            {
                if (!this.mergeItemStack(itemstack1, 9, this.inventorySlots.size(), true))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 0, 9, false))
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