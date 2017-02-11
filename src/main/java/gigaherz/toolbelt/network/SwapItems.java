package gigaherz.toolbelt.network;

import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.belt.ItemToolBelt;
import gigaherz.toolbelt.belt.ToolBeltInventory;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.ItemHandlerHelper;

public class SwapItems
        implements IMessage
{
    public int swapWith;

    public SwapItems()
    {
    }

    public SwapItems(int windowId)
    {
        this.swapWith = windowId;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        swapWith = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(swapWith);
    }

    public static class Handler implements IMessageHandler<SwapItems, IMessage>
    {
        @Override
        public IMessage onMessage(final SwapItems message, MessageContext ctx)
        {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            final WorldServer world = (WorldServer) player.world;

            world.addScheduledTask(() -> swapItem(message.swapWith, player));

            return null; // no response in this case
        }
    }

    public static void swapItem(int swapWith, EntityPlayer player)
    {
        ItemStack stack = BeltFinder.instance.findStack(player);
        if (stack == null)
            return;

        ToolBeltInventory cap = ItemToolBelt.getItems(stack);

        ItemStack inHand = player.getHeldItemMainhand();

        if (!ItemToolBelt.isItemValid(inHand))
            return;

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
    }
}
