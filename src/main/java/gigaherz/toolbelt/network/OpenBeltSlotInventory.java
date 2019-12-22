package gigaherz.toolbelt.network;

import gigaherz.toolbelt.ConfigData;
import gigaherz.toolbelt.common.Screens;
import net.minecraft.network.PacketBuffer;
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
        if (ConfigData.customBeltSlotEnabled)
        {
            context.get().enqueueWork(() -> {
                Screens.openSlotScreen(context.get().getSender());
            });
        }
        context.get().setPacketHandled(true);
    }
}
