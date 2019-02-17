package gigaherz.toolbelt.client.radial;

import com.google.common.collect.Lists;
import gigaherz.toolbelt.client.GuiRadialMenu;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import javax.vecmath.Vector4f;
import java.util.List;

public class GenericRadialMenu
{

    public final List<RadialMenuItem> items = Lists.newArrayList();
    public int hovered = -1;
    public float openAnimation = 0;
    public Vector4f backgroundColor = new Vector4f(0,0,0,.25f);
    public Vector4f backgroundColorHover = new Vector4f(1,1,1,.25f);

    public float animProgress;
    public float radiusIn;
    public float radiusOut;
    public float itemRadius;
    public float animTop;

    private ITextComponent centralText;

    public void setCentralText(@Nullable ITextComponent centralText)
    {
        this.centralText = centralText;
    }

    public ITextComponent getCentralText()
    {
        return centralText;
    }

    public void draw(int width, int height, FontRenderer font, ItemRenderer itemRenderer)
    {
        animProgress = MathHelper.clamp(openAnimation, 0, 1);
        radiusIn = Math.max(0.1f, 30 * animProgress);
        radiusOut = radiusIn * 2;
        itemRadius = (radiusIn + radiusOut) * 0.5f;
        animTop = (1 - animProgress) * height / 2.0f;

        int x = width / 2;
        int y = height / 2;
        float z = 0;

        GlStateManager.pushMatrix();
        GlStateManager.translatef(0, animTop, 0);

        drawBackground(x,y,z, radiusIn, radiusOut);

        GlStateManager.popMatrix();

        drawItems(x,y,z, itemRadius, font, itemRenderer);

        ITextComponent currentCentralText = centralText;
        if (hovered >= 0)
        {
            RadialMenuItem item = items.get(hovered);
            if (item.getCentralText() != null)
                currentCentralText = item.getCentralText();
        }

        if (currentCentralText != null)
        {
            String text = currentCentralText.getFormattedText();
            font.drawStringWithShadow(text, (width-font.getStringWidth(text)) / 2.0f, (height - font.FONT_HEIGHT) / 2.0f, 0xFFFFFFFF);
        }
    }

    public void drawTooltips(int mouseX, int mouseY, int width, int height, FontRenderer fontRenderer, ItemRenderer itemRender)
    {
        if (hovered >= 0)
        {
            items.get(hovered).drawTooltips(mouseX, mouseY, width, height, fontRenderer, itemRender);
        }
    }

    private void drawItems(int x, int y, float z, float itemRadius, FontRenderer font, ItemRenderer itemRenderer)
    {
        int visibleItems = (int)items.stream().filter(RadialMenuItem::isVisible).count();
        int totalItems = items.size();
        for(int v=0,t=0;t<totalItems;t++)
        {
            RadialMenuItem item = items.get(t);
            if (!item.isVisible())
                continue;

            float s = (((v - 0.5f) / (float) visibleItems) + 0.25f) * 360;
            float e = (((v + 0.5f) / (float) visibleItems) + 0.25f) * 360;
            float middle = (s+e)*0.5f;

            float posX = x + itemRadius * (float) Math.cos(middle);
            float posY = y + itemRadius * (float) Math.sin(middle);

            item.draw(posX, posY, z, t == hovered, font, itemRenderer);
            v++;
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
        int visibleItems = (int)items.stream().filter(RadialMenuItem::isVisible).count();
        int totalItems = items.size();
        for(int v=0,t=0;t<totalItems;t++)
        {
            RadialMenuItem item = items.get(t);
            if (!item.isVisible())
                continue;
            float s = (((v - 0.5f) / (float) visibleItems) + 0.25f) * 360;
            float e = (((v + 0.5f) / (float) visibleItems) + 0.25f) * 360;
            Vector4f color = t == hovered ? backgroundColorHover : backgroundColor;
            drawPieArc(buffer, x, y, z, radiusIn, radiusOut, s, e, (int)(color.x*255), (int)(color.y*255), (int)(color.z*255), (int)(color.w*255));
            v++;
        }
        tessellator.draw();

        GlStateManager.enableTexture2D();
    }

    private static final float PRECISION = 5;

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
