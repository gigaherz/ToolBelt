package dev.gigaherz.toolbelt.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.gigaherz.toolbelt.BeltFinder;
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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;

public class ToolBeltLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M>
{
    private static final ResourceLocation TEXTURE_BELT = ToolBelt.location("textures/entity/belt.png");
    private static final ResourceLocation TEXTURE_BELT_DYED = ToolBelt.location("textures/entity/dyed_belt.png");

    private final BeltModel<T> beltModel;

    public ToolBeltLayer(RenderLayerParent<T, M> owner)
    {
        super(owner);

        beltModel = new BeltModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ClientEvents.BELT_LAYER));
    }

    private void translateToBody(LivingEntity entity, PoseStack poseStack)
    {
        this.getParentModel().body.translateAndRotate(poseStack);
        if (entity.isBaby() && !(entity instanceof Villager))
        {
            poseStack.scale(0.52F, 0.52F, 0.52F);
            poseStack.translate(0.0D, 1.4D, 0.0D);
        }
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int lightness, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        if (!ConfigData.showBeltOnPlayers)
            return;

        BeltFinder.findBelt(entity, true).ifPresent((getter) -> {

            if (getter.isHidden())
                return;

            var stack = getter.getBelt();

            matrixStack.pushPose();
            this.translateToBody(entity, matrixStack);

            var cap = stack.getCapability(Capabilities.ItemHandler.ITEM);
            if (cap != null)
            {
                boolean rightHanded = entity.getMainArm() == HumanoidArm.RIGHT;

                {
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

                        renderHeldItem(entity, rightItem, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, matrixStack, buffer, lightness);
                        renderHeldItem(entity, leftItem, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, matrixStack, buffer, lightness);

                        matrixStack.popPose();
                    }
                }
            }

            matrixStack.translate(0.0F, 0.19F, 0.0F);
            matrixStack.scale(0.85f, 0.6f, 0.78f);

            var dyeInfo = stack.get(DataComponents.DYED_COLOR);
            beltModel.hasColor = dyeInfo != null;
            if (beltModel.hasColor)
            {
                var dyeColor = dyeInfo.rgb();
                beltModel.dyeRed = FastColor.ARGB32.red(dyeColor) / 255.0f;
                beltModel.dyeGreen = FastColor.ARGB32.green(dyeColor) / 255.0f;
                beltModel.dyeBlue = FastColor.ARGB32.blue(dyeColor) / 255.0f;
            }

            renderColoredCutoutModel(beltModel, getTextureLocation(entity), matrixStack, buffer, lightness, entity, 1.0f, 1.0f, 1.0f);

            matrixStack.popPose();
        });
    }

    private static void renderHeldItem(LivingEntity player, ItemStack stack, ItemDisplayContext transformType, HumanoidArm handSide, PoseStack matrixStack, MultiBufferSource buffer, int lightness)
    {
        if (stack.isEmpty())
            return;
        matrixStack.pushPose();
        if (handSide == HumanoidArm.LEFT)
            matrixStack.translate(-4.35f / 16.0F, 0.7f, -0.1f);
        else
            matrixStack.translate(4.35f / 16.0F, 0.7f, -0.1f);
        //((IHasArm)this.getEntityModel()).translateHand(handSide, matrixStack);
        matrixStack.mulPose(Axis.XP.rotationDegrees(40));
        float scale = ConfigData.beltItemScale;
        matrixStack.scale(scale, scale, scale);
        Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().renderItem(player, stack, transformType, handSide == HumanoidArm.LEFT, matrixStack, buffer, lightness);
        matrixStack.popPose();
    }

    @Override
    protected ResourceLocation getTextureLocation(T pEntity)
    {
        return BeltFinder.findBelt(pEntity, true).map((getter) -> {
            var stack = getter.getBelt();
            return stack.has(DataComponents.DYED_COLOR)
                    ? TEXTURE_BELT_DYED
                    : TEXTURE_BELT;
        }).orElse(TEXTURE_BELT);
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

        public boolean hasColor;
        public float dyeRed;
        public float dyeGreen;
        public float dyeBlue;

        public BeltModel(ModelPart part)
        {
            super(RenderType::entityCutoutNoCull);
            this.belt = part.getChild(BELT);
            this.buckle = part.getChild(BUCKLE);
            this.left_pocket = part.getChild(LEFT_POCKET);
            this.right_pocket = part.getChild(RIGHT_POCKET);
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

        @Override
        public void renderToBuffer(PoseStack matrixStack, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
        {
            var cRed = red;
            var cGreen = green;
            var cBlue = blue;
            if (hasColor)
            {
                cRed *= dyeRed;
                cGreen *= dyeGreen;
                cBlue *= dyeBlue;
            }

            belt.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn, cRed, cGreen, cBlue, alpha);
            left_pocket.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn, cRed, cGreen, cBlue, alpha);
            right_pocket.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn, cRed, cGreen, cBlue, alpha);

            matrixStack.pushPose();
            matrixStack.scale(0.8f, 1, 1);
            buckle.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            matrixStack.popPose();
        }

        @Override
        public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
        {

        }
    }
}
