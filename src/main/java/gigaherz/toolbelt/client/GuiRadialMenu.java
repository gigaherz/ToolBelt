package gigaherz.toolbelt.client;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.google.common.collect.Lists;
import gigaherz.toolbelt.belt.ItemToolBelt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber(Side.CLIENT)
public class GuiRadialMenu extends GuiScreen
{
    @CapabilityInject(IItemHandler.class)
    public static Capability<IItemHandler> CAP;

    public static final GuiScreen instance = new GuiRadialMenu();

    @SubscribeEvent
    public static void overlayEvent(RenderGameOverlayEvent.Pre event)
    {
        if (event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS)
            return;

        if (Minecraft.getMinecraft().currentScreen == instance)
        {
            event.setCanceled(true);
        }
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        if (!Keyboard.isKeyDown(ClientProxy.keyOpenToolMenu.getKeyCode()))
            Minecraft.getMinecraft().displayGuiScreen(null);
    }

    private static final float PRECISION = 5;
    public void drawPieArc(float x, float y, float z, float radiusIn, float radiusOut, float startAngle, float endAngle, int r, int g, int b, int a)
    {
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        float angle = endAngle - startAngle;
        int sections = Math.max(1, MathHelper.ceil(angle/PRECISION));

        startAngle = (float)Math.toRadians(startAngle);
        endAngle = (float)Math.toRadians(endAngle);
        angle = endAngle - startAngle;

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (int i=0;i<sections;i++)
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
        tessellator.draw();
        GlStateManager.enableTexture2D();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);

        ItemStack stack = findStack(mc.player);
        if (stack == null)
            return;

        IItemHandler cap = stack.getCapability(CAP, null);

        List<ItemStack> items = Lists.newArrayList();
        for (int i=0;i<cap.getSlots();i++)
        {
            ItemStack inSlot = cap.getStackInSlot(i);
            if (inSlot != null && inSlot.stackSize > 0)
                items.add(inSlot);
        }
        int numItems = items.size();
        if (numItems <= 0)
        {
            drawCenteredString(fontRendererObj, "EMPTY", width/2, (height-fontRendererObj.FONT_HEIGHT)/2, 0xFFFFFFFF);
            return;
        }

        drawCenteredString(fontRendererObj, "SWAP", width/2, (height-fontRendererObj.FONT_HEIGHT)/2, 0xFFFFFFFF);

        int x = width / 2;
        int y = height / 2;

        double a = Math.toDegrees(Math.atan2(mouseY-y,mouseX-x));
        double d = Math.sqrt(Math.pow(mouseX-x,2)+Math.pow(mouseY-y,2));
        float s0 = (((0-0.5f)/(float)numItems) + 0.25f) * 360;
        if (a < s0) a += 360;

        for(int i=0;i<numItems;i++)
        {
            float s = (((i-0.5f)/(float)numItems) + 0.25f) * 360;
            float e = (((i+0.5f)/(float)numItems) + 0.25f) * 360;
            if (a >= s && a < e && d >= 30 && d < 60)
                drawPieArc(x, y, zLevel, 30, 60, s, e, 255,255, 255,64);
            else
                drawPieArc(x, y, zLevel, 30, 60, s, e, 0,0, 0,64);
        }

        RenderHelper.enableGUIStandardItemLighting();
        for(int i=0;i<numItems;i++)
        {
            float angle1 = ((i/(float)numItems) + 0.25f) * 2 * (float)Math.PI;
            float posX = x - 8 + 45 * (float) Math.cos(angle1);
            float posY = y - 8 + 45 * (float) Math.sin(angle1);
            ItemStack inSlot = items.get(i);
            mc.getRenderItem().renderItemAndEffectIntoGUI(inSlot, (int)posX, (int)posY);
        }
        RenderHelper.disableStandardItemLighting();
    }

    @Nullable
    private ItemStack findStack(EntityPlayerSP player)
    {
        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        for(int i=0;i<baubles.getSlots();i++)
        {
            ItemStack inSlot = baubles.getStackInSlot(i);
            if (inSlot != null && inSlot.stackSize > 0)
            {
                if (inSlot.getItem() instanceof ItemToolBelt)
                {
                    return inSlot;
                }
            }
        }

        IInventory playerInv = player.inventory;
        for(int i=0;i<playerInv.getSizeInventory();i++)
        {
            ItemStack inSlot = playerInv.getStackInSlot(i);
            if (inSlot != null && inSlot.stackSize > 0)
            {
                if (inSlot.getItem() instanceof ItemToolBelt)
                {
                    return inSlot;
                }
            }
        }

        return null;
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }
}
