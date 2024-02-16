package dev.gigaherz.toolbelt.network;

import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.client.ClientPacketHandlers;
import dev.gigaherz.toolbelt.slot.BeltAttachment;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class SyncBeltSlotContents implements CustomPacketPayload
{
    public static final ResourceLocation ID = ToolBelt.location("sync_slot_contents");

    public ItemStack stack;
    public int entityId;

    public SyncBeltSlotContents(Player player, BeltAttachment extension)
    {
        this.entityId = player.getId();
        this.stack = extension.getContents();
    }

    public SyncBeltSlotContents(FriendlyByteBuf buf)
    {
        entityId = buf.readVarInt();
        stack = buf.readItem();
    }

    public void write(FriendlyByteBuf buf)
    {
        buf.writeVarInt(entityId);
        buf.writeItem(stack);
    }

    @Override
    public ResourceLocation id()
    {
        return ID;
    }

    public void handle(PlayPayloadContext context)
    {
        ClientPacketHandlers.handleBeltSlotContents(this);
    }
}
