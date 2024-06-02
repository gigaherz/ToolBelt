package dev.gigaherz.toolbelt.network;

import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.client.ClientPacketHandlers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BeltContentsChange(
        int player,
        String where,
        int slot,
        ItemStack stack
) implements CustomPacketPayload
{
    public static final ResourceLocation ID = ToolBelt.location("belt_contents_change");
    public static final Type<BeltContentsChange> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, BeltContentsChange> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BeltContentsChange::player,
            ByteBufCodecs.STRING_UTF8, BeltContentsChange::where,
            ByteBufCodecs.VAR_INT, BeltContentsChange::slot,
            ItemStack.OPTIONAL_STREAM_CODEC, BeltContentsChange::stack,
            BeltContentsChange::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public void handle(IPayloadContext context)
    {
        ClientPacketHandlers.handleBeltContentsChange(this);
    }
}
