package dev.gigaherz.toolbelt.network;

import dev.gigaherz.toolbelt.client.ClientPacketHandlers;
import dev.gigaherz.toolbelt.customslots.IExtensionSlot;
import dev.gigaherz.toolbelt.slot.BeltExtensionSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncBeltSlotContents
{
    public final NonNullList<ItemStack> stacks = NonNullList.create();
    public int entityId;

    public SyncBeltSlotContents(PlayerEntity player, BeltExtensionSlot extension)
    {
        this.entityId = player.getEntityId();
        extension.getSlots().stream().map(IExtensionSlot::getContents).forEach(stacks::add);
    }

    public SyncBeltSlotContents(PacketBuffer buf)
    {
        entityId = buf.readVarInt();
        int numStacks = buf.readVarInt();
        for (int i = 0; i < numStacks; i++)
        {
            stacks.add(buf.readItemStack());
        }
    }

    public void encode(PacketBuffer buf)
    {
        buf.writeVarInt(entityId);
        buf.writeVarInt(stacks.size());
        for (ItemStack stack : stacks)
        {
            buf.writeItemStack(stack);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        ClientPacketHandlers.handleBeltSlotContents(this);
        return true;
    }
}
