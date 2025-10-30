package dev.gigaherz.toolbelt.common;

import dev.gigaherz.toolbelt.ToolBelt;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BeltScreen extends AbstractContainerScreen<BeltContainer>
{
    /**
     * The ResourceLocation containing the chest GUI texture.
     */
    private static final ResourceLocation GUI_TEXTURE = ToolBelt.location("textures/gui/belt.png");

    public BeltScreen(BeltContainer container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 133;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
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