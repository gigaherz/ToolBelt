package gigaherz.toolbelt;

import gigaherz.toolbelt.belt.ItemToolBelt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class BeltFinder
{
    public static BeltFinder instance = new BeltFinder();

    @Nullable
    public ItemStack findStack(EntityPlayer player)
    {
        IInventory playerInv = player.inventory;
        for(int i=0;i<playerInv.getSizeInventory();i++)
        {
            ItemStack inSlot = playerInv.getStackInSlot(i);
            if (inSlot != null && inSlot.stackSize > 0)
            {
                if (inSlot.getItem() instanceof ItemToolBelt)
                {
                    return inSlot;
                }
            }
        }

        return null;
    }
}
