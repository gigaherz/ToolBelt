package dev.gigaherz.toolbelt.network;

import dev.gigaherz.toolbelt.client.ClientPacketHandlers;
import dev.gigaherz.toolbelt.customslots.IExtensionSlot;
import dev.gigaherz.toolbelt.slot.BeltExtensionSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.NonNullList;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncBeltSlotContents
{
    public final NonNullList<ItemStack> stacks = NonNullList.create();
    public int entityId;

    public SyncBeltSlotContents(Player player, BeltExtensionSlot extension)
    {
        this.entityId = player.getId();
        extension.getSlots().stream().map(IExtensionSlot::getContents).forEach(stacks::add);
    }

    public SyncBeltSlotContents(FriendlyByteBuf buf)
    {
        entityId = buf.readVarInt();
        int numStacks = buf.readVarInt();
        for (int i = 0; i < numStacks; i++)
        {
            stacks.add(buf.readItem());
        }
    }

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeVarInt(entityId);
        buf.writeVarInt(stacks.size());
        for (ItemStack stack : stacks)
        {
            buf.writeItem(stack);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        ClientPacketHandlers.handleBeltSlotContents(this);
        return true;
    }
}
