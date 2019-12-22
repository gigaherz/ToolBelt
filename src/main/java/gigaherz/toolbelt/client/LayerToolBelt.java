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
import net.minecraft.client.renderer.Vector3f;
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
    public void func_225628_a_(MatrixStack matrixStack, IRenderTypeBuffer buffer, int lightness, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
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

                matrixStack.func_227860_a_();

                if (player.func_225608_bj_())
                {
                    matrixStack.func_227861_a_(0.0F, 0.2F, 0.0F);
                }

                this.translateToBody(matrixStack);

                if (!leftItem.isEmpty() || !rightItem.isEmpty())
                {
                    matrixStack.func_227860_a_();

                    if (this.getEntityModel().isSitting) // FIXME: maybe wrong field, can't tell
                    {
                        matrixStack.func_227861_a_(0.0F, 0.75F, 0.0F);
                        matrixStack.func_227862_a_(0.5F, 0.5F, 0.5F);
                    }

                    renderHeldItem(player, rightItem, TransformType.THIRD_PERSON_RIGHT_HAND, HandSide.RIGHT, matrixStack, buffer, lightness);
                    renderHeldItem(player, leftItem, TransformType.THIRD_PERSON_LEFT_HAND, HandSide.LEFT, matrixStack, buffer, lightness);

                    matrixStack.func_227865_b_();
                }

                matrixStack.func_227861_a_(0.0F, 0.19F, 0.0F);
                matrixStack.func_227862_a_(0.85f, 0.6f, 0.78f);

                func_229141_a_(this.beltModel, TEXTURE_BELT, matrixStack, buffer, lightness, player, 1.0f, 1.0f, 1.0f);

                matrixStack.func_227865_b_();
            });
        });
    }

    private void translateToBody(MatrixStack matrixStack)
    {
        this.getEntityModel().bipedBody.func_228307_a_(matrixStack);
    }

    private void renderHeldItem(LivingEntity player, ItemStack stack, TransformType transformType, HandSide handSide, MatrixStack matrixStack, IRenderTypeBuffer buffer, int lightness) {
        if (stack.isEmpty())
            return;
        matrixStack.func_227860_a_();
        if (handSide == HandSide.LEFT)
            matrixStack.func_227861_a_(-4.35f / 16.0F, 0.7f, -0.1f);
        else
            matrixStack.func_227861_a_(4.35f / 16.0F, 0.7f, -0.1f);
        //((IHasArm)this.getEntityModel()).func_225599_a_(handSide, matrixStack);
        matrixStack.func_227863_a_(Vector3f.field_229179_b_.func_229187_a_(40));
        float scale = ConfigData.beltItemScale;
        matrixStack.func_227862_a_(scale, scale, scale);
        Minecraft.getInstance().getFirstPersonRenderer().func_228397_a_(player, stack, transformType, handSide == HandSide.LEFT, matrixStack, buffer, lightness);
        matrixStack.func_227865_b_();
    }

    private static class ModelBelt extends EntityModel<PlayerEntity>
    {
        final ModelRenderer belt = new ModelRenderer(this);
        final ModelRenderer buckle = new ModelRenderer(this, 10, 10);
        final ModelRenderer pocketL = new ModelRenderer(this, 0, 10);
        final ModelRenderer pocketR = new ModelRenderer(this, 0, 10);

        {
            belt.func_228300_a_(-5, 10, -3, 10, 4, 6);

            buckle.func_228300_a_(-2.5f, 9.5f, -3.5f, 5, 5, 1);

            pocketL.func_228300_a_(-2, 12, 5, 4, 4, 1);
            pocketL.rotateAngleY = (float) Math.toRadians(-90);
            pocketR.func_228300_a_(-2, 12, 5, 4, 4, 1);
            pocketR.rotateAngleY = (float) Math.toRadians(90);
        }

        @Override
        public void func_225597_a_(PlayerEntity p_225597_1_, float p_225597_2_, float p_225597_3_, float p_225597_4_, float p_225597_5_, float p_225597_6_)
        {

        }

        @Override
        public void func_225598_a_(MatrixStack p_225598_1_, IVertexBuilder p_225598_2_, int p_225598_3_, int p_225598_4_, float p_225598_5_, float p_225598_6_, float p_225598_7_, float p_225598_8_)
        {
            RenderSystem.disableRescaleNormal();
            RenderSystem.disableCull();

            belt.func_228309_a_(p_225598_1_, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            pocketL.func_228309_a_(p_225598_1_, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            pocketR.func_228309_a_(p_225598_1_, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);

            p_225598_1_.func_227860_a_();
            p_225598_1_.func_227862_a_(0.8f, 1, 1);
            buckle.func_228309_a_(p_225598_1_, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            p_225598_1_.func_227865_b_();

            RenderSystem.enableCull();
            RenderSystem.enableRescaleNormal();
        }
    }
}
