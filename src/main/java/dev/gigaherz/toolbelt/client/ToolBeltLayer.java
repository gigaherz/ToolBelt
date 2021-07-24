package dev.gigaherz.toolbelt.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.gigaherz.toolbelt.BeltFinder;
import dev.gigaherz.toolbelt.ConfigData;
import dev.gigaherz.toolbelt.ToolBelt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Vector3f;
import net.minecraftforge.items.CapabilityItemHandler;

public class ToolBeltLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M>
{
    private static final ResourceLocation TEXTURE_BELT = ToolBelt.location("textures/entity/belt.png");

    private final BeltModel<T> beltModel;

    public ToolBeltLayer(LivingEntityRenderer<T, M> owner)
    {
        super(owner);

        beltModel = new BeltModel(Minecraft.getInstance().getEntityModels().bakeLayer(ClientEvents.BELT_LAYER));
    }

    private void translateToBody(PoseStack matrixStack)
    {
        this.getParentModel().body.translateAndRotate(matrixStack);
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int lightness, T player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        if (!ConfigData.showBeltOnPlayers)
            return;

        BeltFinder.findBelt(player, true).ifPresent((getter) -> {

            if (getter.isHidden())
                return;

            getter.getBelt().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent((cap) -> {
                boolean rightHanded = player.getMainArm() == HumanoidArm.RIGHT;

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

                    renderHeldItem(player, rightItem, TransformType.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, matrixStack, buffer, lightness);
                    renderHeldItem(player, leftItem, TransformType.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, matrixStack, buffer, lightness);

                    matrixStack.popPose();
                }

                matrixStack.translate(0.0F, 0.19F, 0.0F);
                matrixStack.scale(0.85f, 0.6f, 0.78f);

                renderColoredCutoutModel(beltModel, TEXTURE_BELT, matrixStack, buffer, lightness, player, 1.0f, 1.0f, 1.0f);


                matrixStack.popPose();
            });
        });
    }

    private static void renderHeldItem(LivingEntity player, ItemStack stack, TransformType transformType, HumanoidArm handSide, PoseStack matrixStack, MultiBufferSource buffer, int lightness)
    {
        if (stack.isEmpty())
            return;
        matrixStack.pushPose();
        if (handSide == HumanoidArm.LEFT)
            matrixStack.translate(-4.35f / 16.0F, 0.7f, -0.1f);
        else
            matrixStack.translate(4.35f / 16.0F, 0.7f, -0.1f);
        //((IHasArm)this.getEntityModel()).translateHand(handSide, matrixStack);
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(40));
        float scale = ConfigData.beltItemScale;
        matrixStack.scale(scale, scale, scale);
        Minecraft.getInstance().getItemInHandRenderer().renderItem(player, stack, transformType, handSide == HumanoidArm.LEFT, matrixStack, buffer, lightness);
        matrixStack.popPose();
    }

    public static class BeltModel<T extends LivingEntity> extends EntityModel<T>
    {
        private static final String BELT = "belt";
        private static final String BUCKLE = "buckle";
        private static final String LEFT_POCKET = "left_pocket";
        private static final String RIGHT_POCKET = "right_pocket";
        private final ModelPart belt;
        private final ModelPart buckle;
        private final ModelPart left_pocket;
        private final ModelPart right_pocket;

        public BeltModel(ModelPart part) {
            super(RenderType::entityCutoutNoCull);
            this.belt = part.getChild(BELT);
            this.buckle = part.getChild(BUCKLE);
            this.left_pocket = part.getChild(LEFT_POCKET);
            this.right_pocket = part.getChild(RIGHT_POCKET);
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition meshdefinition = new MeshDefinition();
            PartDefinition partdefinition = meshdefinition.getRoot();
            partdefinition.addOrReplaceChild(BELT, CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-5, 10, -3, 10, 4, 6), PartPose.ZERO);
            partdefinition.addOrReplaceChild(BUCKLE, CubeListBuilder.create()
                    .texOffs(10, 10)
                    .addBox(-2.5f, 9.5f, -3.5f, 5, 5, 1), PartPose.ZERO);
            partdefinition.addOrReplaceChild(LEFT_POCKET, CubeListBuilder.create()
                    .texOffs(0, 10)
                    .addBox(-2, 12, 5, 4, 4, 1), PartPose.rotation(0,-90,0));
            partdefinition.addOrReplaceChild(RIGHT_POCKET, CubeListBuilder.create()
                    .texOffs(0, 10)
                    .addBox(-2, 12, 5, 4, 4, 1), PartPose.rotation(0,90,0));
            return LayerDefinition.create(meshdefinition, 64, 32);
        }


        @Override
        public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
        {
        }

        @Override
        public void renderToBuffer(PoseStack matrixStack, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
        {
            belt.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            left_pocket.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            right_pocket.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha);

            matrixStack.pushPose();
            matrixStack.scale(0.8f, 1, 1);
            buckle.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            matrixStack.popPose();
        }
    }
}
