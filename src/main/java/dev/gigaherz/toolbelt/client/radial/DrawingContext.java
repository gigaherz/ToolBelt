package dev.gigaherz.toolbelt.client.radial;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class DrawingContext
{
    public final int width;
    public final int height;
    public final float x;
    public final float y;
    public final float z;
    public final Font font;
    public final GuiGraphics graphics;

    public DrawingContext(GuiGraphics graphics, int width, int height, float x, float y, float z, Font font)
    {
        this.graphics = graphics;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.z = z;
        this.font = font;
    }
}
