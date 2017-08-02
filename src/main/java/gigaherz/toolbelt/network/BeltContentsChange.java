package gigaherz.toolbelt.network;

import gigaherz.toolbelt.ToolBelt;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class BeltContentsChange
        implements IMessage
{

    public enum ContainingInventory
    {
        MAIN, BAUBLES;

        public static final ContainingInventory[] VALUES = values();
    }

    public int player;
    public ContainingInventory where;
    public int slot;
    public ItemStack stack;

    public BeltContentsChange()
    {
    }

    public BeltContentsChange(EntityPlayer player, ContainingInventory where, int slot, ItemStack stack)
    {
        this.player = player.getEntityId();
        this.where = where;
        this.slot = slot;
        this.stack = stack;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        player = buf.readInt();
        where = ContainingInventory.VALUES[buf.readByte()];
        slot = buf.readByte();
        stack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(player);
        buf.writeByte(where.ordinal());
        buf.writeByte(slot);
        ByteBufUtils.writeItemStack(buf, stack);
    }

    public static class Handler implements IMessageHandler<BeltContentsChange, IMessage>
    {
        @Override
        public IMessage onMessage(final BeltContentsChange message, MessageContext ctx)
        {
            ToolBelt.proxy.handleBeltContentsChange(message);

            return null; // no response in this case
        }
    }
}
