package dev.gigaherz.toolbelt.network;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

public class ContainerSlotsHack
{
    public ContainerSlotsHack()
    {
    }

    public ContainerSlotsHack(FriendlyByteBuf buf)
    {
    }

    public void encode(FriendlyByteBuf buf)
    {
    }

    public boolean handle(NetworkEvent.Context context)
    {
        context.getSender().containerMenu.sendAllDataToRemote();
        return true;
    }
}
