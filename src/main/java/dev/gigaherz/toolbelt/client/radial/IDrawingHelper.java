package dev.gigaherz.toolbelt.client.radial;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public interface IDrawingHelper
{
    void renderTooltip(GuiGraphics graphics, ItemStack stack, int mouseX, int mouseY);
}
