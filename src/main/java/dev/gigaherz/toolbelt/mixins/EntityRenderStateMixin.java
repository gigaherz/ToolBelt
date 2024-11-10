package dev.gigaherz.toolbelt.mixins;

import dev.gigaherz.toolbelt.BeltFinder;
import dev.gigaherz.toolbelt.client.EntityRenderStateToolBeltContext;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements EntityRenderStateToolBeltContext
{
    @Unique
    @Nullable
    private BeltFinder.BeltGetter toolbelt_belt;

    @Nullable
    @Override
    public BeltFinder.BeltGetter toolbelt_getBelt()
    {
        return toolbelt_belt;
    }

    @Override
    public void toolbelt_setBelt(BeltFinder.BeltGetter belt)
    {
        this.toolbelt_belt = belt;
    }
}
