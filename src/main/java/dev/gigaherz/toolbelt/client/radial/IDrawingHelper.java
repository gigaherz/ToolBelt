package dev.gigaherz.toolbelt.client.radial;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;

public interface IDrawingHelper
{
    void renderTooltip(PoseStack matrixStack, ItemStack stack, int mouseX, int mouseY);
}
