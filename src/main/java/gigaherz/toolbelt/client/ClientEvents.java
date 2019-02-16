package gigaherz.toolbelt.client;

import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.Config;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.common.ContainerBeltSlot;
import gigaherz.toolbelt.network.OpenBeltSlotInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

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
    public static void textureStitch(TextureStitchEvent.Pre event)
    {
        event.getMap().registerSprite(Minecraft.getInstance().getResourceManager(), ContainerBeltSlot.EMPTY_SPRITE);
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event)
    {
        if (Minecraft.getInstance().world != null)
            handleKeys();
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
            if (mc.currentScreen == null)
            {
                ItemStack inHand = mc.player.getHeldItemMainhand();
                if (Config.isItemStackAllowed(inHand))
                {
                    BeltFinder.BeltGetter getter = BeltFinder.findBelt(mc.player);
                    if (getter == null)
                        return;

                    mc.displayGuiScreen(new GuiRadialMenu(getter));
                }
            }
        }

        while (keyOpenBeltSlot.isPressed())
        {
            if (mc.currentScreen == null)
            {
                ToolBelt.channel.sendToServer(new OpenBeltSlotInventory());
            }
        }
    }
}
