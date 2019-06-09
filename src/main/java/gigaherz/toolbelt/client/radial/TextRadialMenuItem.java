package gigaherz.toolbelt.client.radial;

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

    public TextRadialMenuItem(GenericRadialMenu owner, ITextComponent text)
    {
        super(owner);
        this.text = text;
        this.color = 0xFFFFFFFF;
    }

    private TextRadialMenuItem(GenericRadialMenu owner, ITextComponent text, int color)
    {
        super(owner);
        this.text = text;
        this.color = color;
    }

    @Override
    public void draw(DrawingContext context)
    {
        String textString = text.getFormattedText();
        float x = context.x - context.fontRenderer.getStringWidth(textString) / 2.0f;
        float y = context.y - context.fontRenderer.FONT_HEIGHT / 2.0f;
        context.fontRenderer.drawStringWithShadow(textString, x, y, color);
    }

    @Override
    public void drawTooltips(DrawingContext context)
    {
        // nothing to do (yet)
    }
}
