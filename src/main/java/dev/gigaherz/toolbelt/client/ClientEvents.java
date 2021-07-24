package dev.gigaherz.toolbelt.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.gigaherz.toolbelt.BeltFinder;
import dev.gigaherz.toolbelt.ConfigData;
import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.common.BeltSlotContainer;
import dev.gigaherz.toolbelt.network.OpenBeltSlotInventory;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import net.minecraftforge.fmlclient.registry.RenderingRegistry;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ToolBelt.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents
{
    static {
        // WORKAROUND FOR EVENTBUS ISSUE
        GuiContainerEvent.class.getName();
        GuiContainerEvent.DrawBackground.class.getName();
    }

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

        boolean isDown = false;
        switch (keybind.getKey().getType())
        {
            case KEYSYM:
                isDown = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keybind.getKey().getValue());
                break;
            case MOUSE:
                isDown = GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), keybind.getKey().getValue()) == GLFW.GLFW_PRESS;
                break;
        }
        return isDown && keybind.getKeyConflictContext().isActive() && keybind.getKeyModifier().isActive(keybind.getKeyConflictContext());
    }

    public static ModelLayerLocation BELT_LAYER = new ModelLayerLocation(new ResourceLocation("minecraft:player"), "toolbelt_belt");

    @Mod.EventBusSubscriber(modid = ToolBelt.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents
    {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event)
        {
            RenderingRegistry.registerLayerDefinition(BELT_LAYER, ToolBeltLayer.BeltModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void textureStitch(TextureStitchEvent.Pre event)
        {
            if (event.getMap().location() == TextureAtlas.LOCATION_BLOCKS)
            {
                event.addSprite(BeltSlotContainer.SLOT_BACKGROUND);
            }
        }

        @SubscribeEvent
        public static void construct(ParticleFactoryRegisterEvent event)
        {
            ((ReloadableResourceManager)Minecraft.getInstance().getResourceManager()).registerReloadListener(new ResourceManagerReloadListener()
            {
                @Override
                public void onResourceManagerReload(ResourceManager p_10758_)
                {
                    addLayerToEntity(EntityType.ARMOR_STAND, ArmorStandRenderer.class);
                    addLayerToEntity(EntityType.ZOMBIE, ZombieRenderer.class);
                    addLayerToEntity(EntityType.SKELETON, SkeletonRenderer.class);
                    addLayerToEntity(EntityType.HUSK, HuskRenderer.class);
                    addLayerToEntity(EntityType.DROWNED, DrownedRenderer.class);
                    addLayerToEntity(EntityType.STRAY, StrayRenderer.class);

                    Map<String, EntityRenderer<? extends Player>> skinMap = Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap();
                    addLayerToPlayerSkin(skinMap, "default");
                    addLayerToPlayerSkin(skinMap, "slim");
                }
            });
        }

        private static void addLayerToPlayerSkin(Map<String, EntityRenderer<? extends Player>> skinMap, String skinName)
        {
            EntityRenderer<? extends Player> render = skinMap.get(skinName);
            if (render instanceof LivingEntityRenderer)
            {
                LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> livingRenderer
                        = (LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>) render;
                livingRenderer.addLayer(new ToolBeltLayer<>(livingRenderer));
            }
        }

        private static <T extends LivingEntity, M extends HumanoidModel<T>, R extends LivingEntityRenderer<? super T, M>> void addLayerToEntity(EntityType<? extends T> entityType, Class<R> rendererClass)
        {
            EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().renderers.get(entityType);
            if (!rendererClass.isInstance(renderer))
                throw new IllegalStateException("Mismatched renderer class?!");
            if (!(((LivingEntityRenderer<?,?>)renderer).getModel() instanceof HumanoidModel))
                throw new IllegalStateException("Wrong model type, renderer for entity "+entityType.getRegistryName()+" needs to use a BipedModel.");
            @SuppressWarnings("unchecked")
            LivingEntityRenderer<T, M> bipedRenderer = (LivingEntityRenderer<T, M>) renderer;
            bipedRenderer.addLayer(new ToolBeltLayer<>(bipedRenderer));
        }
    }
}
