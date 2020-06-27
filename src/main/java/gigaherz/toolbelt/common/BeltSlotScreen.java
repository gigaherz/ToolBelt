package gigaherz.toolbelt.common;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import gigaherz.toolbelt.ToolBelt;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import static net.minecraft.client.gui.screen.inventory.InventoryScreen.drawEntityOnScreen;

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
        this.field_230711_n_ = true;
        this.field_238742_p_ = 97;
    }

    @Override
    public void func_231023_e_() {
        this.recipeBookGui.tick();
    }

    @Override
    protected void func_231160_c_() {
        {
            super.func_231160_c_();
            this.widthTooNarrow = this.field_230708_k_ < 379;
            this.recipeBookGui.init(this.field_230708_k_, this.field_230709_l_, this.field_230706_i_, this.widthTooNarrow, this.container);
            this.removeRecipeBookGui = true;
            this.guiLeft = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.field_230708_k_, this.xSize);
            this.field_230705_e_.add(this.recipeBookGui);
            this.setFocusedDefault(this.recipeBookGui);
            this.func_230480_a_(new ImageButton(this.guiLeft + 104, this.field_230709_l_ / 2 - 22, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, (p_214086_1_) -> {
                this.recipeBookGui.initSearchBar(this.widthTooNarrow);
                this.recipeBookGui.toggleVisibility();
                this.guiLeft = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.field_230708_k_, this.xSize);
                ((ImageButton)p_214086_1_).setPosition(this.guiLeft + 104, this.field_230709_l_ / 2 - 22);
                this.buttonClicked = true;
            }));
        }
    }

    @Override
    protected void func_230451_b_(MatrixStack matrixStack, int mouseX, int mouseY) {
        this.field_230712_o_.func_238422_b_(matrixStack, this.field_230704_d_, (float)this.field_238742_p_, (float)this.field_238743_q_, 4210752);
    }

    @Override
    public void func_230430_a_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.func_230446_a_(matrixStack);
        this.hasActivePotionEffects = !this.recipeBookGui.isVisible();
        if (this.recipeBookGui.isVisible() && this.widthTooNarrow) {
            this.func_230450_a_(matrixStack, partialTicks, mouseX, mouseY);
            this.recipeBookGui.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
        } else {
            this.recipeBookGui.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
            super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
            this.recipeBookGui.func_230477_a_(matrixStack, this.guiLeft, this.guiTop, false, partialTicks);
        }

        this.func_230459_a_(matrixStack, mouseX, mouseY);
        this.recipeBookGui.func_238924_c_(matrixStack, this.guiLeft, this.guiTop, mouseX, mouseY);
        this.oldMouseX = (float)mouseX;
        this.oldMouseY = (float)mouseY;
        this.func_212932_b(this.recipeBookGui);
    }

    @Override
    protected void func_230450_a_(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.field_230706_i_.getTextureManager().bindTexture(SCREEN_BACKGROUND);
        int i = this.guiLeft;
        int j = this.guiTop;
        this.func_238474_b_(matrixStack, i, j, 0, 0, this.xSize, this.ySize);
        drawEntityOnScreen(i + 51, j + 75, 30, (float)(i + 51) - this.oldMouseX, (float)(j + 75 - 50) - this.oldMouseY, this.field_230706_i_.player);
    }


    @Override
    protected boolean isPointInRegion(int x, int y, int width, int height, double mouseX, double mouseY) {
        return (!this.widthTooNarrow || !this.recipeBookGui.isVisible()) && super.isPointInRegion(x, y, width, height, mouseX, mouseY);
    }

    @Override
    public boolean func_231044_a_(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
        if (this.recipeBookGui.func_231044_a_(p_231044_1_, p_231044_3_, p_231044_5_)) {
            this.func_231035_a_(this.recipeBookGui);
            return true;
        } else {
            return this.widthTooNarrow && this.recipeBookGui.isVisible() ? false : super.func_231044_a_(p_231044_1_, p_231044_3_, p_231044_5_);
        }
    }

    @Override
    public boolean func_231048_c_(double p_231048_1_, double p_231048_3_, int p_231048_5_) {
        if (this.buttonClicked) {
            this.buttonClicked = false;
            return true;
        } else {
            return super.func_231048_c_(p_231048_1_, p_231048_3_, p_231048_5_);
        }
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton) {
        boolean flag = mouseX < (double)guiLeftIn || mouseY < (double)guiTopIn || mouseX >= (double)(guiLeftIn + this.xSize) || mouseY >= (double)(guiTopIn + this.ySize);
        return this.recipeBookGui.func_195604_a(mouseX, mouseY, this.guiLeft, this.guiTop, this.xSize, this.ySize, mouseButton) && flag;
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
    public void func_231164_f_() {
        if (this.removeRecipeBookGui) {
            this.recipeBookGui.removed();
        }

        super.func_231164_f_();
    }

    @Override
    public RecipeBookGui getRecipeGui()
    {
        return this.recipeBookGui;
    }
}