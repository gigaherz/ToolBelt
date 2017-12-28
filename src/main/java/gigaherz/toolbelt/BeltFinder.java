package gigaherz.toolbelt;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import gigaherz.toolbelt.belt.ItemToolBelt;
import gigaherz.toolbelt.network.BeltContentsChange;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;

import java.util.List;

import static gigaherz.toolbelt.network.BeltContentsChange.ContainingInventory.MAIN;

public class BeltFinder
{
    public static NonNullList<BeltFinder> instances = NonNullList.create();

    static {
        instances.add(new BeltFinder());
    }

    @Nullable
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

        return null;
    }

    @Nullable
    public static BeltGetter findBelt(EntityPlayer player)
    {
        for(int i=instances.size()-1;i>=0;i--)
        {
            BeltFinder instance = instances.get(i);
            BeltGetter getter = instance.findStack(player);
            if (getter != null)
                return getter;
        }
        return null;
    }

    public static void sendSync(EntityPlayer player)
    {
        BeltFinder.BeltGetter stack = findBelt(player);
        if (stack != null)
        {
            stack.syncToClients();
        }
    }

    public void setToBaubles(EntityPlayer player, int slot, ItemStack stack)
    {
    }
    public void setToBeltSlot(EntityLivingBase player, ItemStack stack)
    {
    }

    public interface BeltGetter
    {
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
                    ToolBelt.channel.sendTo(message, (EntityPlayerMP) p);
            });
        }
    }
}
