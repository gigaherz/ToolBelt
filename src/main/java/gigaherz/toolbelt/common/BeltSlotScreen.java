package gigaherz.toolbelt.common;

import com.mojang.blaze3d.platform.GlStateManager;
import gigaherz.toolbelt.ToolBelt;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class BeltSlotScreen extends DisplayEffectsScreen<BeltSlotContainer> implements IRecipeShownListener
{
    private static final ResourceLocation SCREEN_BACKGROUND = ToolBelt.location("textures/gui/belt_slot.png");
    private static final ResourceLocation RECIPE_BUTTON_TEXTURE = new ResourceLocation("textures/gui/recipe_button.png");
    private float oldMouseX;
    private float oldMouseY;
    private final RecipeBookGui recipeBookGui = new RecipeBookGui();
    private boolean hasRecipeBook;
    private boolean widthTooNarrow;
    private boolean buttonClicked;

    public BeltSlotScreen(BeltSlotContainer container, PlayerInventory playerInventory, ITextComponent title)
    {
        super(container, playerInventory, title);
        this.passEvents = true;
    }

    @Override
    public void tick()
    {
        super.tick();
        this.recipeBookGui.tick();
    }

    @Override
    public void init()
    {
        super.init();

        this.widthTooNarrow = this.width < 379;
        this.recipeBookGui.func_201520_a(this.width, this.height, this.minecraft, this.widthTooNarrow, this.container);
        this.hasRecipeBook = true;
        this.guiLeft = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.xSize);
        this.children.add(this.recipeBookGui);
        this.func_212928_a(this.recipeBookGui);
        ImageButton recipebutton;
        this.addButton(recipebutton=new ImageButton(this.guiLeft + 104, this.height / 2 - 22, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, (p_214086_1_) -> {
            this.recipeBookGui.func_201518_a(this.widthTooNarrow);
            this.recipeBookGui.toggleVisibility();
            this.guiLeft = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.xSize);
            ((ImageButton) p_214086_1_).setPosition(this.guiLeft + 104, this.height / 2 - 22);
            this.buttonClicked = true;
        }));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.font.drawString(I18n.format("container.crafting"), 97, 8, 4210752);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground();
        this.hasActivePotionEffects = !this.recipeBookGui.isVisible();
        if (this.recipeBookGui.isVisible() && this.widthTooNarrow)
        {
            this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
            this.recipeBookGui.render(mouseX, mouseY, partialTicks);
        }
        else
        {
            this.recipeBookGui.render(mouseX, mouseY, partialTicks);
            super.render(mouseX, mouseY, partialTicks);
            this.recipeBookGui.renderGhostRecipe(this.guiLeft, this.guiTop, false, partialTicks);
        }

        this.renderHoveredToolTip(mouseX, mouseY);
        this.recipeBookGui.renderTooltip(this.guiLeft, this.guiTop, mouseX, mouseY);
        this.oldMouseX = (float) mouseX;
        this.oldMouseY = (float) mouseY;
        this.func_212932_b(this.recipeBookGui);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(SCREEN_BACKGROUND);
        int i = this.guiLeft;
        int j = this.guiTop;
        this.blit(i, j, 0, 0, this.xSize, this.ySize);
        InventoryScreen.drawEntityOnScreen(i + 51, j + 75, 30, (float) (i + 51) - this.oldMouseX, (float) (j + 75 - 50) - this.oldMouseY, minecraft.player);
    }

    @Override
    protected boolean isPointInRegion(int p_195359_1_, int p_195359_2_, int p_195359_3_, int p_195359_4_, double p_195359_5_, double p_195359_7_)
    {
        if (this.widthTooNarrow && this.recipeBookGui.isVisible())
            return false;

        return super.isPointInRegion(p_195359_1_, p_195359_2_, p_195359_3_, p_195359_4_, p_195359_5_, p_195359_7_);
    }

    @Override
    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_)
    {
        if (this.recipeBookGui.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_))
            return true;

        if (this.widthTooNarrow && this.recipeBookGui.isVisible())
            return false;

        return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
    }

    @Override
    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_)
    {
        if (this.buttonClicked)
        {
            this.buttonClicked = false;
            return true;
        }
        else
        {
            return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
        }
    }

    @Override
    protected boolean hasClickedOutside(double p_195361_1_, double p_195361_3_, int p_195361_5_, int p_195361_6_, int p_195361_7_)
    {
        boolean flag = p_195361_1_ < (double) p_195361_5_ || p_195361_3_ < (double) p_195361_6_ || p_195361_1_ >= (double) (p_195361_5_ + this.xSize) || p_195361_3_ >= (double) (p_195361_6_ + this.ySize);
        return this.recipeBookGui.func_195604_a(p_195361_1_, p_195361_3_, this.guiLeft, this.guiTop, this.xSize, this.ySize, p_195361_7_) && flag;
    }

    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type)
    {
        super.handleMouseClick(slotIn, slotId, mouseButton, type);
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
        if (this.hasRecipeBook)
        {
            this.recipeBookGui.removed();
        }

        super.removed();
    }

    @Override
    public RecipeBookGui func_194310_f()
    {
        return this.recipeBookGui;
    }
}