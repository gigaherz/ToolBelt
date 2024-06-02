package dev.gigaherz.toolbelt.network;

import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.client.ClientPacketHandlers;
import dev.gigaherz.toolbelt.slot.BeltAttachment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncBeltSlotContents(
        ItemStack stack,
        int entityId
) implements CustomPacketPayload
{
    public static final ResourceLocation ID = ToolBelt.location("sync_slot_contents");
    public static final Type<SyncBeltSlotContents> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncBeltSlotContents> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, SyncBeltSlotContents::stack,
            ByteBufCodecs.VAR_INT, SyncBeltSlotContents::entityId,
            SyncBeltSlotContents::new
    );

    public SyncBeltSlotContents(Entity player, BeltAttachment extension)
    {
        this(extension.getContents(), player.getId());
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public void handle(IPayloadContext context)
    {
        ClientPacketHandlers.handleBeltSlotContents(this);
    }
}
