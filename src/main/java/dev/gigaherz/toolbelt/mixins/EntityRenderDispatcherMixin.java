package dev.gigaherz.toolbelt.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.gigaherz.toolbelt.BeltFinder;
import dev.gigaherz.toolbelt.client.EntityRenderStateToolBeltContext;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin
{
    @ModifyExpressionValue(
            method="render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/EntityRenderer;)V",
            at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/entity/EntityRenderer;createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;"))

    public <T extends Entity, S extends EntityRenderState> S injectToolBeltContextToRenderStateCreation(S renderState, @Local(argsOnly = true) T entity)
    {
        if (entity instanceof LivingEntity living && renderState instanceof EntityRenderStateToolBeltContext context)
            context.toolbelt_setBelt(BeltFinder.findBelt(living, true).orElse(null));
        return renderState;
    }
}
