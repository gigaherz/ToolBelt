package gigaherz.toolbelt.common;

import gigaherz.toolbelt.BeltFinder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class BeltEvents
{
    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event)
    {
        Entity entity = event.getTarget();
        if (!(entity instanceof PlayerEntity))
            return;

        PlayerEntity player = (PlayerEntity) entity;
        BeltFinder.sendSync(player);
    }
}
