package gigaherz.toolbelt;

import gigaherz.toolbelt.belt.ItemToolBelt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class BeltFinder
{
    public static BeltFinder instance = new BeltFinder();

    public BeltGetter findStack(EntityPlayer player)
    {
        IInventory playerInv = player.inventory;
        for (int i = 0; i < playerInv.getSizeInventory(); i++)
        {
            ItemStack inSlot = playerInv.getStackInSlot(i);
            if (inSlot.getCount() > 0)
            {
                if (inSlot.getItem() instanceof ItemToolBelt)
                {
                    return new InventoryBeltGetter(player, i);
                }
            }
        }

        return ItemStack.EMPTY;
    }

    public interface BeltGetter
    {
        @Nullable
        ItemStack getBelt();
    }

    private class InventoryBeltGetter implements BeltGetter
    {
        private final EntityPlayer thePlayer;
        private final int slotNumber;

        private InventoryBeltGetter(EntityPlayer thePlayer, int slotNumber)
        {
            this.thePlayer = thePlayer;
            this.slotNumber = slotNumber;
        }

        @Override
        @Nullable
        public ItemStack getBelt()
        {
            return thePlayer.inventory.getStackInSlot(slotNumber);
        }
    }
}
