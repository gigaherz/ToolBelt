package dev.gigaherz.toolbelt.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.gigaherz.toolbelt.BeltFinder;
import dev.gigaherz.toolbelt.ConfigData;
import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.customslots.ExtensionSlotSlot;
import dev.gigaherz.toolbelt.network.OpenBeltSlotInventory;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ToolBelt.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents
{
    public static KeyMapping OPEN_TOOL_MENU_KEYBIND;
    public static KeyMapping CYCLE_TOOL_MENU_LEFT_KEYBIND;
    public static KeyMapping CYCLE_TOOL_MENU_RIGHT_KEYBIND;

    public static KeyMapping OPEN_BELT_SLOT_KEYBIND;

    public static void wipeOpen()
    {
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
        while (OPEN_TOOL_MENU_KEYBIND.consumeClick())
        {
        }
    }

    public static void initKeybinds()
    {
        ClientRegistry.registerKeyBinding(OPEN_TOOL_MENU_KEYBIND =
                new KeyMapping("key.toolbelt.open", GLFW.GLFW_KEY_R, "key.toolbelt.category"));

        ClientRegistry.registerKeyBinding(CYCLE_TOOL_MENU_LEFT_KEYBIND =
                new KeyMapping("key.toolbelt.cycle.left", InputConstants.UNKNOWN.getValue(), "key.toolbelt.category"));

        ClientRegistry.registerKeyBinding(CYCLE_TOOL_MENU_RIGHT_KEYBIND =
                new KeyMapping("key.toolbelt.cycle.right", InputConstants.UNKNOWN.getValue(), "key.toolbelt.category"));

        ClientRegistry.registerKeyBinding(OPEN_BELT_SLOT_KEYBIND =
                new KeyMapping("key.toolbelt.slot", GLFW.GLFW_KEY_V, "key.toolbelt.category"));
    }

    private static boolean toolMenuKeyWasDown = false;

    @SubscribeEvent
    public static void handleKeys(TickEvent.ClientTickEvent ev)
    {
        if (ev.phase != TickEvent.Phase.START)
            return;

        Minecraft mc = Minecraft.getInstance();

        if (mc.screen == null)
        {
            boolean toolMenuKeyIsDown = OPEN_TOOL_MENU_KEYBIND.isDown();
            if (toolMenuKeyIsDown && !toolMenuKeyWasDown)
            {
                while (OPEN_TOOL_MENU_KEYBIND.consumeClick())
                {
                    if (mc.screen == null)
                    {
                        ItemStack inHand = mc.player.getMainHandItem();
                        if (ConfigData.isItemStackAllowed(inHand))
                        {
                            BeltFinder.findBelt(mc.player).ifPresent((getter) -> mc.setScreen(new RadialMenuScreen(getter)));
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
            while (OPEN_BELT_SLOT_KEYBIND.consumeClick())
            {
                if (mc.screen == null)
                {
                    ToolBelt.channel.sendToServer(new OpenBeltSlotInventory());
                }
            }
        }
    }

    public static boolean isKeyDown(KeyMapping keybind)
    {
        if (keybind.isUnbound())
            return false;

        boolean isDown = switch (keybind.getKey().getType())
                {
                    case KEYSYM -> InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keybind.getKey().getValue());
                    case MOUSE -> GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), keybind.getKey().getValue()) == GLFW.GLFW_PRESS;
                    default -> false;
                };
        return isDown && keybind.getKeyConflictContext().isActive() && keybind.getKeyModifier().isActive(keybind.getKeyConflictContext());
    }

    public static ModelLayerLocation BELT_LAYER = new ModelLayerLocation(new ResourceLocation("minecraft:player"), "toolbelt_belt");

    @Mod.EventBusSubscriber(modid = ToolBelt.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents
    {
        @SubscribeEvent
        public static void textureStitch(TextureStitchEvent.Pre event)
        {
            if (event.getAtlas().location() == InventoryMenu.BLOCK_ATLAS)
            {
                event.addSprite(ExtensionSlotSlot.SLOT_BACKGROUND);
            }
        }

        @SubscribeEvent
        public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event)
        {
            event.registerLayerDefinition(BELT_LAYER, ToolBeltLayer.BeltModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void construct(EntityRenderersEvent.AddLayers event)
        {
            addLayerToEntity(event, EntityType.ARMOR_STAND);
            addLayerToEntity(event, EntityType.ZOMBIE);
            addLayerToEntity(event, EntityType.SKELETON);
            addLayerToEntity(event, EntityType.HUSK);
            addLayerToEntity(event, EntityType.DROWNED);
            addLayerToEntity(event, EntityType.STRAY);

            addLayerToPlayerSkin(event, "default");
            addLayerToPlayerSkin(event, "slim");
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static void addLayerToPlayerSkin(EntityRenderersEvent.AddLayers event, String skinName)
        {
            EntityRenderer<? extends Player> render = event.getSkin(skinName);
            if (render instanceof LivingEntityRenderer livingRenderer)
            {
                livingRenderer.addLayer(new ToolBeltLayer<>(livingRenderer));
            }
        }

        private static <T extends LivingEntity, M extends HumanoidModel<T>, R extends LivingEntityRenderer<T, M>> void addLayerToEntity(
                EntityRenderersEvent.AddLayers event, EntityType<? extends T> entityType)
        {
            R renderer = event.getRenderer(entityType);
            if (renderer != null) renderer.addLayer(new ToolBeltLayer<>(renderer));
        }
    }
}
