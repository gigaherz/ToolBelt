package gigaherz.toolbelt.client.radial;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;

public class DrawingContext
{
    public final int width;
    public final int height;
    public final float x;
    public final float y;
    public final float z;
    public final FontRenderer fontRenderer;
    public final ItemRenderer itemRenderer;

    public DrawingContext(int width, int height, float x, float y, float z, FontRenderer fontRenderer, ItemRenderer itemRenderer)
    {
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.z = z;
        this.fontRenderer = fontRenderer;
        this.itemRenderer = itemRenderer;
    }
}
