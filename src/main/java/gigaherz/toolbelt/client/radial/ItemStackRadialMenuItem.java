package gigaherz.toolbelt.client.radial;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.util.List;

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
            RenderHelper.func_227780_a_();
            RenderSystem.pushMatrix();
            RenderSystem.translatef(-8, -8, context.z);
            context.itemRenderer.renderItemAndEffectIntoGUI(stack, (int) context.x, (int) context.y);
            context.itemRenderer.renderItemOverlayIntoGUI(context.fontRenderer, stack, (int) context.x, (int) context.y, "");
            RenderSystem.popMatrix();
            RenderHelper.disableStandardItemLighting();
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
            net.minecraftforge.fml.client.config.GuiUtils.preItemToolTip(stack);
            GuiUtils.drawHoveringText(stack, getItemToolTip(stack), (int) context.x, (int) context.y, (int) context.width, (int) context.height, -1, context.fontRenderer);
            GuiUtils.postItemToolTip();
        }
        else
        {
            super.drawTooltips(context);
        }
    }

    private List<String> getItemToolTip(ItemStack stack)
    {
        Minecraft mc = Minecraft.getInstance();
        List<ITextComponent> list = stack.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
        List<String> list1 = Lists.newArrayList();

        for (ITextComponent itextcomponent : list)
        {
            list1.add(itextcomponent.getFormattedText());
        }

        return list1;
    }
}
