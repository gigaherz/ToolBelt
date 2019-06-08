package gigaherz.toolbelt.client;

import com.mojang.blaze3d.platform.GlStateManager;
import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.ConfigData;
import gigaherz.toolbelt.ToolBelt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.CapabilityItemHandler;

public class LayerToolBelt<T extends PlayerEntity, M extends PlayerModel<T>> extends LayerRenderer<T, M>
{
    private static final ResourceLocation TEXTURE_BELT = ToolBelt.location("textures/entity/belt.png");

    private final LivingRenderer<T,M> owner;

    private final ModelBelt beltModel = new ModelBelt();

    public LayerToolBelt(LivingRenderer<T,M> owner)
    {
        super(owner);
        this.owner = owner;
    }

    @Override
    public void func_212842_a_(T player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        boolean flag = player.getPrimaryHand() == HandSide.RIGHT;

        if (!ConfigData.showBeltOnPlayers)
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

                if (this.func_215332_c().field_217113_d) // FIXME: maybe wrong field, can't tell
                {
                    GlStateManager.translatef(0.0F, 0.75F, 0.0F);
                    GlStateManager.scalef(0.5F, 0.5F, 0.5F);
                }

                this.renderHeldItem(player, rightItem, TransformType.THIRD_PERSON_RIGHT_HAND, HandSide.RIGHT);
                this.renderHeldItem(player, leftItem, TransformType.THIRD_PERSON_LEFT_HAND, HandSide.LEFT);

                GlStateManager.popMatrix();
            }

            GlStateManager.translatef(0.0F, 0.19F, 0.0F);
            GlStateManager.scalef(0.85f, 0.6f, 0.78f);

            this.owner.bindTexture(TEXTURE_BELT);
            this.beltModel.render(player, 0, 0, 0, 0, 0, scale);

            GlStateManager.popMatrix();
        });
    }

    private void translateToBody()
    {
        func_215332_c().field_78115_e.postRender(0.0625F);
    }

    private void renderHeldItem(LivingEntity player, ItemStack stack, TransformType cameraTransform, HandSide handSide)
    {
        if (stack.isEmpty())
            return;

        GlStateManager.pushMatrix();

        if (handSide == HandSide.LEFT)
            GlStateManager.translatef(-4.35f / 16.0F, 0.7f, -0.1f);
        else
            GlStateManager.translatef(4.35f / 16.0F, 0.7f, -0.1f);
        GlStateManager.rotatef(40.0F, 1.0F, 0.0F, 0.0F);
        double scale = ConfigData.beltItemScale;
        GlStateManager.scaled(scale, scale, scale);
        Minecraft.getInstance().getItemRenderer().renderItem(stack, player, cameraTransform, handSide == HandSide.LEFT);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return false;
    }

    private static class ModelBelt extends EntityModel<PlayerEntity>
    {
        final RendererModel belt = new RendererModel(this);
        final RendererModel buckle = new RendererModel(this, 10, 10);
        final RendererModel pocketL = new RendererModel(this, 0, 10);
        final RendererModel pocketR = new RendererModel(this, 0, 10);

        {
            belt.addBox(-5, 10, -3, 10, 4, 6);

            buckle.addBox(-2.5f, 9.5f, -3.5f, 5, 5, 1);

            pocketL.addBox(-2, 12, 5, 4, 4, 1);
            pocketL.rotateAngleY = (float) Math.toRadians(-90);
            pocketR.addBox(-2, 12, 5, 4, 4, 1);
            pocketR.rotateAngleY = (float) Math.toRadians(90);
        }

        @Override
        public void render(PlayerEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
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
    }
}
