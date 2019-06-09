package gigaherz.toolbelt.network;

import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.ConfigData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.function.Supplier;

public class SwapItems
{
    public int swapWith;

    public SwapItems(int windowId)
    {
        this.swapWith = windowId;
    }

    public SwapItems(PacketBuffer buf)
    {
        swapWith = buf.readInt();
    }

    public void encode(PacketBuffer buf)
    {
        buf.writeInt(swapWith);
    }

    public void handle(Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() -> swapItem(swapWith, context.get().getSender()));
    }

    public static void swapItem(int swapWith, PlayerEntity player)
    {
        BeltFinder.BeltGetter getter = BeltFinder.findBelt(player);
        if (getter == null)
            return;

        ItemStack stack = getter.getBelt();
        if (stack.getCount() <= 0)
            return;

        ItemStack inHand = player.getHeldItemMainhand();

        if (!ConfigData.isItemStackAllowed(inHand))
            return;

        IItemHandlerModifiable cap = (IItemHandlerModifiable) (
                stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
                        .orElseThrow(() -> new RuntimeException("No inventory!")));
        if (swapWith < 0)
        {
            player.setHeldItem(Hand.MAIN_HAND, ItemHandlerHelper.insertItem(cap, inHand, false));
        }
        else
        {
            ItemStack inSlot = cap.getStackInSlot(swapWith);
            player.setHeldItem(Hand.MAIN_HAND, inSlot);
            cap.setStackInSlot(swapWith, inHand);
        }
        getter.syncToClients();
    }
}
