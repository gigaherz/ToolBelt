package gigaherz.toolbelt;

/*
import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import gigaherz.toolbelt.belt.ItemToolBelt;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
    public void setToBaubles(PlayerEntity player, int slot, ItemStack stack)
    {
        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        baubles.setStackInSlot(slot, stack);
    }

    @Nullable
    public BeltGetter findStack(PlayerEntity player)
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
        private final PlayerEntity thePlayer;
        private final int slotNumber;

        private BaublesBeltGetter(PlayerEntity thePlayer, int slotNumber)
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
        }
    }
}
*/