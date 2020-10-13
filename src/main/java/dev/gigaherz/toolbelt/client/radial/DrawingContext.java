package dev.gigaherz.toolbelt.client.radial;

import com.mojang.blaze3d.matrix.MatrixStack;
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
    public final MatrixStack matrixStack;
    public final IDrawingHelper drawingHelper;

    public DrawingContext(MatrixStack matrixStack, int width, int height, float x, float y, float z, FontRenderer fontRenderer, ItemRenderer itemRenderer, IDrawingHelper drawingHelper)
    {
        this.matrixStack = matrixStack;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.z = z;
        this.fontRenderer = fontRenderer;
        this.itemRenderer = itemRenderer;
        this.drawingHelper = drawingHelper;
    }
}
