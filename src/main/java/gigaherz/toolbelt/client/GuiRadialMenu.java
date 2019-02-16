package gigaherz.toolbelt.client;

import com.google.common.collect.Lists;
import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.Config;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.network.SwapItems;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import javax.swing.*;
import javax.vecmath.Vector4f;
import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class GuiRadialMenu extends GuiScreen
{
    private final BeltFinder.BeltGetter getter;
    private ItemStack stackEquipped;
    private IItemHandler inventory;
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
                cachedStacks = null;
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

        List<Integer> items = Lists.newArrayList();
        for (int i = 0; i < inventory.getSlots(); i++)
        {
            ItemStack inSlot = inventory.getStackInSlot(i);
            if (inSlot.getCount() > 0 || Config.displayEmptySlots)
                items.add(i);
        }

        boolean hasAddButton = false;
        int numItems = items.size();
        if (!Config.displayEmptySlots && (numItems < inventory.getSlots() && inHand.getCount() > 0))
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

            if (inHand.getCount() <= 0 && inventory.getStackInSlot(swapWith).getCount() <= 0)
            {
                if (triggeredByMouse)
                {
                    // ignore click
                    return;
                }
            }
            else
            {
                SwapItems.swapItem(swapWith, mc.player);
                ToolBelt.channel.sendToServer(new SwapItems(swapWith));
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

        List<ItemStack> items = cachedStacks;
        if (items == null)
        {
            items = Lists.newArrayList();
            for (int i = 0; i < inventory.getSlots(); i++)
            {
                ItemStack inSlot = inventory.getStackInSlot(i);
                if (inSlot.getCount() > 0 || Config.displayEmptySlots)
                    items.add(inSlot);
            }
            cachedStacks = items;
        }

        ItemStack inHand = mc.player.getHeldItemMainhand();
        if (!Config.isItemStackAllowed(inHand))
            return;

        boolean hasAddButton = false;
        int numItems = items.size();
        if (!Config.displayEmptySlots && (numItems < inventory.getSlots() && inHand.getCount() > 0))
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
                ? (float) (1 - ((Minecraft.getInstance().world.getGameTime() + partialTicks - startAnimation) / OPEN_ANIMATION_LENGTH))
                : (float) ((Minecraft.getInstance().world.getGameTime() + partialTicks - startAnimation) / OPEN_ANIMATION_LENGTH);

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
            if (InputMappings.isKeyDown(ClientProxy.keyCycleToolMenuL.getKey().getKeyCode()))
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

            if (InputMappings.isKeyDown(ClientProxy.keyCycleToolMenuR.getKey().getKeyCode()))
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

        boolean hasMouseOver = false;
        ItemStack itemMouseOver = ItemStack.EMPTY;

        if (!closing)
        {
            selectedItem = -1;
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

        if (Config.clipMouseToCircle)
        {
            double[] xPos = new double[1];
            double[] yPos = new double[1];
            GLFW.glfwGetCursorPos(mc.mainWindow.getHandle(), xPos, yPos);
            double scaledX = xPos[0] - (mc.mainWindow.getWidth() / 2.0f);
            double scaledY = yPos[0] - (mc.mainWindow.getHeight() / 2.0f);

            double distance = Math.sqrt(scaledX * scaledX + scaledY * scaledY);
            double radius = 60.0 * (mc.mainWindow.getWidth() / width);

            if (distance > radius)
            {
                double fixedX = scaledX * radius / distance;
                double fixedY = scaledY * radius / distance;

                GLFW.glfwSetCursorPos(mc.mainWindow.getHandle(), (int) (mc.mainWindow.getWidth() / 2 + fixedX), (int) (mc.mainWindow.getHeight() / 2 + fixedY));
            }
        }

        boolean hasItemInHand = inHand.getCount() > 0;
        if (hasMouseOver)
        {
            if (hasItemInHand)
            {
                if (itemMouseOver.getCount() > 0)
                    drawCenteredString(fontRenderer, I18n.format("text.toolbelt.swap"), width / 2, (height - fontRenderer.FONT_HEIGHT) / 2, 0xFFFFFFFF);
                else
                    drawCenteredString(fontRenderer, I18n.format("text.toolbelt.insert"), width / 2, (height - fontRenderer.FONT_HEIGHT) / 2, 0xFFFFFFFF);
            }
            else if (itemMouseOver.getCount() > 0)
                drawCenteredString(fontRenderer, I18n.format("text.toolbelt.extract"), width / 2, (height - fontRenderer.FONT_HEIGHT) / 2, 0xFFFFFFFF);
        }

        if (Config.displayEmptySlots)
        {
            for (int i = 0; i < inventory.getSlots(); i++)
            {
                ItemStack inSlot = inventory.getStackInSlot(i);
                if (inSlot.getCount() <= 0)
                {
                    float angle1 = ((i / (float) numItems) + 0.25f) * 2 * (float) Math.PI;
                    float posX = x + itemRadius * (float) Math.cos(angle1);
                    float posY = y + itemRadius * (float) Math.sin(angle1);
                    drawCenteredString(fontRenderer, I18n.format("text.toolbelt.empty"), (int)posX, (int)posY - fontRenderer.FONT_HEIGHT / 2, 0x7FFFFFFF);
                }
            }
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
        GLFW.glfwSetCursorPos(mc.mainWindow.getHandle(), (int) (x * mc.mainWindow.getWidth() / width), (int) (y * mc.mainWindow.getHeight() / height));
    }

    private static final float PRECISION = 5;

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    private static class GenericRadialMenu
    {
        public interface IRadialMenuItem
        {
            void draw(float x, float y, float z, boolean hover);
        }

        final List<IRadialMenuItem> items = Lists.newArrayList();
        int hovered = -1;
        float openAnimation = 0;
        Vector4f backgroundColor = new Vector4f(0,0,0,.25f);
        Vector4f backgroundColorHover = new Vector4f(1,1,1,.25f);

        private void draw(int width, int height)
        {

            float animProgress = MathHelper.clamp(openAnimation, 0, 1);
            float radiusIn = Math.max(0.1f, 30 * animProgress);
            float radiusOut = radiusIn * 2;
            float itemRadius = (radiusIn + radiusOut) * 0.5f;
            float animTop = (1 - animProgress) * height / 2.0f;

            int x = width / 2;
            int y = height / 2;
            float z = 0;

            GlStateManager.pushMatrix();
            GlStateManager.translatef(0, animTop, 0);

            drawBackground(x,y,z, radiusIn, radiusOut);

            GlStateManager.popMatrix();

            drawItems(x,y,z, itemRadius);
        }

        private void drawItems(int x, int y, float z, float itemRadius)
        {
            int numItems = items.size();
            for(int i=0;i<numItems;i++)
            {
                float s = (((i - 0.5f) / (float) numItems) + 0.25f) * 360;
                float e = (((i + 0.5f) / (float) numItems) + 0.25f) * 360;
                float middle = (s+e)*0.5f;

                float posX = x + itemRadius * (float) Math.cos(middle);
                float posY = y + itemRadius * (float) Math.sin(middle);

                items.get(i).draw(posX, posY, z, i == hovered);
            }
        }

        private void drawBackground(float x, float y, float z, float radiusIn, float radiusOut)
        {
            GlStateManager.disableAlphaTest();
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            int numItems = items.size();
            for(int i=0;i<numItems;i++)
            {
                float s = (((i - 0.5f) / (float) numItems) + 0.25f) * 360;
                float e = (((i + 0.5f) / (float) numItems) + 0.25f) * 360;
                Vector4f color = i == hovered ? backgroundColorHover : backgroundColor;
                drawPieArc(buffer, x, y, z, radiusIn, radiusOut, s, e, (int)(color.x*255), (int)(color.y*255), (int)(color.z*255), (int)(color.w*255));
            }
            tessellator.draw();

            GlStateManager.enableTexture2D();
        }

        private void drawPieArc(BufferBuilder buffer, float x, float y, float z, float radiusIn, float radiusOut, float startAngle, float endAngle, int r, int g, int b, int a)
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

    }
}
