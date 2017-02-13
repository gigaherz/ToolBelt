package gigaherz.toolbelt.client;

import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.belt.ItemToolBelt;
import gigaherz.toolbelt.belt.ToolBeltInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;

import javax.annotation.Nullable;

public class LayerToolBelt implements LayerRenderer<EntityPlayer>
{
    private final RenderLivingBase<?> livingEntityRenderer;

    public LayerToolBelt(RenderLivingBase<?> livingEntityRendererIn)
    {
        this.livingEntityRenderer = livingEntityRendererIn;
    }

    @Override
    public void doRenderLayer(EntityPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        boolean flag = player.getPrimaryHand() == EnumHandSide.RIGHT;

        ItemStack stack = BeltFinder.instance.findStack(player);
        if (stack == null || stack.stackSize <= 0)
            return;

        ToolBeltInventory cap = ItemToolBelt.getItems(stack);

        ItemStack firstItem = cap.getStackInSlot(0);
        ItemStack secondItem = cap.getStackInSlot(1);

        ItemStack leftItem = flag ? firstItem : secondItem;
        ItemStack rightItem = flag ? secondItem : firstItem;

        if (leftItem != null || rightItem != null)
        {
            GlStateManager.pushMatrix();

            if (this.livingEntityRenderer.getMainModel().isChild)
            {
                GlStateManager.translate(0.0F, 0.75F, 0.0F);
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
            }

            this.renderHeldItem(player, rightItem, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, EnumHandSide.RIGHT);
            this.renderHeldItem(player, leftItem, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, EnumHandSide.LEFT);

            GlStateManager.popMatrix();
        }
    }

    private void renderHeldItem(EntityLivingBase player, @Nullable ItemStack stack, ItemCameraTransforms.TransformType cameraTransform, EnumHandSide handSide)
    {
        if (stack == null)
            return;

        GlStateManager.pushMatrix();

        if (player.isSneaking())
        {
            GlStateManager.translate(0.0F, 0.2F, 0.0F);
        }
        // Forge: moved this call down, fixes incorrect offset while sneaking.
        this.translateToBody();
        if (handSide == EnumHandSide.LEFT)
            GlStateManager.translate(-4.2 / 16.0F, 0.6f, 0);
        else
            GlStateManager.translate(4.2 / 16.0F, 0.6f, 0);
        GlStateManager.rotate(40.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        Minecraft.getMinecraft().getItemRenderer().renderItemSide(player, stack, cameraTransform, handSide == EnumHandSide.LEFT);
        GlStateManager.popMatrix();
    }

    protected void translateToBody()
    {
        ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedBody.postRender(0.0625F);
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return false;
    }
}
