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


    private final EntityModel<S> beltModel;
    private final EntityModel<S> buckleModel;

    public ToolBeltLayer(RenderLayerParent<S, M> owner)
    {
        super(owner);

        beltModel = new BeltModel(Minecraft.getInstance().getEntityModels().bakeLayer(ToolBeltClient.BELT_LAYER));
        buckleModel = new BeltModel(Minecraft.getInstance().getEntityModels().bakeLayer(ToolBeltClient.BUCKLE_LAYER));
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
        var dyeColor = dyeInfo != null ? 0xFF000000 | dyeInfo.rgb() : -1;

        renderColoredCutoutModel(beltModel, getTextureLocation(stack), poseStack, buffer, lightness, renderState, dyeColor);
        renderColoredCutoutModel(buckleModel, TEXTURE_BELT, poseStack, buffer, lightness, renderState, -1);

        poseStack.popPose();
    }

    private static void renderHeldItem(ItemStack stack, ItemDisplayContext transformType,
                                       boolean leftHand, PoseStack poseStack,
                                       MultiBufferSource buffer, int lightmapCoords)
    {
        if (stack.isEmpty())
            return;
        poseStack.pushPose();
        if (leftHand)
            poseStack.translate(-4.35f / 16.0F, 0.7f, -0.1f);
        else
            poseStack.translate(4.35f / 16.0F, 0.7f, -0.1f);
        //((IHasArm)this.getEntityModel()).translateHand(handSide, poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(40));
        float scale = ConfigData.beltItemScale;
        poseStack.scale(scale, scale, scale);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, transformType, lightmapCoords, OverlayTexture.NO_OVERLAY,
                poseStack, buffer,  Minecraft.getInstance().level, 42);
        poseStack.popPose();
    }

    protected ResourceLocation getTextureLocation(ItemStack stack)
    {
        return stack.isEmpty() ? TEXTURE_BELT : (stack.has(DataComponents.DYED_COLOR)
                    ? TEXTURE_BELT_DYED : TEXTURE_BELT);
    }

    public class BeltModel extends EntityModel<S>
    {
        private static final String BELT = "belt";
        private static final String BUCKLE = "buckle";
        private static final String LEFT_POCKET = "left_pocket";
        private static final String RIGHT_POCKET = "right_pocket";

        protected BeltModel(ModelPart root)
        {
            super(root);
        }

        public static LayerDefinition createBodyLayer()
        {
            MeshDefinition meshdefinition = new MeshDefinition();
            PartDefinition partdefinition = meshdefinition.getRoot();
            partdefinition.addOrReplaceChild(BELT, CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-5, 10, -3, 10, 4, 6), PartPose.ZERO);
            partdefinition.addOrReplaceChild(LEFT_POCKET, CubeListBuilder.create()
                    .texOffs(0, 10)
                    .addBox(-2, 12, 5, 4, 4, 1), PartPose.rotation(0, -90, 0));
            partdefinition.addOrReplaceChild(RIGHT_POCKET, CubeListBuilder.create()
                    .texOffs(0, 10)
                    .addBox(-2, 12, 5, 4, 4, 1), PartPose.rotation(0, 90, 0));
            return LayerDefinition.create(meshdefinition, 64, 32);
        }

        public static LayerDefinition createBuckleLayer()
        {
            MeshDefinition meshdefinition = new MeshDefinition();
            PartDefinition partdefinition = meshdefinition.getRoot();
            partdefinition.addOrReplaceChild(BUCKLE, CubeListBuilder.create()
                    .texOffs(10, 10)
                    .addBox(-2.5f, 9.5f, -3.5f, 5, 5, 1), PartPose.ZERO);
            return LayerDefinition.create(meshdefinition, 64, 32);
        }
    }
}
