package gigaherz.toolbelt.network;

import gigaherz.toolbelt.ToolBelt;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ContainerSlotsHack
{
    public ContainerSlotsHack()
    {
    }

    public void fromBytes(PacketBuffer buf)
    {
    }

    public void toBytes(PacketBuffer buf)
    {
    }

    public static void encode(ContainerSlotsHack message, PacketBuffer packet)
    {
        message.toBytes(packet);
    }

    public static ContainerSlotsHack decode(PacketBuffer packet)
    {
        ContainerSlotsHack message = new ContainerSlotsHack();
        message.fromBytes(packet);
        return message;
    }

    public static void onMessage(final ContainerSlotsHack message, Supplier<NetworkEvent.Context> context)
    {
        context.get().getSender().sendContainerToPlayer(context.get().getSender().openContainer);
    }
}
