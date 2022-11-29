package dev.gigaherz.toolbelt.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.gigaherz.toolbelt.client.ClientPacketHandlers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

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

    public BeltContentsChange(PacketBuffer buf)
    {
        player = buf.readVarInt();
        where = buf.readUtf();
        slot = buf.readVarInt();
        stack = buf.readItem();
    }

    public void encode(PacketBuffer buf)
    {
        buf.writeVarInt(player);
        buf.writeUtf(where);
        buf.writeVarInt(slot);
        buf.writeItem(stack);
    }

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        ClientPacketHandlers.handleBeltContentsChange(this);
        return true;
    }
}
