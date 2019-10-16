package gigaherz.toolbelt.network;

import gigaherz.toolbelt.client.ClientPacketHandlers;
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
        this.player = player.getEntityId();
        this.where = where;
        this.slot = slot;
        this.stack = stack;
    }

    public BeltContentsChange(PacketBuffer buf)
    {
        player = buf.readVarInt();
        where = buf.readString();
        slot = buf.readVarInt();
        stack = buf.readItemStack();
    }

    public void encode(PacketBuffer buf)
    {
        buf.writeVarInt(player);
        buf.writeString(where);
        buf.writeVarInt(slot);
        buf.writeItemStack(stack);
    }

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        ClientPacketHandlers.handleBeltContentsChange(this);
        return true;
    }
}
