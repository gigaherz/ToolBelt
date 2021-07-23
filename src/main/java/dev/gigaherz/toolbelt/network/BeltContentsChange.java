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
    public JsonElement slot;
    public ItemStack stack;

    public BeltContentsChange(LivingEntity player, String where, JsonElement slot, ItemStack stack)
    {
        this.player = player.getEntityId();
        this.where = where;
        this.slot = slot;
        this.stack = stack.copy();
    }

    public BeltContentsChange(PacketBuffer buf)
    {
        player = buf.readVarInt();
        where = buf.readString();
        slot = (new JsonParser()).parse(buf.readString(2048));
        stack = buf.readItemStack();
    }

    public void encode(PacketBuffer buf)
    {
        buf.writeVarInt(player);
        buf.writeString(where);
        buf.writeString(slot.toString(), 2048);
        buf.writeItemStack(stack);
    }

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        ClientPacketHandlers.handleBeltContentsChange(this);
        return true;
    }
}
