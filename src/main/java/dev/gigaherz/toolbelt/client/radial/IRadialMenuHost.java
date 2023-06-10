package dev.gigaherz.toolbelt.client.radial;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;

public interface IRadialMenuHost extends IDrawingHelper
{
    Screen getScreen();

    Font getFontRenderer();
}
