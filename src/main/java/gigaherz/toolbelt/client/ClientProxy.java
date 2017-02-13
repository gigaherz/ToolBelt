package gigaherz.toolbelt.client;

import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.Config;
import gigaherz.toolbelt.ISideProxy;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.belt.ItemToolBelt;
import gigaherz.toolbelt.belt.ToolBeltInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;

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
    public static void handleKeys(InputEvent ev)
    {
        Minecraft mc = Minecraft.getMinecraft();
        while (keyOpenToolMenu.isPressed())
        {
            ToolBelt.logger.warn("POKE! {0}", mc.world.getTotalWorldTime());
            if (mc.currentScreen == null)
            {
                ItemStack inHand = mc.player.getHeldItemMainhand();
                if (Config.isItemStackAllowed(inHand))
                {
                    ItemStack stack = BeltFinder.instance.findStack(mc.player);
                    if (stack.getCount() <= 0)
                        return;

                    ToolBeltInventory cap = ItemToolBelt.getItems(stack);

                    mc.displayGuiScreen(new GuiRadialMenu(cap));
                }
            }
        }
    }

    @Override
    public void init()
    {
        ClientRegistry.registerKeyBinding(keyOpenToolMenu =
                new KeyBinding("key.toolbelt.open", Keyboard.KEY_R, "key.toolbelt.category"));

        Map<String, RenderPlayer> skinMap = Minecraft.getMinecraft().getRenderManager().getSkinMap();

        RenderPlayer render = skinMap.get("default");
        render.addLayer(new LayerToolBelt(render));

        render = skinMap.get("slim");
        render.addLayer(new LayerToolBelt(render));
    }
}
