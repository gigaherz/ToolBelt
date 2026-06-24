package dev.gigaherz.toolbelt.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.gigaherz.toolbelt.BeltFinder;
import dev.gigaherz.toolbelt.ConfigData;
import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.network.OpenBeltSlotInventory;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.player.Input;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@EventBusSubscriber(value = Dist.CLIENT, modid = ToolBelt.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ClientEvents
{
    @Nullable public static KeyMapping OPEN_TOOL_MENU_KEYBIND;
    @Nullable public static KeyMapping CYCLE_TOOL_MENU_LEFT_KEYBIND;
    @Nullable public static KeyMapping CYCLE_TOOL_MENU_RIGHT_KEYBIND;
    @Nullable public static KeyMapping OPEN_BELT_SLOT_KEYBIND;

    public static void wipeOpen()
    {
        // Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
        //noinspection StatementWithEmptyBody
        while (OPEN_TOOL_MENU_KEYBIND != null && OPEN_TOOL_MENU_KEYBIND.consumeClick())
        {
        }
    }

    private static boolean toolMenuKeyWasDown = false;

    @SubscribeEvent
    public static void handleKeys(ClientTickEvent.Pre ev)
    {
        Minecraft mc = Minecraft.getInstance();

        if (mc.screen == null)
        {
            boolean toolMenuKeyIsDown = OPEN_TOOL_MENU_KEYBIND != null && OPEN_TOOL_MENU_KEYBIND.isDown();
            if (toolMenuKeyIsDown && !toolMenuKeyWasDown)
            {
                while (OPEN_TOOL_MENU_KEYBIND != null && OPEN_TOOL_MENU_KEYBIND.consumeClick())
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
            while (OPEN_BELT_SLOT_KEYBIND != null && OPEN_BELT_SLOT_KEYBIND.consumeClick())
            {
                if (mc.screen == null)
                {
                    PacketDistributor.sendToServer(OpenBeltSlotInventory.INSTANCE);
                }
            }
        }
    }

    @SubscribeEvent
    public static void updateInputEvent(MovementInputUpdateEvent event) {
        if (Minecraft.getInstance().screen instanceof RadialMenuScreen) {
            Options settings = Minecraft.getInstance().options;
            Input eInput = event.getInput();
            eInput.up = isKeyDown0(settings.keyUp);
            eInput.down = isKeyDown0(settings.keyDown);
            eInput.left = isKeyDown0(settings.keyLeft);
            eInput.right = isKeyDown0(settings.keyRight);

            eInput.forwardImpulse = eInput.up == eInput.down ? 0.0F : (eInput.up ? 1.0F : -1.0F);
            eInput.leftImpulse = eInput.left == eInput.right ? 0.0F : (eInput.left ? 1.0F : -1.0F);
            eInput.jumping = isKeyDown0(settings.keyJump);
            eInput.shiftKeyDown = isKeyDown0(settings.keyShift);
            if (Minecraft.getInstance().player.isMovingSlowly()) {
                eInput.leftImpulse = (float) ((double) eInput.leftImpulse * 0.3D);
                eInput.forwardImpulse = (float) ((double) eInput.forwardImpulse * 0.3D);
            }
        }
    }

    public static boolean isKeyDown0(KeyMapping keybind)
    {
        if (keybind.isUnbound())
            return false;

        if (ToolBelt.controllableEnabled)
        {
            /*
            Boolean triStateDown = ControllableSupport.isButtonDown(keybind);
            if (Boolean.TRUE.equals(triStateDown))
                return true;
             */
        }

        return switch (keybind.getKey().getType())
        {
            case KEYSYM ->
                    InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keybind.getKey().getValue());
            case MOUSE ->
                    GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), keybind.getKey().getValue()) == GLFW.GLFW_PRESS;
            default -> keybind.isDown();
        };
    }

    public static boolean isKeyDown(KeyMapping keybind)
    {
        return isKeyDown0(keybind) && keybind.getKeyConflictContext().isActive() && keybind.getKeyModifier().isActive(keybind.getKeyConflictContext());
    }

    public static ModelLayerLocation BELT_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("minecraft","player"), "toolbelt_belt");

    @EventBusSubscriber(modid = ToolBelt.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents
    {
        @SubscribeEvent
        public static void initKeybinds(RegisterKeyMappingsEvent event)
        {
            event.register(OPEN_TOOL_MENU_KEYBIND =
                    new KeyMapping("key.toolbelt.open", GLFW.GLFW_KEY_R, "key.toolbelt.category"));

            event.register(CYCLE_TOOL_MENU_LEFT_KEYBIND =
                    new KeyMapping("key.toolbelt.cycle.left", InputConstants.UNKNOWN.getValue(), "key.toolbelt.category"));

            event.register(CYCLE_TOOL_MENU_RIGHT_KEYBIND =
                    new KeyMapping("key.toolbelt.cycle.right", InputConstants.UNKNOWN.getValue(), "key.toolbelt.category"));

            event.register(OPEN_BELT_SLOT_KEYBIND =
                    new KeyMapping("key.toolbelt.slot", GLFW.GLFW_KEY_V, "key.toolbelt.category"));
        }

        @SubscribeEvent
        public static void registerAdditionalModels(ModelEvent.RegisterAdditional event)
        {
            var rm = Minecraft.getInstance().getResourceManager();

            event.register(ModelResourceLocation.standalone(ToolBelt.location("item/dyed_belt")));

            for (int i = 2; i <= 9; i++) {
                if (rm.getResource(ToolBelt.location("models/item/belt_" + i + ".json")).isPresent()
                        && rm.getResource(ToolBelt.location("textures/item/belt_" + i + ".png")).isPresent())
                    event.register(ModelResourceLocation.standalone(ToolBelt.location("item/belt_" + i)));

                if (rm.getResource(ToolBelt.location("models/item/belt_" + i + "_dyed.json")).isPresent()
                        && rm.getResource(ToolBelt.location("textures/item/belt_" + i + "_dyed.png")).isPresent())
                    event.register(ModelResourceLocation.standalone(ToolBelt.location("item/belt_" + i + "_dyed")));
            }
        }

        @SubscribeEvent
        public static void modifyBakingResult(ModelEvent.ModifyBakingResult event)
        {
            Map<ModelResourceLocation, BakedModel> models = event.getModels();

            ModelResourceLocation beltKey = ModelResourceLocation.inventory(ToolBelt.location("belt"));
            BakedModel baseModel = models.get(beltKey);
            if (baseModel == null) {
                return;
            }

            Map<Integer, BakedModel> tierModels = new HashMap<>();
            Map<Integer, BakedModel> tierDyedModels = new HashMap<>();
            for (int i = 2; i <= 9; i++) {
                BakedModel tier = models.get(ModelResourceLocation.standalone(ToolBelt.location("item/belt_" + i)));
                if (tier != null) tierModels.put(i, tier);
                BakedModel dyedTier = models.get(ModelResourceLocation.standalone(ToolBelt.location("item/belt_" + i + "_dyed")));
                if (dyedTier != null) tierDyedModels.put(i, dyedTier);
            }

            BakedModel dyedBase = models.get(ModelResourceLocation.standalone(ToolBelt.location("item/dyed_belt")));

            models.put(beltKey, new DynamicBeltModel(baseModel, tierModels, tierDyedModels, dyedBase));
        }

        @SubscribeEvent
        public static void colors(RegisterColorHandlersEvent.Item event)
        {
            event.register(
                    (ItemStack pStack, int pTintIndex) ->
                            pTintIndex == 0 && pStack.has(DataComponents.DYED_COLOR)
                                    ? (0xFF000000 | pStack.get(DataComponents.DYED_COLOR).rgb()) : -1,
                    ToolBelt.BELT.get()
            );
        }

        @SubscribeEvent
        public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event)
        {
            event.registerLayerDefinition(BELT_LAYER, ToolBeltLayer.BeltModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void construct(EntityRenderersEvent.AddLayers event)
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
        private static <E extends Player, M extends HumanoidModel<E>>
        void addLayerToPlayerSkin(EntityRenderersEvent.AddLayers event, PlayerSkin.Model skinName, Function<LivingEntityRenderer<E, M>, ? extends RenderLayer<E, M>> factory)
        {
            EntityRenderer renderer = event.getSkin(skinName);
            if (renderer instanceof LivingEntityRenderer ler) ler.addLayer(factory.apply(ler));
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static <E extends LivingEntity, M extends HumanoidModel<E>>
        void addLayerToHumanoid(EntityRenderersEvent.AddLayers event, EntityType<E> entityType, Function<LivingEntityRenderer<E, M>, ? extends RenderLayer<E, M>> factory)
        {
            EntityRenderer<E> renderer = event.getRenderer(entityType);
            if (renderer instanceof LivingEntityRenderer ler) ler.addLayer(factory.apply(ler));
        }

        private static <E extends LivingEntity, M extends EntityModel<E>>
        void addLayerToLiving(EntityRenderersEvent.AddLayers event, EntityType<E> entityType, Function<LivingEntityRenderer<E, M>, ? extends RenderLayer<E, M>> factory)
        {
            LivingEntityRenderer<E, M> renderer = event.getRenderer(entityType);
            if (renderer != null) renderer.addLayer(factory.apply(renderer));
        }
    }
}
