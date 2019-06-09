package gigaherz.toolbelt.client;

import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.network.BeltContentsChange;
import gigaherz.toolbelt.network.SyncBeltSlotContents;
import gigaherz.toolbelt.slot.ExtensionSlotBelt;
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
            switch (message.where)
            {
                case MAIN:
                    player.inventory.setInventorySlotContents(message.slot, message.stack);
                    break;
                case BELT_SLOT:
                    BeltFinder.instances.forEach((i) -> i.setToBeltSlot(player, message.stack));
                    break;
                case BAUBLES:
                    BeltFinder.instances.forEach((i) -> i.setToBaubles(player, message.slot, message.stack));
                    break;
            }
        });
    }

    public static void handleBeltSlotContents(SyncBeltSlotContents message)
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            Entity entity = minecraft.world.getEntityByID(message.entityId);
            if (entity instanceof PlayerEntity)
            {
                ExtensionSlotBelt.get((LivingEntity) entity).setAll(message.stacks);
            }
        });
    }
}
