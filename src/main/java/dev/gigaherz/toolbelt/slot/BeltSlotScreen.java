package dev.gigaherz.toolbelt.slot;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.gigaherz.toolbelt.ToolBelt;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.CraftingRecipeBookComponent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BeltSlotScreen extends AbstractRecipeBookScreen<BeltSlotMenu>
{
    private static final ResourceLocation SCREEN_BACKGROUND = ToolBelt.location("textures/gui/belt_slot.png");
    private float oldMouseX;
    private float oldMouseY;
    private boolean buttonClicked;
    private final EffectsInInventory effects;

    public BeltSlotScreen(BeltSlotMenu container, Inventory playerInventory, Component title)
    {
        super(container,
                new CraftingRecipeBookComponent(container),
                playerInventory,
                title);
        this.titleLabelX = 97;
        this.effects = new EffectsInInventory(this);
    }

    @Override
    public void containerTick()
    {
        super.containerTick();
    }

    @Override
    protected void init()
    {
        super.init();
    }

    @Override
    protected ScreenPosition getRecipeBookButtonPosition()
    {
        return new ScreenPosition(this.leftPos + 104, this.height / 2 - 22);
    }

    @Override
    protected void onRecipeBookButtonClick()
    {
        this.buttonClicked = true;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY)
    {
        guiGraphics.drawString(font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.effects.renderEffects(guiGraphics, mouseX, mouseY);
        this.effects.renderTooltip(guiGraphics, mouseX, mouseY);
        this.oldMouseX = (float)mouseX;
        this.oldMouseY = (float)mouseY;
    }

    @Override
    public boolean showsActiveEffects() {
        return this.effects.canSeeEffects();
    }

    @Override
    protected boolean isBiggerResultSlot() {
        return false;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY)
    {
        int i = this.leftPos;
        int j = this.topPos;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SCREEN_BACKGROUND, i, j, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, i + 26, j + 8, i + 75, j + 78, 30, 0.0625F, this.oldMouseX, this.oldMouseY, this.minecraft.player);

    }

    @Override
    public boolean mouseReleased(MouseButtonEvent p_446114_)
    {
        if (this.buttonClicked)
        {
            this.buttonClicked = false;
            return true;
        }
        else
        {
            return super.mouseReleased(p_446114_);
        }
    }
}
