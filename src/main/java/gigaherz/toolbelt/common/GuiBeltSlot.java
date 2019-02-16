package gigaherz.toolbelt.common;

import gigaherz.toolbelt.ToolBelt;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerRecipeBook;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class GuiBeltSlot extends InventoryEffectRenderer implements IRecipeShownListener
{

    /** The old x position of the mouse pointer */
    private float oldMouseX;
    /** The old y position of the mouse pointer */
    private float oldMouseY;

    private final GuiRecipeBook recipeBookGui = new GuiRecipeBook();
    private boolean widthTooNarrow;
    private boolean buttonClicked;

    private static final ResourceLocation GUI_TEXTURE = ToolBelt.location("textures/gui/belt_slot.png");

    public GuiBeltSlot(EntityPlayer player)
    {
        super(new ContainerBeltSlot(player.inventory, !player.world.isRemote, player));
        this.allowUserInput = true;
    }

    @Override
    public void tick() {
        super.tick();
        this.recipeBookGui.tick();
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.widthTooNarrow = this.width < 379;
        this.recipeBookGui.func_201520_a(this.width, this.height, this.mc, this.widthTooNarrow, (ContainerRecipeBook)this.inventorySlots);
        this.guiLeft = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.xSize);
        this.children.add(this.recipeBookGui);
        this.addButton(new GuiButtonImage(10, this.guiLeft + 104, this.height / 2 - 22,
                20, 18, 178, 0, 19, INVENTORY_BACKGROUND) {
            @Override
            public void onClick(double mouseX, double mouseY)
            {
                GuiBeltSlot.this.recipeBookGui.func_201518_a(GuiBeltSlot.this.widthTooNarrow);
                GuiBeltSlot.this.recipeBookGui.toggleVisibility();
                GuiBeltSlot.this.guiLeft = GuiBeltSlot.this.recipeBookGui.updateScreenPosition(GuiBeltSlot.this.widthTooNarrow, GuiBeltSlot.this.width, GuiBeltSlot.this.xSize);
                this.setPosition(GuiBeltSlot.this.guiLeft + 5, GuiBeltSlot.this.height / 2 - 49);
            }
        });
    }

    @Override
    @Nullable
    public IGuiEventListener getFocused() {
        return this.recipeBookGui;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRenderer.drawString(I18n.format("container.crafting"), 97, 8, 4210752);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
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
        this.oldMouseX = (float)mouseX;
        this.oldMouseY = (float)mouseY;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
        int i = this.guiLeft;
        int j = this.guiTop;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        GuiInventory.drawEntityOnScreen(i + 51, j + 75, 30, (float)(i + 51) - this.oldMouseX, (float)(j + 75 - 50) - this.oldMouseY, this.mc.player);
    }

    @Override
    protected boolean isPointInRegion(int p_195359_1_, int p_195359_2_, int p_195359_3_, int p_195359_4_, double p_195359_5_, double p_195359_7_) {
        return (!this.widthTooNarrow || !this.recipeBookGui.isVisible()) && super.isPointInRegion(p_195359_1_, p_195359_2_, p_195359_3_, p_195359_4_, p_195359_5_, p_195359_7_);
    }

    @Override
    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
        if (this.recipeBookGui.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_)) {
            return true;
        } else {
            return this.widthTooNarrow && this.recipeBookGui.isVisible() ? false : super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        }
    }

    @Override
    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
        if (this.buttonClicked) {
            this.buttonClicked = false;
            return true;
        } else {
            return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
        }
    }

    @Override
    protected boolean hasClickedOutside(double p_195361_1_, double p_195361_3_, int p_195361_5_, int p_195361_6_, int p_195361_7_) {
        boolean flag = p_195361_1_ < (double)p_195361_5_ || p_195361_3_ < (double)p_195361_6_ || p_195361_1_ >= (double)(p_195361_5_ + this.xSize) || p_195361_3_ >= (double)(p_195361_6_ + this.ySize);
        return this.recipeBookGui.func_195604_a(p_195361_1_, p_195361_3_, this.guiLeft, this.guiTop, this.xSize, this.ySize, p_195361_7_) && flag;
    }

    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        super.handleMouseClick(slotIn, slotId, mouseButton, type);
        this.recipeBookGui.slotClicked(slotIn);
    }

    @Override
    public void recipesUpdated() {
        this.recipeBookGui.recipesUpdated();
    }

    @Override
    public void onGuiClosed() {
        this.recipeBookGui.removed();
        super.onGuiClosed();
    }

    @Override
    public GuiRecipeBook func_194310_f() {
        return this.recipeBookGui;
    }
}