package dev.gigaherz.toolbelt.client.radial;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;

public class DrawingContext
{
    public final int width;
    public final int height;
    public final float x;
    public final float y;
    public final float z;
    public final Font fontRenderer;
    public final ItemRenderer itemRenderer;
    public final PoseStack matrixStack;
    public final IDrawingHelper drawingHelper;

    public DrawingContext(PoseStack matrixStack, int width, int height, float x, float y, float z, Font fontRenderer, ItemRenderer itemRenderer, IDrawingHelper drawingHelper)
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
