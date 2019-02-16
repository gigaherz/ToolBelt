package gigaherz.toolbelt.network;

import gigaherz.toolbelt.ToolBelt;
import net.minecraft.entity.EntityLivingBase;
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

    public BeltContentsChange()
    {
    }

    public BeltContentsChange(EntityLivingBase player, ContainingInventory where, int slot, ItemStack stack)
    {
        this.player = player.getEntityId();
        this.where = where;
        this.slot = slot;
        this.stack = stack;
    }

    public void fromBytes(PacketBuffer buf)
    {
        player = buf.readInt();
        where = ContainingInventory.VALUES[buf.readByte()];
        slot = buf.readByte();
        stack = buf.readItemStack();
    }

    public void toBytes(PacketBuffer buf)
    {
        buf.writeInt(player);
        buf.writeByte(where.ordinal());
        buf.writeByte(slot);
        buf.writeItemStack(stack);
    }

    public static void encode(BeltContentsChange message, PacketBuffer packet)
    {
        message.toBytes(packet);
    }

    public static BeltContentsChange decode(PacketBuffer packet)
    {
        BeltContentsChange message = new BeltContentsChange();
        message.fromBytes(packet);
        return message;
    }

    public static void onMessage(final BeltContentsChange message, Supplier<NetworkEvent.Context> context)
    {
        ToolBelt.proxy.handleBeltContentsChange(message);
    }
}
