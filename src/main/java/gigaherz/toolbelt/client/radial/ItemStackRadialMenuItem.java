package gigaherz.toolbelt.client.radial;

import com.google.common.collect.Lists;
import gigaherz.toolbelt.client.GuiRadialMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
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

    public ItemStackRadialMenuItem(int slot, ItemStack stack, ITextComponent altText)
    {
        super(altText);
        this.slot = slot;
        this.stack = stack;
    }

    @Override
    public void draw(float x, float y, float z, boolean hover, FontRenderer font, ItemRenderer itemRenderer)
    {
        if (stack.getCount() > 0)
        {
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.pushMatrix();
            GlStateManager.translatef(0, 0, z);
            itemRenderer.renderItemAndEffectIntoGUI(stack, (int) x, (int) y);
            itemRenderer.renderItemOverlayIntoGUI(font, stack, (int) x, (int) y, "");
            GlStateManager.popMatrix();
            RenderHelper.disableStandardItemLighting();
        }
        else
        {
            super.draw(x,y,z,hover,font,itemRenderer);
        }
    }

    @Override
    public void drawTooltips(float mouseX, float mouseY, float screenWidth, float screenHeight, FontRenderer font, ItemRenderer itemRenderer)
    {
        if (stack.getCount() > 0)
        {
            net.minecraftforge.fml.client.config.GuiUtils.preItemToolTip(stack);
            GuiUtils.drawHoveringText(stack, getItemToolTip(stack), (int) mouseX, (int) mouseY, (int)screenWidth, (int)screenHeight, -1, font);
            GuiUtils.postItemToolTip();
        }
        else
        {
            super.drawTooltips(mouseX, mouseY, screenWidth, screenHeight, font, itemRenderer);
        }
    }

    private List<String> getItemToolTip(ItemStack stack)
    {
        Minecraft mc = Minecraft.getInstance();
        List<ITextComponent> list = stack.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
        List<String> list1 = Lists.newArrayList();

        for(ITextComponent itextcomponent : list) {
            list1.add(itextcomponent.getFormattedText());
        }

        return list1;
    }
}
