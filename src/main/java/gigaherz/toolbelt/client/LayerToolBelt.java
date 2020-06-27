package gigaherz.toolbelt.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.ConfigData;
import gigaherz.toolbelt.ToolBelt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.items.CapabilityItemHandler;

public class LayerToolBelt extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>
{
    private static final ResourceLocation TEXTURE_BELT = ToolBelt.location("textures/entity/belt.png");

    private final LivingRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> owner;

    private final ModelBelt beltModel = new ModelBelt();

    public LayerToolBelt(LivingRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> owner)
    {
        super(owner);
        this.owner = owner;
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int lightness, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        boolean flag = player.getPrimaryHand() == HandSide.RIGHT;

        if (!ConfigData.showBeltOnPlayers)
            return;

        BeltFinder.findBelt(player).ifPresent((getter) -> {
            getter.getBelt().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent((cap) -> {

                ItemStack firstItem = cap.getStackInSlot(0);
                ItemStack secondItem = cap.getStackInSlot(1);

                ItemStack leftItem = flag ? firstItem : secondItem;
                ItemStack rightItem = flag ? secondItem : firstItem;

                matrixStack.push();

                this.translateToBody(matrixStack);

                if (!leftItem.isEmpty() || !rightItem.isEmpty())
                {
                    matrixStack.push();

                    if (this.getEntityModel().isChild)
                    {
                        matrixStack.translate(0.0F, 0.75F, 0.0F);
                        matrixStack.scale(0.5F, 0.5F, 0.5F);
                    }

                    renderHeldItem(player, rightItem, TransformType.THIRD_PERSON_RIGHT_HAND, HandSide.RIGHT, matrixStack, buffer, lightness);
                    renderHeldItem(player, leftItem, TransformType.THIRD_PERSON_LEFT_HAND, HandSide.LEFT, matrixStack, buffer, lightness);

                    matrixStack.pop();
                }

                matrixStack.translate(0.0F, 0.19F, 0.0F);
                matrixStack.scale(0.85f, 0.6f, 0.78f);

                renderCutoutModel(this.beltModel, TEXTURE_BELT, matrixStack, buffer, lightness, player, 1.0f, 1.0f, 1.0f);

                matrixStack.pop();
            });
        });
    }

    private void translateToBody(MatrixStack matrixStack)
    {
        this.getEntityModel().bipedBody.translateRotate(matrixStack);
    }

    private void renderHeldItem(LivingEntity player, ItemStack stack, TransformType transformType, HandSide handSide, MatrixStack matrixStack, IRenderTypeBuffer buffer, int lightness)
    {
        if (stack.isEmpty())
            return;
        matrixStack.push();
        if (handSide == HandSide.LEFT)
            matrixStack.translate(-4.35f / 16.0F, 0.7f, -0.1f);
        else
            matrixStack.translate(4.35f / 16.0F, 0.7f, -0.1f);
        //((IHasArm)this.getEntityModel()).func_225599_a_(handSide, matrixStack);
        matrixStack.rotate(Vector3f.XP.rotationDegrees(40));
        float scale = ConfigData.beltItemScale;
        matrixStack.scale(scale, scale, scale);
        Minecraft.getInstance().getFirstPersonRenderer().renderItemSide(player, stack, transformType, handSide == HandSide.LEFT, matrixStack, buffer, lightness);
        matrixStack.pop();
    }

    private static class ModelBelt extends EntityModel<PlayerEntity>
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
        public void setRotationAngles(PlayerEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
        {
        }

        @Override
        public void render(MatrixStack matrixStack, IVertexBuilder vertexBuilder, int p_225598_3_, int p_225598_4_, float p_225598_5_, float p_225598_6_, float p_225598_7_, float p_225598_8_)
        {
            RenderSystem.disableRescaleNormal();
            RenderSystem.disableCull();

            belt.render(matrixStack, vertexBuilder, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            pocketL.render(matrixStack, vertexBuilder, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            pocketR.render(matrixStack, vertexBuilder, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);

            matrixStack.push();
            matrixStack.scale(0.8f, 1, 1);
            buckle.render(matrixStack, vertexBuilder, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            matrixStack.pop();

            RenderSystem.enableCull();
            RenderSystem.enableRescaleNormal();
        }
    }
}
