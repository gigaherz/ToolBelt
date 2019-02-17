package gigaherz.toolbelt.client.radial;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

public abstract class RadialMenuItem
{
    private ITextComponent centralText;
    private boolean visible;

    public boolean isVisible()
    {
        return visible;
    }

    public void setVisible(boolean newVisible)
    {
        visible = newVisible;
    }

    @Nullable
    public ITextComponent getCentralText()
    {
        return centralText;
    }

    public void setCentralText(@Nullable ITextComponent centralText)
    {
        this.centralText = centralText;
    }

    public abstract void draw(float x, float y, float z, boolean hover, FontRenderer font, ItemRenderer itemRenderer);
    public abstract void drawTooltips(float mouseX, float mouseY, float screenWidth, float screenHeight, FontRenderer font, ItemRenderer itemRenderer);
}
