package dev.gigaherz.toolbelt.common;

import dev.gigaherz.toolbelt.BeltFinder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber
public class BeltEvents
{
    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event)
    {
        Entity entity = event.getTarget();
        if (!(entity instanceof Player))
            return;

        Player player = (Player) entity;
        BeltFinder.sendSync(player);
    }
}
