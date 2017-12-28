package gigaherz.toolbelt.network;

import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.Config;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.belt.ItemToolBelt;
import gigaherz.toolbelt.belt.ToolBeltInventory;
import gigaherz.toolbelt.common.GuiHandler;
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

public class OpenBeltSlotInventory
        implements IMessage
{
    public OpenBeltSlotInventory()
    {
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
    }

    public static class Handler implements IMessageHandler<OpenBeltSlotInventory, IMessage>
    {
        @Override
        public IMessage onMessage(final OpenBeltSlotInventory message, MessageContext ctx)
        {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            final WorldServer world = (WorldServer) player.world;

            world.addScheduledTask(() -> player.openGui(ToolBelt.instance, GuiHandler.BELT_SLOT, world, 0, 0, 0));

            return null; // no response in this case
        }
    }

    public static void swapItem(int swapWith, EntityPlayer player)
    {
        BeltFinder.BeltGetter getter = BeltFinder.findBelt(player);
        if (getter == null)
            return;

        ItemStack stack = getter.getBelt();
        if (stack.getCount() <= 0)
            return;

        ToolBeltInventory cap = ItemToolBelt.getItems(stack);

        ItemStack inHand = player.getHeldItemMainhand();

        if (!Config.isItemStackAllowed(inHand))
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

        getter.syncToClients();
    }
}
