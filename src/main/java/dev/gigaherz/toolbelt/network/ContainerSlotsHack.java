package dev.gigaherz.toolbelt.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

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

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        context.get().getSender().containerMenu.sendAllDataToRemote();
        return true;
    }
}
