package dev.gigaherz.toolbelt.client;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ToolBeltLayer<S extends HumanoidRenderState, M extends HumanoidModel<? super S>> extends RenderLayer<S, M>
{
    private static final Identifier TEXTURE_BELT = ToolBelt.location("textures/entity/belt.png");
    private static final Identifier TEXTURE_BELT_DYED = ToolBelt.location("textures/entity/dyed_belt.png");

    private static ItemDisplayContext LEFTSIDE = Enum.valueOf(ItemDisplayContext.class, "TOOLBELT_LEFTSIDE");
    private static ItemDisplayContext RIGHTSIDE = Enum.valueOf(ItemDisplayContext.class, "TOOLBELT_RIGHTSIDE");

    public static class RenderState
    {
        public ItemStackRenderState leftItem = new ItemStackRenderState();
        public ItemStackRenderState rightItem = new ItemStackRenderState();
        public int dyeColor;
        public Identifier textureLocation;
    }

    public static final ContextKey<ToolBeltLayer.RenderState> KEY = new ContextKey<>(ToolBelt.location("belt_layer_state"));
    public static void extractRenderState(Avatar owner, AvatarRenderState avatarState)
    {
        var itemModelResolver = Minecraft.getInstance().getItemModelResolver();

        var beltState = new RenderState();

        var getter = BeltFinder.findBelt(owner, true).orElse(null);
        if (getter == null || getter.isHidden())
            return;

        var stack = getter.getBelt();

        var cap = stack.get(DataComponents.CONTAINER);
        if (cap != null)
        {
            boolean rightHanded = avatarState.attackArm == HumanoidArm.RIGHT;

            ItemStack firstItem = cap.getSlots() >= 1 ? cap.getStackInSlot(0) : ItemStack.EMPTY;
            ItemStack secondItem = cap.getSlots() >= 2 ? cap.getStackInSlot(1) : ItemStack.EMPTY;
            ItemStack leftItem = rightHanded ? firstItem : secondItem;
            ItemStack rightItem = rightHanded ? secondItem : firstItem;

            itemModelResolver.updateForTopItem(beltState.leftItem, leftItem, LEFTSIDE, owner.level(), owner, 0);
            itemModelResolver.updateForTopItem(beltState.rightItem, rightItem, RIGHTSIDE, owner.level(), owner, 0);
        }

        var dyeInfo = stack.get(DataComponents.DYED_COLOR);
        beltState.dyeColor = dyeInfo != null ? 0xFF000000 | dyeInfo.rgb() : -1;

        beltState.textureLocation = getTextureLocation(stack);

        avatarState.setRenderData(KEY, beltState);
    }

    private final EntityModel<S> beltModel;
    private final EntityModel<S> buckleModel;

    public ToolBeltLayer(RenderLayerParent<S, M> owner)
    {
        super(owner);

        beltModel = new BeltModel(Minecraft.getInstance().getEntityModels().bakeLayer(ToolBeltClient.BELT_LAYER));
        buckleModel = new BeltModel(Minecraft.getInstance().getEntityModels().bakeLayer(ToolBeltClient.BUCKLE_LAYER));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightness, S renderState, float v, float v1)
    {
        if (!ConfigData.showBeltOnPlayers)
            return;

        var beltState = renderState.getRenderData(KEY);
        if (beltState == null)
            return;

        poseStack.pushPose();
        this.translateToBody(renderState, poseStack);

        if (!beltState.leftItem.isEmpty() || !beltState.rightItem.isEmpty())
        {
            poseStack.pushPose();

            if (renderState.isBaby)
            {
                poseStack.translate(0.0F, 0.75F, 0.0F);
                poseStack.scale(0.5F, 0.5F, 0.5F);
            }

            renderHeldItem(beltState.rightItem, false, poseStack, collector, lightness, renderState.outlineColor);
            renderHeldItem(beltState.leftItem, true, poseStack, collector, lightness, renderState.outlineColor);

            poseStack.popPose();
        }

        poseStack.translate(0.0F, 0.19F, 0.0F);
        poseStack.scale(0.85f, 0.6f, 0.78f);

        renderColoredCutoutModel(beltModel, beltState.textureLocation, poseStack, collector, lightness, renderState, beltState.dyeColor, 0);
        renderColoredCutoutModel(buckleModel, TEXTURE_BELT, poseStack, collector, lightness, renderState, -1, 0);

        poseStack.popPose();
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

    private static void renderHeldItem(ItemStackRenderState renderState, boolean leftHand, PoseStack poseStack,
                                       SubmitNodeCollector collector, int lightmapCoords, int outlineColor)
    {
        if (renderState.isEmpty())
            return;
        poseStack.pushPose();
        if (leftHand)
            poseStack.translate(-4.35f / 16.0F, 0.7f, -0.1f);
        else
            poseStack.translate(4.35f / 16.0F, 0.7f, -0.1f);
        poseStack.mulPose(Axis.XP.rotationDegrees(40));
        float scale = ConfigData.beltItemScale;
        poseStack.scale(scale, scale, scale);
        renderState.submit(poseStack, collector, lightmapCoords, OverlayTexture.NO_OVERLAY, outlineColor);
        poseStack.popPose();
    }

    private static Identifier getTextureLocation(ItemStack stack)
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
