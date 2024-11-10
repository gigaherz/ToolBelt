package dev.gigaherz.toolbelt.client;

import dev.gigaherz.toolbelt.BeltFinder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface EntityRenderStateToolBeltContext
{
    @Nullable
    BeltFinder.BeltGetter toolbelt_getBelt();

    @ApiStatus.Internal
    void toolbelt_setBelt(@Nullable BeltFinder.BeltGetter belt);
}
