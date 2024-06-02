package dev.gigaherz.toolbelt.client;

import dev.gigaherz.toolbelt.BeltFinder;
import dev.gigaherz.toolbelt.network.BeltContentsChange;
import dev.gigaherz.toolbelt.network.SyncBeltSlotContents;
import dev.gigaherz.toolbelt.slot.BeltAttachment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class ClientPacketHandlers
{
    public static void handleBeltContentsChange(final BeltContentsChange message)
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            Entity entity = minecraft.level.getEntity(message.player());
            if (!(entity instanceof Player))
                return;
            Player player = (Player) entity;
            BeltFinder.setBeltFromPacket(player, message.where(), message.slot(), message.stack());
        });
    }

    public static void handleBeltSlotContents(SyncBeltSlotContents message)
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            Entity entity = minecraft.level.getEntity(message.entityId());
            if (entity instanceof Player)
            {
                BeltAttachment.get((LivingEntity) entity).setContents(message.stack());
            }
        });
    }
}
