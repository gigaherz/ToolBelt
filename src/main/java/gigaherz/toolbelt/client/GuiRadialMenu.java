package gigaherz.toolbelt.client;

import com.google.common.collect.Lists;
import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.Config;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.client.radial.GenericRadialMenu;
import gigaherz.toolbelt.client.radial.ItemStackRadialMenuItem;
import gigaherz.toolbelt.client.radial.RadialMenuItem;
import gigaherz.toolbelt.client.radial.TextRadialMenuItem;
import gigaherz.toolbelt.network.SwapItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class GuiRadialMenu extends GuiScreen
{
    private final BeltFinder.BeltGetter getter;
    private ItemStack stackEquipped;
    private IItemHandler inventory;

    private boolean closing;
    private boolean doneClosing;
    private double startAnimation;

    private boolean keyCycleBeforeL = false;
    private boolean keyCycleBeforeR = false;

    private boolean needsRecheckStacks = true;
    private final List<ItemStackRadialMenuItem> cachedMenuItems = Lists.newArrayList();
    private final TextRadialMenuItem insertMenuItem = new TextRadialMenuItem(new TextComponentTranslation("text.toolbelt.radial_menu.insert"));
    private final GenericRadialMenu menu = new GenericRadialMenu();

    GuiRadialMenu(BeltFinder.BeltGetter getter)
    {
        this.getter = getter;
        this.stackEquipped = getter.getBelt();
        inventory = stackEquipped.getCount() > 0 ? stackEquipped.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(null) : null;
        startAnimation = Minecraft.getInstance().world.getGameTime() + (double) Minecraft.getInstance().getRenderPartialTicks();
    }

    @SubscribeEvent
    public static void overlayEvent(RenderGameOverlayEvent.Pre event)
    {
        if (event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS)
            return;

        if (Minecraft.getInstance().currentScreen instanceof GuiRadialMenu)
        {
            event.setCanceled(true);
        }
    }

    @Override
    public void tick()
    {
        super.tick();

        if (closing)
        {
            if (doneClosing || inventory == null)
            {
                Minecraft.getInstance().displayGuiScreen(null);

                ClientProxy.wipeOpen();
            }

            return;
        }

        ItemStack inHand = mc.player.getHeldItemMainhand();
        if (!Config.isItemStackAllowed(inHand))
        {
            inventory = null;
        }
        else
        {
            ItemStack stack = getter.getBelt();
            if (stack.getCount() <= 0)
            {
                inventory = null;
                stackEquipped = null;
            }
            // Reference comparison intended
            else if (stackEquipped != stack)
            {
                stackEquipped = stack;
                inventory = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(() -> new RuntimeException("No inventory?"));
                needsRecheckStacks = true;
            }
        }

        if (inventory == null)
        {
            Minecraft.getInstance().displayGuiScreen(null);
        }
        else if (!InputMappings.isKeyDown(ClientProxy.keyOpenToolMenu.getKey().getKeyCode()))
        {
            if (Config.releaseToSwap)
            {
                processClick(false);
            }
            else
            {
                animateClose();
            }
        }
    }

    @Override
    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_)
    {
        processClick(true);
        return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
    }

    protected void processClick(boolean triggeredByMouse)
    {
        if (closing)
            return;

        if (inventory == null)
            return;

        ItemStack inHand = mc.player.getHeldItemMainhand();
        if (!Config.isItemStackAllowed(inHand))
            return;

        if (menu.hovered >= 0)
        {
            RadialMenuItem item = menu.items.get(menu.hovered);

            if (item instanceof ItemStackRadialMenuItem)
            {
                ItemStackRadialMenuItem stackItem = ((ItemStackRadialMenuItem) item);
                int slotNumber = stackItem.getSlot();
                ItemStack itemMouseOver = stackItem.getStack();

                if (inHand.getCount() <= 0 && itemMouseOver.getCount() <= 0)
                {
                    if (triggeredByMouse)
                    {
                        // ignore click
                        return;
                    }
                }
                else
                {
                    SwapItems.swapItem(slotNumber, mc.player);
                    ToolBelt.channel.sendToServer(new SwapItems(slotNumber));
                }
            }

        }

        animateClose();
    }

    private void animateClose()
    {
        closing = true;
        doneClosing = false;
        startAnimation = Minecraft.getInstance().world.getGameTime() + (double) Minecraft.getInstance().getRenderPartialTicks();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        super.render(mouseX, mouseY, partialTicks);

        if (inventory == null)
            return;

        ItemStack inHand = mc.player.getHeldItemMainhand();
        if (!Config.isItemStackAllowed(inHand))
            return;

        if (needsRecheckStacks)
        {

            cachedMenuItems.clear();
            for (int i = 0; i < inventory.getSlots(); i++)
            {
                ItemStack inSlot = inventory.getStackInSlot(i);
                ItemStackRadialMenuItem item = new ItemStackRadialMenuItem(i, inSlot, new TextComponentTranslation("text.toolbelt.empty"));
                item.setVisible(inSlot.getCount() > 0 || Config.displayEmptySlots);
                if (inHand.getCount() > 0)
                {
                    if (inSlot.getCount() > 0)
                        item.setCentralText(new TextComponentTranslation("text.toolbelt.swap"));
                    else
                        item.setCentralText(new TextComponentTranslation("text.toolbelt.insert"));
                }
                else
                {
                    if (inSlot.getCount() > 0)
                        item.setCentralText(new TextComponentTranslation("text.toolbelt.extract"));
                    else
                        item.setCentralText(new TextComponentTranslation("text.toolbelt.empty"));
                }
                cachedMenuItems.add(item);
            }

            menu.items.clear();
            menu.items.addAll(cachedMenuItems);
            menu.items.add(insertMenuItem);

            needsRecheckStacks = false;
        }

        boolean hasAddButton = false;
        if (!Config.displayEmptySlots && (cachedMenuItems.stream().allMatch(RadialMenuItem::isVisible) && inHand.getCount() > 0))
        {
            hasAddButton = true;
        }
        insertMenuItem.setVisible(hasAddButton);

        if (cachedMenuItems.stream().noneMatch(RadialMenuItem::isVisible))
        {
            menu.setCentralText(new TextComponentTranslation("text.toolbelt.empty"));
            if (closing)
                doneClosing = true;
            return;
        }

        final float OPEN_ANIMATION_LENGTH = 2.5f;

        menu.openAnimation = closing
                ? (float) (1 - ((Minecraft.getInstance().world.getGameTime() + partialTicks - startAnimation) / OPEN_ANIMATION_LENGTH))
                : (float) ((Minecraft.getInstance().world.getGameTime() + partialTicks - startAnimation) / OPEN_ANIMATION_LENGTH);

        if (closing && menu.openAnimation <= 0)
            doneClosing = true;

        menu.draw(width, height, fontRenderer, itemRender);

        if (!closing)
        {
            checkCycleKeybinds();
        }

        checkMouseOver(inHand, mouseX, mouseY);

        menu.drawTooltips(mouseX, mouseY, width, height, fontRenderer, itemRender);

        if (Config.clipMouseToCircle)
        {
            double[] xPos = new double[1];
            double[] yPos = new double[1];
            GLFW.glfwGetCursorPos(mc.mainWindow.getHandle(), xPos, yPos);
            double scaledX = xPos[0] - (mc.mainWindow.getWidth() / 2.0f);
            double scaledY = yPos[0] - (mc.mainWindow.getHeight() / 2.0f);

            double distance = Math.sqrt(scaledX * scaledX + scaledY * scaledY);
            double radius = 60.0 * (mc.mainWindow.getWidth() / (float)width);

            if (distance > radius)
            {
                double fixedX = scaledX * radius / distance;
                double fixedY = scaledY * radius / distance;

                GLFW.glfwSetCursorPos(mc.mainWindow.getHandle(), (int) (mc.mainWindow.getWidth() / 2 + fixedX), (int) (mc.mainWindow.getHeight() / 2 + fixedY));
            }
        }
    }

    private void checkMouseOver(ItemStack inHand, int mouseX, int mouseY)
    {
        float radiusIn = menu.radiusIn;
        float radiusOut = menu.radiusOut;

        int numItems = (int) menu.items.stream().filter(RadialMenuItem::isVisible).count();

        int x = width / 2;
        int y = height / 2;

        double a = Math.toDegrees(Math.atan2(mouseY - y, mouseX - x));
        double d = Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2));
        float s0 = (((0 - 0.5f) / (float) numItems) + 0.25f) * 360;
        if (a < s0) a += 360;

        if (!closing)
        {
            menu.hovered = -1;
            for (int i = 0; i < numItems; i++)
            {
                float s = (((i - 0.5f) / (float) numItems) + 0.25f) * 360;
                float e = (((i + 0.5f) / (float) numItems) + 0.25f) * 360;
                if (a >= s && a < e && d >= radiusIn && d < radiusOut)
                    if (a >= s && a < e && d >= radiusIn && (d < radiusOut || Config.clipMouseToCircle || Config.allowClickOutsideBounds))
                    {
                        menu.hovered = i;
                        break;
                    }
            }
        }

        /*
        for (int i = 0; i < numItems; i++)
        {
            if (selectedItem == i)
            {
                if (i > 0 || !hasAddButton)
                {
                    hasMouseOver = true;

                    ItemStack inSlot = ItemStack.EMPTY;
                    if (hasAddButton)
                    {
                        if (i > 0)
                        {
                            inSlot = items.get(i - 1);
                        }
                    }
                    else
                    {
                        inSlot = items.get(i);
                    }

                    itemMouseOver = inSlot;
                }
            }
        }
        */

    }

    private void checkCycleKeybinds()
    {
        float itemRadius = menu.itemRadius;

        int x = width / 2;
        int y = height / 2;

        int numItems = (int) menu.items.stream().filter(RadialMenuItem::isVisible).count();

        if (InputMappings.isKeyDown(ClientProxy.keyCycleToolMenuL.getKey().getKeyCode()))
        {
            if (!keyCycleBeforeL)
            {
                menu.hovered--;
                if (menu.hovered < 0)
                    menu.hovered = numItems - 1;
                setMousePosition(
                        x + itemRadius * Math.cos(-0.5 * Math.PI - menu.hovered * 2 * Math.PI / numItems),
                        y + itemRadius * Math.sin(-0.5 * Math.PI + menu.hovered * 2 * Math.PI / numItems)
                );
            }
            keyCycleBeforeL = true;
        }
        else
        {
            keyCycleBeforeL = false;
        }

        if (InputMappings.isKeyDown(ClientProxy.keyCycleToolMenuR.getKey().getKeyCode()))
        {
            if (!keyCycleBeforeR)
            {
                if (menu.hovered < 0)
                    menu.hovered = 0;
                else
                {
                    menu.hovered++;
                    if (menu.hovered >= numItems)
                        menu.hovered = 0;
                }
                setMousePosition(
                        x + itemRadius * Math.cos(-0.5 * Math.PI - menu.hovered * 2 * Math.PI / numItems),
                        y + itemRadius * Math.sin(-0.5 * Math.PI + menu.hovered * 2 * Math.PI / numItems)
                );
            }
            keyCycleBeforeR = true;
        }
        else
        {
            keyCycleBeforeR = false;
        }
    }

    private void setMousePosition(double x, double y)
    {
        GLFW.glfwSetCursorPos(mc.mainWindow.getHandle(), (int) (x * mc.mainWindow.getWidth() / width), (int) (y * mc.mainWindow.getHeight() / height));
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }
}
