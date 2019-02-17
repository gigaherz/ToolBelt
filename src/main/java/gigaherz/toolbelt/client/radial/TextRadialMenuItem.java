package gigaherz.toolbelt.client.radial;

import gigaherz.toolbelt.client.GuiRadialMenu;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.util.text.ITextComponent;

public class TextRadialMenuItem extends RadialMenuItem
{
    private final ITextComponent text;
    private final int color;

    public ITextComponent getText()
    {
        return text;
    }

    public int getColor()
    {
        return color;
    }

    public TextRadialMenuItem(ITextComponent text)
    {
        this.text = text;
        this.color = 0xFFFFFFFF;
    }

    private TextRadialMenuItem(ITextComponent text, int color)
    {
        this.text = text;
        this.color = color;
    }

    @Override
    public void draw(float x, float y, float z, boolean hover, FontRenderer font, ItemRenderer itemRenderer)
    {
        String textString = text.getFormattedText();
        y -= font.FONT_HEIGHT;
        x -= font.getStringWidth(textString);
        font.drawStringWithShadow(textString, x, y, color);
    }

    @Override
    public void drawTooltips(float mouseX, float mouseY, float screenWidth, float screenHeight, FontRenderer font, ItemRenderer itemRenderer)
    {
        // nothing to do (yet)
    }
}
