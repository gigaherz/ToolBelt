package gigaherz.toolbelt.network;

import gigaherz.toolbelt.client.ClientPacketHandlers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class BeltContentsChange
{

    public enum ContainingInventory
    {
        MAIN, BAUBLES, BELT_SLOT;

        public static final ContainingInventory[] VALUES = values();
    }

    public int player;
    public ContainingInventory where;
    public int slot;
    public ItemStack stack;

    public BeltContentsChange(LivingEntity player, ContainingInventory where, int slot, ItemStack stack)
    {
        this.player = player.getEntityId();
        this.where = where;
        this.slot = slot;
        this.stack = stack;
    }

    public BeltContentsChange(PacketBuffer buf)
    {
        player = buf.readInt();
        where = ContainingInventory.VALUES[buf.readByte()];
        slot = buf.readByte();
        stack = buf.readItemStack();
    }

    public void encode(PacketBuffer buf)
    {
        buf.writeInt(player);
        buf.writeByte(where.ordinal());
        buf.writeByte(slot);
        buf.writeItemStack(stack);
    }

    public void handle(Supplier<NetworkEvent.Context> context)
    {
        ClientPacketHandlers.handleBeltContentsChange(this);
    }
}
