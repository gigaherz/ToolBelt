package dev.gigaherz.toolbelt.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.gigaherz.toolbelt.ConfigData;
import dev.gigaherz.toolbelt.ToolBelt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;

public class ToolBeltLayer<S extends HumanoidRenderState, M extends HumanoidModel<? super S>> extends RenderLayer<S, M>
{
    private static final ResourceLocation TEXTURE_BELT = ToolBelt.location("textures/entity/belt.png");
    private static final ResourceLocation TEXTURE_BELT_DYED = ToolBelt.location("textures/entity/dyed_belt.png");

    private static ItemDisplayContext LEFTSIDE = Enum.valueOf(ItemDisplayContext.class, "TOOLBELT_LEFTSIDE");
    private static ItemDisplayContext RIGHTSIDE = Enum.valueOf(ItemDisplayContext.class, "TOOLBELT_RIGHTSIDE");


    private final BeltModel beltModel;

    public ToolBeltLayer(RenderLayerParent<S, M> owner)
    {
        super(owner);

        beltModel = new BeltModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ClientEvents.BELT_LAYER));
    }

    private void translateToBody(S renderState, PoseStack poseStack)
    {
        this.getParentModel().body.translateAndRotate(poseStack);
        if (renderState.isBaby)
        {
            poseStack.scale(0.52F, 0.52F, 0.52F);
            poseStack.translate(0.0D, 1.4D, 0.0D);
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int lightness, S renderState, float v, float v1)
    {
        if (!ConfigData.showBeltOnPlayers)
            return;

        if (!(renderState instanceof EntityRenderStateToolBeltContext beltState) )
            return;

        var getter = beltState.toolbelt_getBelt();
        if (getter == null || getter.isHidden())
            return;

        var stack = getter.getBelt();

        poseStack.pushPose();
        this.translateToBody(renderState, poseStack);

        var cap = stack.getCapability(Capabilities.ItemHandler.ITEM);
        if (cap != null)
        {
            boolean rightHanded = renderState.attackArm == HumanoidArm.RIGHT;

            {
                ItemStack firstItem = cap.getStackInSlot(0);
                ItemStack secondItem = cap.getStackInSlot(1);

                ItemStack leftItem = rightHanded ? firstItem : secondItem;
                ItemStack rightItem = rightHanded ? secondItem : firstItem;

                if (!leftItem.isEmpty() || !rightItem.isEmpty())
                {
                    poseStack.pushPose();

                    if (renderState.isBaby)
                    {
                        poseStack.translate(0.0F, 0.75F, 0.0F);
                        poseStack.scale(0.5F, 0.5F, 0.5F);
                    }

                    renderHeldItem(rightItem, RIGHTSIDE, false, poseStack, buffer, lightness);
                    renderHeldItem(leftItem, LEFTSIDE, true, poseStack, buffer, lightness);

                    poseStack.popPose();
                }
            }
        }

        poseStack.translate(0.0F, 0.19F, 0.0F);
        poseStack.scale(0.85f, 0.6f, 0.78f);

        var dyeInfo = stack.get(DataComponents.DYED_COLOR);
        beltModel.hasColor = dyeInfo != null;
        if (beltModel.hasColor)
        {
            var dyeColor = dyeInfo.rgb();
            beltModel.dyeRed = ARGB.red(dyeColor) / 255.0f;
            beltModel.dyeGreen = ARGB.green(dyeColor) / 255.0f;
            beltModel.dyeBlue = ARGB.blue(dyeColor) / 255.0f;
        }

        renderColoredCutoutModel(beltModel, getTextureLocation(stack), poseStack, buffer, lightness, renderState, 0xFFFFFFFF);

        poseStack.popPose();
    }

    private static void renderHeldItem(ItemStack stack, ItemDisplayContext transformType,
                                       boolean leftHand, PoseStack matrixStack,
                                       MultiBufferSource buffer, int lightmapCoords)
    {
        if (stack.isEmpty())
            return;
        matrixStack.pushPose();
        if (leftHand)
            matrixStack.translate(-4.35f / 16.0F, 0.7f, -0.1f);
        else
            matrixStack.translate(4.35f / 16.0F, 0.7f, -0.1f);
        //((IHasArm)this.getEntityModel()).translateHand(handSide, matrixStack);
        matrixStack.mulPose(Axis.XP.rotationDegrees(40));
        float scale = ConfigData.beltItemScale;
        matrixStack.scale(scale, scale, scale);
        var model = Minecraft.getInstance().getItemRenderer().getModel(stack, Minecraft.getInstance().level, null, 42);
        Minecraft.getInstance().getItemRenderer().render(stack, transformType,leftHand,
                matrixStack, buffer, lightmapCoords, OverlayTexture.NO_OVERLAY, model);
        matrixStack.popPose();
    }

    protected ResourceLocation getTextureLocation(ItemStack stack)
    {
        return stack.isEmpty() ? TEXTURE_BELT : (stack.has(DataComponents.DYED_COLOR)
                    ? TEXTURE_BELT_DYED : TEXTURE_BELT);
    }

    public static class BeltModel<T extends LivingEntityRenderState> extends EntityModel<T>
    {
        private static final String BELT = "belt";
        private static final String BUCKLE = "buckle";
        private static final String LEFT_POCKET = "left_pocket";
        private static final String RIGHT_POCKET = "right_pocket";
        //private final ModelPart belt;
        //private final ModelPart buckle;
        //private final ModelPart left_pocket;
        //private final ModelPart right_pocket;

        public boolean hasColor;
        public float dyeRed;
        public float dyeGreen;
        public float dyeBlue;

        public BeltModel(ModelPart part)
        {
            super(part);
            //this.belt = part.getChild(BELT);
            //this.buckle = part.getChild(BUCKLE);
            //this.left_pocket = part.getChild(LEFT_POCKET);
            //this.right_pocket = part.getChild(RIGHT_POCKET);
        }

        public static LayerDefinition createBodyLayer()
        {
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
                    .addBox(-2, 12, 5, 4, 4, 1), PartPose.rotation(0, -90, 0));
            partdefinition.addOrReplaceChild(RIGHT_POCKET, CubeListBuilder.create()
                    .texOffs(0, 10)
                    .addBox(-2, 12, 5, 4, 4, 1), PartPose.rotation(0, 90, 0));
            return LayerDefinition.create(meshdefinition, 64, 32);
        }

        /*@Override
        public void renderToBuffer(PoseStack matrixStack, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, int color)
        {
            var color2 = color;

            if (hasColor)
            {
                var dye = ARGB.colorFromFloat(dyeRed, dyeGreen, dyeBlue, 1.0f);
                color2 = ARGB.multiply(color, dye);
            }

            belt.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn, color2);
            left_pocket.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn, color2);
            right_pocket.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn, color2);

            matrixStack.pushPose();
            matrixStack.scale(0.8f, 1, 1);
            buckle.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn, color);
            matrixStack.popPose();
        }*/
    }
}
