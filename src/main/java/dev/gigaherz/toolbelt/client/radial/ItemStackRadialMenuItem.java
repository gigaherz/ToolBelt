package dev.gigaherz.toolbelt.client.radial;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
    public void draw(DrawingContext context)
    {
        if (stack.getCount() > 0)
        {
            PoseStack viewModelPose = RenderSystem.getModelViewStack();
            viewModelPose.pushPose();
            viewModelPose.mulPoseMatrix(context.matrixStack.last().pose());
            viewModelPose.translate(-8, -8, context.z);
            RenderSystem.applyModelViewMatrix();
            context.itemRenderer.renderAndDecorateItem(context.matrixStack, stack, (int) context.x, (int) context.y);
            context.itemRenderer.renderGuiItemDecorations(context.matrixStack, context.fontRenderer, stack, (int) context.x, (int) context.y, "");
            viewModelPose.popPose();
            RenderSystem.applyModelViewMatrix();
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
