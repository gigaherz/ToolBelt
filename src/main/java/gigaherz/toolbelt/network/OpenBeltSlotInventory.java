package gigaherz.toolbelt.network;

import gigaherz.toolbelt.common.GuiHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenBeltSlotInventory
{
    public OpenBeltSlotInventory()
    {
    }
    public OpenBeltSlotInventory(PacketBuffer buf)
    {
    }

    public void encode(PacketBuffer buf)
    {
    }

    public void handle(Supplier<NetworkEvent.Context> context)
    {
        final EntityPlayerMP player = context.get().getSender();
        final WorldServer world = (WorldServer) player.world;

        world.addScheduledTask(() -> {
            GuiHandler.openSlotGui(player);
        });
    }
}
