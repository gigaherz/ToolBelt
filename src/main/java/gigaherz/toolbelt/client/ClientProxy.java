package gigaherz.toolbelt.client;

import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.Config;
import gigaherz.toolbelt.ISideProxy;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.common.ContainerBeltSlot;
import gigaherz.toolbelt.network.BeltContentsChange;
import gigaherz.toolbelt.network.OpenBeltSlotInventory;
import gigaherz.toolbelt.network.SyncBeltSlotContents;
import gigaherz.toolbelt.slot.ExtensionSlotBelt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;

import java.util.Map;

import static gigaherz.common.client.ModelHelpers.registerItemModel;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy implements ISideProxy
{
    public static KeyBinding keyOpenToolMenu;
    public static KeyBinding keyCycleToolMenuL;
    public static KeyBinding keyCycleToolMenuR;

    public static KeyBinding keyOpenBeltSlot;

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        registerItemModel(ToolBelt.belt);
        registerItemModel(ToolBelt.pouch);
    }

    @SubscribeEvent
    public static void textureStitch(TextureStitchEvent.Pre event)
    {
        event.getMap().registerSprite(ContainerBeltSlot.EMPTY_SPRITE);
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
                    BeltFinder.BeltGetter getter = BeltFinder.findBelt(mc.player);
                    if (getter == null)
                        return;

                    mc.displayGuiScreen(new GuiRadialMenu(getter));
                }
            }
        }

        if (Config.customBeltSlotEnabled)
        {
            while (keyOpenBeltSlot.isPressed())
            {
                if (mc.currentScreen == null)
                {
                    ToolBelt.channel.sendToServer(new OpenBeltSlotInventory());
                }
            }
        }
    }

    public static void wipeOpen()
    {
        while (keyOpenToolMenu.isPressed())
        {
        }
    }

    @Override
    public void init()
    {
        ClientRegistry.registerKeyBinding(keyOpenToolMenu =
                new KeyBinding("key.toolbelt.open", Keyboard.KEY_R, "key.toolbelt.category"));
        //keyOpenToolMenu.

        ClientRegistry.registerKeyBinding(keyCycleToolMenuL =
                new KeyBinding("key.toolbelt.cycle.left", 0, "key.toolbelt.category"));

        ClientRegistry.registerKeyBinding(keyCycleToolMenuR =
                new KeyBinding("key.toolbelt.cycle.right", 0, "key.toolbelt.category"));

        if (Config.customBeltSlotEnabled)
        {
            ClientRegistry.registerKeyBinding(keyOpenBeltSlot =
                    new KeyBinding("key.toolbelt.slot", Keyboard.KEY_V, "key.toolbelt.category"));
        }

        Map<String, RenderPlayer> skinMap = Minecraft.getMinecraft().getRenderManager().getSkinMap();

        RenderPlayer render = skinMap.get("default");
        render.addLayer(new LayerToolBelt(render));

        render = skinMap.get("slim");
        render.addLayer(new LayerToolBelt(render));
    }

    @Override
    public void handleBeltContentsChange(final BeltContentsChange message)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            Entity entity = Minecraft.getMinecraft().world.getEntityByID(message.player);
            if (!(entity instanceof EntityPlayer))
                return;
            EntityPlayer player = (EntityPlayer) entity;
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

    @Override
    public void handleBeltSlotContents(SyncBeltSlotContents message)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            Entity entity = Minecraft.getMinecraft().world.getEntityByID(message.entityId);
            if (entity instanceof EntityPlayer)
            {
                ExtensionSlotBelt.get((EntityPlayer) entity).setAll(message.stacks);
            }
        });
    }
}
