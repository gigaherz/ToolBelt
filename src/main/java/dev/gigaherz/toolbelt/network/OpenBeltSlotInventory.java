package dev.gigaherz.toolbelt.network;

import dev.gigaherz.toolbelt.ConfigData;
import dev.gigaherz.toolbelt.common.Screens;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

public class OpenBeltSlotInventory
{
    public OpenBeltSlotInventory()
    {
    }

    public OpenBeltSlotInventory(FriendlyByteBuf buf)
    {
    }

    public void encode(FriendlyByteBuf buf)
    {
    }

    public boolean handle(NetworkEvent.Context context)
    {
        if (ConfigData.customBeltSlotEnabled)
        {
            context.enqueueWork(() -> {
                Screens.openSlotScreen(context.getSender());
            });
        }
        return true;
    }
}
