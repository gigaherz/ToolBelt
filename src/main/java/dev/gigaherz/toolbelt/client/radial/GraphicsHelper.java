package dev.gigaherz.toolbelt.client.radial;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

public interface GraphicsHelper
{
    void extractTooltip(GuiGraphicsExtractor graphics, ItemStack stack, int mouseX, int mouseY);
}
