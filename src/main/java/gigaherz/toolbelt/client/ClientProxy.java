package gigaherz.toolbelt.client;

import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.Config;
import gigaherz.toolbelt.ISideProxy;
import gigaherz.toolbelt.ToolBelt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.RecipeBookClient;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static gigaherz.common.client.ModelHelpers.registerItemModel;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy implements ISideProxy
{
    public static KeyBinding keyOpenToolMenu;

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        registerItemModel(ToolBelt.belt);
        registerItemModel(ToolBelt.pouch);
    }

    @SubscribeEvent
    public static void handleKeys(InputEvent ev) // Not a mistake, I want both kb & mouse events handled.
    {
        Minecraft mc = Minecraft.getMinecraft();

        while (keyOpenToolMenu.isPressed())
        {
            if (mc.currentScreen == null)
            {
                ItemStack inHand = mc.player.getHeldItemMainhand();
                if (Config.isItemStackAllowed(inHand))
                {
                    BeltFinder.BeltGetter getter = BeltFinder.instance.findStack(mc.player);
                    if (getter == null)
                        return;

                    mc.displayGuiScreen(new GuiRadialMenu(getter));
                }
            }
        }
    }

    @Override
    public void init()
    {
        ClientRegistry.registerKeyBinding(keyOpenToolMenu =
                new KeyBinding("key.toolbelt.open", Keyboard.KEY_R, "key.toolbelt.category"));
        //keyOpenToolMenu.

        Map<String, RenderPlayer> skinMap = Minecraft.getMinecraft().getRenderManager().getSkinMap();

        RenderPlayer render = skinMap.get("default");
        render.addLayer(new LayerToolBelt(render));

        render = skinMap.get("slim");
        render.addLayer(new LayerToolBelt(render));
    }
}
