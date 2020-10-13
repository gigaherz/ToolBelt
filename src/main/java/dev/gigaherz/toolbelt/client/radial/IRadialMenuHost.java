package dev.gigaherz.toolbelt.client.radial;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.ItemRenderer;

public interface IRadialMenuHost extends IDrawingHelper
{
    Screen getScreen();

    FontRenderer getFontRenderer();

    ItemRenderer getItemRenderer();
}
