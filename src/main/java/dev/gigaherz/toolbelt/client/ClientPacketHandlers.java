package dev.gigaherz.toolbelt.client;

import dev.gigaherz.toolbelt.BeltFinder;
import dev.gigaherz.toolbelt.network.BeltContentsChange;
import dev.gigaherz.toolbelt.network.SyncBeltSlotContents;
import dev.gigaherz.toolbelt.slot.BeltExtensionSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class ClientPacketHandlers
{
    public static void handleBeltContentsChange(final BeltContentsChange message)
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            Entity entity = minecraft.world.getEntityByID(message.player);
            if (!(entity instanceof PlayerEntity))
                return;
            PlayerEntity player = (PlayerEntity) entity;
            BeltFinder.setFinderSlotContents(player, message.where, message.slot, message.stack);
        });
    }

    public static void handleBeltSlotContents(SyncBeltSlotContents message)
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            Entity entity = minecraft.world.getEntityByID(message.entityId);
            if (entity instanceof PlayerEntity)
            {
                BeltExtensionSlot.get((LivingEntity) entity).ifPresent((slot) -> slot.setAll(message.stacks));
            }
        });
    }
}
