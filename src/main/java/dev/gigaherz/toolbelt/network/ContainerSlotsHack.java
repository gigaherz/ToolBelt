package dev.gigaherz.toolbelt.network;

import dev.gigaherz.toolbelt.ToolBelt;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ContainerSlotsHack implements CustomPacketPayload
{
    public static final ResourceLocation ID = ToolBelt.location("container_slots_hack");
    public static final Type<ContainerSlotsHack> TYPE = new Type<>(ID);
    public static final ContainerSlotsHack INSTANCE = new ContainerSlotsHack();
    public static final StreamCodec<ByteBuf, ContainerSlotsHack> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private ContainerSlotsHack()
    {
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public void handle(IPayloadContext context)
    {
        context.player().containerMenu.sendAllDataToRemote();
    }
}
