package gigaherz.toolbelt;

import gigaherz.toolbelt.belt.ItemToolBelt;
import gigaherz.toolbelt.network.BeltContentsChange;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.Set;

import static gigaherz.toolbelt.network.BeltContentsChange.ContainingInventory.MAIN;

public class BeltFinder
{
    public static BeltFinder instance = new BeltFinder();

    @Nullable
    public BeltGetter findStack(EntityPlayer player)
    {
        IInventory playerInv = player.inventory;
        for (int i = 0; i < playerInv.getSizeInventory(); i++)
        {
            ItemStack inSlot = playerInv.getStackInSlot(i);
            if (inSlot != null && inSlot.stackSize > 0)
            {
                if (inSlot.getItem() instanceof ItemToolBelt)
                {
                    return new InventoryBeltGetter(player, i);
                }
            }
        }

        return null;
    }

    public void setToBaubles(EntityPlayer player, int slot, ItemStack stack)
    {

    }

    public static void sendSync(EntityPlayer player)
    {
        BeltFinder.BeltGetter stack = instance.findStack(player);
        if (stack != null)
        {
            stack.syncToClients();
        }
    }

    public interface BeltGetter
    {
        @Nullable
        ItemStack getBelt();

        void syncToClients();
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

        @Override
        public void syncToClients()
        {
            if (thePlayer.world.isRemote)
                return;
            BeltContentsChange message = new BeltContentsChange(thePlayer, MAIN, slotNumber, getBelt());
            ((WorldServer) thePlayer.world).getEntityTracker().getTrackingPlayers(thePlayer).forEach((p) -> {
                if (p instanceof EntityPlayerMP)
                    ToolBelt.channel.sendTo(message, (EntityPlayerMP)p);
            });
        }
    }
}
