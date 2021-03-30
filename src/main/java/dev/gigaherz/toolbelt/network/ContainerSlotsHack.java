package dev.gigaherz.toolbelt.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ContainerSlotsHack
{
    public ContainerSlotsHack()
    {
    }

    public ContainerSlotsHack(PacketBuffer buf)
    {
    }

    public void encode(PacketBuffer buf)
    {
    }

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        context.get().getSender().refreshContainer(context.get().getSender().containerMenu);
        return true;
    }
}
