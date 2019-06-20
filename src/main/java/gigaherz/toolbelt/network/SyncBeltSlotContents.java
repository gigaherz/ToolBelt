package gigaherz.toolbelt.network;

import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.customslots.IExtensionSlot;
import gigaherz.toolbelt.slot.ExtensionSlotBelt;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SyncBeltSlotContents implements IMessage
{
    public final NonNullList<ItemStack> stacks = NonNullList.create();
    public int entityId;

    public SyncBeltSlotContents()
    {
    }

    public SyncBeltSlotContents(EntityPlayer player, ExtensionSlotBelt extension)
    {
        this.entityId = player.getEntityId();
        extension.getSlots().stream().map(IExtensionSlot::getContents).forEach(stacks::add);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        entityId = ByteBufUtils.readVarInt(buf,5);
        int numStacks = ByteBufUtils.readVarInt(buf,5);
        for (int i = 0; i < numStacks; i++)
        {
            stacks.add(ByteBufUtils.readItemStack(buf));
        }
    }

    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeVarInt(buf, entityId,5);
        ByteBufUtils.writeVarInt(buf, stacks.size(), 5);
        for (ItemStack stack : stacks)
        {
            ByteBufUtils.writeItemStack(buf, stack);
        }
    }

    public static class Handler implements IMessageHandler<SyncBeltSlotContents, IMessage>
    {
        @Override
        public IMessage onMessage(final SyncBeltSlotContents message, MessageContext ctx)
        {
            ToolBelt.proxy.handleBeltSlotContents(message);

            return null; // no response in this case
        }
    }
}
