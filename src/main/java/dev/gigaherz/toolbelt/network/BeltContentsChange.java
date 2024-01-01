package dev.gigaherz.toolbelt.network;

import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class BeltContentsChange implements CustomPacketPayload
{
    public static final ResourceLocation ID = ToolBelt.location("belt_contents_change");

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

    public void write(FriendlyByteBuf buf)
    {
        buf.writeVarInt(player);
        buf.writeUtf(where);
        buf.writeVarInt(slot);
        buf.writeItem(stack);
    }

    @Override
    public ResourceLocation id()
    {
        return ID;
    }

    public void handle(PlayPayloadContext context)
    {
        ClientPacketHandlers.handleBeltContentsChange(this);
    }
}
