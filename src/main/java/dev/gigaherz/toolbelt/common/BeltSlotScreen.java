package dev.gigaherz.toolbelt.common;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.gigaherz.toolbelt.ToolBelt;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import static net.minecraft.client.gui.screen.inventory.InventoryScreen.renderEntityInInventory;

public class BeltSlotScreen extends DisplayEffectsScreen<BeltSlotContainer> implements IRecipeShownListener
{
    private static final ResourceLocation SCREEN_BACKGROUND = ToolBelt.location("textures/gui/belt_slot.png");
    private static final ResourceLocation RECIPE_BUTTON_TEXTURE = new ResourceLocation("textures/gui/recipe_button.png");
    private float oldMouseX;
    private float oldMouseY;
    private final RecipeBookGui recipeBookGui = new RecipeBookGui();
    private boolean removeRecipeBookGui;
    private boolean widthTooNarrow;
    private boolean buttonClicked;

    public BeltSlotScreen(BeltSlotContainer container, PlayerInventory playerInventory, ITextComponent title)
    {
        super(container, playerInventory, title);
        this.passEvents = true;
        this.titleLabelX = 97;
    }

    @Override
    public void tick()
    {
        this.recipeBookGui.tick();
    }

    @Override
    protected void init()
    {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.recipeBookGui.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
        this.removeRecipeBookGui = true;
        this.leftPos = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.imageWidth);
        this.children.add(this.recipeBookGui);
        this.setInitialFocus(this.recipeBookGui);
        this.addButton(new ImageButton(this.leftPos + 104, this.height / 2 - 22, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, (button) -> {
            this.recipeBookGui.initVisuals(this.widthTooNarrow);
            this.recipeBookGui.toggleVisibility();
            this.leftPos = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.imageWidth);
            ((ImageButton) button).setPosition(this.leftPos + 104, this.height / 2 - 22);
            this.buttonClicked = true;
        }));
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY)
    {
        this.font.draw(matrixStack, this.title, (float) this.titleLabelX, (float) this.titleLabelY, 4210752);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        this.doRenderEffects = !this.recipeBookGui.isVisible();
        if (this.recipeBookGui.isVisible() && this.widthTooNarrow)
        {
            this.renderBg(matrixStack, partialTicks, mouseX, mouseY);
            this.recipeBookGui.render(matrixStack, mouseX, mouseY, partialTicks);
        }
        else
        {
            this.recipeBookGui.render(matrixStack, mouseX, mouseY, partialTicks);
            super.render(matrixStack, mouseX, mouseY, partialTicks);
            this.recipeBookGui.renderGhostRecipe(matrixStack, this.leftPos, this.topPos, false, partialTicks);
        }

        this.renderTooltip(matrixStack, mouseX, mouseY);
        this.recipeBookGui.renderTooltip(matrixStack, this.leftPos, this.topPos, mouseX, mouseY);
        this.oldMouseX = (float) mouseX;
        this.oldMouseY = (float) mouseY;
        this.magicalSpecialHackyFocus(this.recipeBookGui);
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(SCREEN_BACKGROUND);
        int i = this.leftPos;
        int j = this.topPos;
        this.blit(matrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
        renderEntityInInventory(i + 51, j + 75, 30, (float) (i + 51) - this.oldMouseX, (float) (j + 75 - 50) - this.oldMouseY, this.minecraft.player);
    }


    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY)
    {
        return (!this.widthTooNarrow || !this.recipeBookGui.isVisible()) && super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (this.recipeBookGui.mouseClicked(mouseX, mouseY, button))
        {
            this.setFocused(this.recipeBookGui);
            return true;
        }
        else
        {
            return this.widthTooNarrow && this.recipeBookGui.isVisible() ? false : super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (this.buttonClicked)
        {
            this.buttonClicked = false;
            return true;
        }
        else
        {
            return super.mouseReleased(mouseX, mouseY, button);
        }
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton)
    {
        boolean flag = mouseX < (double) guiLeftIn || mouseY < (double) guiTopIn || mouseX >= (double) (guiLeftIn + this.imageWidth) || mouseY >= (double) (guiTopIn + this.imageHeight);
        return this.recipeBookGui.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, mouseButton) && flag;
    }

    @Override
    protected void slotClicked(Slot slotIn, int slotId, int mouseButton, ClickType type)
    {
        super.slotClicked(slotIn, slotId, mouseButton, type);
        this.recipeBookGui.slotClicked(slotIn);
    }

    @Override
    public void recipesUpdated()
    {
        this.recipeBookGui.recipesUpdated();
    }

    @Override
    public void removed()
    {
        if (this.removeRecipeBookGui)
        {
            this.recipeBookGui.removed();
        }

        super.removed();
    }

    @Override
    public RecipeBookGui getRecipeBookComponent()
    {
        return this.recipeBookGui;
    }
}