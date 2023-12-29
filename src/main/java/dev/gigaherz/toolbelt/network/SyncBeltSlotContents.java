package dev.gigaherz.toolbelt.network;

import dev.gigaherz.toolbelt.client.ClientPacketHandlers;
import dev.gigaherz.toolbelt.slot.BeltAttachment;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.NetworkEvent;

public class SyncBeltSlotContents
{
    public ItemStack stack = ItemStack.EMPTY;
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

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeVarInt(entityId);
        buf.writeItem(stack);
    }

    public void handle(NetworkEvent.Context context)
    {
        ClientPacketHandlers.handleBeltSlotContents(this);
    }
}
