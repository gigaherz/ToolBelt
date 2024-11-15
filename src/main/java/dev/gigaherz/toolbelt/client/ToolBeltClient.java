package dev.gigaherz.toolbelt.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.gigaherz.toolbelt.BeltFinder;
import dev.gigaherz.toolbelt.ConfigData;
import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.network.OpenBeltSlotInventory;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.function.Function;

@Mod(value=ToolBelt.MODID, dist =Dist.CLIENT)
public class ToolBeltClient
{
    public ToolBeltClient(ModContainer container, IEventBus modEventBus)
    {
        modEventBus.addListener(this::initKeybinds);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::colors);
        modEventBus.addListener(this::registerLayer);
        modEventBus.addListener(this::addLayers);

        NeoForge.EVENT_BUS.addListener(this::handleKeys);
        NeoForge.EVENT_BUS.addListener(this::updateInputEvent);

        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    public void initKeybinds(RegisterKeyMappingsEvent event)
    {
        event.register(ToolBeltClient.OPEN_TOOL_MENU_KEYBIND =
                new KeyMapping("key.toolbelt.open", GLFW.GLFW_KEY_R, "key.toolbelt.category"));

        event.register(ToolBeltClient.CYCLE_TOOL_MENU_LEFT_KEYBIND =
                new KeyMapping("key.toolbelt.cycle.left", InputConstants.UNKNOWN.getValue(), "key.toolbelt.category"));

        event.register(ToolBeltClient.CYCLE_TOOL_MENU_RIGHT_KEYBIND =
                new KeyMapping("key.toolbelt.cycle.right", InputConstants.UNKNOWN.getValue(), "key.toolbelt.category"));

        event.register(ToolBeltClient.OPEN_BELT_SLOT_KEYBIND =
                new KeyMapping("key.toolbelt.slot", GLFW.GLFW_KEY_V, "key.toolbelt.category"));
    }

    public void clientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            ItemProperties.register(ToolBelt.BELT.get(), ToolBelt.location("has_custom_color"),
                    (ItemStack pStack, @Nullable ClientLevel pLevel, @Nullable LivingEntity pEntity, int pSeed) ->
                            pStack.has(DataComponents.DYED_COLOR)
                                    ? 1 : 0
            );
        });
    }

    public void colors(RegisterColorHandlersEvent.Item event)
    {
        event.register(
                (ItemStack pStack, int pTintIndex) ->
                        pTintIndex == 0 && pStack.has(DataComponents.DYED_COLOR)
                                ? (0xFF000000 | pStack.get(DataComponents.DYED_COLOR).rgb()) : -1,
                ToolBelt.BELT.get()
        );
    }

    public void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(ToolBeltClient.BELT_LAYER, ToolBeltLayer.BeltModel::createBodyLayer);
    }

    public void addLayers(EntityRenderersEvent.AddLayers event)
    {
        addLayerToHumanoid(event, EntityType.ARMOR_STAND, ToolBeltLayer::new);
        addLayerToHumanoid(event, EntityType.ZOMBIE, ToolBeltLayer::new);
        addLayerToHumanoid(event, EntityType.SKELETON, ToolBeltLayer::new);
        addLayerToHumanoid(event, EntityType.HUSK, ToolBeltLayer::new);
        addLayerToHumanoid(event, EntityType.DROWNED, ToolBeltLayer::new);
        addLayerToHumanoid(event, EntityType.STRAY, ToolBeltLayer::new);

        addLayerToPlayerSkin(event, PlayerSkin.Model.WIDE, ToolBeltLayer::new);
        addLayerToPlayerSkin(event, PlayerSkin.Model.SLIM, ToolBeltLayer::new);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <E extends Player, S extends HumanoidRenderState, M extends HumanoidModel<S>>
    void addLayerToPlayerSkin(EntityRenderersEvent.AddLayers event, PlayerSkin.Model skinName, Function<LivingEntityRenderer<E, S, M>, ? extends RenderLayer<S, M>> factory)
    {
        EntityRenderer renderer = event.getSkin(skinName);
        if (renderer instanceof LivingEntityRenderer ler) ler.addLayer(factory.apply(ler));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <E extends LivingEntity, S extends HumanoidRenderState, M extends HumanoidModel<S>>
    void addLayerToHumanoid(EntityRenderersEvent.AddLayers event, EntityType<E> entityType, Function<LivingEntityRenderer<E, S, M>, ? extends RenderLayer<S, M>> factory)
    {
        EntityRenderer<E, S> renderer = event.getRenderer(entityType);
        if (renderer instanceof LivingEntityRenderer ler) ler.addLayer(factory.apply(ler));
    }


    public static KeyMapping OPEN_TOOL_MENU_KEYBIND;
    public static KeyMapping CYCLE_TOOL_MENU_LEFT_KEYBIND;
    public static KeyMapping CYCLE_TOOL_MENU_RIGHT_KEYBIND;

    public static KeyMapping OPEN_BELT_SLOT_KEYBIND;

    public static void wipeOpen()
    {
        // Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
        while (OPEN_TOOL_MENU_KEYBIND.consumeClick())
        {
        }
    }

    private boolean toolMenuKeyWasDown = false;

    public void handleKeys(ClientTickEvent.Pre ev)
    {
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
                    PacketDistributor.sendToServer(OpenBeltSlotInventory.INSTANCE);
                }
            }
        }
    }

    public void updateInputEvent(MovementInputUpdateEvent event)
    {
        if (Minecraft.getInstance().screen instanceof RadialMenuScreen)
        {
            Options settings = Minecraft.getInstance().options;
            var eInput = event.getInput();
            eInput.keyPresses = new Input(
                    isKeyDown0(settings.keyUp),
                    isKeyDown0(settings.keyDown),
                    isKeyDown0(settings.keyLeft),
                    isKeyDown0(settings.keyRight),
                    isKeyDown0(settings.keyJump),
                    isKeyDown0(settings.keyShift),
                    isKeyDown0(settings.keySprint)
            );
            eInput.forwardImpulse = eInput.keyPresses.forward() == eInput.keyPresses.backward() ? 0.0F : (eInput.keyPresses.forward() ? 1.0F : -1.0F);
            eInput.leftImpulse = eInput.keyPresses.left() == eInput.keyPresses.right() ? 0.0F : (eInput.keyPresses.left() ? 1.0F : -1.0F);
            if (Minecraft.getInstance().player.isMovingSlowly())
            {
                eInput.leftImpulse = (float) ((double) eInput.leftImpulse * 0.3D);
                eInput.forwardImpulse = (float) ((double) eInput.forwardImpulse * 0.3D);
            }
        }
    }

    public static boolean isKeyDown0(KeyMapping keybind)
    {
        if (keybind.isUnbound())
            return false;

        return switch (keybind.getKey().getType())
        {
            case KEYSYM ->
                    InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keybind.getKey().getValue());
            case MOUSE ->
                    GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), keybind.getKey().getValue()) == GLFW.GLFW_PRESS;
            default -> false;
        };
    }

    public static boolean isKeyDown(KeyMapping keybind)
    {
        if (keybind.isUnbound())
            return false;

        boolean isDown = switch (keybind.getKey().getType())
        {
            case KEYSYM ->
                    InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keybind.getKey().getValue());
            case MOUSE ->
                    GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), keybind.getKey().getValue()) == GLFW.GLFW_PRESS;
            default -> false;
        };
        return isDown && keybind.getKeyConflictContext().isActive() && keybind.getKeyModifier().isActive(keybind.getKeyConflictContext());
    }

    public static ModelLayerLocation BELT_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("minecraft", "player"), "toolbelt_belt");
}
