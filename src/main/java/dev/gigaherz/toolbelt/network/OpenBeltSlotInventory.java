package dev.gigaherz.toolbelt.network;

import dev.gigaherz.toolbelt.ConfigData;
import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.common.Screens;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class OpenBeltSlotInventory implements CustomPacketPayload
{
    public static final ResourceLocation ID = ToolBelt.location("open_belt_slot_inventory");

    public OpenBeltSlotInventory()
    {
    }

    public OpenBeltSlotInventory(FriendlyByteBuf buf)
    {
    }

    public void write(FriendlyByteBuf buf)
    {
    }

    @Override
    public ResourceLocation id()
    {
        return ID;
    }

    public void handle(PlayPayloadContext context)
    {
        if (ConfigData.customBeltSlotEnabled)
        {
            context.workHandler().execute(() -> Screens.openSlotScreen(context.player().orElseThrow()));
        }
    }
}
