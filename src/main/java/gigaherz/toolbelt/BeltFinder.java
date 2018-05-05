package gigaherz.toolbelt;

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

import static gigaherz.toolbelt.network.BeltContentsChange.ContainingInventory.MAIN;

public abstract class BeltFinder
{
    public static NonNullList<BeltFinder> instances = NonNullList.create();

    @Nullable
    public abstract BeltGetter findStack(EntityPlayer player);

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
}
