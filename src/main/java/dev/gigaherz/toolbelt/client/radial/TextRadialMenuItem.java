package dev.gigaherz.toolbelt.client.radial;

import net.minecraft.network.chat.Component;

public class TextRadialMenuItem extends RadialMenuItem
{
    private final Component text;
    private final int color;

    public Component getText()
    {
        return text;
    }

    public int getColor()
    {
        return color;
    }

    public TextRadialMenuItem(GenericRadialMenu owner, Component text)
    {
        super(owner);
        this.text = text;
        this.color = 0xFFFFFFFF;
    }

    public TextRadialMenuItem(GenericRadialMenu owner, Component text, int color)
    {
        super(owner);
        this.text = text;
        this.color = color;
    }

    @Override
    public void draw(DrawingContext context)
    {
        String textString = text.getString();
        float x = context.x - context.font.width(textString) / 2.0f;
        float y = context.y - context.font.lineHeight / 2.0f;
        context.graphics.drawString(context.font, textString, (int)x, (int)y, color, true);
    }

    @Override
    public void drawTooltips(DrawingContext context)
    {
        // nothing to do (yet)
    }
}
