package dev.gigaherz.toolbelt.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import dev.gigaherz.toolbelt.BeltFinder;
import dev.gigaherz.toolbelt.ConfigData;
import dev.gigaherz.toolbelt.ToolBelt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.items.CapabilityItemHandler;

public class ToolBeltLayer<T extends LivingEntity, M extends BipedModel<T>> extends LayerRenderer<T, M>
{
    private static final ResourceLocation TEXTURE_BELT = ToolBelt.location("textures/entity/belt.png");

    private final BeltModel<T> beltModel = new BeltModel<>();

    public ToolBeltLayer(LivingRenderer<T, M> owner)
    {
        super(owner);
    }

    private void translateToBody(MatrixStack matrixStack)
    {
        this.getParentModel().body.translateAndRotate(matrixStack);
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int lightness, T player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        if (!ConfigData.showBeltOnPlayers)
            return;

        BeltFinder.findBelt(player, true).ifPresent((getter) -> {

            if (getter.isHidden())
                return;

            getter.getBelt().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent((cap) -> {
                boolean rightHanded = player.getMainArm() == HandSide.RIGHT;

                matrixStack.pushPose();
                this.translateToBody(matrixStack);

                ItemStack firstItem = cap.getStackInSlot(0);
                ItemStack secondItem = cap.getStackInSlot(1);

                ItemStack leftItem = rightHanded ? firstItem : secondItem;
                ItemStack rightItem = rightHanded ? secondItem : firstItem;

                if (!leftItem.isEmpty() || !rightItem.isEmpty())
                {
                    matrixStack.pushPose();

                    if (getParentModel().young)
                    {
                        matrixStack.translate(0.0F, 0.75F, 0.0F);
                        matrixStack.scale(0.5F, 0.5F, 0.5F);
                    }

                    renderHeldItem(player, rightItem, TransformType.THIRD_PERSON_RIGHT_HAND, HandSide.RIGHT, matrixStack, buffer, lightness);
                    renderHeldItem(player, leftItem, TransformType.THIRD_PERSON_LEFT_HAND, HandSide.LEFT, matrixStack, buffer, lightness);

                    matrixStack.popPose();
                }

                matrixStack.translate(0.0F, 0.19F, 0.0F);
                matrixStack.scale(0.85f, 0.6f, 0.78f);

                renderColoredCutoutModel(beltModel, TEXTURE_BELT, matrixStack, buffer, lightness, player, 1.0f, 1.0f, 1.0f);


                matrixStack.popPose();
            });
        });
    }

    private static void renderHeldItem(LivingEntity player, ItemStack stack, TransformType transformType, HandSide handSide, MatrixStack matrixStack, IRenderTypeBuffer buffer, int lightness)
    {
        if (stack.isEmpty())
            return;
        matrixStack.pushPose();
        if (handSide == HandSide.LEFT)
            matrixStack.translate(-4.35f / 16.0F, 0.7f, -0.1f);
        else
            matrixStack.translate(4.35f / 16.0F, 0.7f, -0.1f);
        //((IHasArm)this.getEntityModel()).translateHand(handSide, matrixStack);
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(40));
        float scale = ConfigData.beltItemScale;
        matrixStack.scale(scale, scale, scale);
        Minecraft.getInstance().getItemInHandRenderer().renderItem(player, stack, transformType, handSide == HandSide.LEFT, matrixStack, buffer, lightness);
        matrixStack.popPose();
    }

    private static class BeltModel<T extends LivingEntity> extends EntityModel<T>
    {
        final ModelRenderer belt = new ModelRenderer(this);
        final ModelRenderer buckle = new ModelRenderer(this, 10, 10);
        final ModelRenderer pocketL = new ModelRenderer(this, 0, 10);
        final ModelRenderer pocketR = new ModelRenderer(this, 0, 10);

        {
            belt.addBox(-5, 10, -3, 10, 4, 6);

            buckle.addBox(-2.5f, 9.5f, -3.5f, 5, 5, 1);

            pocketL.addBox(-2, 12, 5, 4, 4, 1);
            pocketL.yRot = (float) Math.toRadians(-90);
            pocketR.addBox(-2, 12, 5, 4, 4, 1);
            pocketR.yRot = (float) Math.toRadians(90);
        }

        @Override
        public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
        {
        }

        @Override
        public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
        {
            belt.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            pocketL.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            pocketR.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha);

            matrixStack.pushPose();
            matrixStack.scale(0.8f, 1, 1);
            buckle.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            matrixStack.popPose();
        }
    }
}
