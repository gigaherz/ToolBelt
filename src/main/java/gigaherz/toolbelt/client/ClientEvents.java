package gigaherz.toolbelt.client;

import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.ConfigData;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.common.BeltSlotContainer;
import gigaherz.toolbelt.network.OpenBeltSlotInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.Map;


@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ToolBelt.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents
{
    public static KeyBinding OPEN_TOOL_MENU_KEYBIND;
    public static KeyBinding CYCLE_TOOL_MENU_LEFT_KEYBIND;
    public static KeyBinding CYCLE_TOOL_MENU_RIGHT_KEYBIND;

    public static KeyBinding OPEN_BELT_SLOT_KEYBIND;

    public static void wipeOpen()
    {
        Minecraft.getInstance().keyboardListener.enableRepeatEvents(false);
        while (OPEN_TOOL_MENU_KEYBIND.isPressed())
        {
        }
    }

    public static void initKeybinds()
    {
        ClientRegistry.registerKeyBinding(OPEN_TOOL_MENU_KEYBIND =
                new KeyBinding("key.toolbelt.open", GLFW.GLFW_KEY_R, "key.toolbelt.category"));

        ClientRegistry.registerKeyBinding(CYCLE_TOOL_MENU_LEFT_KEYBIND =
                new KeyBinding("key.toolbelt.cycle.left",  InputMappings.INPUT_INVALID.getKeyCode(), "key.toolbelt.category"));

        ClientRegistry.registerKeyBinding(CYCLE_TOOL_MENU_RIGHT_KEYBIND =
                new KeyBinding("key.toolbelt.cycle.right",  InputMappings.INPUT_INVALID.getKeyCode(), "key.toolbelt.category"));

        ClientRegistry.registerKeyBinding(OPEN_BELT_SLOT_KEYBIND =
                new KeyBinding("key.toolbelt.slot", GLFW.GLFW_KEY_V, "key.toolbelt.category"));

        Map<String, PlayerRenderer> skinMap = Minecraft.getInstance().getRenderManager().getSkinMap();

        PlayerRenderer render = skinMap.get("default");
        render.addLayer(new LayerToolBelt(render));

        render = skinMap.get("slim");
        render.addLayer(new LayerToolBelt(render));
    }

    private static boolean toolMenuKeyWasDown = false;

    @SubscribeEvent
    public static void handleKeys(TickEvent.ClientTickEvent ev)
    {
        Minecraft mc = Minecraft.getInstance();

        if (mc.currentScreen == null)
        {
            boolean toolMenuKeyIsDown = isKeyDown(OPEN_TOOL_MENU_KEYBIND);
            if (toolMenuKeyIsDown && !toolMenuKeyWasDown)
            {
                while (OPEN_TOOL_MENU_KEYBIND.isPressed())
                {
                    if (mc.currentScreen == null)
                    {
                        ItemStack inHand = mc.player.getHeldItemMainhand();
                        if (ConfigData.isItemStackAllowed(inHand))
                        {
                            BeltFinder.findBelt(mc.player).ifPresent((getter) -> mc.displayGuiScreen(new RadialMenuScreen(getter)));
                        }
                    }
                }
            }
            toolMenuKeyWasDown = toolMenuKeyIsDown;
        }
        else
        {
            toolMenuKeyWasDown = true;
        }

        if (ConfigData.customBeltSlotEnabled)
        {
            while (OPEN_BELT_SLOT_KEYBIND.isPressed())
            {
                if (mc.currentScreen == null)
                {
                    ToolBelt.channel.sendToServer(new OpenBeltSlotInventory());
                }
            }
        }
    }

    public static boolean isKeyDown(KeyBinding keybind)
    {
        if (keybind.isInvalid())
            return false;
        return InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), keybind.getKey().getKeyCode())
                && keybind.getKeyConflictContext().isActive() && keybind.getKeyModifier().isActive(keybind.getKeyConflictContext());
    }

    @Mod.EventBusSubscriber(modid = ToolBelt.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents
    {
        @SubscribeEvent
        public static void textureStitch(TextureStitchEvent.Pre event)
        {
            if (event.getMap().getTextureLocation() == AtlasTexture.LOCATION_BLOCKS_TEXTURE)
            {
                event.addSprite(BeltSlotContainer.SLOT_BACKGROUND);
            }
        }
    }
}
