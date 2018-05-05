package gigaherz.toolbelt;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import gigaherz.toolbelt.belt.ItemToolBelt;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import javax.annotation.Nullable;

public class BeltFinderBaubles extends BeltFinder
{
    @CapabilityInject(IBaublesItemHandler.class)
    public static void initBaubles(Capability cap)
    {
        BeltFinder.instances.add(new BeltFinderBaubles());
    }

    @Override
    public void setToBaubles(EntityPlayer player, int slot, ItemStack stack)
    {
        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        baubles.setStackInSlot(slot, stack);
    }

    @Nullable
    public BeltGetter findStack(EntityPlayer player)
    {
        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        for (int i = 0; i < baubles.getSlots(); i++)
        {
            ItemStack inSlot = baubles.getStackInSlot(i);
            if (inSlot.getCount() > 0)
            {
                if (inSlot.getItem() instanceof ItemToolBelt)
                {
                    return new BaublesBeltGetter(player, i);
                }
            }
        }

        return null;
    }

    private class BaublesBeltGetter implements BeltGetter
    {
        private final EntityPlayer thePlayer;
        private final int slotNumber;

        private BaublesBeltGetter(EntityPlayer thePlayer, int slotNumber)
        {
            this.thePlayer = thePlayer;
            this.slotNumber = slotNumber;
        }

        @Override
        public ItemStack getBelt()
        {
            IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(thePlayer);
            return baubles.getStackInSlot(slotNumber);
        }

        @Override
        public void syncToClients()
        {
            // No need! Baubles does its own sync.
            /*if (thePlayer.world.isRemote)
                return;
            BeltContentsChange message = new BeltContentsChange(thePlayer, BAUBLES, slotNumber, getBelt());
            ((WorldServer) thePlayer.world).getEntityTracker().getTrackingPlayers(thePlayer).forEach((p) -> {
                if (p instanceof EntityPlayerMP)
                    ToolBelt.channel.sendTo(message, (EntityPlayerMP) p);
            });
            */
        }
    }
}
