package gigaherz.toolbelt.client;

import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.Config;
import gigaherz.toolbelt.ToolBelt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.ModelBase;
import net.minecraft.client.renderer.entity.model.ModelBiped;
import net.minecraft.client.renderer.entity.model.ModelRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class LayerToolBelt implements LayerRenderer<EntityPlayer>
{
    private static final ResourceLocation TEXTURE_BELT = ToolBelt.location("textures/entity/belt.png");

    private final RenderLivingBase<?> livingEntityRenderer;

    private final ModelBase beltModel = new ModelBase()
    {
        final ModelRenderer belt = new ModelRenderer(this);
        final ModelRenderer buckle = new ModelRenderer(this, 10, 10);
        final ModelRenderer pocketL = new ModelRenderer(this, 0, 10);
        final ModelRenderer pocketR = new ModelRenderer(this, 0, 10);

        {
            belt.addBox(-5, 10, -3, 10, 4, 6);

            buckle.addBox(-2.5f, 9.5f, -3.5f, 5, 5, 1);

            pocketL.addBox(-2, 12, 5, 4, 4, 1);
            pocketL.rotateAngleY = (float) Math.toRadians(-90);
            pocketR.addBox(-2, 12, 5, 4, 4, 1);
            pocketR.rotateAngleY = (float) Math.toRadians(90);
        }

        @Override
        public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
        {
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableCull();
            belt.render(scale);
            pocketL.render(scale);
            pocketR.render(scale);

            GlStateManager.pushMatrix();
            GlStateManager.scalef(0.8f, 1, 1);
            buckle.render(scale);
            GlStateManager.popMatrix();
        }
    };

    public LayerToolBelt(RenderLivingBase<?> livingEntityRendererIn)
    {
        this.livingEntityRenderer = livingEntityRendererIn;
    }

    @Override
    public void render(EntityPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        boolean flag = player.getPrimaryHand() == EnumHandSide.RIGHT;

        if (!Config.showBeltOnPlayers)
            return;

        BeltFinder.BeltGetter getter = BeltFinder.findBelt(player);
        if (getter == null)
            return;

        ItemStack stack = getter.getBelt();
        if (stack.getCount() <= 0)
            return;

        stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent((cap) -> {

            ItemStack firstItem = cap.getStackInSlot(0);
            ItemStack secondItem = cap.getStackInSlot(1);

            ItemStack leftItem = flag ? firstItem : secondItem;
            ItemStack rightItem = flag ? secondItem : firstItem;

            GlStateManager.pushMatrix();

            if (player.isSneaking())
            {
                GlStateManager.translatef(0.0F, 0.2F, 0.0F);
            }

            this.translateToBody();

            if (!leftItem.isEmpty() || !rightItem.isEmpty())
            {
                GlStateManager.pushMatrix();

                if (this.livingEntityRenderer.getMainModel().isChild)
                {
                    GlStateManager.translatef(0.0F, 0.75F, 0.0F);
                    GlStateManager.scalef(0.5F, 0.5F, 0.5F);
                }

                this.renderHeldItem(player, rightItem, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, EnumHandSide.RIGHT);
                this.renderHeldItem(player, leftItem, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, EnumHandSide.LEFT);

                GlStateManager.popMatrix();
            }

            GlStateManager.translatef(0.0F, 0.19F, 0.0F);
            GlStateManager.scalef(0.85f, 0.6f, 0.78f);

            this.livingEntityRenderer.bindTexture(TEXTURE_BELT);
            this.beltModel.render(player, 0, 0, 0, 0, 0, scale);

            GlStateManager.popMatrix();
        });
    }

    private void translateToBody()
    {
        ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedBody.postRender(0.0625F);
    }

    private void renderHeldItem(EntityLivingBase player, ItemStack stack, ItemCameraTransforms.TransformType cameraTransform, EnumHandSide handSide)
    {
        if (stack.isEmpty())
            return;

        GlStateManager.pushMatrix();

        if (handSide == EnumHandSide.LEFT)
            GlStateManager.translatef(-4.35f / 16.0F, 0.7f, -0.1f);
        else
            GlStateManager.translatef(4.35f / 16.0F, 0.7f, -0.1f);
        GlStateManager.rotatef(40.0F, 1.0F, 0.0F, 0.0F);
        float scale = Config.beltItemScale;
        GlStateManager.scalef(scale, scale, scale);
        Minecraft.getInstance().getItemRenderer().renderItem(stack, player, cameraTransform, handSide == EnumHandSide.LEFT);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return false;
    }
}
