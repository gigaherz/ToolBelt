package dev.gigaherz.toolbelt.client;

import com.google.common.collect.Lists;
import dev.gigaherz.toolbelt.BeltFinder;
import dev.gigaherz.toolbelt.ConfigData;
import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import dev.gigaherz.toolbelt.client.radial.*;
import dev.gigaherz.toolbelt.common.ItemContainerWrapper;
import dev.gigaherz.toolbelt.network.SwapItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RadialMenuScreen extends Screen
{
    @EventBusSubscriber(value=Dist.CLIENT, modid= ToolBelt.MODID)
    public static class Client
    {

        @SubscribeEvent
        public static void overlayEvent(RenderGuiLayerEvent.Pre event)
        {
            if (!event.getName().equals(VanillaGuiLayers.CROSSHAIR))
                return;

            if (Minecraft.getInstance().screen instanceof RadialMenuScreen)
            {
                event.setCanceled(true);
            }
        }
    }

    private final BeltFinder.BeltGetter getter;
    private ItemStack stackEquipped;
    private int inventorySize;

    private boolean keyCycleBeforeL = false;
    private boolean keyCycleBeforeR = false;

    private boolean needsRecheckStacks = true;
    private final List<ItemStackRadialMenuItem> cachedMenuItems = Lists.newArrayList();
    private final TextRadialMenuItem insertMenuItem;
    private final GenericRadialMenu menu;

    public RadialMenuScreen(BeltFinder.BeltGetter getter)
    {
        super(Component.literal("RADIAL MENU"));

        this.getter = getter;
        this.stackEquipped = getter.getBelt();

        //inventory = stackEquipped.isEmpty() ? null : stackEquipped.get(DataComponents.CONTAINER);
        inventorySize = ToolBeltItem.getBeltSize(stackEquipped);
        menu = new GenericRadialMenu(Minecraft.getInstance(), new IRadialMenuHost()
        {
            @Override
            public void renderTooltip(GuiGraphics graphics, ItemStack stack, int mouseX, int mouseY)
            {
                graphics.setTooltipForNextFrame(font, stack, mouseX, mouseY);
            }

            @Override
            public Screen getScreen()
            {
                return RadialMenuScreen.this;
            }

            @Override
            public Font getFontRenderer()
            {
                return font;
            }
        })
        {
            @Override
            public void onClickOutside()
            {
                close();
            }
        };
        insertMenuItem = new TextRadialMenuItem(menu, Component.translatable("text.toolbelt.insert"))
        {
            @Override
            public boolean onClick()
            {
                return RadialMenuScreen.this.trySwap(-1, ItemStack.EMPTY);
            }
        };
    }

    @Override // removed
    public void removed()
    {
        super.removed();
        ToolBeltClient.wipeOpen();
    }

    @Override
    public void onClose()
    {
        ToolBeltClient.wipeOpen();
        super.onClose();
    }

    @Override // tick
    public void tick()
    {
        super.tick();

        menu.tick();

        if (menu.isClosed())
        {
            this.onClose();
        }
        if (!menu.isReady())
        {
            return;
        }

        ItemStack inHand = minecraft.player.getMainHandItem();
        if (!ConfigData.isItemStackAllowed(inHand))
        {
            stackEquipped = null;
        }
        else
        {
            ItemStack stack = getter.getBelt();
            if (stack.isEmpty())
            {
                stackEquipped = null;
            }
            // Reference comparison intended
            else if (stackEquipped != stack)
            {
                stackEquipped = stack;
                inventorySize = ToolBeltItem.getBeltSize(stackEquipped);
                needsRecheckStacks = true;
            }
        }

        if (stackEquipped == null)
        {
            menu.close();
        }
        else if (ToolBeltClient.OPEN_TOOL_MENU_KEYBIND != null && !ToolBeltClient.isKeyDown(ToolBeltClient.OPEN_TOOL_MENU_KEYBIND))
        {
            if (ConfigData.releaseToSwap)
            {
                processClick(false);
            }
            else
            {
                menu.close();
            }
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent p_446955_)
    {
        processClick(true);
        return super.mouseReleased(p_446955_);
    }

    protected void processClick(boolean triggeredByMouse)
    {
        menu.clickItem();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        var poseStack = graphics.pose();
        poseStack.pushMatrix();
        super.render(graphics, mouseX, mouseY, partialTicks);
        poseStack.popMatrix();

        ItemStack inHand = minecraft.player.getMainHandItem();
        if (ConfigData.isItemStackAllowed(inHand) && menu.isReady())
        {
            if (needsRecheckStacks)
            {
                var inv0 = stackEquipped.isEmpty() ? null : stackEquipped.get(DataComponents.CONTAINER);
                var inventory = new ItemContainerWrapper(inv0, inventorySize, stackEquipped, getter);

                cachedMenuItems.clear();
                for (int i = 0; i < inventorySize; i++)
                {
                    ItemStack inSlot = i < inventory.getContainerSize() ? inventory.getItem(i) : ItemStack.EMPTY;
                    ItemStackRadialMenuItem item = getMenuItemForStack(i, inSlot, inHand);
                    cachedMenuItems.add(item);
                }

                menu.clear();
                menu.addAll(cachedMenuItems);
                menu.add(insertMenuItem);

                needsRecheckStacks = false;
            }

            boolean hasAddButton = false;
            if (!ConfigData.displayEmptySlots && !cachedMenuItems.stream().allMatch(RadialMenuItem::isVisible) && inHand.getCount() > 0)
            {
                hasAddButton = true;
            }
            insertMenuItem.setVisible(hasAddButton);

            if (cachedMenuItems.stream().noneMatch(RadialMenuItem::isVisible))
            {
                menu.setCentralText(Component.translatable("text.toolbelt.empty"));
            }
            else
            {
                menu.setCentralText(null);
            }

            checkCycleKeybinds();
        }

        menu.draw(graphics, partialTicks, mouseX, mouseY);
    }

    private @NotNull ItemStackRadialMenuItem getMenuItemForStack(int i, ItemStack inSlot, ItemStack inHand)
    {
        ItemStackRadialMenuItem item = new ItemStackRadialMenuItem(menu, i, inSlot, Component.translatable("text.toolbelt.empty"))
        {
            @Override
            public boolean onClick()
            {
                return RadialMenuScreen.this.trySwap(getSlot(), getStack());
            }
        };
        item.setVisible(inSlot.getCount() > 0 || ConfigData.displayEmptySlots);
        if (inHand.getCount() > 0)
        {
            if (inSlot.getCount() > 0)
                item.setCentralText(Component.translatable("text.toolbelt.swap"));
            else
                item.setCentralText(Component.translatable("text.toolbelt.insert"));
        }
        else
        {
            if (inSlot.getCount() > 0)
                item.setCentralText(Component.translatable("text.toolbelt.extract"));
            else
                item.setCentralText(Component.translatable("text.toolbelt.empty"));
        }
        return item;
    }

    private boolean trySwap(int slotNumber, ItemStack itemMouseOver)
    {
        ItemStack inHand = minecraft.player.getMainHandItem();
        if (!ConfigData.isItemStackAllowed(inHand) || !menu.isReady())
            return false;

        if (inHand.getCount() > 0 || itemMouseOver.getCount() > 0)
        {
            SwapItems.swapItem(slotNumber, minecraft.player);
            ClientPacketDistributor.sendToServer(new SwapItems(slotNumber));
        }

        menu.close();
        return true;
    }

    private void checkCycleKeybinds()
    {
        if (ToolBeltClient.CYCLE_TOOL_MENU_LEFT_KEYBIND != null && ToolBeltClient.isKeyDown(ToolBeltClient.CYCLE_TOOL_MENU_LEFT_KEYBIND))
        {
            if (!keyCycleBeforeL)
            {
                menu.cyclePrevious();
            }
            keyCycleBeforeL = true;
        }
        else
        {
            keyCycleBeforeL = false;
        }

        if (ToolBeltClient.CYCLE_TOOL_MENU_RIGHT_KEYBIND != null && ToolBeltClient.isKeyDown(ToolBeltClient.CYCLE_TOOL_MENU_RIGHT_KEYBIND))
        {
            if (!keyCycleBeforeR)
            {
                menu.cycleNext();
            }
            keyCycleBeforeR = true;
        }
        else
        {
            keyCycleBeforeR = false;
        }
    }

    @Override // isPauseScreen
    public boolean isPauseScreen()
    {
        return false;
    }
}
