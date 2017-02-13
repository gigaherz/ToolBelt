package gigaherz.toolbelt;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import gigaherz.toolbelt.belt.ItemToolBelt;
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
        BeltFinder.instance = new BeltFinderBaubles();
    }

    @Nullable
    public ItemStack findStack(EntityPlayer player)
    {
        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        for (int i = 0; i < baubles.getSlots(); i++)
        {
            ItemStack inSlot = baubles.getStackInSlot(i);
            if (inSlot != null && inSlot.stackSize > 0)
            {
                if (inSlot.getItem() instanceof ItemToolBelt)
                {
                    return inSlot;
                }
            }
        }

        return super.findStack(player);
    }
}
