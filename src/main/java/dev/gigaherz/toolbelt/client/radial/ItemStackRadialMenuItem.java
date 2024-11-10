package dev.gigaherz.toolbelt.client.radial;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fStack;

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
    public void draw(DrawingContext context)
    {
        if (stack.getCount() > 0)
        {
            context.graphics.renderItem(stack, (int) context.x - 8, (int) context.y - 8);
            context.graphics.renderItemDecorations(context.font, stack, (int) context.x - 8, (int) context.y - 8, "");
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
            context.graphics.renderTooltip(context.font, stack, (int) context.x, (int) context.y);
        }
        else
        {
            super.drawTooltips(context);
        }
    }
}
