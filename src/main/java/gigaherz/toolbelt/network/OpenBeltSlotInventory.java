package gigaherz.toolbelt.network;

import gigaherz.toolbelt.common.GuiHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenBeltSlotInventory
{
    public OpenBeltSlotInventory()
    {
    }

    public void fromBytes(ByteBuf buf)
    {
    }

    public void toBytes(ByteBuf buf)
    {
    }

    public static void encode(OpenBeltSlotInventory message, PacketBuffer packet)
    {
        message.toBytes(packet);
    }

    public static OpenBeltSlotInventory decode(PacketBuffer packet)
    {
        OpenBeltSlotInventory message = new OpenBeltSlotInventory();
        message.fromBytes(packet);
        return message;
    }

    public static void onMessage(final OpenBeltSlotInventory message, Supplier<NetworkEvent.Context> context)
    {
        final EntityPlayerMP player = context.get().getSender();
        final WorldServer world = (WorldServer) player.world;

        world.addScheduledTask(() -> {
            GuiHandler.openSlotGui(player);
        });
    }
}
