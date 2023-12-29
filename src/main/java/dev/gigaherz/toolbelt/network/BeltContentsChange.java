package dev.gigaherz.toolbelt.network;

import dev.gigaherz.toolbelt.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.NetworkEvent;

public class BeltContentsChange
{
    public int player;
    public String where;
    public int slot;
    public ItemStack stack;

    public BeltContentsChange(LivingEntity player, String where, int slot, ItemStack stack)
    {
        this.player = player.getId();
        this.where = where;
        this.slot = slot;
        this.stack = stack.copy();
    }

    public BeltContentsChange(FriendlyByteBuf buf)
    {
        player = buf.readVarInt();
        where = buf.readUtf();
        slot = buf.readVarInt();
        stack = buf.readItem();
    }

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeVarInt(player);
        buf.writeUtf(where);
        buf.writeVarInt(slot);
        buf.writeItem(stack);
    }

    public void handle(NetworkEvent.Context context)
    {
        ClientPacketHandlers.handleBeltContentsChange(this);
    }
}
