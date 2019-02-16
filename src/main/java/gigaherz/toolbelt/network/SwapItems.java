package gigaherz.toolbelt.network;

import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.Config;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.function.Supplier;

public class SwapItems
{
    public int swapWith;

    public SwapItems()
    {
    }

    public SwapItems(int windowId)
    {
        this.swapWith = windowId;
    }

    public void fromBytes(PacketBuffer buf)
    {
        swapWith = buf.readInt();
    }

    public void toBytes(PacketBuffer buf)
    {
        buf.writeInt(swapWith);
    }

    public static void encode(SwapItems message, PacketBuffer packet)
    {
        message.toBytes(packet);
    }

    public static SwapItems decode(PacketBuffer packet)
    {
        SwapItems message = new SwapItems();
        message.fromBytes(packet);
        return message;
    }

    public static void onMessage(final SwapItems message, Supplier<NetworkEvent.Context> context)
    {
        final EntityPlayerMP player = context.get().getSender();
        final WorldServer world = (WorldServer) player.world;

        world.addScheduledTask(() -> swapItem(message.swapWith, player));
    }

    public static void swapItem(int swapWith, EntityPlayer player)
    {
        BeltFinder.BeltGetter getter = BeltFinder.findBelt(player);
        if (getter == null)
            return;

        ItemStack stack = getter.getBelt();
        if (stack.getCount() <= 0)
            return;

        ItemStack inHand = player.getHeldItemMainhand();

        if (!Config.isItemStackAllowed(inHand))
            return;

        IItemHandlerModifiable cap = (IItemHandlerModifiable)(
                stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
                        .orElseThrow(() -> new RuntimeException("No inventory!")));
        if (swapWith < 0)
        {
            player.setHeldItem(EnumHand.MAIN_HAND, ItemHandlerHelper.insertItem(cap, inHand, false));
        }
        else
        {
            ItemStack inSlot = cap.getStackInSlot(swapWith);
            player.setHeldItem(EnumHand.MAIN_HAND, inSlot);
            cap.setStackInSlot(swapWith, inHand);
        }
        getter.syncToClients();
    }
}
