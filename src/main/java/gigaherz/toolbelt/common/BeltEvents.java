package gigaherz.toolbelt.common;

import gigaherz.toolbelt.BeltFinder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class BeltEvents
{
    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event)
    {
        Entity entity = event.getTarget();
        if (!(entity instanceof EntityPlayer))
            return;

        EntityPlayer player = (EntityPlayer) entity;
        BeltFinder.sendSync(player);
    }
}
