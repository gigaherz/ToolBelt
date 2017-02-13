package gigaherz.toolbelt.common;

import gigaherz.toolbelt.ToolBelt;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

@SideOnly(Side.CLIENT)
public class GuiBelt extends GuiContainer
{
    /**
     * The ResourceLocation containing the chest GUI texture.
     */
    private static final ResourceLocation GUI_TEXTURE = ToolBelt.location("textures/gui/belt.png");
    private final IInventory playerInventory;
    private final ItemStack beltStack;

    public GuiBelt(IInventory playerInventory, IItemHandler beltInventory, int blockedSlot, ItemStack beltStack)
    {
        super(new ContainerBelt(playerInventory, beltInventory, blockedSlot));
        this.playerInventory = playerInventory;
        this.beltStack = beltStack;
        this.allowUserInput = false;
        this.xSize = 176;
        this.ySize = 133;
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRenderer.drawString(this.beltStack.getDisplayName(), 8, 6, 4210752);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

        int slots = ((ContainerBelt) this.inventorySlots).beltSlots;
        int width = slots * 18;
        int x = 7 + ((9 - slots) * 18) / 2;
        this.drawTexturedModalRect(i + x, j + 19, 0, this.ySize, width, 18);
    }
}