package gigaherz.toolbelt.client;

import com.google.common.collect.Lists;
import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.Config;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.belt.ItemToolBelt;
import gigaherz.toolbelt.belt.ToolBeltInventory;
import gigaherz.toolbelt.network.SwapItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.List;

@Mod.EventBusSubscriber(Side.CLIENT)
public class GuiRadialMenu extends GuiScreen
{
    private final BeltFinder.BeltGetter getter;
    private ItemStack stackEquipped;
    private ToolBeltInventory inventory;
    private List<ItemStack> cachedStacks = null;

    private boolean closing;
    private boolean doneClosing;
    private double startAnimation;

    private int selectedItem = -1;
    private boolean keyCycleBeforeL = false;
    private boolean keyCycleBeforeR = false;

    GuiRadialMenu(BeltFinder.BeltGetter getter)
    {
        this.getter = getter;
        this.stackEquipped = getter.getBelt();
        inventory = stackEquipped.getCount() > 0 ? ItemToolBelt.getItems(stackEquipped) : null;
        startAnimation = Minecraft.getMinecraft().world.getTotalWorldTime() + (double) Minecraft.getMinecraft().getRenderPartialTicks();
    }

    @SubscribeEvent
    public static void overlayEvent(RenderGameOverlayEvent.Pre event)
    {
        if (event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS)
            return;

        if (Minecraft.getMinecraft().currentScreen instanceof GuiRadialMenu)
        {
            event.setCanceled(true);
        }
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        if (closing)
        {
            if (doneClosing || inventory == null)
            {
                Minecraft.getMinecraft().displayGuiScreen(null);

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
                inventory = ItemToolBelt.getItems(stack);
                cachedStacks = null;
            }
        }

        if (inventory == null)
        {
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
        else if (!GameSettings.isKeyDown(ClientProxy.keyOpenToolMenu))
        {
            if (Config.releaseToSwap)
            {
                int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
                int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
                mouseReleased(x, y, -1);
            }
            else
            {
                animateClose();
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);

        if (closing)
            return;

        if (inventory == null)
            return;

        ItemStack inHand = mc.player.getHeldItemMainhand();
        if (!Config.isItemStackAllowed(inHand))
            return;

        List<Integer> items = Lists.newArrayList();
        for (int i = 0; i < inventory.getSlots(); i++)
        {
            ItemStack inSlot = inventory.getStackInSlot(i);
            if (inSlot.getCount() > 0)
                items.add(i);
        }

        boolean hasAddButton = false;
        int numItems = items.size();
        if (numItems < inventory.getSlots() && inHand.getCount() > 0)
        {
            hasAddButton = true;
            numItems++;
        }
        if (numItems <= 0)
            return;

        if (selectedItem >= 0)
        {
            int swapWith;
            if (hasAddButton)
                swapWith = selectedItem == 0 ? -1 : items.get(selectedItem - 1);
            else
                swapWith = items.get(selectedItem);
            SwapItems.swapItem(swapWith, mc.player);
            ToolBelt.channel.sendToServer(new SwapItems(swapWith));
        }

        animateClose();
    }

    private void animateClose()
    {
        closing = true;
        doneClosing = false;
        startAnimation = Minecraft.getMinecraft().world.getTotalWorldTime() + (double) Minecraft.getMinecraft().getRenderPartialTicks();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (inventory == null)
            return;

        List<ItemStack> items = cachedStacks;
        if (items == null)
        {
            items = Lists.newArrayList();
            for (int i = 0; i < inventory.getSlots(); i++)
            {
                ItemStack inSlot = inventory.getStackInSlot(i);
                if (inSlot.getCount() > 0)
                    items.add(inSlot);
            }
            cachedStacks = items;
        }

        ItemStack inHand = mc.player.getHeldItemMainhand();
        if (!Config.isItemStackAllowed(inHand))
            return;

        boolean hasAddButton = false;
        int numItems = items.size();
        if (numItems < inventory.getSlots() && inHand.getCount() > 0)
        {
            hasAddButton = true;
            numItems++;
        }
        if (numItems <= 0)
        {
            drawCenteredString(fontRenderer, I18n.format("text.toolbelt.empty"), width / 2, (height - fontRenderer.FONT_HEIGHT) / 2, 0xFFFFFFFF);
            if (closing)
                doneClosing = true;
            return;
        }

        if (hasAddButton)
        {
            drawCenteredString(fontRenderer, I18n.format("text.toolbelt.insert"), width / 2, height / 2 + 45 - fontRenderer.FONT_HEIGHT / 2, 0xFFFFFFFF);
        }

        final float OPEN_ANIMATION_LENGTH = 2.5f;

        float openAnimation = closing
                ? (float) (1 - ((Minecraft.getMinecraft().world.getTotalWorldTime() + partialTicks - startAnimation) / OPEN_ANIMATION_LENGTH))
                : (float) ((Minecraft.getMinecraft().world.getTotalWorldTime() + partialTicks - startAnimation) / OPEN_ANIMATION_LENGTH);

        if (closing && openAnimation <= 0)
            doneClosing = true;

        float animProgress = MathHelper.clamp(openAnimation, 0, 1);
        float radiusIn = Math.max(0.1f, 30 * animProgress);
        float radiusOut = radiusIn * 2;
        float itemRadius = (radiusIn + radiusOut) * 0.5f;
        float animTop = (1 - animProgress) * height / 2.0f;

        int x = width / 2;
        int y = height / 2;

        if (!closing)
        {
            if (GameSettings.isKeyDown(ClientProxy.keyCycleToolMenuL))
            {
                if (!keyCycleBeforeL)
                {
                    selectedItem--;
                    if (selectedItem < 0)
                        selectedItem = numItems - 1;
                    setMousePosition(
                            x + itemRadius * Math.cos(-0.5 * Math.PI - selectedItem * 2 * Math.PI / numItems),
                            y + itemRadius * Math.sin(-0.5 * Math.PI + selectedItem * 2 * Math.PI / numItems)
                    );
                }
                keyCycleBeforeL = true;
            }
            else
            {
                keyCycleBeforeL = false;
            }

            if (GameSettings.isKeyDown(ClientProxy.keyCycleToolMenuR))
            {
                if (!keyCycleBeforeR)
                {
                    if (selectedItem < 0)
                        selectedItem = 0;
                    else
                    {
                        selectedItem++;
                        if (selectedItem >= numItems)
                            selectedItem = 0;
                    }
                    setMousePosition(
                            x + itemRadius * Math.cos(-0.5 * Math.PI - selectedItem * 2 * Math.PI / numItems),
                            y + itemRadius * Math.sin(-0.5 * Math.PI + selectedItem * 2 * Math.PI / numItems)
                    );
                }
                keyCycleBeforeR = true;
            }
            else
            {
                keyCycleBeforeR = false;
            }
        }

        double a = Math.toDegrees(Math.atan2(mouseY - y, mouseX - x));
        double d = Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2));
        float s0 = (((0 - 0.5f) / (float) numItems) + 0.25f) * 360;
        if (a < s0) a += 360;

        GlStateManager.pushMatrix();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.translate(0, animTop, 0);

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        boolean hasMouseOver = false;
        ItemStack itemMouseOver = ItemStack.EMPTY;

        if (!closing)
        {
            for (int i = 0; i < numItems; i++)
            {
                float s = (((i - 0.5f) / (float) numItems) + 0.25f) * 360;
                float e = (((i + 0.5f) / (float) numItems) + 0.25f) * 360;
                if (a >= s && a < e && d >= radiusIn && d < radiusOut)
                    if (a >= s && a < e && d >= radiusIn && (d < radiusOut || Config.clipMouseToCircle || Config.allowClickOutsideBounds))
                    {
                        selectedItem = i;
                        break;
                    }
            }
        }

        for (int i = 0; i < numItems; i++)
        {
            float s = (((i - 0.5f) / (float) numItems) + 0.25f) * 360;
            float e = (((i + 0.5f) / (float) numItems) + 0.25f) * 360;
            if (selectedItem == i)
            {
                drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 255, 255, 255, 64);

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
            else
            {
                drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 0, 0, 0, 64);
            }
        }

