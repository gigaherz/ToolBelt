package gigaherz.toolbelt.client.radial;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.ItemRenderer;

public interface IRadialMenuHost
{
    GuiScreen getScreen();
    FontRenderer getFontRenderer();
    ItemRenderer getItemRenderer();
}
