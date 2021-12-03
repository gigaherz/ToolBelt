package dev.gigaherz.toolbelt.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.gigaherz.toolbelt.client.ClientPacketHandlers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BeltContentsChange
{
    public int player;
    public String where;
    public JsonElement slot;
    public ItemStack stack;

    public BeltContentsChange(LivingEntity player, String where, JsonElement slot, ItemStack stack)
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
        slot = (new JsonParser()).parse(buf.readUtf(2048));
        stack = buf.readItem();
    }

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeVarInt(player);
        buf.writeUtf(where);
        buf.writeUtf(slot.toString(), 2048);
        buf.writeItem(stack);
    }

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        ClientPacketHandlers.handleBeltContentsChange(this);
        return true;
    }
}