        if (Config.clipMouseToCircle)
        {
            double scaledX = Mouse.getX() - (mc.displayWidth / 2.0f);
            double scaledY = Mouse.getY() - (mc.displayHeight / 2.0f);

            double distance = Math.sqrt(scaledX * scaledX + scaledY * scaledY);
            double radius = 60.0 * (mc.displayWidth / width);

            if (distance > radius)
            {
                double fixedX = scaledX * radius / distance;
                double fixedY = scaledY * radius / distance;

                Mouse.setCursorPosition((int) (mc.displayWidth / 2 + fixedX), (int) (mc.displayHeight / 2 + fixedY));
            }
        }

        tessellator.draw();
        GlStateManager.enableTexture2D();

        if (hasMouseOver)
        {
            if (inHand.getCount() > 0)
                drawCenteredString(fontRenderer, I18n.format("text.toolbelt.swap"), width / 2, (height - fontRenderer.FONT_HEIGHT) / 2, 0xFFFFFFFF);
            else
                drawCenteredString(fontRenderer, I18n.format("text.toolbelt.extract"), width / 2, (height - fontRenderer.FONT_HEIGHT) / 2, 0xFFFFFFFF);
        }


        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i < numItems; i++)
        {
            float angle1 = ((i / (float) numItems) + 0.25f) * 2 * (float) Math.PI;
            float posX = x - 8 + itemRadius * (float) Math.cos(angle1);
            float posY = y - 8 + itemRadius * (float) Math.sin(angle1);
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

            if (inSlot.getCount() > 0)
            {
                this.itemRender.renderItemAndEffectIntoGUI(inSlot, (int) posX, (int) posY);
                this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, inSlot, (int) posX, (int) posY, "");
            }
        }
        RenderHelper.disableStandardItemLighting();

        GlStateManager.popMatrix();

        if (itemMouseOver.getCount() > 0)
            renderToolTip(itemMouseOver, mouseX, mouseY);
    }

    private void setMousePosition(double x, double y)
    {
        Mouse.setCursorPosition((int) (x * mc.displayWidth / width), (int) (y * mc.displayHeight / height));
    }

    private static final float PRECISION = 5;

    private void drawPieArc(VertexBuffer buffer, float x, float y, float z, float radiusIn, float radiusOut, float startAngle, float endAngle, int r, int g, int b, int a)
    {
        float angle = endAngle - startAngle;
        int sections = Math.max(1, MathHelper.ceil(angle / PRECISION));

        startAngle = (float) Math.toRadians(startAngle);
        endAngle = (float) Math.toRadians(endAngle);
        angle = endAngle - startAngle;

        for (int i = 0; i < sections; i++)
        {
            float angle1 = startAngle + (i / (float) sections) * angle;
            float angle2 = startAngle + ((i + 1) / (float) sections) * angle;

            float pos1InX = x + radiusIn * (float) Math.cos(angle1);
            float pos1InY = y + radiusIn * (float) Math.sin(angle1);
            float pos1OutX = x + radiusOut * (float) Math.cos(angle1);
            float pos1OutY = y + radiusOut * (float) Math.sin(angle1);
            float pos2OutX = x + radiusOut * (float) Math.cos(angle2);
            float pos2OutY = y + radiusOut * (float) Math.sin(angle2);
            float pos2InX = x + radiusIn * (float) Math.cos(angle2);
            float pos2InY = y + radiusIn * (float) Math.sin(angle2);

            buffer.pos(pos1OutX, pos1OutY, z).color(r, g, b, a).endVertex();
            buffer.pos(pos1InX, pos1InY, z).color(r, g, b, a).endVertex();
            buffer.pos(pos2InX, pos2InY, z).color(r, g, b, a).endVertex();
            buffer.pos(pos2OutX, pos2OutY, z).color(r, g, b, a).endVertex();
        }
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }
}
