package dev.gigaherz.toolbelt.client.radial;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

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

    public ItemStackRadialMenuItem(GenericRadialMenu owner, int slot, ItemStack stack, Component altText)
    {
        super(owner, altText, 0x7FFFFFFF);
        this.slot = slot;
        this.stack = stack;
    }

    @Override
    public void extractRenderState(DrawingContext context)
    {
        if (stack.getCount() > 0)
        {
            context.graphics.item(stack, (int) context.x - 8, (int) context.y - 8);
            context.graphics.itemDecorations(context.font, stack, (int) context.x - 8, (int) context.y - 8, "");
        }
        else
        {
            super.extractRenderState(context);
        }
    }

    @Override
    public void prepareTooltip(DrawingContext context)
    {
        if (stack.getCount() > 0)
        {
            context.graphics.setTooltipForNextFrame(context.font, stack, (int) context.x, (int) context.y);
        }
        else
        {
            super.prepareTooltip(context);
        }
    }
}
