package dev.gigaherz.toolbelt.client;

import dev.gigaherz.toolbelt.BeltFinder;
import dev.gigaherz.toolbelt.ConfigData;
import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.common.BeltSlotContainer;
import dev.gigaherz.toolbelt.network.OpenBeltSlotInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
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
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
        while (OPEN_TOOL_MENU_KEYBIND.consumeClick())
        {
        }
    }

    public static void initKeybinds()
    {
        ClientRegistry.registerKeyBinding(OPEN_TOOL_MENU_KEYBIND =
                new KeyBinding("key.toolbelt.open", GLFW.GLFW_KEY_R, "key.toolbelt.category"));

        ClientRegistry.registerKeyBinding(CYCLE_TOOL_MENU_LEFT_KEYBIND =
                new KeyBinding("key.toolbelt.cycle.left", InputMappings.UNKNOWN.getValue(), "key.toolbelt.category"));

        ClientRegistry.registerKeyBinding(CYCLE_TOOL_MENU_RIGHT_KEYBIND =
                new KeyBinding("key.toolbelt.cycle.right", InputMappings.UNKNOWN.getValue(), "key.toolbelt.category"));

        ClientRegistry.registerKeyBinding(OPEN_BELT_SLOT_KEYBIND =
                new KeyBinding("key.toolbelt.slot", GLFW.GLFW_KEY_V, "key.toolbelt.category"));

        addLayerToEntity(EntityType.ARMOR_STAND, ArmorStandRenderer.class);
        addLayerToEntity(EntityType.ZOMBIE, ZombieRenderer.class);
        addLayerToEntity(EntityType.SKELETON, SkeletonRenderer.class);
        addLayerToEntity(EntityType.HUSK, HuskRenderer.class);
        addLayerToEntity(EntityType.DROWNED, DrownedRenderer.class);
        addLayerToEntity(EntityType.STRAY, StrayRenderer.class);

        Map<String, PlayerRenderer> skinMap = Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap();
        addLayerToPlayerSkin(skinMap, "default");
        addLayerToPlayerSkin(skinMap, "slim");
    }

    private static void addLayerToPlayerSkin(Map<String, PlayerRenderer> skinMap, String skinName)
    {
        PlayerRenderer render = skinMap.get(skinName);
        render.addLayer(new ToolBeltLayer<>(render));
    }

    private static <T extends LivingEntity, M extends BipedModel<T>, R extends LivingRenderer<? super T, M>> void addLayerToEntity(EntityType<? extends T> entityType, Class<R> rendererClass)
    {
        EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().renderers.get(entityType);
        if (!rendererClass.isInstance(renderer))
            throw new IllegalStateException("Mismatched renderer class?!");
        if (!(((LivingRenderer<?,?>)renderer).getModel() instanceof BipedModel))
            throw new IllegalStateException("Wrong model type, renderer for entity "+entityType.getRegistryName()+" needs to use a BipedModel.");
        @SuppressWarnings("unchecked")
        LivingRenderer<T, M> bipedRenderer = (LivingRenderer<T, M>) renderer;
        bipedRenderer.addLayer(new ToolBeltLayer<>(bipedRenderer));
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

    public static boolean isKeyDown(KeyBinding keybind)
    {
        if (keybind.isUnbound())
            return false;

        boolean isDown = false;
        switch (keybind.getKey().getType())
        {
            case KEYSYM:
                isDown = InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keybind.getKey().getValue());
                break;
            case MOUSE:
                isDown = GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), keybind.getKey().getValue()) == GLFW.GLFW_PRESS;
                break;
        }
        return isDown && keybind.getKeyConflictContext().isActive() && keybind.getKeyModifier().isActive(keybind.getKeyConflictContext());
    }

    @Mod.EventBusSubscriber(modid = ToolBelt.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents
    {
        @SubscribeEvent
        public static void textureStitch(TextureStitchEvent.Pre event)
        {
            if (event.getMap().location() == AtlasTexture.LOCATION_BLOCKS)
            {
                event.addSprite(BeltSlotContainer.SLOT_BACKGROUND);
            }
        }
    }
}
