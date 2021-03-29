package dev.gigaherz.toolbelt.client.radial;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class ItemStackRadialMenuItem extends TextRadialMenuItem
{
    private final int slot;
    private final ItemStack stack;

    public int getSlot()
    {
        return slot;
    }

    public ItemStack getStack()
    {
        return stack;
    }

    public ItemStackRadialMenuItem(GenericRadialMenu owner, int slot, ItemStack stack, ITextComponent altText)
    {
        super(owner, altText, 0x7FFFFFFF);
        this.slot = slot;
        this.stack = stack;
    }

    @Override
    public void draw(DrawingContext context)
    {
        if (stack.getCount() > 0)
        {
            RenderHelper.turnBackOn();
            RenderSystem.pushMatrix();
            RenderSystem.translatef(-8, -8, context.z);
            context.itemRenderer.renderAndDecorateItem(stack, (int) context.x, (int) context.y);
            context.itemRenderer.renderGuiItemDecorations(context.fontRenderer, stack, (int) context.x, (int) context.y, "");
            RenderSystem.popMatrix();
            RenderHelper.turnOff();
        }
        else
        {
            super.draw(context);
        }
    }

    @Override
    public void drawTooltips(DrawingContext context)
    {
        if (stack.getCount() > 0)
        {
            context.drawingHelper.renderTooltip(context.matrixStack, stack, (int) context.x, (int) context.y);
        }
        else
        {
            super.drawTooltips(context);
        }
    }
}
