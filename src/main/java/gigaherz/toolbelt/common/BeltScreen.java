package gigaherz.toolbelt.common;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import gigaherz.toolbelt.ToolBelt;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class BeltScreen extends ContainerScreen<BeltContainer>
{
    /**
     * The ResourceLocation containing the chest GUI texture.
     */
    private static final ResourceLocation GUI_TEXTURE = ToolBelt.location("textures/gui/belt.png");

    public BeltScreen(BeltContainer container, PlayerInventory playerInventory, ITextComponent title)
    {
        super(container, playerInventory, title);
        this.xSize = 176;
        this.ySize = 133;
        this.field_238745_s_ = this.ySize - 94;
    }

    public void func_230430_a_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.func_230446_a_(matrixStack);
        super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
        this.func_230459_a_(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void func_230450_a_(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        field_230706_i_.getTextureManager().bindTexture(GUI_TEXTURE);
        int i = (this.field_230708_k_ - this.xSize) / 2;
        int j = (this.field_230709_l_ - this.ySize) / 2;
        this.func_238474_b_(matrixStack, i, j, 0, 0, this.xSize, this.ySize);

        int slots = this.getContainer().beltSlots;
        int width = slots * 18;
        int x = 7 + ((9 - slots) * 18) / 2;
        this.func_238474_b_(matrixStack,i + x, j + 19, 0, this.ySize, width, 18);
    }
}