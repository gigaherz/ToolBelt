package gigaherz.toolbelt.network;

import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.customslots.IExtensionSlot;
import gigaherz.toolbelt.slot.ExtensionSlotBelt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncBeltSlotContents
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

    public void fromBytes(PacketBuffer buf)
    {
        entityId = buf.readVarInt();
        int numStacks = buf.readVarInt();
        for(int i=0;i<numStacks;i++)
        {
            stacks.add(buf.readItemStack());
        }
    }

    public void toBytes(PacketBuffer buf)
    {
        buf.writeVarInt(entityId);
        buf.writeVarInt(stacks.size());
        for(ItemStack stack : stacks)
        {
            buf.writeItemStack(stack);
        }
    }

    public static void encode(SyncBeltSlotContents message, PacketBuffer packet)
    {
        message.toBytes(packet);
    }

    public static SyncBeltSlotContents decode(PacketBuffer packet)
    {
        SyncBeltSlotContents message = new SyncBeltSlotContents();
        message.fromBytes(packet);
        return message;
    }

    public static void onMessage(final SyncBeltSlotContents message, Supplier<NetworkEvent.Context> context)
    {
        ToolBelt.proxy.handleBeltSlotContents(message);
    }
}
