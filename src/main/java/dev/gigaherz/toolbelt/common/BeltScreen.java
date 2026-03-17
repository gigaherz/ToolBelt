package dev.gigaherz.toolbelt.common;

import dev.gigaherz.toolbelt.ToolBelt;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class BeltScreen extends AbstractContainerScreen<BeltContainer>
{
    /**
     * The Identifier containing the chest GUI texture.
     */
    private static final Identifier GUI_TEXTURE = ToolBelt.location("textures/gui/belt.png");

    public BeltScreen(BeltContainer container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title, 176, 133);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
    {
        this.extractBackground(graphics, mouseX, mouseY, partialTicks);
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        this.extractTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
    {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight, 256,256);

        int slots = this.getMenu().inventorySize;
        int width = slots * 18;
        int x = 7 + ((9 - slots) * 18) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, i + x, j + 19, 0, this.imageHeight, width, 18, 256,256);
    }
}