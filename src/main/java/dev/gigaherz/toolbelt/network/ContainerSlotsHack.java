package dev.gigaherz.toolbelt.network;

import dev.gigaherz.toolbelt.ToolBelt;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class ContainerSlotsHack implements CustomPacketPayload
{
    public static final ResourceLocation ID = ToolBelt.location("container_slots_hack");

    public ContainerSlotsHack()
    {
    }

    public ContainerSlotsHack(FriendlyByteBuf buf)
    {
    }

    public void write(FriendlyByteBuf buf)
    {
    }

    @Override
    public ResourceLocation id()
    {
        return ID;
    }

    public void handle(PlayPayloadContext context)
    {
        context.player().ifPresent(player -> player.containerMenu.sendAllDataToRemote());
    }
}
