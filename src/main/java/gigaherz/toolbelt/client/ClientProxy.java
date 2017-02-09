package gigaherz.toolbelt.client;

import gigaherz.toolbelt.ISideProxy;
import gigaherz.toolbelt.ToolBelt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import static gigaherz.common.client.ModelHelpers.registerItemModel;

@Mod.EventBusSubscriber
public class ClientProxy implements ISideProxy
{
    public static KeyBinding keyOpenToolMenu;

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        registerItemModel(ToolBelt.belt);
    }

    @SubscribeEvent
    public static void handleKeys(InputEvent.KeyInputEvent ev)
    {
        Minecraft mc = Minecraft.getMinecraft();
        while (keyOpenToolMenu.isPressed())
        {
            ToolBelt.logger.warn("POKE! {0}", mc.world.getTotalWorldTime());
            if (mc.currentScreen == null)
            {
                mc.displayGuiScreen(GuiRadialMenu.instance);
            }
        }
    }

    @Override
    public void init()
    {
        ClientRegistry.registerKeyBinding(keyOpenToolMenu = new KeyBinding("key.toolbelt.open", Keyboard.KEY_Y, "key.toolbelt.category"));
    }
}
