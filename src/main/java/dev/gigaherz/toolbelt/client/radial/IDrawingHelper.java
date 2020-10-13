package dev.gigaherz.toolbelt.client.radial;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.item.ItemStack;

public interface IDrawingHelper
{
    void renderTooltip(MatrixStack matrixStack, ItemStack stack, int mouseX, int mouseY);
}
