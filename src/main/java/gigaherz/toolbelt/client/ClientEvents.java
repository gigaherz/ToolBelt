package gigaherz.toolbelt.client;

import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.ConfigData;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.common.BeltSlotContainer;
import gigaherz.toolbelt.network.OpenBeltSlotInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Collections;

import static gigaherz.toolbelt.client.ClientProxy.keyOpenBeltSlot;
import static gigaherz.toolbelt.client.ClientProxy.keyOpenToolMenu;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEvents
{
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        //registerItemModel(ToolBelt.belt);
        //registerItemModel(ToolBelt.pouch);
    }

    @SubscribeEvent
    public static void handleKeys(InputEvent.MouseInputEvent ev)
    {
        handleKeys();
    }

    @SubscribeEvent
    public static void handleKeys(InputEvent.KeyInputEvent ev)
    {
        handleKeys();
    }

    private static void handleKeys()
    {
        Minecraft mc = Minecraft.getInstance();

        while (keyOpenToolMenu.isPressed())
        {
            if (mc.field_71462_r == null)
            {
                ItemStack inHand = mc.player.getHeldItemMainhand();
                if (ConfigData.isItemStackAllowed(inHand))
                {
                    BeltFinder.BeltGetter getter = BeltFinder.findBelt(mc.player);
                    if (getter == null)
                        return;

                    mc.displayGuiScreen(new RadialMenuScreen(getter));
                }
            }
        }

        while (keyOpenBeltSlot.isPressed())
        {
            if (mc.field_71462_r == null)
            {
                ToolBelt.channel.sendToServer(new OpenBeltSlotInventory());
            }
        }
    }
}
